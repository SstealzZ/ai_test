package com.certalert.tlscert.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String email;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        CERT_ADMIN,
        CERT_VIEWER
    }
}
