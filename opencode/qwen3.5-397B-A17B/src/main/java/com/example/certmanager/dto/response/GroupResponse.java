package com.example.certmanager.dto.response;

import java.time.Instant;
import java.util.List;

public class GroupResponse {

    private Long id;
    private String name;
    private String description;
    private Long createdBy;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
    private List<GroupMemberResponse> members;
    private Integer certificateCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<GroupMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<GroupMemberResponse> members) {
        this.members = members;
    }

    public Integer getCertificateCount() {
        return certificateCount;
    }

    public void setCertificateCount(Integer certificateCount) {
        this.certificateCount = certificateCount;
    }
}
