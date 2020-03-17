package com.loohos.factoryinspection.model.local;

import com.loohos.factoryinspection.enumeration.CameraBrand;
import com.loohos.factoryinspection.enumeration.CameraLocation;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Camera {
    private String cameraId;
    private CameraBrand cameraBrand;
    private String cameraName;
    private String cameraUrl;
    private String cameraSerialNum;
    private Date createdTime;
    private CameraLocation cameraLocation;
    private int plantCode;
    private int pitCode;

    @Id
    @Column(nullable = false)
    public String getCameraId() {
        return cameraId;
    }

    public void setCameraId(String cameraId) {
        this.cameraId = cameraId;
    }

    @Column
    public CameraBrand getCameraBrand() {
        return cameraBrand;
    }

    public void setCameraBrand(CameraBrand cameraBrand) {
        this.cameraBrand = cameraBrand;
    }

    @Column
    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }
    @Column
    public String getCameraUrl() {
        return cameraUrl;
    }

    public void setCameraUrl(String cameraUrl) {
        this.cameraUrl = cameraUrl;
    }
    @Column
    public String getCameraSerialNum() {
        return cameraSerialNum;
    }

    public void setCameraSerialNum(String cameraSerialNum) {
        this.cameraSerialNum = cameraSerialNum;
    }
    @Column
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    @Column
    public CameraLocation getCameraLocation() {
        return cameraLocation;
    }

    public void setCameraLocation(CameraLocation cameraLocation) {
        this.cameraLocation = cameraLocation;
    }

    @Column
    public int getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(int plantCode) {
        this.plantCode = plantCode;
    }

    @Column
    public int getPitCode() {
        return pitCode;
    }

    public void setPitCode(int pitCode) {
        this.pitCode = pitCode;
    }
}