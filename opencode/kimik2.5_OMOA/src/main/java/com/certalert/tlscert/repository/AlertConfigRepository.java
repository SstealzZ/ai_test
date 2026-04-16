package com.certalert.tlscert.repository;

import com.certalert.tlscert.entity.AlertConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AlertConfigRepository extends JpaRepository<AlertConfig, UUID> {
}
