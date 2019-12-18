package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.local.Territory;

import java.util.List;
import java.util.Set;

public interface TerritoryService extends DAO<Territory> {
    List<Territory> getTerritoriesByPlant(Plant plant1);

    Territory getTerritoryByPlantAndTerritoryCode(Plant plant, int territoryCode);
}
