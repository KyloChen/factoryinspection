package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.local.Territory;
import com.loohos.factoryinspection.service.TerritoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;
import java.util.Set;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class TerritoryServiceImpl extends DaoSupport<Territory> implements TerritoryService{
    @Override
    public List<Territory> getTerritoriesByPlant(Plant plant1) {
        Query query = em.createQuery("select o from Territory o where o.plant = ?1 order by o.territoryCode asc ");
        query.setParameter(1, plant1);
        try{
            List<Territory> territorySet = query.getResultList();
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

    @Override
    public Territory getTerritoryByPlantAndTerritoryCode(Plant plant, int territoryCode) {
        Query query = em.createQuery("select o from Territory o where o.plant = ?1 and o.territoryCode = ?2");
        query.setParameter(1, plant);
        query.setParameter(2, territoryCode);
        query.setMaxResults(1);
        try {
            return (Territory) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
