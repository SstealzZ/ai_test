package com.certalert.repository;

import com.certalert.model.AppGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppGroupRepository extends JpaRepository<AppGroup, Long> {
    Optional<AppGroup> findByName(String name);
}