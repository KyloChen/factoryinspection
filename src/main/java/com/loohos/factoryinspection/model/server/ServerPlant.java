package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerPlant {
    private String plantId;
    private int plantCode;
    private String plantName;
    private double clientX;
    private double clientY;
    private int alarmLevel;
    private String localPlantId;

    @Id
    @Column(nullable = false)
    public String getPlantId() {
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    @Column
    public int getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(int plantCode) {
        this.plantCode = plantCode;
    }

    @Column
    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }

    @Column(nullable = false)
    public double getClientX() {
        return clientX;
    }

    public void setClientX(double clientX) {
        this.clientX = clientX;
    }

    @Column(nullable = false)
    public double getClientY() {
        return clientY;
    }

    public void setClientY(double clientY) {
        this.clientY = clientY;
    }

    @Column
    public int getAlarmLevel() {
        return alarmLevel;
    }

    public void setAlarmLevel(int alarmLevel) {
        this.alarmLevel = alarmLevel;
    }

    @Column
    public String getLocalPlantId() {
        return localPlantId;
    }

    public void setLocalPlantId(String localPlantId) {
        this.localPlantId = localPlantId;
    }
}
