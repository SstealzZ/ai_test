package com.certwatcher;

import com.certwatcher.domain.AppUser;
import com.certwatcher.domain.CertGroup;
import com.certwatcher.domain.UserRole;
import com.certwatcher.repository.AppUserRepository;
import com.certwatcher.repository.CertGroupRepository;
import com.certwatcher.repository.CertificateRepository;
import com.certwatcher.service.CertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CertWatcherApplicationTests {

    @Autowired
    MockMvc mvc;

    @Autowired
    CertGroupRepository groupRepo;

    @Autowired
    AppUserRepository userRepo;

    @Autowired
    CertificateRepository certRepo;

    @Autowired
    CertificateService certService;

    private CertGroup opsGroup;
    private CertGroup devGroup;

    @BeforeEach
    void setUp() {
        certRepo.deleteAll();
        userRepo.deleteAll();
        groupRepo.deleteAll();

        opsGroup = groupRepo.save(new CertGroup("ops"));
        devGroup = groupRepo.save(new CertGroup("dev"));

        userRepo.save(new AppUser("alice", UserRole.CERT_ADMIN, opsGroup));
        userRepo.save(new AppUser("bob",   UserRole.CERT_VIEWER, opsGroup));
        userRepo.save(new AppUser("carol", UserRole.CERT_ADMIN, devGroup));
    }

    @Test
    void contextLoads() {
        assertThat(groupRepo.findByName("ops")).isPresent();
        assertThat(userRepo.findByUsername("alice")).isPresent();
    }

    @Test
    void unauthenticatedRequestRedirectsToLogin() throws Exception {
        mvc.perform(get("/ui/certs"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/v1/certificates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "bob")
    void viewerCannotUploadCertificate() {
        byte[] fakeCert = new byte[]{};
        MockMultipartFile file = new MockMultipartFile("file", "test.cer",
                MediaType.APPLICATION_OCTET_STREAM_VALUE, fakeCert);

        assertThatThrownBy(() -> certService.addFromFile("my-cert", file))
                .hasMessageContaining("CERT_ADMIN");
    }

    @Test
    @WithMockUser(username = "alice")
    void adminCanListCertificatesInOwnGroup() {
        var certs = certService.listForCurrentUser();
        assertThat(certs).isNotNull();
    }

    @Test
    @WithMockUser(username = "alice")
    void groupIsolation_aliceCannotSeeCarolsCerts() throws Exception {
        var aliceCerts = certService.listForCurrentUser();
        assertThat(aliceCerts).allMatch(c -> c.group().equals("ops"));
    }

    @Test
    @WithMockUser(username = "alice")
    void adminCanUpdateThreshold() throws Exception {
        mvc.perform(put("/api/v1/certificates/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alertThresholdDays\": 14, \"webhookUrl\": \"https://example.com/hook\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alertThresholdDays").value(14));

        CertGroup updated = groupRepo.findByName("ops").orElseThrow();
        assertThat(updated.getAlertThresholdDays()).isEqualTo(14);
        assertThat(updated.getWebhookUrl()).isEqualTo("https://example.com/hook");
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "carol")
    void adminCanFetchCertFromUrl() {
        var response = certService.addFromUrl("google-tls", "www.google.com", 443);
        assertThat(response.alias()).isEqualTo("google-tls");
        assertThat(response.subjectDn()).isNotBlank();
        assertThat(response.notAfter()).isNotNull();
        assertThat(response.group()).isEqualTo("dev");
    }
}
