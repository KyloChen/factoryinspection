package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerPlant;
import com.loohos.factoryinspection.model.server.ServerTerritory;
import com.loohos.factoryinspection.service.ServerTerritoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerTerritoryServiceImpl extends DaoSupport<ServerTerritory> implements ServerTerritoryService {
    private Logger logger = LoggerFactory.getLogger(ServerTerritoryServiceImpl.class);

    @Override
    public ServerTerritory getServerTerritoryByLocalId(String localTerritoryId) {
        Query query = em.createQuery("select o from ServerTerritory o where o.localTerritoryId = ?1");
        query.setParameter(1, localTerritoryId);
        query.setMaxResults(1);
        try {
            return (ServerTerritory) query.getSingleResult();
        }catch (Exception e){
            logger.info("server error get territory by code");
            return null;
        }

    }

    @Override
    public List<ServerTerritory> getTerritoriesByPlant(ServerPlant serverPlant) {
        Query query = em.createQuery("select o from ServerTerritory o where o.serverPlant = ?1 order by o.territoryCode asc ");
        query.setParameter(1, serverPlant);
        try{
            List<ServerTerritory> territorySet = query.getResultList();
            if (territorySet.size() > 0) {
                return territorySet;
            } else {
                System.out.println("无对应传感器！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
