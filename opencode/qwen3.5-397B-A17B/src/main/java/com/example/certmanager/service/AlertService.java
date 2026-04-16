package com.example.certmanager.service;

import com.example.certmanager.dto.response.AlertResponse;
import com.example.certmanager.dto.response.AlertSummaryResponse;
import com.example.certmanager.entity.Certificate;
import com.example.certmanager.entity.CertificateAlert;
import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.User;
import com.example.certmanager.exception.ResourceNotFoundException;
import com.example.certmanager.repository.CertificateAlertRepository;
import com.example.certmanager.repository.CertificateRepository;
import com.example.certmanager.repository.GroupMemberRepository;
import com.example.certmanager.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Service for managing certificate alerts.
 */
@Service
@Transactional
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final CertificateAlertRepository alertRepository;
    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AuditService auditService;

    public AlertService(CertificateAlertRepository alertRepository,
                        CertificateRepository certificateRepository,
                        UserRepository userRepository,
                        GroupMemberRepository groupMemberRepository,
                        AuditService auditService) {
        this.alertRepository = alertRepository;
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.auditService = auditService;
    }

    /**
     * Send expiry warning alert.
     */
    public void sendExpiryWarning(Certificate certificate, int daysRemaining) {
        log.info("Sending expiry warning for certificate {}: {} days remaining",
                certificate.getCommonName(), daysRemaining);

        List<User> usersToNotify = getGroupMembersForCertificate(certificate);

        for (User user : usersToNotify) {
            CertificateAlert alert = new CertificateAlert();
            alert.setCertificate(certificate);
            alert.setUser(user);
            alert.setAlertType(CertificateAlert.AlertType.EXPIRY_WARNING);
            alert.setMessage(String.format(
                    "Certificate '%s' will expire in %d days (expires: %s)",
                    certificate.getCommonName(),
                    daysRemaining,
                    certificate.getNotAfter()
            ));
            alertRepository.save(alert);
        }

        auditService.logAction(
                null,
                "ALERT_EXPIRY_WARNING",
                "CERTIFICATE",
                certificate.getId(),
                "Expiry warning sent to " + usersToNotify.size() + " users",
                null
        );
    }

    /**
     * Send expired alert.
     */
    public void sendExpiredAlert(Certificate certificate) {
        log.info("Sending expired alert for certificate {}", certificate.getCommonName());

        List<User> usersToNotify = getGroupMembersForCertificate(certificate);

        for (User user : usersToNotify) {
            CertificateAlert alert = new CertificateAlert();
            alert.setCertificate(certificate);
            alert.setUser(user);
            alert.setAlertType(CertificateAlert.AlertType.EXPIRED);
            alert.setMessage(String.format(
                    "Certificate '%s' has expired (expired: %s)",
                    certificate.getCommonName(),
                    certificate.getNotAfter()
            ));
            alertRepository.save(alert);
        }

        auditService.logAction(
                null,
                "ALERT_EXPIRED",
                "CERTIFICATE",
                certificate.getId(),
                "Expired alert sent to " + usersToNotify.size() + " users",
                null
        );
    }

    /**
     * Get alerts for a user.
     */
    @Transactional(readOnly = true)
    public Page<CertificateAlert> getAlertsForUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        return alertRepository.findByUser(user, pageable);
    }

    /**
     * Get alert summary for a user.
     */
    @Transactional(readOnly = true)
    public AlertSummaryResponse getAlertSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        AlertSummaryResponse summary = new AlertSummaryResponse();
        summary.setTotalCertificates(certificateRepository.count());
        summary.setActiveCertificates((long) certificateRepository.findAllActive().size());
        summary.setExpired((long) certificateRepository.findExpiredCertificates().size());
        summary.setExpiringSoon((long) certificateRepository.findExpiringCertificates(Instant.now().plus(30, ChronoUnit.DAYS)).size());
        summary.setUnreadAlerts(alertRepository.countUnreadByUser(user));

        return summary;
    }

    /**
     * List alerts for a user.
     */
    @Transactional(readOnly = true)
    public List<AlertResponse> listAlerts(Long userId, Boolean unreadOnly) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));
        List<CertificateAlert> alerts = alertRepository.findByUserOrderByCreatedAtDesc(user);
        if (Boolean.TRUE.equals(unreadOnly)) {
            alerts = alerts.stream().filter(a -> Boolean.FALSE.equals(a.getIsRead())).toList();
        }
        return alerts.stream().map(this::mapToResponse).toList();
    }

    private AlertResponse mapToResponse(CertificateAlert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setCertificateId(alert.getCertificate() != null ? alert.getCertificate().getId() : null);
        response.setCertificateCommonName(alert.getCertificate() != null ? alert.getCertificate().getCommonName() : null);
        response.setUserId(alert.getUser() != null ? alert.getUser().getId() : null);
        response.setAlertType(alert.getAlertType() != null ? alert.getAlertType().name() : null);
        response.setMessage(alert.getMessage());
        response.setIsRead(alert.getIsRead());
        response.setCreatedAt(alert.getCreatedAt());
        response.setReadAt(alert.getReadAt());
        return response;
    }

    /**
     * Record an alert as sent.
     */
    public void recordAlertSent(Long certificateId, String alertType, String status) {
        auditService.logAction(
                null,
                "ALERT_SENT",
                "CERTIFICATE_ALERT",
                certificateId,
                "Alert type: " + alertType + ", Status: " + status,
                null
        );
    }

    /**
     * Mark an alert as read.
     */
    public void markAlertAsRead(Long alertId, Long userId) {
        CertificateAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> ResourceNotFoundException.alertNotFound(alertId));

        if (!alert.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Alert does not belong to user");
        }

        alert.setIsRead(true);
        alert.setReadAt(Instant.now());
        alertRepository.save(alert);
    }

    /**
     * Delete an alert.
     */
    public void deleteAlert(Long alertId, Long userId) {
        CertificateAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> ResourceNotFoundException.alertNotFound(alertId));

        if (!alert.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Alert does not belong to user");
        }

        alertRepository.delete(alert);

        auditService.logAction(
                userId,
                "DELETE_ALERT",
                "CERTIFICATE_ALERT",
                alertId,
                "Deleted alert",
                null
        );
    }

    /**
     * Get users to notify for a certificate (group members).
     */
    private List<User> getGroupMembersForCertificate(Certificate certificate) {
        if (certificate == null || certificate.getGroup() == null) {
            return List.of();
        }

        return groupMemberRepository.findAllByGroup(certificate.getGroup()).stream()
                .map(GroupMember::getUser)
                .toList();
    }
}
