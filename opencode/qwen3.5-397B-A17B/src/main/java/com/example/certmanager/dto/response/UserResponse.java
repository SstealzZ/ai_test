package com.example.certmanager.dto.response;

import java.time.Instant;
import java.util.List;

public class UserResponse {

    private Long id;
    private String email;
    private String name;
    private String givenName;
    private String familyName;
    private String picture;
    private String locale;
    private Instant lastLoginAt;
    private List<UserGroupResponse> groups;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public List<UserGroupResponse> getGroups() {
        return groups;
    }

    public void setGroups(List<UserGroupResponse> groups) {
        this.groups = groups;
    }
}
