package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Factory;
import org.springframework.stereotype.Repository;

@Repository
public interface FactoryService extends DAO<Factory> {
    Factory getFactoryById(String factoryId);
}
