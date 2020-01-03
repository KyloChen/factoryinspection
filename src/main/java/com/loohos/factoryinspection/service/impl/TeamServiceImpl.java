package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Team;
import com.loohos.factoryinspection.model.local.Territory;
import com.loohos.factoryinspection.service.TeamService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class TeamServiceImpl extends DaoSupport<Team> implements TeamService  {
    @Override
    public List<Team> getTeamsByTerritory(Territory territory2) {
//        Query query = em.createQuery("select o from Team o where o.territory = ?1 order by o.teamCode asc ");
        Query query = em.createQuery("select o from Team o where o.territory = ?1");
        query.setParameter(1, territory2);
        try{
            List<Team> teams = query.getResultList();
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

    @Override
    public Team getTeamsByTerritoryAndTeamCode(Territory territory, int teamCode) {
        Query query = em.createQuery("select o from Team o where o.territory = ?1 and o.teamCode = ?2");
        query.setParameter(1, territory);
        query.setParameter(2, teamCode);
        query.setMaxResults(1);
        try {
            return (Team) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
