package com.certwatcher.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private CertGroup group;

    public AppUser() {}

    public AppUser(String username, UserRole role, CertGroup group) {
        this.username = username;
        this.role = role;
        this.group = group;
    }

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }

    public CertGroup getGroup() { return group; }
    public void setGroup(CertGroup group) { this.group = group; }
}
