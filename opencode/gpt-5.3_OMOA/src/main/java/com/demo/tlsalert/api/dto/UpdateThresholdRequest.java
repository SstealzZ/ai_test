package com.demo.tlsalert.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateThresholdRequest(@Min(1) @Max(3650) int thresholdDays) {
}
