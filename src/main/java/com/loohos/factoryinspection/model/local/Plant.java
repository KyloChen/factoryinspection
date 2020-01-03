package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Plant {
    private String plantId;
    private int plantCode;
    private String plantName;
    private int clientX;
    private int clientY;
    private int alarmLevel;


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
    public int getClientX() {
        return clientX;
    }

    public void setClientX(int clientX) {
        this.clientX = clientX;
    }

    @Column
    public int getClientY() {
        return clientY;
    }

    public void setClientY(int clientY) {
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
    public String getPlantName() {
        return plantName;
    }

    public void setPlantName(String plantName) {
        this.plantName = plantName;
    }


}
