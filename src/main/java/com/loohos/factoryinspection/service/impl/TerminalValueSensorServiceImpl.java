package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.model.local.TerminalValueSensor;
import com.loohos.factoryinspection.service.TerminalValueSensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@SuppressWarnings("JpaQlInspection")

@Service
@Transactional
public class TerminalValueSensorServiceImpl extends DaoSupport<TerminalValueSensor> implements TerminalValueSensorService {
    @Override
    public double getLatestTopTempByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o.topTemp from TerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
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
        Query query = em.createQuery("select o.midTemp from TerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
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
        Query query = em.createQuery("select o.botTemp from TerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc ");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (double) query.getSingleResult();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public List<TerminalValueSensor> getSensorsByTerminalAndDateASC(Terminal terminalId) {
        Query query = em.createQuery("select o from TerminalValueSensor o where o.terminalId = ?1 and date(createdTime) = curdate() order by o.createdTime asc");
        query.setParameter(1, terminalId);
        try{
            List<TerminalValueSensor> sensorList = query.getResultList();
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
    public List<TerminalValueSensor> getHistorySensorsByTerminal(Terminal terminalId) {
        Query query = em.createQuery("select o from TerminalValueSensor o where o.terminalId = ?1 order by o.createdTime asc ");
        query.setParameter(1, terminalId);
        try{
            List<TerminalValueSensor> sensorList = query.getResultList();
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
    public List<TerminalValueSensor> getHistoryByDateRange(Terminal terminalId, Date startDate, Date endDate) {
        Query query = em.createQuery("select o from TerminalValueSensor o where o.terminalId = ?1 and o.createdTime between ?2 and ?3");
        query.setParameter(1, terminalId);
        query.setParameter(2, startDate);
        query.setParameter(3, endDate);
        try{
            List<TerminalValueSensor> sensorList = query.getResultList();
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
    public TerminalValueSensor getLatestSensorByTerminalId(Terminal terminalId) {
        Query query = em.createQuery("select o from TerminalValueSensor o where o.terminalId = ?1 order by o.createdTime desc");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (TerminalValueSensor) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
