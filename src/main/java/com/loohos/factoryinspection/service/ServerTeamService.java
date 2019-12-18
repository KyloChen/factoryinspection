package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerTeam;
import com.loohos.factoryinspection.model.server.ServerTerritory;

import java.util.List;

public interface ServerTeamService extends DAO<ServerTeam> {
    ServerTeam getServerTeamByLocalId(String localTeamId);

    List<ServerTeam> getTeamsByTerritory(ServerTerritory territory);
}
