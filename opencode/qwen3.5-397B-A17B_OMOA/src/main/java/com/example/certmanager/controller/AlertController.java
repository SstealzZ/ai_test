package com.example.certmanager.controller;

import com.example.certmanager.dto.response.AlertResponse;
import com.example.certmanager.dto.response.AlertSummaryResponse;
import com.example.certmanager.dto.response.ApiResponse;
import com.example.certmanager.service.AlertService;
import org.springframework.http.ResponseEntity;
import com.example.certmanager.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlertResponse>>> listAlerts(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false, defaultValue = "false") Boolean unreadOnly) {

        Long userId = principal.getUserId();
        List<AlertResponse> response = alertService.listAlerts(userId, unreadOnly);

        return ResponseEntity.ok(ApiResponse.success("Alerts retrieved successfully", response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<AlertSummaryResponse>> getAlertSummary(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        AlertSummaryResponse response = alertService.getAlertSummary(userId);

        return ResponseEntity.ok(ApiResponse.success("Alert summary retrieved successfully", response));
    }
}
