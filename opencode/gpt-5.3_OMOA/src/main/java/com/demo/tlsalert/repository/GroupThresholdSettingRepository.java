package com.demo.tlsalert.repository;

import com.demo.tlsalert.domain.GroupThresholdSettingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupThresholdSettingRepository extends JpaRepository<GroupThresholdSettingEntity, String> {
}
