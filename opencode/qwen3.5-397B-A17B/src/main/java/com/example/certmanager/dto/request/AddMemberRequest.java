package com.example.certmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AddMemberRequest {

    @NotBlank(message = "User email is required")
    private String email;

    @Pattern(regexp = "^(OWNER|ADMIN|MEMBER|VIEWER)$", message = "Role must be one of: OWNER, ADMIN, MEMBER, VIEWER")
    private String role = "MEMBER";

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
