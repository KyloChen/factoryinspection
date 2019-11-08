package com.loohos.factoryinspection.model.local;

import javax.persistence.*;

@Entity
public class Factory {
    private String factoryId;
    private String factoryCode;
    private String factoryName;
    private String factoryClientX;
    private String factoryClientY;
    private String factoryAlarmLevel;
    private String factoryAlarmColor;

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column(length = 40, nullable = false)
    public String getFactoryId() {
        return factoryId;
    }


    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    @Column(length = 40, nullable = false)
    public String getFactoryCode() {
        return factoryCode;
    }

    public void setFactoryCode(String factoryCode) {
        this.factoryCode = factoryCode;
    }

    @Column(length = 40)
    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    @Column(length = 40, nullable = false)
    public String getFactoryClientX() {
        return factoryClientX;
    }

    public void setFactoryClientX(String factoryClientX) {
        this.factoryClientX = factoryClientX;
    }

    @Column(length = 40, nullable = false)
    public String getFactoryClientY() {
        return factoryClientY;
    }

    public void setFactoryClientY(String factoryClientY) {
        this.factoryClientY = factoryClientY;
    }

    @Column(length = 40, nullable = false)
    public String getFactoryAlarmLevel() {
        return factoryAlarmLevel;
    }

    public void setFactoryAlarmLevel(String factoryAlarmLevel) {
        this.factoryAlarmLevel = factoryAlarmLevel;
    }

    @Column(length = 40, nullable = false)
    public String getFactoryAlarmColor() {
        return factoryAlarmColor;
    }

    public void setFactoryAlarmColor(String factoryAlarmColor) {
        this.factoryAlarmColor = factoryAlarmColor;
    }
}
