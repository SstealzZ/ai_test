package com.certwatcher.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "certificate")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private String subjectDn;

    @Column(nullable = false)
    private String issuerDn;

    @Column(nullable = false)
    private Instant notBefore;

    @Column(nullable = false)
    private Instant notAfter;

    @Column(nullable = false)
    private String serialNumber;

    @Column(nullable = false, length = 32)
    private String source;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private CertGroup group;

    @Column(nullable = false)
    private String addedByUsername;

    @Column(nullable = false)
    private Instant addedAt;

    public Certificate() {}

    public Long getId() { return id; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getSubjectDn() { return subjectDn; }
    public void setSubjectDn(String subjectDn) { this.subjectDn = subjectDn; }

    public String getIssuerDn() { return issuerDn; }
    public void setIssuerDn(String issuerDn) { this.issuerDn = issuerDn; }

    public Instant getNotBefore() { return notBefore; }
    public void setNotBefore(Instant notBefore) { this.notBefore = notBefore; }

    public Instant getNotAfter() { return notAfter; }
    public void setNotAfter(Instant notAfter) { this.notAfter = notAfter; }

    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public CertGroup getGroup() { return group; }
    public void setGroup(CertGroup group) { this.group = group; }

    public String getAddedByUsername() { return addedByUsername; }
    public void setAddedByUsername(String addedByUsername) { this.addedByUsername = addedByUsername; }

    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
