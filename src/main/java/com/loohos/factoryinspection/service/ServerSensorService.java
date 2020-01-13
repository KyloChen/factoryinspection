package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerCellar;
import com.loohos.factoryinspection.model.server.ServerSensor;

public interface ServerSensorService extends DAO<ServerSensor> {
    ServerSensor getServerSensorByLocalId(String localSensorId);

    ServerSensor getSensorByCellar(ServerCellar cellar);

    ServerSensor getWorkingSensorByCellar(ServerCellar cellar);
}
