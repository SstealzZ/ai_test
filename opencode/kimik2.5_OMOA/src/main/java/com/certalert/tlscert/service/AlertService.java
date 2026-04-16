package com.certalert.tlscert.service;

import com.certalert.tlscert.dto.AlertConfigResponse;
import com.certalert.tlscert.dto.CertificateAlertResponse;
import com.certalert.tlscert.entity.AlertConfig;
import com.certalert.tlscert.entity.Certificate;
import com.certalert.tlscert.entity.CertificateAlert;
import com.certalert.tlscert.repository.AlertConfigRepository;
import com.certalert.tlscert.repository.CertificateAlertRepository;
import com.certalert.tlscert.security.CurrentUser;
import com.certalert.tlscert.security.SecurityUserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class AlertService {

    private final AlertConfigRepository alertConfigRepository;
    private final CertificateAlertRepository certificateAlertRepository;
    private final CertificateService certificateService;

    public AlertService(AlertConfigRepository alertConfigRepository,
                        CertificateAlertRepository certificateAlertRepository,
                        CertificateService certificateService) {
        this.alertConfigRepository = alertConfigRepository;
        this.certificateAlertRepository = certificateAlertRepository;
        this.certificateService = certificateService;
    }

    @Transactional(readOnly = true)
    public AlertConfigResponse getConfig() {
        AlertConfig config = alertConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> alertConfigRepository.save(AlertConfig.builder().defaultThresholdDays(30).build()));
        return AlertConfigResponse.builder()
                .defaultThresholdDays(config.getDefaultThresholdDays())
                .build();
    }

    @Transactional
    public AlertConfigResponse updateConfig(int thresholdDays) {
        AlertConfig config = alertConfigRepository.findAll().stream()
                .findFirst()
                .orElse(AlertConfig.builder().defaultThresholdDays(30).build());
        config.setDefaultThresholdDays(thresholdDays);
        alertConfigRepository.save(config);
        return AlertConfigResponse.builder()
                .defaultThresholdDays(config.getDefaultThresholdDays())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CertificateAlertResponse> listAlertsForCurrentUser() {
        SecurityUserDetails currentUser = CurrentUser.get();
        return certificateAlertRepository.findByCertificateGroupIdOrderByAlertSentAtDesc(currentUser.getGroupId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void processExpiringCertificates() {
        AlertConfig config = alertConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> alertConfigRepository.save(AlertConfig.builder().defaultThresholdDays(30).build()));

        List<Certificate> expiring = certificateService.findAllExpiringWithin(config.getDefaultThresholdDays());
        for (Certificate cert : expiring) {
            if (hasRecentAlert(cert.getId(), 7)) {
                continue;
            }
            long daysLeft = ChronoUnit.DAYS.between(Instant.now(), cert.getValidUntil());
            if (daysLeft < 0) {
                daysLeft = 0;
            }

            CertificateAlert alert = CertificateAlert.builder()
                    .certificate(cert)
                    .alertSentAt(Instant.now())
                    .daysUntilExpiry((int) daysLeft)
                    .acknowledged(false)
                    .build();
            certificateAlertRepository.save(alert);
        }
    }

    @Transactional
    public void checkAndCreateAlertFor(Certificate cert) {
        AlertConfig config = alertConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> alertConfigRepository.save(AlertConfig.builder().defaultThresholdDays(30).build()));

        long daysLeft = ChronoUnit.DAYS.between(Instant.now(), cert.getValidUntil());
        if (daysLeft < 0) {
            daysLeft = 0;
        }

        if (daysLeft <= config.getDefaultThresholdDays()) {
            if (!hasRecentAlert(cert.getId(), 7)) {
                CertificateAlert alert = CertificateAlert.builder()
                        .certificate(cert)
                        .alertSentAt(Instant.now())
                        .daysUntilExpiry((int) daysLeft)
                        .acknowledged(false)
                        .build();
                certificateAlertRepository.save(alert);
            }
        }
    }

    private boolean hasOpenAlert(java.util.UUID certificateId) {
        return certificateAlertRepository.findByCertificateIdAndAcknowledgedFalse(certificateId).isPresent();
    }

    private boolean hasRecentAlert(java.util.UUID certificateId, int cooldownDays) {
        return certificateAlertRepository.findTopByCertificateIdOrderByAlertSentAtDesc(certificateId)
                .map(alert -> alert.getAlertSentAt().isAfter(Instant.now().minus(cooldownDays, ChronoUnit.DAYS)))
                .orElse(false);
    }

    private CertificateAlertResponse toResponse(CertificateAlert alert) {
        return CertificateAlertResponse.builder()
                .id(alert.getId())
                .certificateId(alert.getCertificate().getId())
                .certificateSubject(alert.getCertificate().getSubjectDn())
                .alertSentAt(alert.getAlertSentAt())
                .daysUntilExpiry(alert.getDaysUntilExpiry())
                .acknowledged(alert.isAcknowledged())
                .build();
    }
}
