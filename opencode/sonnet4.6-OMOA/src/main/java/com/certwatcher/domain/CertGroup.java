package com.certwatcher.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cert_group")
public class CertGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int alertThresholdDays = 30;

    @Column
    private String webhookUrl;

    public CertGroup() {}

    public CertGroup(String name) {
        this.name = name;
    }

    public Long getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAlertThresholdDays() { return alertThresholdDays; }
    public void setAlertThresholdDays(int alertThresholdDays) { this.alertThresholdDays = alertThresholdDays; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
}
