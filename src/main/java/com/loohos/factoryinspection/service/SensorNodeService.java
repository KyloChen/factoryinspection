package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.model.local.Sensor;
import com.loohos.factoryinspection.model.local.SensorNode;

import java.util.Date;
import java.util.List;

public interface SensorNodeService extends DAO<SensorNode> {
    SensorNode getNodeBySensorWithTypeAndCurTime(Sensor sensor, SensorType sensorType);

    List<Double> getNodeBySensorWithTypeAndCurDay(Sensor sensor, SensorType topTempSensor);

    List<Date> getTimeBySensorAndCurDay(Sensor sensor);

    List<Double> getHistoryNodeBySensorWithTypeAndCurDay(Sensor sensor, SensorType topTempSensor);

    List<Date> getHistoryTimeBySensorAndCurDay(Sensor sensor);


    List<Double> getValuesBySensorAndRange(Sensor sensor, SensorType topTempSensor, Date startDate, Date endDate);

    List<Date> getDatesBySensorAndRange(Sensor sensor, Date startDate, Date endDate);
}
