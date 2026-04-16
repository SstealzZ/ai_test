package com.certalert.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CertificateUrlRequest {

    @NotBlank(message = "URL is required")
    private String url;
}