package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.server.ServerTerminalValueSensor;
import com.loohos.factoryinspection.service.ServerTerminalValueSensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerTerminalValueSensorServiceImpl extends DaoSupport<ServerTerminalValueSensor> implements ServerTerminalValueSensorService {
    @Override
    public double getLatestTopTempByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.topTemp from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (double) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public double getLatestMidTempByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.midTemp from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (double) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public double getLatestBotTempByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.botTemp from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (double) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public List<ServerTerminalValueSensor> getSensorsByTerminalAndDateASC(Terminal terminalId) {
        Query query = em.createQuery("select o from ServerTerminalValueSensor o where o.terminalId = ?1 and date(createdTime) = curdate() order by o.createdTime asc");
        query.setParameter(1, terminalId);
        try{
            List<ServerTerminalValueSensor> sensorList = query.getResultList();
            if (sensorList.size() > 0) {
                return sensorList;
            } else {
                System.out.println("无对应传感器！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ServerTerminalValueSensor> getHistorySensorsByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime asc ");
        query.setParameter(1, terminalId);
        try{
            List<ServerTerminalValueSensor> sensorList = query.getResultList();
            if (sensorList.size() > 0) {
                return sensorList;
            } else {
                System.out.println("无历史记录！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<ServerTerminalValueSensor> getHistoryByDateRange(Terminal terminalId, Date startDate, Date endDate) {
        Query query = em.createQuery("select o from ServerTerminalValueSensor o where o.terminalId = ?1 and o.createdTime between ?2 and ?3");
        query.setParameter(1, terminalId);
        query.setParameter(2, startDate);
        query.setParameter(3, endDate);
        try{
            List<ServerTerminalValueSensor> sensorList = query.getResultList();
            if (sensorList.size() > 0) {
                return sensorList;
            } else {
                System.out.println("无历史数据！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ServerTerminalValueSensor getLatestSensorByTerminalId(Terminal terminalId) {
        Query query = em.createQuery("select o from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (ServerTerminalValueSensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public String getBatteryStateByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.batteryState from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (String) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public int getLatestTopAlarmLevelByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.topAlarmLevel from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (int) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public int getLatestMidAlarmLevelByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.midAlarmLevel from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (int) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public int getLatestBotAlarmLevelByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.botAlarmLevel from ServerTerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (int) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

}
