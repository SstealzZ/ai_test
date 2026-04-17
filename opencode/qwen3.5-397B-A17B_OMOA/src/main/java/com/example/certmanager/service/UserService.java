package com.example.certmanager.service;

import com.example.certmanager.dto.response.UserResponse;

public interface UserService {

    UserResponse getCurrentUser(Long userId);
}
