package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Team;
import com.loohos.factoryinspection.model.local.Territory;

import java.util.List;

public interface TeamService extends DAO<Team> {
    List<Team> getTeamsByTerritory(Territory territory2);

    Team getTeamsByTerritoryAndTeamCode(Territory territory, int teamCode);
}
