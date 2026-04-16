package com.demo.tlsalert.api;

import com.demo.tlsalert.api.dto.ThresholdResponse;
import com.demo.tlsalert.api.dto.UpdateThresholdRequest;
import com.demo.tlsalert.security.CurrentUserService;
import com.demo.tlsalert.service.ThresholdService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final ThresholdService thresholdService;
    private final CurrentUserService currentUserService;

    public SettingsController(ThresholdService thresholdService, CurrentUserService currentUserService) {
        this.thresholdService = thresholdService;
        this.currentUserService = currentUserService;
    }

    @PreAuthorize("hasAuthority('CERT_VIEW')")
    @GetMapping("/threshold")
    public ThresholdResponse getThreshold() {
        return new ThresholdResponse(currentUserService.currentGroup(), thresholdService.getCurrentGroupThreshold());
    }

    @PreAuthorize("hasAuthority('CERT_ADD')")
    @PutMapping("/threshold")
    public ThresholdResponse updateThreshold(@RequestBody @Valid UpdateThresholdRequest request) {
        int value = thresholdService.updateCurrentGroupThreshold(request.thresholdDays());
        return new ThresholdResponse(currentUserService.currentGroup(), value);
    }
}
