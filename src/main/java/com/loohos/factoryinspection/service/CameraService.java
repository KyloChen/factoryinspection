package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Camera;

import java.util.List;

public interface CameraService extends DAO<Camera>{

    List<Camera> getCamerasByPlantCode(int plantCode);

    Camera getUrlExistedCamera(String cameraUrl);

    Camera getSerialExistedCamera(String cameraSerialNum);

    Camera getCamerasByPlantAndPit(int plantCode, int pitCode);
}
