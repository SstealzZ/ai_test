package com.certalert.tlscert.controller;

import com.certalert.tlscert.dto.AlertConfigResponse;
import com.certalert.tlscert.dto.CertificateAlertResponse;
import com.certalert.tlscert.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/config")
    public ResponseEntity<AlertConfigResponse> getConfig() {
        return ResponseEntity.ok(alertService.getConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<AlertConfigResponse> updateConfig(@RequestParam("thresholdDays") int thresholdDays) {
        AlertConfigResponse response = alertService.updateConfig(thresholdDays);
        alertService.processExpiringCertificates();
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CertificateAlertResponse>> listAlerts() {
        return ResponseEntity.ok(alertService.listAlertsForCurrentUser());
    }
}
