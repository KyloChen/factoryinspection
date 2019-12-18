package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerPlant;
import com.loohos.factoryinspection.model.server.ServerTerritory;

import java.util.List;

public interface ServerTerritoryService extends DAO<ServerTerritory> {
    ServerTerritory getServerTerritoryByLocalId(String localTerritoryId);

    List<ServerTerritory> getTerritoriesByPlant(ServerPlant plant);
}
