package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.server.ServerTerminalValueSensor;

import java.util.Date;
import java.util.List;

public interface ServerTerminalValueSensorService extends DAO<ServerTerminalValueSensor> {
    double getLatestTopTempByTerminal(Terminal terminalId);

    double getLatestMidTempByTerminal(Terminal terminalId);

    double getLatestBotTempByTerminal(Terminal terminalId);

    List<ServerTerminalValueSensor> getSensorsByTerminalAndDateASC(Terminal terminalId);

    List<ServerTerminalValueSensor> getHistorySensorsByTerminal(Terminal terminalId);

    List<ServerTerminalValueSensor> getHistoryByDateRange(Terminal terminalId, Date startDate, Date endDate);

    ServerTerminalValueSensor getLatestSensorByTerminalId(Terminal terminalId);

}
