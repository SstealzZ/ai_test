package com.certalert.controller;

import com.certalert.dto.AlertResponse;
import com.certalert.service.AlertService;
import com.certalert.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;
    private final CertificateService certificateService;

    @GetMapping
    public ResponseEntity<List<AlertResponse>> getAlerts(
            @AuthenticationPrincipal Jwt jwt) {

        String username = jwt.getClaimAsString("preferred_username");
        if (username == null) {
            username = jwt.getSubject();
        }

        Long groupId = certificateService.getUserGroupId(username);
        List<AlertResponse> alerts = alertService.getRecentAlerts(groupId);
        return ResponseEntity.ok(alerts);
    }

    @PostMapping("/check")
    public ResponseEntity<Void> triggerCheck() {
        alertService.checkAndAlert();
        return ResponseEntity.ok().build();
    }
}