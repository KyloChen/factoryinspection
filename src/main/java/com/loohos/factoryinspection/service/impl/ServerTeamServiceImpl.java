package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerTeam;
import com.loohos.factoryinspection.model.server.ServerTerritory;
import com.loohos.factoryinspection.service.ServerTeamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerTeamServiceImpl extends DaoSupport<ServerTeam> implements ServerTeamService {
    private Logger logger = LoggerFactory.getLogger(ServerTeamServiceImpl.class);
    @Override
    public ServerTeam getServerTeamByLocalId(String localTeamId) {
        Query query = em.createQuery("select o from ServerTeam o where o.localTeamId = ?1");
        query.setParameter(1, localTeamId);
        query.setMaxResults(1);
        try {
            return (ServerTeam) query.getSingleResult();
        }catch (Exception e){
            logger.info("server error get team by code");
            return null;
        }
    }

    @Override
    public List<ServerTeam> getTeamsByTerritory(ServerTerritory territory) {

        Query query = em.createQuery("select o from ServerTeam o where o.serverTerritory = ?1 order by o.teamCode asc");
        query.setParameter(1, territory);
        try{
            List<ServerTeam> teams = query.getResultList();
            if (teams.size() > 0) {
                return teams;
            } else {
                System.out.println("无对应班组！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
