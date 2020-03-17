package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerPit;
import com.loohos.factoryinspection.model.server.ServerTeam;

import java.util.List;

public interface ServerPitService extends DAO<ServerPit> {
    ServerPit getServerPitByLocalId(String localPitId);

    List<ServerPit> getPitsByTeam(ServerTeam team);

    String getLocalIdByServerPitId(String serverPitId);

    int getPitCodeById(String serverPitId);
}
