package com.certalert.config;

import com.certalert.model.AppGroup;
import com.certalert.model.AppUser;
import com.certalert.repository.AppGroupRepository;
import com.certalert.repository.AppUserRepository;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${cert-alert.jwt.secret:certalert-dev-secret-key-that-is-long-enough-for-hmac-sha256}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/h2-console/**", "/", "/index.html",
                        "/css/**", "/js/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        byte[] secretBytes = jwtSecret.getBytes();
        if (secretBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(secretBytes, 0, padded, 0, Math.min(secretBytes.length, 32));
            secretBytes = padded;
        }
        SecretKey secretKey = new javax.crypto.spec.SecretKeySpec(secretBytes, "HmacSHA256");

        return NimbusJwtDecoder.withSecretKey(secretKey)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopesConverter = new JwtGrantedAuthoritiesConverter();
        scopesConverter.setAuthorityPrefix("SCOPE_");
        scopesConverter.setAuthoritiesClaimName("scope");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = scopesConverter.convert(jwt);

            String role = jwt.getClaimAsString("role");
            if (role != null) {
                authorities = new java.util.ArrayList<>(authorities);
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
            }

            String group = jwt.getClaimAsString("group");
            if (group != null) {
                authorities = new java.util.ArrayList<>(authorities);
                authorities.add(new SimpleGrantedAuthority("GROUP_" + group.toUpperCase()));
            }

            return authorities;
        });

        return converter;
    }

    @Bean
    public CommandLineRunner seedData(AppGroupRepository groupRepository, AppUserRepository userRepository) {
        return args -> {
            if (groupRepository.count() > 0) {
                log.info("Database already seeded, skipping");
                return;
            }

            AppGroup opsGroup = groupRepository.save(AppGroup.builder().name("operations").build());
            AppGroup secGroup = groupRepository.save(AppGroup.builder().name("security").build());
            AppGroup devGroup = groupRepository.save(AppGroup.builder().name("developers").build());

            userRepository.save(AppUser.builder()
                    .username("admin").email("admin@certalert.local")
                    .role(AppUser.Role.CERT_MANAGER).group(opsGroup).build());
            userRepository.save(AppUser.builder()
                    .username("ops-user1").email("ops1@certalert.local")
                    .role(AppUser.Role.CERT_MANAGER).group(opsGroup).build());
            userRepository.save(AppUser.builder()
                    .username("ops-user2").email("ops2@certalert.local")
                    .role(AppUser.Role.CERT_VIEWER).group(opsGroup).build());
            userRepository.save(AppUser.builder()
                    .username("sec-user1").email("sec1@certalert.local")
                    .role(AppUser.Role.CERT_MANAGER).group(secGroup).build());
            userRepository.save(AppUser.builder()
                    .username("sec-user2").email("sec2@certalert.local")
                    .role(AppUser.Role.CERT_VIEWER).group(secGroup).build());
            userRepository.save(AppUser.builder()
                    .username("dev-user1").email("dev1@certalert.local")
                    .role(AppUser.Role.CERT_VIEWER).group(devGroup).build());

            log.info("Seeded {} groups and {} users", groupRepository.count(), userRepository.count());
        };
    }
}