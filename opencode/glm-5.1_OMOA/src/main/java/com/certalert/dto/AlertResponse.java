package com.certalert.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertResponse {
    private Long id;
    private CertificateResponse certificate;
    private Integer thresholdDays;
    private Long daysRemaining;
    private String type;
    private Instant notifiedAt;
    private String message;
}