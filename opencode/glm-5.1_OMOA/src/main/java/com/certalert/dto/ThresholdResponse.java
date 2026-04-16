package com.certalert.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ThresholdResponse {
    private Long id;
    private String name;
    private Integer days;
    private Boolean active;
    private Long groupId;
    private Instant createdAt;
    private Instant updatedAt;
}