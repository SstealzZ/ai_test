package com.certalert.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThresholdRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Min(value = 1, message = "Threshold must be at least 1 day")
    private Integer days;

    private Boolean active;

    private Long groupId;
}