package com.certalert.controller;

import com.certalert.dto.ThresholdRequest;
import com.certalert.dto.ThresholdResponse;
import com.certalert.service.ThresholdService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thresholds")
@RequiredArgsConstructor
public class ThresholdController {

    private final ThresholdService thresholdService;

    @PostMapping
    public ResponseEntity<ThresholdResponse> createThreshold(@RequestBody ThresholdRequest request) {
        ThresholdResponse response = thresholdService.createThreshold(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ThresholdResponse>> listThresholds() {
        List<ThresholdResponse> thresholds = thresholdService.listThresholds();
        return ResponseEntity.ok(thresholds);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ThresholdResponse>> listActiveThresholds() {
        List<ThresholdResponse> thresholds = thresholdService.listActiveThresholds();
        return ResponseEntity.ok(thresholds);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThresholdResponse> updateThreshold(
            @PathVariable Long id,
            @RequestBody ThresholdRequest request) {
        ThresholdResponse response = thresholdService.updateThreshold(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteThreshold(@PathVariable Long id) {
        thresholdService.deleteThreshold(id);
    }
}