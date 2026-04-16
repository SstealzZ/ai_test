package com.certalert.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateResponse {
    private Long id;
    private String subject;
    private String issuer;
    private String serialNumber;
    private Instant notBefore;
    private Instant notAfter;
    private String sourceUrl;
    private String originalFilename;
    private String groupName;
    private String addedBy;
    private Instant createdAt;
    private Long daysUntilExpiry;
    private boolean expired;
    private String status;
}