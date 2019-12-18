package com.loohos.factoryinspection.model.server;

import com.loohos.factoryinspection.enumeration.SensorType;

import javax.persistence.*;
import java.util.Date;

@Entity
public class ServerSensorNode {
    private String sensorNodeId;
    private Date createdTime;
    private double sensorValue;
    private SensorType sensorType;
    private int AlarmLevel = 3;
    private ServerSensor serverSensor;

    @Id
    @Column
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
        return AlarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        AlarmLevel = alarmLevel;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor_id")
    public ServerSensor getServerSensor() {
        return serverSensor;
    }

    public void setServerSensor(ServerSensor serverSensor) {
        this.serverSensor = serverSensor;
    }

}
