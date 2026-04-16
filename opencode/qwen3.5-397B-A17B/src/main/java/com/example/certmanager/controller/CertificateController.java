package com.example.certmanager.controller;

import com.example.certmanager.dto.request.CertificateRequest;
import com.example.certmanager.dto.request.CertificateUrlRequest;
import com.example.certmanager.dto.request.ThresholdUpdateRequest;
import com.example.certmanager.dto.response.ApiResponse;
import com.example.certmanager.dto.response.CertificateResponse;
import com.example.certmanager.dto.response.PagedResponse;
import com.example.certmanager.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.certmanager.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateService certificateService;

    public CertificateController(CertificateService certificateService) {
        this.certificateService = certificateService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<CertificateResponse>>> listCertificates(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(required = false) Long groupId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long userId = principal.getUserId();
        PagedResponse<CertificateResponse> response = certificateService.listCertificates(
                userId, groupId, status, org.springframework.data.domain.PageRequest.of(page, size));

        return ResponseEntity.ok(ApiResponse.success("Certificates retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CertificateResponse>> getCertificate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        Long userId = principal.getUserId();
        CertificateResponse response = certificateService.getCertificateById(id, userId);

        return ResponseEntity.ok(ApiResponse.success("Certificate retrieved successfully", response));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CertificateResponse>> addCertificate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long groupId) {

        Long userId = principal.getUserId();
        CertificateRequest request = new CertificateRequest();
        request.setName(name != null ? name : file.getOriginalFilename());
        request.setDescription(description);
        request.setGroupId(groupId);

        try {
            request.setPemContent(new String(file.getBytes()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to read certificate file"));
        }

        CertificateResponse response = certificateService.addCertificate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Certificate added successfully", response));
    }

    @PostMapping(value = "/from-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CertificateResponse>> addCertificateFromUrl(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Valid @RequestBody CertificateUrlRequest request) {

        Long userId = principal.getUserId();
        CertificateResponse response = certificateService.addCertificateFromUrl(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Certificate added from URL successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCertificate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        Long userId = principal.getUserId();
        certificateService.deleteCertificate(id, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success("Certificate deleted successfully", null));
    }

    @PutMapping("/{id}/threshold")
    public ResponseEntity<ApiResponse<CertificateResponse>> updateThreshold(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody ThresholdUpdateRequest request) {

        Long userId = principal.getUserId();
        CertificateResponse response = certificateService.updateThreshold(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success("Threshold updated successfully", response));
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<ApiResponse<CertificateResponse>> refreshCertificate(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {

        Long userId = principal.getUserId();
        CertificateResponse response = certificateService.refreshCertificate(id, userId);

        return ResponseEntity.ok(ApiResponse.success("Certificate refreshed successfully", response));
    }
}
