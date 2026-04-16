package com.example.certmanager.scheduler;

import com.example.certmanager.entity.Certificate;
import com.example.certmanager.entity.CertificateAlert;
import com.example.certmanager.entity.GroupMember;
import com.example.certmanager.entity.User;
import com.example.certmanager.repository.CertificateAlertRepository;
import com.example.certmanager.repository.CertificateRepository;
import com.example.certmanager.repository.GroupMemberRepository;
import com.example.certmanager.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
public class CertificateAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(CertificateAlertScheduler.class);

    private final CertificateRepository certificateRepository;
    private final CertificateAlertRepository alertRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final EmailService emailService;

    @Value("${certmanager.scheduler.alert-threshold-days:30}")
    private int defaultAlertThresholdDays;

    @Value("${certmanager.scheduler.enabled:true}")
    private boolean schedulerEnabled;

    public CertificateAlertScheduler(
            CertificateRepository certificateRepository,
            CertificateAlertRepository alertRepository,
            GroupMemberRepository groupMemberRepository,
            EmailService emailService) {
        this.certificateRepository = certificateRepository;
        this.alertRepository = alertRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.emailService = emailService;
    }

    /**
     * Runs every hour to check for expiring and expired certificates.
     * Cron: 0 0 * * * * (at minute 0 of every hour, UTC timezone)
     */
    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    @Transactional
    public void checkExpiringCertificates() {
        if (!schedulerEnabled) {
            log.debug("Certificate alert scheduler is disabled");
            return;
        }

        log.info("Starting hourly certificate expiration check at {}", Instant.now());

        Instant now = Instant.now();
        Instant thresholdDate = now.plus(Duration.ofDays(defaultAlertThresholdDays));

        // Check for expired certificates
        List<Certificate> expiredCertificates = certificateRepository.findExpiredCertificates();
        log.info("Found {} expired certificates", expiredCertificates.size());
        for (Certificate certificate : expiredCertificates) {
            processExpiredCertificate(certificate, now);
        }

        // Check for expiring certificates (not yet expired, but within threshold)
        List<Certificate> expiringCertificates = certificateRepository.findExpiringCertificates(thresholdDate);
        log.info("Found {} expiring certificates (within {} days)", expiringCertificates.size(), defaultAlertThresholdDays);
        for (Certificate certificate : expiringCertificates) {
            long daysRemaining = Duration.between(now, certificate.getNotAfter()).toDays();
            if (daysRemaining >= 0) {
                processCertificate(certificate, (int) daysRemaining);
            }
        }

        log.info("Certificate expiration check completed. Processed {} expired, {} expiring",
                expiredCertificates.size(), expiringCertificates.size());
    }

    /**
     * Process an individual certificate that is expiring soon.
     * Sends alerts to all group members based on rate limiting rules.
     */
    @Transactional
    public void processCertificate(Certificate certificate, int daysRemaining) {
        if (certificate.getStatus() != Certificate.Status.ACTIVE) {
            log.debug("Skipping certificate {} - status is {}", certificate.getCommonName(), certificate.getStatus());
            return;
        }

        if (certificate.getGroup() == null) {
            log.debug("No group found for certificate {}", certificate.getCommonName());
            return;
        }

        List<GroupMember> members = groupMemberRepository.findByGroup(certificate.getGroup());
        if (members.isEmpty()) {
            log.debug("No members found for certificate {} group", certificate.getCommonName());
            return;
        }

        for (GroupMember member : members) {
            User user = member.getUser();
            if (user == null || user.getEmail() == null) {
                continue;
            }

            if (shouldSendExpiryAlert(certificate, user.getEmail(), daysRemaining)) {
                sendExpiryAlert(certificate, user, daysRemaining);
            }
        }
    }

    /**
     * Process an expired certificate.
     * Sends daily alerts to all group members.
     */
    @Transactional
    public void processExpiredCertificate(Certificate certificate, Instant now) {
        if (certificate.getStatus() != Certificate.Status.EXPIRED) {
            certificate.setStatus(Certificate.Status.EXPIRED);
            certificateRepository.save(certificate);
        }

        if (certificate.getGroup() == null) {
            log.debug("No group found for expired certificate {}", certificate.getCommonName());
            return;
        }

        List<GroupMember> members = groupMemberRepository.findByGroup(certificate.getGroup());
        if (members.isEmpty()) {
            log.debug("No members found for expired certificate {} group", certificate.getCommonName());
            return;
        }

        for (GroupMember member : members) {
            User user = member.getUser();
            if (user == null || user.getEmail() == null) {
                continue;
            }

            if (shouldSendExpiredAlert(certificate, user.getEmail())) {
                sendExpiredAlert(certificate, user, now);
            }
        }
    }

    /**
     * Determine if an expiry alert should be sent for a certificate.
     * Rate limiting logic:
     * - Send once when certificate first reaches threshold
     * - Then send daily if still within threshold
     *
     * @param certificate The certificate to check
     * @param recipientEmail The recipient's email
     * @param daysRemaining Days until expiration
     * @return true if alert should be sent
     */
    public boolean shouldSendExpiryAlert(Certificate certificate, String recipientEmail, int daysRemaining) {
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(Duration.ofDays(1));

        // Check if we've sent an alert in the last 24 hours
        List<CertificateAlert> recentAlerts = alertRepository.findByCertificateIdAndRecipientEmailAndSentAtAfter(
                certificate.getId(), recipientEmail, oneDayAgo);

        if (!recentAlerts.isEmpty()) {
            boolean atSameThreshold = recentAlerts.stream()
                    .filter(alert -> alert.getAlertType() == CertificateAlert.AlertType.EXPIRY_WARNING)
                    .anyMatch(alert -> Integer.valueOf(daysRemaining).equals(alert.getDaysRemaining()));

            if (atSameThreshold) {
                log.debug("Skipping expiry alert for {} to {} - already sent at this threshold within 24h",
                        certificate.getCommonName(), recipientEmail);
                return false;
            }
        }

        // Check if this is the first time reaching this threshold
        List<CertificateAlert> previousAlerts = alertRepository.findLatestAlertsByCertificateAndTypeAndEmail(
                certificate.getId(), CertificateAlert.AlertType.EXPIRY_WARNING, recipientEmail);

        if (previousAlerts.isEmpty()) {
            log.info("First expiry alert for certificate {} at {} days remaining",
                    certificate.getCommonName(), daysRemaining);
            return true;
        }

        CertificateAlert lastAlert = previousAlerts.get(0);
        if (lastAlert.getDaysRemaining() == null || daysRemaining < lastAlert.getDaysRemaining()) {
            log.info("New threshold reached for certificate {}: {} days (previous: {})",
                    certificate.getCommonName(), daysRemaining, lastAlert.getDaysRemaining());
            return true;
        }

        // Send daily for certificates within threshold
        Instant lastAlertTime = lastAlert.getSentAt();
        if (Duration.between(lastAlertTime, now).toDays() >= 1) {
            log.info("Daily expiry alert for certificate {} ({} days remaining)",
                    certificate.getCommonName(), daysRemaining);
            return true;
        }

        return false;
    }

    /**
     * Determine if an expired alert should be sent for a certificate.
     * Sends daily alerts for expired certificates.
     *
     * @param certificate The expired certificate
     * @param recipientEmail The recipient's email
     * @return true if alert should be sent
     */
    public boolean shouldSendExpiredAlert(Certificate certificate, String recipientEmail) {
        Instant now = Instant.now();
        Instant oneDayAgo = now.minus(Duration.ofDays(1));

        List<CertificateAlert> recentAlerts = alertRepository.findByCertificateIdAndRecipientEmailAndSentAtAfter(
                certificate.getId(), recipientEmail, oneDayAgo);

        boolean hasRecentExpiredAlert = recentAlerts.stream()
                .anyMatch(alert -> alert.getAlertType() == CertificateAlert.AlertType.EXPIRED);

        if (hasRecentExpiredAlert) {
            log.debug("Skipping expired alert for {} to {} - already sent within 24h",
                    certificate.getCommonName(), recipientEmail);
            return false;
        }

        return true;
    }

    /**
     * Send expiry warning alert and record in database.
     */
    private void sendExpiryAlert(Certificate certificate, User user, int daysRemaining) {
        try {
            emailService.sendExpiryEmail(user.getEmail(), certificate, daysRemaining);

            CertificateAlert alert = new CertificateAlert();
            alert.setCertificate(certificate);
            alert.setAlertType(CertificateAlert.AlertType.EXPIRY_WARNING);
            alert.setDaysRemaining(daysRemaining);
            alert.setRecipientEmail(user.getEmail());
            alert.setSentAt(Instant.now());

            alertRepository.save(alert);
            log.info("Sent expiry alert for {} to {} ({} days remaining)",
                    certificate.getCommonName(), user.getEmail(), daysRemaining);
        } catch (Exception e) {
            log.error("Failed to send expiry alert for {} to {}",
                    certificate.getCommonName(), user.getEmail(), e);
        }
    }

    /**
     * Send expired certificate alert and record in database.
     */
    private void sendExpiredAlert(Certificate certificate, User user, Instant now) {
        try {
            emailService.sendExpiredEmail(user.getEmail(), certificate);

            CertificateAlert alert = new CertificateAlert();
            alert.setCertificate(certificate);
            alert.setAlertType(CertificateAlert.AlertType.EXPIRED);
            alert.setDaysRemaining(0);
            alert.setRecipientEmail(user.getEmail());
            alert.setSentAt(now);

            alertRepository.save(alert);
            log.info("Sent expired alert for {} to {}", certificate.getCommonName(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send expired alert for {} to {}",
                    certificate.getCommonName(), user.getEmail(), e);
        }
    }
}
