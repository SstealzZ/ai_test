package com.certwatcher.dto;

import com.certwatcher.domain.Certificate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record CertificateResponse(
        Long id,
        String alias,
        String subjectDn,
        String issuerDn,
        Instant notBefore,
        Instant notAfter,
        long daysUntilExpiry,
        String serialNumber,
        String source,
        String group,
        String addedByUsername,
        Instant addedAt
) {
    public static CertificateResponse from(Certificate c) {
        long days = ChronoUnit.DAYS.between(Instant.now(), c.getNotAfter());
        return new CertificateResponse(
                c.getId(),
                c.getAlias(),
                c.getSubjectDn(),
                c.getIssuerDn(),
                c.getNotBefore(),
                c.getNotAfter(),
                days,
                c.getSerialNumber(),
                c.getSource(),
                c.getGroup().getName(),
                c.getAddedByUsername(),
                c.getAddedAt()
        );
    }
}
