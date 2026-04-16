package com.certalert.tlscert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificate_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certificate_id", nullable = false)
    private Certificate certificate;

    @Column(name = "alert_sent_at")
    private Instant alertSentAt;

    @Column(name = "days_until_expiry")
    private Integer daysUntilExpiry;

    @Column(name = "acknowledged")
    private boolean acknowledged;
}
