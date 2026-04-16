package com.certwatcher.scheduler;

import com.certwatcher.domain.Certificate;
import com.certwatcher.domain.CertGroup;
import com.certwatcher.repository.CertGroupRepository;
import com.certwatcher.repository.CertificateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Component
public class ExpiryAlertScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryAlertScheduler.class);

    private final CertificateRepository certRepo;
    private final CertGroupRepository groupRepo;
    private final RestClient restClient;

    public ExpiryAlertScheduler(CertificateRepository certRepo,
                                 CertGroupRepository groupRepo,
                                 RestClient.Builder restClientBuilder) {
        this.certRepo = certRepo;
        this.groupRepo = groupRepo;
        this.restClient = restClientBuilder.build();
    }

    @Scheduled(cron = "${certwatcher.scheduler.cron:0 0 8 * * *}")
    public void checkExpiry() {
        List<CertGroup> groups = groupRepo.findAll();
        for (CertGroup group : groups) {
            if (group.getWebhookUrl() == null || group.getWebhookUrl().isBlank()) {
                continue;
            }
            Instant deadline = Instant.now().plus(group.getAlertThresholdDays(), ChronoUnit.DAYS);
            List<Certificate> expiring = certRepo.findExpiringBefore(Instant.now(), deadline);

            List<Certificate> forGroup = expiring.stream()
                    .filter(c -> c.getGroup().getId().equals(group.getId()))
                    .toList();

            if (forGroup.isEmpty()) {
                continue;
            }

            sendWebhook(group, forGroup);
        }
    }

    private void sendWebhook(CertGroup group, List<Certificate> certs) {
        List<Map<String, Object>> payload = certs.stream()
                .map(c -> {
                    Map<String, Object> entry = new java.util.LinkedHashMap<>();
                    entry.put("alias", c.getAlias());
                    entry.put("subjectDn", c.getSubjectDn());
                    entry.put("notAfter", c.getNotAfter().toString());
                    entry.put("daysUntilExpiry", ChronoUnit.DAYS.between(Instant.now(), c.getNotAfter()));
                    entry.put("source", c.getSource());
                    return entry;
                })
                .toList();

        Map<String, Object> body = Map.of(
                "group", group.getName(),
                "alertThresholdDays", group.getAlertThresholdDays(),
                "expiringCertificates", payload
        );

        try {
            restClient.post()
                    .uri(group.getWebhookUrl())
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Webhook sent for group '{}': {} expiring certificates", group.getName(), certs.size());
        } catch (Exception e) {
            log.error("Webhook delivery failed for group '{}': {}", group.getName(), e.getMessage());
        }
    }
}
