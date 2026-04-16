package com.certalert.tlscert.repository;

import com.certalert.tlscert.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    Optional<UserAccount> findByUsername(String username);
}
