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
    public ConfigAlarmLevel getLevelByTemp(double temp) {
        Query query = em.createQuery("select o from ConfigAlarmLevel o where ?1 between o.minValue and o.maxValue");
        query.setMaxResults(1);
        query.setParameter(1, temp);
        try {
            return (ConfigAlarmLevel) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
