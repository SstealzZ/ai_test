package com.demo.tlsalert.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertScheduler {

    private final AlertService alertService;

    public AlertScheduler(AlertService alertService) {
        this.alertService = alertService;
    }

    @Scheduled(fixedDelayString = "${app.alert.scan-delay-ms:300000}")
    public void runAlertScan() {
        alertService.scanAndCreateAlerts();
    }
}
