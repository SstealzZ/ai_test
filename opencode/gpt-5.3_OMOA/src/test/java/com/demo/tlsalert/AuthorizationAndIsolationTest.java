package com.demo.tlsalert;

import com.demo.tlsalert.domain.CertificateEntity;
import com.demo.tlsalert.domain.SourceType;
import com.demo.tlsalert.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthorizationAndIsolationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CertificateRepository certificateRepository;

    @BeforeEach
    void setupData() {
        certificateRepository.deleteAll();

        certificateRepository.save(buildCert("GROUP_A", "a-late", Instant.parse("2030-06-01T00:00:00Z")));
        certificateRepository.save(buildCert("GROUP_A", "a-early", Instant.parse("2028-01-01T00:00:00Z")));
        certificateRepository.save(buildCert("GROUP_B", "b-only", Instant.parse("2029-03-01T00:00:00Z")));
    }

    @Test
    @WithUserDetails(value = "bob", userDetailsServiceBeanName = "demoUserStore")
    void viewerCannotAddCertificate() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "demo.cer", "application/pkix-cert", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/certificates/upload").file(file))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "alice", userDetailsServiceBeanName = "demoUserStore")
    void editorCanAccessAddEndpoint() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "demo.cer", "application/pkix-cert", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/certificates/upload").file(file))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails(value = "alice", userDetailsServiceBeanName = "demoUserStore")
    void listShowsOnlyCurrentGroupAndSortedByExpiryDesc() throws Exception {
        mockMvc.perform(get("/api/certificates"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].sourceValue").value("a-late"))
            .andExpect(jsonPath("$[1].sourceValue").value("a-early"));
    }

    private CertificateEntity buildCert(String group, String sourceValue, Instant notAfter) {
        CertificateEntity entity = new CertificateEntity();
        entity.setGroupName(group);
        entity.setSourceType(SourceType.FILE);
        entity.setSourceValue(sourceValue);
        entity.setSubjectDn("CN=" + sourceValue);
        entity.setIssuerDn("CN=Issuer");
        entity.setSerialNumber(sourceValue + "-serial");
        entity.setFingerprintSha256(sourceValue + "-fp");
        entity.setNotBefore(Instant.parse("2024-01-01T00:00:00Z"));
        entity.setNotAfter(notAfter);
        entity.setCreatedBy("seed");
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
