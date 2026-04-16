package com.demo.tlsalert.api;

import com.demo.tlsalert.api.dto.AddFromUrlRequest;
import com.demo.tlsalert.api.dto.CertificateResponse;
import com.demo.tlsalert.service.CertificateIngestionService;
import com.demo.tlsalert.service.CertificateService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateIngestionService ingestionService;
    private final CertificateService certificateService;

    public CertificateController(CertificateIngestionService ingestionService, CertificateService certificateService) {
        this.ingestionService = ingestionService;
        this.certificateService = certificateService;
    }

    @PreAuthorize("hasAuthority('CERT_ADD')")
    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CertificateResponse upload(@RequestPart("file") MultipartFile file) {
        return ingestionService.addFromFile(file);
    }

    @PreAuthorize("hasAuthority('CERT_ADD')")
    @PostMapping(path = "/from-url", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CertificateResponse addFromUrl(@RequestBody @Valid AddFromUrlRequest request) {
        return ingestionService.addFromUrl(request.url());
    }

    @PreAuthorize("hasAuthority('CERT_VIEW')")
    @GetMapping
    public List<CertificateResponse> list() {
        return certificateService.listCurrentGroupCertificates();
    }
}
