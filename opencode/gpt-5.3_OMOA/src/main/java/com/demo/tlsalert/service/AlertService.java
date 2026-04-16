package com.demo.tlsalert.service;

import com.demo.tlsalert.api.dto.AlertResponse;
import com.demo.tlsalert.domain.AlertEntity;
import com.demo.tlsalert.domain.AlertStatus;
import com.demo.tlsalert.domain.AlertType;
import com.demo.tlsalert.domain.CertificateEntity;
import com.demo.tlsalert.repository.AlertRepository;
import com.demo.tlsalert.repository.CertificateRepository;
import com.demo.tlsalert.security.CurrentUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertService {

    private static final Logger log = LoggerFactory.getLogger(AlertService.class);

    private final AlertRepository alertRepository;
    private final CertificateRepository certificateRepository;
    private final ThresholdService thresholdService;
    private final CurrentUserService currentUserService;

    public AlertService(
        AlertRepository alertRepository,
        CertificateRepository certificateRepository,
        ThresholdService thresholdService,
        CurrentUserService currentUserService
    ) {
        this.alertRepository = alertRepository;
        this.certificateRepository = certificateRepository;
        this.thresholdService = thresholdService;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> listCurrentGroupAlerts() {
        String group = currentUserService.currentGroup();
        return alertRepository.findByCertificateGroupNameOrderByTriggeredAtDesc(group)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public void scanAndCreateAlerts() {
        Instant now = Instant.now();
        Instant dedupeWindow = now.minus(24, ChronoUnit.HOURS);
        List<CertificateEntity> certificates = certificateRepository.findAll();

        for (CertificateEntity certificate : certificates) {
            int thresholdDays = thresholdService.getGroupThreshold(certificate.getGroupName());
            long daysRemaining = Duration.between(now, certificate.getNotAfter()).toDays();

            AlertType type = null;
            String message = null;

            if (daysRemaining < 0) {
                type = AlertType.EXPIRED;
                message = "Certificate expired " + Math.abs(daysRemaining) + " day(s) ago";
            } else if (daysRemaining <= thresholdDays) {
                type = AlertType.EXPIRING_SOON;
                message = "Certificate expires in " + daysRemaining + " day(s)";
            }

            if (type != null && !alertRepository.existsByCertificateIdAndAlertTypeAndTriggeredAtAfter(
                certificate.getId(), type, dedupeWindow)) {
                AlertEntity alert = new AlertEntity();
                alert.setCertificate(certificate);
                alert.setAlertType(type);
                alert.setThresholdDays(thresholdDays);
                alert.setStatus(AlertStatus.OPEN);
                alert.setMessage(message);
                alert.setTriggeredAt(now);
                alertRepository.save(alert);
                log.warn("[ALERT] group={} cert={} type={} message={}",
                    certificate.getGroupName(), certificate.getId(), type, message);
            }
        }
    }

    private AlertResponse toResponse(AlertEntity alert) {
        CertificateEntity cert = alert.getCertificate();
        return new AlertResponse(
            alert.getId(),
            alert.getAlertType(),
            alert.getStatus(),
            alert.getMessage(),
            alert.getThresholdDays(),
            alert.getTriggeredAt(),
            cert.getId(),
            cert.getSourceValue(),
            cert.getNotAfter()
        );
    }
}
