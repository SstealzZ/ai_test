package com.demo.tlsalert.api.dto;

import com.demo.tlsalert.domain.SourceType;

import java.time.Instant;
import java.util.UUID;

public record CertificateResponse(
    UUID id,
    SourceType sourceType,
    String sourceValue,
    String subjectDn,
    String issuerDn,
    String serialNumber,
    String fingerprintSha256,
    Instant notBefore,
    Instant notAfter,
    long daysRemaining
) {
}
