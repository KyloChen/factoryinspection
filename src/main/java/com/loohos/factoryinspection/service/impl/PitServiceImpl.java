package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Pit;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.local.Team;
import com.loohos.factoryinspection.service.PitService;
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
public class PitServiceImpl extends DaoSupport<Pit> implements PitService {
    Logger logger = LoggerFactory.getLogger(PitServiceImpl.class);
    @Override
    public List<Pit> getPitsByTeam(Team team) {
//        Query query = em.createQuery("select o from Pit o where o.team = ?1 order by o.pitCode asc");
        Query query = em.createQuery("select o from Pit o where o.team = ?1");
        query.setParameter(1, team);
        try{
            List<Pit> pits = query.getResultList();
            if (pits.size() > 0) {
                return pits;
            } else {
                logger.info("本班组无垮记录！");
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Pit getPitByTeamAndPitCode(Team team, int pitCode) {
        Query query = em.createQuery("select o from Pit o where o.team = ?1 and o.pitCode = ?2");
        query.setParameter(1, team);
        query.setParameter(2, pitCode);
        try {
            return (Pit) query.getSingleResult();
        }catch (Exception e){
            return null;
        }

    }

}
