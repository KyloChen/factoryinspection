package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerCellar;
import com.loohos.factoryinspection.model.server.ServerSensor;
import com.loohos.factoryinspection.service.ServerSensorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerSensorServiceImpl extends DaoSupport<ServerSensor> implements ServerSensorService {
    private Logger logger = LoggerFactory.getLogger(ServerSensorServiceImpl.class);
    @Override
    public ServerSensor getServerSensorByLocalId(String localSensorId) {
        Query query = em.createQuery("select o from ServerSensor o where o.localSensorId = ?1");
        query.setParameter(1, localSensorId);
        query.setMaxResults(1);
        try {
            return (ServerSensor) query.getSingleResult();
        }catch (Exception e){
            logger.info("error get plant by code");
            return null;
        }

    }

    @Override
    public ServerSensor getSensorByCellar(ServerCellar cellar) {
        Query query = em.createQuery("select o from ServerSensor o where o.serverCellar = ?1");
        query.setParameter(1, cellar);
        query.setMaxResults(1);
        try {
            return (ServerSensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
