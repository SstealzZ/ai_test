package com.example.certmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CertificateRequest {

    @NotBlank(message = "Certificate name is required")
    @Size(max = 255, message = "Certificate name must be less than 255 characters")
    private String name;

    @Size(max = 512, message = "Description must be less than 512 characters")
    private String description;

    private Long groupId;

    @NotBlank(message = "Certificate content is required")
    private String pemContent;

    private String pemPrivateKey;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getPemContent() {
        return pemContent;
    }

    public void setPemContent(String pemContent) {
        this.pemContent = pemContent;
    }

    public String getPemPrivateKey() {
        return pemPrivateKey;
    }

    public void setPemPrivateKey(String pemPrivateKey) {
        this.pemPrivateKey = pemPrivateKey;
    }
}
