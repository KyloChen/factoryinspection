package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerPit;
import com.loohos.factoryinspection.model.server.ServerTeam;
import com.loohos.factoryinspection.service.ServerPitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerPitServiceImpl extends DaoSupport<ServerPit> implements ServerPitService {
    private Logger logger = LoggerFactory.getLogger(ServerPitServiceImpl.class);

    @Override
    public ServerPit getServerPitByLocalId(String localPitId) {
        Query query = em.createQuery("select o from ServerPit o where o.localPitId = ?1");
        query.setParameter(1, localPitId);
        query.setMaxResults(1);
        try {
            return (ServerPit) query.getSingleResult();
        }catch (Exception e){
            logger.info("server error get pit by code");
            return null;
        }
    }

    @Override
    public List<ServerPit> getPitsByTeam(ServerTeam team) {
        Query query = em.createQuery("select o from ServerPit o where o.serverTeam = ?1 order by o.pitCode asc");
        query.setParameter(1, team);
        try{
            List<ServerPit> pits = query.getResultList();
            if (pits.size() > 0) {
                return pits;
            } else {
                logger.info("本班组无垮记录！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
