package com.certalert.tlscert.controller;

import com.certalert.tlscert.dto.LoginRequest;
import com.certalert.tlscert.dto.LoginResponse;
import com.certalert.tlscert.security.JwtService;
import com.certalert.tlscert.security.SecurityUserDetails;
import com.certalert.tlscert.security.SecurityUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final SecurityUserDetailsService userDetailsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          SecurityUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityUserDetails user = (SecurityUserDetails) auth.getPrincipal();
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .username(user.getUsername())
                .role(user.getRole().name())
                .groupName(user.getGroupName())
                .build());
    }
}
