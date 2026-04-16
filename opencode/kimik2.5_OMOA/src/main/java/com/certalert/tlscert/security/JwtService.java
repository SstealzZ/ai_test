package com.certalert.tlscert.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String SECRET = "cert-alert-256-bit-secret-key-for-jwt-signing-only!";
    private static final long TWENTY_FOUR_HOURS_IN_MS = 86400000L;
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String generateToken(SecurityUserDetails user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + TWENTY_FOUR_HOURS_IN_MS);
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("groupId", user.getGroupId().toString())
                .claim("groupName", user.getGroupName())
                .claim("role", user.getRole().name())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
