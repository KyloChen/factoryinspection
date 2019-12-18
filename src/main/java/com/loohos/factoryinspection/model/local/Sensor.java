package com.loohos.factoryinspection.model.local;


import com.loohos.factoryinspection.enumeration.SensorWorkingType;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class Sensor {
    private String sensorId;
    private String sensorCode;
    private String sensorName;
    private Date createdTime;
    private int frequencyPoint;
    private int workRate;
    private int activatePeriod;
    private int workingHours;
    private String batteryState = "00";
    private int alarmLevel = 4;
    private SensorWorkingType sensorWorkingType = SensorWorkingType.SENSOR_IS_WORKING;
    private Cellar cellar;
    private Set<SensorNode> sensorNodeSet;

    @Id
    @Column(nullable = false)
    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    @Column(nullable = false)
    public String getSensorCode() {
        return sensorCode;
    }

    public void setSensorCode(String sensorCode) {
        this.sensorCode = sensorCode;
    }

    @Column
    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Column
    public int getFrequencyPoint() {
        return frequencyPoint;
    }

    public void setFrequencyPoint(int frequencyPoint) {
        this.frequencyPoint = frequencyPoint;
    }

    @Column
    public int getWorkRate() {
        return workRate;
    }

    public void setWorkRate(int workRate) {
        this.workRate = workRate;
    }

    @Column
    public int getActivatePeriod() {
        return activatePeriod;
    }

    public void setActivatePeriod(int activatePeriod) {
        this.activatePeriod = activatePeriod;
    }

    @Column
    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    @Column
    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    @Column
    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(length = 40, nullable = true)
    public SensorWorkingType getSensorWorkingType() {
        return sensorWorkingType;
    }

    public void setSensorWorkingType(SensorWorkingType sensorWorkingType) {
        this.sensorWorkingType = sensorWorkingType;
    }

    @OneToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "cellar_id")
    public Cellar getCellar() {
        return cellar;
    }

    public void setCellar(Cellar cellar) {
        this.cellar = cellar;
    }

    @OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy = "sensor")
    public Set<SensorNode> getSensorNodeSet() {
        return sensorNodeSet;
    }

    public void setSensorNodeSet(Set<SensorNode> sensorNodeSet) {
        this.sensorNodeSet = sensorNodeSet;
    }
}