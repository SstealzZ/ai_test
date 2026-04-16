package com.certalert.tlscert.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateResponse {
    private UUID id;
    private String subjectDn;
    private String issuerDn;
    private String serialNumber;
    private String sha256Fingerprint;
    private Instant validFrom;
    private Instant validUntil;
    private String sourceType;
    private String sourceUrl;
    private String uploadedByUsername;
    private Instant createdAt;
    private Long daysUntilExpiry;
}
