package com.demo.tlsalert.service;

import com.demo.tlsalert.domain.GroupThresholdSettingEntity;
import com.demo.tlsalert.repository.GroupThresholdSettingRepository;
import com.demo.tlsalert.security.CurrentUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ThresholdService {

    private final GroupThresholdSettingRepository repository;
    private final CurrentUserService currentUserService;
    private final int defaultThresholdDays;

    public ThresholdService(
        GroupThresholdSettingRepository repository,
        CurrentUserService currentUserService,
        @Value("${app.alert.default-threshold-days:30}") int defaultThresholdDays
    ) {
        this.repository = repository;
        this.currentUserService = currentUserService;
        this.defaultThresholdDays = defaultThresholdDays;
    }

    public int getCurrentGroupThreshold() {
        return getGroupThreshold(currentUserService.currentGroup());
    }

    public int getGroupThreshold(String groupName) {
        return repository.findById(groupName)
            .map(GroupThresholdSettingEntity::getThresholdDays)
            .orElse(defaultThresholdDays);
    }

    @Transactional
    public int updateCurrentGroupThreshold(int thresholdDays) {
        String group = currentUserService.currentGroup();
        GroupThresholdSettingEntity setting = repository.findById(group).orElseGet(() -> {
            GroupThresholdSettingEntity entity = new GroupThresholdSettingEntity();
            entity.setGroupName(group);
            return entity;
        });
        setting.setThresholdDays(thresholdDays);
        repository.save(setting);
        return thresholdDays;
    }
}
