package com.loohos.factoryinspection.model.config;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ConfigAlarmLevel {
    private String alarmId;
    private double minValue;
    private double maxValue;
    private String alarmColor;
    private int alarmLevel;

    @Id
    @Column(nullable = false)
    public String getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(String alarmId) {
        this.alarmId = alarmId;
    }

    @Column(nullable = false)
    public double getMinValue() {
        return minValue;
    }

    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    @Column(nullable = false)
    public double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }


    @Column(nullable = false)
    public String getAlarmColor() {
        return alarmColor;
    }

    public void setAlarmColor(String alarmColor) {
        this.alarmColor = alarmColor;
    }


    @Column(nullable = false)
    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
}
