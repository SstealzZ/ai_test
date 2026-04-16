package com.certwatcher.dto;

import jakarta.validation.constraints.NotBlank;

public record AddCertByUrlRequest(
        @NotBlank String alias,
        @NotBlank String hostname,
        int port
) {
    public int port() {
        return port == 0 ? 443 : port;
    }
}
