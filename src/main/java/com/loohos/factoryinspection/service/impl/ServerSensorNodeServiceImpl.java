package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.model.server.ServerSensor;
import com.loohos.factoryinspection.model.server.ServerSensorNode;
import com.loohos.factoryinspection.service.ServerSensorNodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerSensorNodeServiceImpl extends DaoSupport<ServerSensorNode> implements ServerSensorNodeService{
    @Override
    public ServerSensorNode getNodeBySensorWithTypeAndCurTime(ServerSensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o from ServerSensorNode o where o.serverSensor = ?1 and o.sensorType = ?2 order by o.createdTime desc");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        query.setMaxResults(1);
        try {
            return (ServerSensorNode) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public List<Double> getNodeBySensorWithTypeAndCurDay(ServerSensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o.sensorValue from ServerSensorNode o where o.serverSensor = ?1 and o.sensorType = ?2 and date(createdTime) = curdate() order by o.createdTime asc");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        try {
            List<Double> curDayTemps = query.getResultList();
            if(curDayTemps.size() > 0 )
            {
                return curDayTemps;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public List<Date> getTimeBySensorAndCurDay(ServerSensor sensor) {
        Query query = em.createQuery("select o.createdTime from ServerSensorNode o " +
                "where o.serverSensor = ?1 " +
                "and o.sensorType = 'TOP_TEMP_SENSOR' " +
                "and date(createdTime) = curdate() " +
                "order by o.createdTime asc");
        query.setParameter(1, sensor);
        try {
            List<Date> curDate = query.getResultList();
            if(curDate.size() > 0 )
            {
                return curDate;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Double> getHistoryNodeBySensorWithTypeAndCurDay(ServerSensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o.sensorValue from ServerSensorNode o " +
                "where o.serverSensor = ?1 " +
                "and o.sensorType = ?2 " +
                "order by o.createdTime asc");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        try {
            List<Double> hisDayTemps = query.getResultList();
            if(hisDayTemps.size() > 0 )
            {
                return hisDayTemps;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Date> getHistoryTimeBySensorAndCurDay(ServerSensor sensor) {
        Query query = em.createQuery("select o.createdTime from ServerSensorNode o " +
                "where o.serverSensor = ?1 " +
                "and o.sensorType = 'TOP_TEMP_SENSOR' " +
                "order by o.createdTime asc");
        query.setParameter(1, sensor);
        try {
            List<Date> hisDate = query.getResultList();
            if(hisDate.size() > 0 )
            {
                return hisDate;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Double> getValuesBySensorAndRange(ServerSensor sensor, SensorType sensorType, Date startDate, Date endDate) {
        Query query = em.createQuery("select o.sensorValue from ServerSensorNode o where " +
                "o.serverSensor = ?1 and " +
                "o.sensorType = ?2 and " +
                "o.createdTime between ?3 and ?4");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        query.setParameter(3, startDate);
        query.setParameter(4, endDate);
        try{
            List<Double> nodeList = query.getResultList();
            if (nodeList.size() > 0) {
                return nodeList;
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
    public List<Date> getDatesBySensorAndRange(ServerSensor sensor, Date startDate, Date endDate) {
        Query query = em.createQuery("select o.createdTime from ServerSensorNode o where " +
                "o.serverSensor = ?1 and " +
                "o.sensorType = 'TOP_TEMP_SENSOR' and " +
                "o.createdTime between ?2 and ?3");
        query.setParameter(1, sensor);
        query.setParameter(2, startDate);
        query.setParameter(3, endDate);
        try{
            List<Date> nodeList = query.getResultList();
            if (nodeList.size() > 0) {
                return nodeList;
            } else {
                System.out.println("无历史数据！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
