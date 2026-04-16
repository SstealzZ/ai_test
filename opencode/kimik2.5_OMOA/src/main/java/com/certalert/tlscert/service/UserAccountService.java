package com.certalert.tlscert.service;

import com.certalert.tlscert.entity.UserAccount;
import com.certalert.tlscert.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public UserAccount findById(UUID id) {
        return userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }
}
