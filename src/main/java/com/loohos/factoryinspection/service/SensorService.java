package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Sensor;

import java.util.List;

public interface SensorService extends DAO<Sensor>{

    Sensor getSensorByCellar(Cellar cellar);

    Sensor getDeleteSensorByCellar(Cellar cellar,SensorWorkingType sensorWorkingType);

    Sensor getBoolByIsWorkingAndSensorCode(String sensorCode,SensorWorkingType sensorWorkingType);

    List<Sensor> getWorkingSensor();

}
