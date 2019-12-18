package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.server.ServerPit;
import com.loohos.factoryinspection.model.server.ServerRow;

import java.util.List;

public interface ServerRowService extends DAO<ServerRow>{
    ServerRow getServerRowByLocalId(String localRowId);

    List<ServerRow> getRowByPit(ServerPit pit);
}
