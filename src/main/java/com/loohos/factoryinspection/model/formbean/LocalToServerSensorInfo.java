package com.loohos.factoryinspection.model.formbean;

public class LocalToServerSensorInfo extends BaseFormBean {
    private String sensorCode;
    private int frequencyPoint;
    private int workRate;
    private int activatePeriod;
    private int workingHours;
    private String batteryState = "00";
    private String localSensorId;

    public String getSensorCode() {
        return sensorCode;
    }

    public void setSensorCode(String sensorCode) {
        this.sensorCode = sensorCode;
    }

    public int getFrequencyPoint() {
        return frequencyPoint;
    }

    public void setFrequencyPoint(int frequencyPoint) {
        this.frequencyPoint = frequencyPoint;
    }

    public int getWorkRate() {
        return workRate;
    }

    public void setWorkRate(int workRate) {
        this.workRate = workRate;
    }

    public int getActivatePeriod() {
        return activatePeriod;
    }

    public void setActivatePeriod(int activatePeriod) {
        this.activatePeriod = activatePeriod;
    }

    public int getWorkingHours() {
        return workingHours;
    }

    public void setWorkingHours(int workingHours) {
        this.workingHours = workingHours;
    }

    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    public String getLocalSensorId() {
        return localSensorId;
    }

    public void setLocalSensorId(String localSensorId) {
        this.localSensorId = localSensorId;
    }
}
