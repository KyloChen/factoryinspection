package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.model.local.Terminal;

import java.util.List;

public interface TerminalService extends DAO<Terminal> {
    List<Terminal> getTerminalsByFactory(Factory factoryId);

    Terminal getIdByCode(String terminalCode);

    Terminal getLastestTerminal();
}
