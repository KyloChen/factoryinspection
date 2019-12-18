package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Plant;

public interface PlantService extends DAO<Plant>{
    Plant getPlantByCode(int plantCode);
}
