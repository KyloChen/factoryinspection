package com.loohos.factoryinspection.model.local;

import com.loohos.factoryinspection.enumeration.SensorType;

import javax.persistence.*;
import java.util.Date;

@Entity
public class SensorNode {
    private String sensorNodeId;
    private Date createdTime;
    private double sensorValue;
    private SensorType sensorType;
    private int alarmLevel = 3;
    private Sensor sensor;

    @Id
    @Column(nullable = false)
    public String getSensorNodeId() {
        return sensorNodeId;
    }

    public void setSensorNodeId(String sensorNodeId) {
        this.sensorNodeId = sensorNodeId;
    }


    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Column
    public double getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(double sensorValue) {
        this.sensorValue = sensorValue;
    }

    @Enumerated(value = EnumType.STRING)
    @Column(length = 40, nullable = true)
    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    @Column
    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }





    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id")
    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }
}
