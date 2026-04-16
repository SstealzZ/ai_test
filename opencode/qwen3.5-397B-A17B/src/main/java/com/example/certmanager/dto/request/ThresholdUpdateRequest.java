package com.example.certmanager.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ThresholdUpdateRequest {

    @NotNull(message = "Threshold days is required")
    @Min(value = 1, message = "Threshold must be at least 1 day")
    @Max(value = 365, message = "Threshold must be at most 365 days")
    private Integer thresholdDays;

    public Integer getThresholdDays() {
        return thresholdDays;
    }

    public void setThresholdDays(Integer thresholdDays) {
        this.thresholdDays = thresholdDays;
    }
}
