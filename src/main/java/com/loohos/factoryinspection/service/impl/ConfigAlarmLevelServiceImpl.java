package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.service.ConfigAlarmLevelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ConfigAlarmLevelServiceImpl extends DaoSupport<ConfigAlarmLevel> implements ConfigAlarmLevelService {
    @Override
    public int getLevelByTemp(double temp) {
        Query query = em.createQuery("select o.alarmLevel from ConfigAlarmLevel o where ?1 between o.minValue and o.maxValue");
        query.setMaxResults(1);
        query.setParameter(1, temp);
        try {
            return (int) query.getSingleResult();
        }catch (Exception e){
            return 3;
        }
    }

    @Override
    public ConfigAlarmLevel getLevelByLevel(int alarmlevel) {
        Query query = em.createQuery("select o from ConfigAlarmLevel o where o.alarmLevel = ?1");
        query.setParameter(1, alarmlevel);
        query.setMaxResults(1);
        try {
            return (ConfigAlarmLevel) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int getIsOrNotUnderRange(double value) {
        Query query = em.createQuery("select o.alarmLevel from ConfigAlarmLevel o where ?1 between o.minValue and o.maxValue");
        query.setMaxResults(1);
        query.setParameter(1, value);
        try {
            return (int)query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public ConfigAlarmLevel getLevelById(String alarmId) {
        Query query = em.createQuery("select o.alarmLevel from ConfigAlarmLevel o where o.alarmId = ?1");
        query.setMaxResults(1);
        query.setParameter(1, alarmId);
        try {
            return (ConfigAlarmLevel) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
