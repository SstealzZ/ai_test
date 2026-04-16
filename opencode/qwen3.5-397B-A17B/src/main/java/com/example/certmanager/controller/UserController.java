package com.example.certmanager.controller;

import com.example.certmanager.dto.response.ApiResponse;
import com.example.certmanager.dto.response.UserResponse;
import com.example.certmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import com.example.certmanager.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails principal) {

        Long userId = principal.getUserId();
        UserResponse response = userService.getCurrentUser(userId);

        return ResponseEntity.ok(ApiResponse.success("User info retrieved successfully", response));
    }
}
