package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerCellar;
import com.loohos.factoryinspection.model.server.ServerRow;
import com.loohos.factoryinspection.service.ServerCellarService;
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
public class ServerCellarServiceImpl extends DaoSupport<ServerCellar> implements ServerCellarService {
    private Logger logger = LoggerFactory.getLogger(ServerRowServiceImpl.class);

    @Override
    public ServerCellar getServerCellarByLocalId(String localCellarId) {
        Query query = em.createQuery("select o from ServerCellar o where o.localCellarId = ?1");
        query.setParameter(1, localCellarId);
        query.setMaxResults(1);
        try {
            return (ServerCellar) query.getSingleResult();
        }catch (Exception e){
            logger.info("server error get row by code");
            return null;
        }

    }

    @Override
    public List<ServerCellar> getCellarByRow(ServerRow row) {
        Query query = em.createQuery("select o from ServerCellar o where o.serverRow = ?1 order by o.cellarCode asc");
        query.setParameter(1, row);
        try{
            List<ServerCellar> cellars = query.getResultList();
            if (cellars.size() > 0) {
                return cellars;
            } else {
                logger.info("本车间服务器无垮记录！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ServerCellar> getCellarByRowDesc(ServerRow row) {
        Query query = em.createQuery("select o from ServerCellar o where o.serverRow = ?1 order by o.cellarCode desc");
        query.setParameter(1, row);
        try{
            List<ServerCellar> cellars = query.getResultList();
            if (cellars.size() > 0) {
                return cellars;
            } else {
                logger.info("本server车间服务器无垮记录！");
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
