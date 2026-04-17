package com.example.certmanager.service;

import com.example.certmanager.dto.response.UserGroupResponse;
import com.example.certmanager.dto.response.UserResponse;
import com.example.certmanager.entity.User;
import com.example.certmanager.exception.ResourceNotFoundException;
import com.example.certmanager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.userNotFound(userId));

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        response.setGivenName(user.getGivenName());
        response.setFamilyName(user.getFamilyName());
        response.setPicture(user.getPicture());
        response.setLastLoginAt(user.getLastLoginAt());

        return response;
    }
}
