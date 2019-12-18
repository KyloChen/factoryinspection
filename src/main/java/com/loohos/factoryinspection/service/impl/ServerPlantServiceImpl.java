package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.server.ServerPlant;
import com.loohos.factoryinspection.service.ServerPlantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerPlantServiceImpl extends DaoSupport<ServerPlant> implements ServerPlantService {
    private Logger logger = LoggerFactory.getLogger(PlantServiceImpl.class);

    @Override
    public ServerPlant getServerPlantByLocalId(String localPlantId) {
        Query query = em.createQuery("select o from ServerPlant o where o.localPlantId = ?1");
        query.setParameter(1, localPlantId);
        query.setMaxResults(1);
        try {
            return (ServerPlant) query.getSingleResult();
        }catch (Exception e){
            logger.info("error get plant by code");
            return null;
        }
    }
}
