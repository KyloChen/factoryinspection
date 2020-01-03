package com.loohos.factoryinspection.model.server;

import com.loohos.factoryinspection.enumeration.SensorWorkingType;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
public class ServerSensor {
    private String sensorId;
    private String sensorCode;
    private String sensorName;
    private Date createdTime;
    private int frequencyPoint;
    private int workRate;
    private int activatePeriod;
    private int workingHours;
    private String batteryState = "00";
    private int alarmLevel;
    private double curTopNodeValue = 100;
    private double curMidNodeValue = 100;
    private double curBotNodeValue = 100;
    private SensorWorkingType sensorWorkingType;
    private ServerCellar serverCellar;
    private String localSensorId;
    private String savingType;

    @Id
    @Column
    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    @Column
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

    @Column
    public double getCurTopNodeValue() {
        return curTopNodeValue;
    }

    public void setCurTopNodeValue(double curTopNodeValue) {
        this.curTopNodeValue = curTopNodeValue;
    }

    @Column
    public double getCurMidNodeValue() {
        return curMidNodeValue;
    }

    public void setCurMidNodeValue(double curMidNodeValue) {
        this.curMidNodeValue = curMidNodeValue;
    }

    @Column
    public double getCurBotNodeValue() {
        return curBotNodeValue;
    }

    public void setCurBotNodeValue(double curBotNodeValue) {
        this.curBotNodeValue = curBotNodeValue;
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
    public ServerCellar getServerCellar() {
        return serverCellar;
    }

    public void setServerCellar(ServerCellar serverCellar) {
        this.serverCellar = serverCellar;
    }

    @Column
    public String getLocalSensorId() {
        return localSensorId;
    }

    public void setLocalSensorId(String localSensorId) {
        this.localSensorId = localSensorId;
    }

    @Column
    public String getSavingType() {
        return savingType;
    }

    public void setSavingType(String savingType) {
        this.savingType = savingType;
    }

}
