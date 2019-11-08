package com.loohos.factoryinspection.model.formbean;

public class ValueAlarmLevel extends BaseFormBean {
    private double sensorValue;
    private String alarmColor;
    private String alarmLevel;

    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    public String getAlarmColor() {
        return alarmColor;
    }

    public void setAlarmColor(String alarmColor) {
        this.alarmColor = alarmColor;
    }

    public String getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(String alarmLevel) {
        this.alarmLevel = alarmLevel;
    }
}
