package com.certalert.tlscert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_user_id", nullable = false)
    private UserAccount uploadedBy;

    @Column(name = "subject_dn")
    private String subjectDn;

    @Column(name = "issuer_dn")
    private String issuerDn;

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "sha256_fingerprint")
    private String sha256Fingerprint;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @Column(name = "source_url")
    private String sourceUrl;

    @Column(name = "raw_pem", length = 10000)
    private String rawPem;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

    public enum SourceType {
        UPLOAD,
        URL
    }
}
