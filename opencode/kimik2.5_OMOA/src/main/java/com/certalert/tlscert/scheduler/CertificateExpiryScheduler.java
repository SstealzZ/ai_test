package com.certalert.tlscert.scheduler;

import com.certalert.tlscert.service.AlertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CertificateExpiryScheduler {

    private final AlertService alertService;

    public CertificateExpiryScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public void checkCertificatesDaily() {
        log.info("Running daily certificate expiry check...");
        alertService.processExpiringCertificates();
        log.info("Certificate expiry check completed.");
    }
}
