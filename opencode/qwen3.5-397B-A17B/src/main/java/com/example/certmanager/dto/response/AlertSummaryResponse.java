package com.example.certmanager.dto.response;

public class AlertSummaryResponse {

    private Long totalCertificates;
    private Long activeCertificates;
    private Long expiringSoon;
    private Long expired;
    private Long unreadAlerts;

    public Long getTotalCertificates() {
        return totalCertificates;
    }

    public void setTotalCertificates(Long totalCertificates) {
        this.totalCertificates = totalCertificates;
    }

    public Long getActiveCertificates() {
        return activeCertificates;
    }

    public void setActiveCertificates(Long activeCertificates) {
        this.activeCertificates = activeCertificates;
    }

    public Long getExpiringSoon() {
        return expiringSoon;
    }

    public void setExpiringSoon(Long expiringSoon) {
        this.expiringSoon = expiringSoon;
    }

    public Long getExpired() {
        return expired;
    }

    public void setExpired(Long expired) {
        this.expired = expired;
    }

    public Long getUnreadAlerts() {
        return unreadAlerts;
    }

    public void setUnreadAlerts(Long unreadAlerts) {
        this.unreadAlerts = unreadAlerts;
    }
}
