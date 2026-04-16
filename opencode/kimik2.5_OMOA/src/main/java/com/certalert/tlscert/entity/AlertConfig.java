package com.certalert.tlscert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "alert_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "default_threshold_days", nullable = false)
    private int defaultThresholdDays;
}
