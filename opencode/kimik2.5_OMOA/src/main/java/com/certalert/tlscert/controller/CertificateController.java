package com.certalert.tlscert.controller;

import com.certalert.tlscert.dto.CertificateResponse;
import com.certalert.tlscert.entity.Certificate;
import com.certalert.tlscert.service.AlertService;
import com.certalert.tlscert.service.CertificateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/v1/certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final AlertService alertService;

    public CertificateController(CertificateService certificateService, AlertService alertService) {
        this.certificateService = certificateService;
        this.alertService = alertService;
    }

    @PostMapping("/upload")
    public ResponseEntity<CertificateResponse> upload(@RequestParam("file") MultipartFile file) throws Exception {
        Certificate cert = certificateService.uploadFromFile(file);
        alertService.checkAndCreateAlertFor(cert);
        return ResponseEntity.ok(toResponse(cert));
    }

    @PostMapping("/from-url")
    public ResponseEntity<CertificateResponse> fromUrl(@RequestParam("url") String url) throws Exception {
        Certificate cert = certificateService.fetchFromUrl(url);
        alertService.checkAndCreateAlertFor(cert);
        return ResponseEntity.ok(toResponse(cert));
    }

    @GetMapping
    public ResponseEntity<List<CertificateResponse>> list() {
        List<Certificate> certs = certificateService.listForCurrentUser();
        return ResponseEntity.ok(certs.stream().map(this::toResponse).toList());
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<CertificateResponse>> listExpiring(@RequestParam(name = "days", required = false) Integer days) {
        int thresholdDays = days != null ? days : alertService.getConfig().getDefaultThresholdDays();
        List<Certificate> certs = certificateService.listExpiringForCurrentUser(thresholdDays);
        return ResponseEntity.ok(certs.stream().map(this::toResponse).toList());
    }

    private CertificateResponse toResponse(Certificate cert) {
        Long daysUntilExpiry = null;
        if (cert.getValidUntil() != null) {
            daysUntilExpiry = ChronoUnit.DAYS.between(Instant.now(), cert.getValidUntil());
            if (daysUntilExpiry < 0) {
                daysUntilExpiry = 0L;
            }
        }
        return CertificateResponse.builder()
                .id(cert.getId())
                .subjectDn(cert.getSubjectDn())
                .issuerDn(cert.getIssuerDn())
                .serialNumber(cert.getSerialNumber())
                .sha256Fingerprint(cert.getSha256Fingerprint())
                .validFrom(cert.getValidFrom())
                .validUntil(cert.getValidUntil())
                .sourceType(cert.getSourceType().name())
                .sourceUrl(cert.getSourceUrl())
                .uploadedByUsername(cert.getUploadedBy() != null ? cert.getUploadedBy().getUsername() : null)
                .createdAt(cert.getCreatedAt())
                .daysUntilExpiry(daysUntilExpiry)
                .build();
    }
}
