package com.certalert.controller;

import com.certalert.model.AppGroup;
import com.certalert.model.AppUser;
import com.certalert.repository.AppGroupRepository;
import com.certalert.repository.AppUserRepository;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserRepository appUserRepository;
    private final AppGroupRepository appGroupRepository;

    @Value("${cert-alert.jwt.secret:certalert-dev-secret-key-that-is-long-enough-for-hmac-sha256}")
    private String jwtSecret;

    @PostMapping("/dev-token")
    public ResponseEntity<Map<String, Object>> generateDevToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");

        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        try {
            byte[] secret = jwtSecret.getBytes();
            if (secret.length < 32) {
                byte[] padded = new byte[32];
                System.arraycopy(secret, 0, padded, 0, Math.min(secret.length, 32));
                secret = padded;
            }

            MACSigner signer = new MACSigner(secret);

            Date now = new Date();
            Date expiry = new Date(now.getTime() + 86400000L);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(user.getUsername())
                    .claim("preferred_username", user.getUsername())
                    .claim("role", user.getRole().name())
                    .claim("group", user.getGroup().getName())
                    .claim("scope", "certalert.read certalert.write")
                    .issueTime(now)
                    .expirationTime(expiry)
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.HS256)
                            .type(JOSEObjectType.JWT)
                            .build(),
                    claims);

            jwt.sign(signer);

            return ResponseEntity.ok(Map.of(
                    "token", jwt.serialize(),
                    "username", user.getUsername(),
                    "group", user.getGroup().getName(),
                    "role", user.getRole().name()
            ));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers() {
        return ResponseEntity.ok(appUserRepository.findAll().stream()
                .map(u -> Map.of(
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "role", u.getRole().name(),
                        "group", u.getGroup().getName()
                ))
                .toList());
    }
}