package com.certwatcher.repository;

import com.certwatcher.domain.CertGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CertGroupRepository extends JpaRepository<CertGroup, Long> {
    Optional<CertGroup> findByName(String name);
}
