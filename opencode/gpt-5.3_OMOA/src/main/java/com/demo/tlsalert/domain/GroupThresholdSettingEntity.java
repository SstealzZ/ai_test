package com.demo.tlsalert.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "group_threshold_settings")
public class GroupThresholdSettingEntity {

    @Id
    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "threshold_days", nullable = false)
    private int thresholdDays;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getThresholdDays() {
        return thresholdDays;
    }

    public void setThresholdDays(int thresholdDays) {
        this.thresholdDays = thresholdDays;
    }
}
