package com.certalert.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "certificates")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false)
    private String issuer;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "not_before", nullable = false)
    private Instant notBefore;

    @Column(name = "not_after", nullable = false)
    private Instant notAfter;

    @Column(nullable = false, length = 65536)
    private String pemData;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "original_filename")
    private String originalFilename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private AppGroup group;

    @Column(name = "added_by", nullable = false)
    private String addedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    public boolean isExpired() {
        return Instant.now().isAfter(notAfter);
    }

    @Transient
    public long daysUntilExpiry() {
        return java.time.Duration.between(Instant.now(), notAfter).toDays();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}