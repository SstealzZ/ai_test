package com.demo.tlsalert.api.dto;

import com.demo.tlsalert.domain.AlertStatus;
import com.demo.tlsalert.domain.AlertType;

import java.time.Instant;
import java.util.UUID;

public record AlertResponse(
    UUID id,
    AlertType alertType,
    AlertStatus status,
    String message,
    int thresholdDays,
    Instant triggeredAt,
    UUID certificateId,
    String certificateSource,
    Instant certificateNotAfter
) {
}
