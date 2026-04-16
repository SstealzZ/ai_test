package com.certwatcher.dto;

import jakarta.validation.constraints.Min;

public record UpdateThresholdRequest(
        @Min(1) int alertThresholdDays,
        String webhookUrl
) {}
