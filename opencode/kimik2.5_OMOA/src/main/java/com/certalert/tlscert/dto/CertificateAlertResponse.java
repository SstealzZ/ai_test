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
public class CertificateAlertResponse {
    private UUID id;
    private UUID certificateId;
    private String certificateSubject;
    private Instant alertSentAt;
    private Integer daysUntilExpiry;
    private boolean acknowledged;
}
