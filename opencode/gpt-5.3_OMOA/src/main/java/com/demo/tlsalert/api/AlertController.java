package com.demo.tlsalert.api;

import com.demo.tlsalert.api.dto.AlertResponse;
import com.demo.tlsalert.service.AlertService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PreAuthorize("hasAuthority('CERT_VIEW')")
    @GetMapping
    public List<AlertResponse> list() {
        return alertService.listCurrentGroupAlerts();
    }
}
