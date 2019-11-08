package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.service.FactoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class FactoryServiceImpl extends DaoSupport<Factory> implements FactoryService {
    @Override
    public Factory getFactoryById(String factoryId) {
        Query query = em.createQuery("select o from Factory o where o.factoryId = ?1");
        query.setParameter(1, factoryId);
        query.setMaxResults(1);
        try {
            return (Factory)query.getSingleResult();
        }catch (Exception e){
            return new Factory();
        }
    }
}
