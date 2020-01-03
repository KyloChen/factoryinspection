package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Row;
import com.loohos.factoryinspection.service.CellarService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class CellarServiceImpl extends DaoSupport<Cellar> implements CellarService {
    Logger logger = LoggerFactory.getLogger(CellarServiceImpl.class);
    @Override
    public List<Cellar> getCellarByRow(Row row) {
        Query query = em.createQuery("select o from Cellar o where o.row = ?1 order by o.cellarCode asc");
        query.setParameter(1, row);
        try{
            List<Cellar> cellars = query.getResultList();
            if (cellars.size() > 0) {
                return cellars;
            } else {
                logger.info("本车间无垮记录！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Cellar getCellarByRowAndCellarCode(Row row, int cellarCode) {
        Query query = em.createQuery("select o from Cellar o where o.row = ?1 and o.cellarCode = ?2");
        query.setParameter(1, row);
        query.setParameter(2, cellarCode);
        try {
            return (Cellar) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public List<Cellar> getCellarByRowDesc(Row row) {
//        Query query = em.createQuery("select o from Cellar o where o.row = ?1 order by o.cellarCode desc");
        Query query = em.createQuery("select o from Cellar o where o.row = ?1 and o.sensor.sensorWorkingType <> ?2");
        query.setParameter(1, row);
        query.setParameter(2, SensorWorkingType.SENSOR_WAS_DELETED);
        try{
            List<Cellar> cellars = query.getResultList();
            if (cellars.size() > 0) {
                return cellars;
            } else {
                logger.info("1本车间无垮记录！");
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
