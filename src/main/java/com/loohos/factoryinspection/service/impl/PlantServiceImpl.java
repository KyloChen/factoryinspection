package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.service.PlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class PlantServiceImpl extends DaoSupport<Plant> implements PlantService  {
    Logger logger = LoggerFactory.getLogger(PlantServiceImpl.class);
    @Override
    public Plant getPlantByCode(int plantCode) {
        Query query = em.createQuery("select o from Plant o where o.plantCode = ?1");
        query.setParameter(1, plantCode);
        query.setMaxResults(1);
        try {
            return (Plant) query.getSingleResult();
        }catch (Exception e){
            logger.info("error get plant by code");
            return null;
        }
    }
}
