package com.certwatcher.controller;

import com.certwatcher.dto.AddCertByUrlRequest;
import com.certwatcher.dto.CertificateResponse;
import com.certwatcher.dto.UpdateThresholdRequest;
import com.certwatcher.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateApiController {

    private final CertificateService certService;

    public CertificateApiController(CertificateService certService) {
        this.certService = certService;
    }

    @GetMapping
    public List<CertificateResponse> list() {
        return certService.listForCurrentUser();
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse uploadFile(
            @RequestParam("alias") String alias,
            @RequestParam("file") MultipartFile file) {
        return certService.addFromFile(alias, file);
    }

    @PostMapping("/from-url")
    @ResponseStatus(HttpStatus.CREATED)
    public CertificateResponse fromUrl(@Valid @RequestBody AddCertByUrlRequest request) {
        return certService.addFromUrl(request.alias(), request.hostname(), request.port());
    }

    @PutMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(
            @Valid @RequestBody UpdateThresholdRequest request) {
        certService.updateGroupSettings(request);
        return ResponseEntity.ok(Map.of(
                "alertThresholdDays", request.alertThresholdDays(),
                "webhookUrl", request.webhookUrl() != null ? request.webhookUrl() : ""
        ));
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        var group = certService.groupSettings();
        return ResponseEntity.ok(Map.of(
                "group", group.getName(),
                "alertThresholdDays", group.getAlertThresholdDays(),
                "webhookUrl", group.getWebhookUrl() != null ? group.getWebhookUrl() : ""
        ));
    }
}
