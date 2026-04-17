package com.example.certmanager.service;

import com.example.certmanager.entity.Certificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!prod")
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${certmanager.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.application.name:cert-manager}")
    private String applicationName;

    public void sendExpiryEmail(String recipient, Certificate certificate, int daysRemaining) {
        log.info("[MOCK EMAIL] Sending expiry warning to: {}", recipient);
        log.info("[MOCK EMAIL] Certificate: {} (CN: {}) expires in {} days", 
                certificate.getCommonName(), 
                certificate.getCommonName(), 
                daysRemaining);
        log.info("[MOCK EMAIL] Email would contain: expiry-warning template with certificate details");
    }

    public void sendExpiredEmail(String recipient, Certificate certificate) {
        log.info("[MOCK EMAIL] Sending expired alert to: {}", recipient);
        log.info("[MOCK EMAIL] Certificate: {} (CN: {}) has EXPIRED", 
                certificate.getCommonName(), 
                certificate.getCommonName());
        log.info("[MOCK EMAIL] Email would contain: expired template with certificate details");
    }
}
