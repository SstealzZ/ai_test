package com.certalert.tlscert;

import com.certalert.tlscert.dto.LoginRequest;
import com.certalert.tlscert.dto.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TlsCertAlertApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        LoginRequest request = new LoginRequest("alice", "password");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity("/api/v1/auth/login", request, LoginResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
        assertThat(response.getBody().getRole()).isEqualTo("CERT_ADMIN");
    }

    @Test
    void login_withInvalidCredentials_returns401() {
        LoginRequest request = new LoginRequest("alice", "wrong");
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity("/api/v1/auth/login", request, LoginResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listCertificates_withoutAuth_returns401() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/certificates", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listCertificates_withAuth_returns200() {
        String token = obtainToken("alice", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/certificates", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void uploadEndpoint_viewerAccess_returns403() {
        String token = obtainToken("bob", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/certificates/upload", HttpMethod.POST, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void viewerCanReadAlertConfig_returns200() {
        String token = obtainToken("bob", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/alerts/config", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void viewerCannotUpdateAlertConfig_returns403() {
        String token = obtainToken("bob", "password");
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange("/api/v1/alerts/config?thresholdDays=15", HttpMethod.PUT, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    private String obtainToken(String username, String password) {
        LoginRequest request = new LoginRequest(username, password);
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity("/api/v1/auth/login", request, LoginResponse.class);
        return response.getBody().getToken();
    }
}
