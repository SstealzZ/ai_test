package com.certalert.service;

import com.certalert.dto.ThresholdRequest;
import com.certalert.dto.ThresholdResponse;
import com.certalert.model.Threshold;
import com.certalert.repository.ThresholdRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThresholdService {

    private final ThresholdRepository thresholdRepository;

    @Value("${cert-alert.threshold.default-days:30}")
    private Integer defaultThresholdDays;

    @Transactional
    public ThresholdResponse createThreshold(ThresholdRequest request) {
        Threshold threshold = Threshold.builder()
                .name(request.getName())
                .days(request.getDays())
                .active(request.getActive() != null ? request.getActive() : true)
                .groupId(request.getGroupId())
                .build();
        threshold = thresholdRepository.save(threshold);
        log.info("Threshold created: id={}, name={}, days={}", threshold.getId(), threshold.getName(), threshold.getDays());
        return toResponse(threshold);
    }

    public List<ThresholdResponse> listThresholds() {
        return thresholdRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<ThresholdResponse> listActiveThresholds() {
        return thresholdRepository.findByActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ThresholdResponse updateThreshold(Long id, ThresholdRequest request) {
        Threshold threshold = thresholdRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Threshold not found: " + id));

        if (request.getName() != null) threshold.setName(request.getName());
        if (request.getDays() != null) threshold.setDays(request.getDays());
        if (request.getActive() != null) threshold.setActive(request.getActive());
        if (request.getGroupId() != null) threshold.setGroupId(request.getGroupId());

        threshold = thresholdRepository.save(threshold);
        log.info("Threshold updated: id={}, name={}, days={}", threshold.getId(), threshold.getName(), threshold.getDays());
        return toResponse(threshold);
    }

    @Transactional
    public void deleteThreshold(Long id) {
        if (!thresholdRepository.existsById(id)) {
            throw new IllegalArgumentException("Threshold not found: " + id);
        }
        thresholdRepository.deleteById(id);
        log.info("Threshold deleted: id={}", id);
    }

    public Integer getDefaultThresholdDays() {
        return defaultThresholdDays;
    }

    private ThresholdResponse toResponse(Threshold threshold) {
        return ThresholdResponse.builder()
                .id(threshold.getId())
                .name(threshold.getName())
                .days(threshold.getDays())
                .active(threshold.getActive())
                .groupId(threshold.getGroupId())
                .createdAt(threshold.getCreatedAt())
                .updatedAt(threshold.getUpdatedAt())
                .build();
    }
}