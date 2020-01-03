package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.model.server.ServerSensor;
import com.loohos.factoryinspection.model.server.ServerSensorNode;

import java.util.Date;
import java.util.List;

public interface ServerSensorNodeService extends DAO<ServerSensorNode> {
    ServerSensorNode getNodeBySensorWithTypeAndCurTime(ServerSensor sensor, SensorType topTempSensor);

    List<Double> getNodeBySensorWithTypeAndCurDay(ServerSensor sensor, SensorType topTempSensor);

    List<Date> getTimeBySensorAndCurDay(ServerSensor sensor);

    List<Double> getHistoryNodeBySensorWithTypeAndCurDay(ServerSensor sensor, SensorType topTempSensor);

    List<Date> getHistoryTimeBySensorAndCurDay(ServerSensor sensor);

    List<Double> getValuesBySensorAndRange(ServerSensor sensor, SensorType topTempSensor, Date startDate, Date endDate);

    List<Date> getDatesBySensorAndRange(ServerSensor sensor, Date startDate, Date endDate);

    List<Double> getLastSixNodeBySensorWithTypeAndCurDay(ServerSensor sensor, SensorType topTempSensor, int hours);

    List<Date> getLastSixTimeBySensorAndCurDay(ServerSensor sensor, int hours);
}
