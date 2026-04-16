package com.certalert.repository;

import com.certalert.model.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    List<Threshold> findByActiveTrue();
    List<Threshold> findByGroupIdAndActiveTrue(Long groupId);
}