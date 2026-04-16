package com.demo.tlsalert.api.dto;

import jakarta.validation.constraints.NotBlank;

public record AddFromUrlRequest(@NotBlank String url) {
}
