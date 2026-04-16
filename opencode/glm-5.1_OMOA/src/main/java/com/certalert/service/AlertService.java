package com.certalert.service;

import com.certalert.dto.AlertResponse;
import com.certalert.dto.CertificateResponse;
import com.certalert.model.AlertLog;
import com.certalert.model.AppUser;
import com.certalert.model.Certificate;
import com.certalert.model.Threshold;
import com.certalert.repository.AlertLogRepository;
import com.certalert.repository.AppUserRepository;
import com.certalert.repository.CertificateRepository;
import com.certalert.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final CertificateRepository certificateRepository;
    private final ThresholdRepository thresholdRepository;
    private final AlertLogRepository alertLogRepository;
    private final AppUserRepository appUserRepository;
    private final JavaMailSender mailSender;

    @Value("${cert-alert.alert.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${cert-alert.alert.email.from:cert-alert@example.com}")
    private String emailFrom;

    @Value("${cert-alert.alert.email.admin-email:admin@example.com}")
    private String adminEmail;

    @Scheduled(cron = "${cert-alert.scheduler.check-cron:0 0 8 * * ?}")
    @Transactional
    public void checkAndAlert() {
        log.info("Running scheduled certificate expiration check...");

        List<Threshold> thresholds = thresholdRepository.findByActiveTrue();
        if (thresholds.isEmpty()) {
            log.info("No active thresholds configured, skipping check");
            return;
        }

        List<Certificate> allCerts = certificateRepository.findAll();
        Instant now = Instant.now();

        for (Certificate cert : allCerts) {
            long daysUntilExpiry = ChronoUnit.DAYS.between(now, cert.getNotAfter());

            if (cert.isExpired()) {
                logAndAlert(cert, 0, daysUntilExpiry, AlertLog.AlertType.EXPIRED);
            } else {
                for (Threshold threshold : thresholds) {
                    if (daysUntilExpiry <= threshold.getDays()) {
                        logAndAlert(cert, threshold.getDays(), daysUntilExpiry, AlertLog.AlertType.EXPIRATION_WARNING);
                        break;
                    }
                }
            }
        }
    }

    private void logAndAlert(Certificate cert, Integer thresholdDays, long daysRemaining, AlertLog.AlertType type) {
        AlertLog alertLog = AlertLog.builder()
                .certificate(cert)
                .thresholdDays(thresholdDays)
                .daysRemaining(daysRemaining)
                .type(type)
                .message(formatMessage(cert, type, daysRemaining))
                .build();

        alertLogRepository.save(alertLog);
        log.warn("ALERT [{}]: Certificate '{}' expires in {} days (subject={})",
                type, cert.getId(), daysRemaining, cert.getSubject());

        if (emailEnabled) {
            sendEmailAlert(cert, type, daysRemaining);
        }
    }

    private String formatMessage(Certificate cert, AlertLog.AlertType type, long daysRemaining) {
        return switch (type) {
            case EXPIRED -> String.format("Certificate '%s' has EXPIRED (subject: %s)", cert.getId(), cert.getSubject());
            case EXPIRATION_WARNING -> String.format("Certificate '%s' expires in %d days (subject: %s)", cert.getId(), daysRemaining, cert.getSubject());
            case RECOVERY -> String.format("Certificate '%s' has been renewed (subject: %s)", cert.getId(), cert.getSubject());
        };
    }

    private void sendEmailAlert(Certificate cert, AlertLog.AlertType type, long daysRemaining) {
        try {
            List<AppUser> groupMembers = appUserRepository.findByGroupId(cert.getGroup().getId());
            String[] recipients = groupMembers.stream()
                    .map(AppUser::getEmail)
                    .toArray(String[]::new);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailFrom);
            message.setTo(recipients);
            message.setSubject(String.format("[CertAlert] %s - Certificate Expiration Alert", type));
            message.setText(formatMessage(cert, type, daysRemaining));
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email alert for certificate {}: {}", cert.getId(), e.getMessage());
        }
    }

    public List<AlertResponse> getRecentAlerts(Long groupId) {
        List<Certificate> groupCerts = certificateRepository.findByGroupIdOrderByNotAfterDesc(groupId);
        List<Long> certIds = groupCerts.stream().map(Certificate::getId).toList();

        return alertLogRepository.findAll().stream()
                .filter(alert -> certIds.contains(alert.getCertificate().getId()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private AlertResponse toResponse(AlertLog alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .certificate(CertificateResponse.builder()
                        .id(alert.getCertificate().getId())
                        .subject(alert.getCertificate().getSubject())
                        .issuer(alert.getCertificate().getIssuer())
                        .serialNumber(alert.getCertificate().getSerialNumber())
                        .notBefore(alert.getCertificate().getNotBefore())
                        .notAfter(alert.getCertificate().getNotAfter())
                        .groupName(alert.getCertificate().getGroup().getName())
                        .addedBy(alert.getCertificate().getAddedBy())
                        .createdAt(alert.getCertificate().getCreatedAt())
                        .daysUntilExpiry(alert.getCertificate().daysUntilExpiry())
                        .expired(alert.getCertificate().isExpired())
                        .status(alert.getCertificate().isExpired() ? "EXPIRED" : "WARNING")
                        .build())
                .thresholdDays(alert.getThresholdDays())
                .daysRemaining(alert.getDaysRemaining())
                .type(alert.getType().name())
                .notifiedAt(alert.getNotifiedAt())
                .message(alert.getMessage())
                .build();
    }
}