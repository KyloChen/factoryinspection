package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerPlant;

public interface ServerPlantService extends DAO<ServerPlant> {
    ServerPlant getServerPlantByLocalId(String localPlantId);
}
