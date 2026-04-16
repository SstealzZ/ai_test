package com.certalert.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "alert_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AlertLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @Column(name = "threshold_days", nullable = false)
    private Integer thresholdDays;

    @Column(name = "days_remaining", nullable = false)
    private Long daysRemaining;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Column(name = "notified_at", nullable = false)
    private Instant notifiedAt;

    @Column(name = "message", length = 2000)
    private String message;

    public enum AlertType {
        EXPIRATION_WARNING,
        EXPIRED,
        RECOVERY
    }

    @PrePersist
    protected void onCreate() {
        notifiedAt = Instant.now();
    }
}