package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;

import java.util.Date;
import java.util.List;

public interface TerminalValueSensorService extends DAO<TerminalValueSensor> {
    double getLatestTopTempByTerminal(Terminal terminalId);

    double getLatestMidTempByTerminal(Terminal terminalId);

    double getLatestBotTempByTerminal(Terminal terminalId);

    List<TerminalValueSensor> getSensorsByTerminalAndDateASC(Terminal terminalId);

    List<TerminalValueSensor> getHistorySensorsByTerminal(Terminal terminalId);

    List<TerminalValueSensor> getHistoryByDateRange(Terminal terminalId, Date startDate, Date endDate);

    TerminalValueSensor getLatestSensorByTerminalId(Terminal terminalId);
}
