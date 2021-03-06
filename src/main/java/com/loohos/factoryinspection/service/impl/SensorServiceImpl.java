package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.enumeration.SensorWorkingType;
import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Sensor;
import com.loohos.factoryinspection.service.SensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class SensorServiceImpl extends DaoSupport<Sensor> implements SensorService {


    @Override
    public List<Sensor> getSensorByCellar(Cellar cellar) {
        Query query = em.createQuery("select o from Sensor o where o.cellar = ?1");
        query.setParameter(1, cellar);
        try{
            List<Sensor> sensors = query.getResultList();
            if (sensors.size() > 0) {
                return sensors;
            } else {
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Sensor getDeleteSensorByCellar(Cellar cellar, SensorWorkingType sensorWorkingType) {
        Query query = em.createQuery("select o from Sensor o where o.cellar = ?1 and o.sensorWorkingType = ?2");
        query.setParameter(1, cellar);
        query.setParameter(2, sensorWorkingType);
        query.setMaxResults(1);
        try {
            return (Sensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Sensor getBoolByIsWorkingAndSensorCode(String sensorCode, SensorWorkingType sensorWorkingType) {
        Query query = em.createQuery("select o from Sensor o where o.sensorCode = ?1 and o.sensorWorkingType = ?2");
        query.setParameter(1, sensorCode);
        query.setParameter(2, sensorWorkingType);
        query.setMaxResults(1);
        try {
            return (Sensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public List<Sensor> getWorkingSensor() {
        Query query = em.createQuery("select o from Sensor o where o.sensorWorkingType = ?1");
        query.setParameter(1, SensorWorkingType.SENSOR_IS_WORKING);
        try{
            List<Sensor> sensors = query.getResultList();
            if (sensors.size() > 0) {
                return sensors;
            } else {
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Sensor getWorkingSensorByCellar(Cellar cellar) {
        Query query = em.createQuery("select o from Sensor o where o.sensorWorkingType = ?1 and o.cellar = ?2");
        query.setParameter(1, SensorWorkingType.SENSOR_IS_WORKING);
        query.setParameter(2, cellar);
        query.setMaxResults(1);
        try {
            return (Sensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
