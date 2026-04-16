package com.certalert.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "app_users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private AppGroup group;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public enum Role {
        CERT_MANAGER,
        CERT_VIEWER
    }

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}