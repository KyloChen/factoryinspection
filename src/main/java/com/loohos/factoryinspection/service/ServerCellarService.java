package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerCellar;
import com.loohos.factoryinspection.model.server.ServerRow;

import java.util.List;

public interface ServerCellarService extends DAO<ServerCellar> {
    ServerCellar getServerCellarByLocalId(String localCellarId);

    List<ServerCellar> getCellarByRow(ServerRow row);
}
