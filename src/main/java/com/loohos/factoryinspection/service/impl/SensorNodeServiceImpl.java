package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.enumeration.SensorType;
import com.loohos.factoryinspection.model.local.Sensor;
import com.loohos.factoryinspection.model.local.SensorNode;
import com.loohos.factoryinspection.service.SensorNodeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.loohos.factoryinspection.enumeration.SensorType.BOT_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.MID_TEMP_SENSOR;
import static com.loohos.factoryinspection.enumeration.SensorType.TOP_TEMP_SENSOR;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class SensorNodeServiceImpl extends DaoSupport<SensorNode> implements SensorNodeService {
    @Override
    public SensorNode getNodeBySensorWithTypeAndCurTime(Sensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o from SensorNode o where o.sensor = ?1 and o.sensorType = ?2 order by o.createdTime desc");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        query.setMaxResults(1);
        try {
            return (SensorNode) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public List<Double> getNodeBySensorWithTypeAndCurDay(Sensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o.sensorValue from SensorNode o where o.sensor = ?1 and o.sensorType = ?2 and date(createdTime) = curdate() order by o.createdTime asc");
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
    public List<Date> getTimeBySensorAndCurDay(Sensor sensor) {
        Query query = em.createQuery("select o.createdTime from SensorNode o " +
                "where o.sensor = ?1 " +
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
    public List<Double> getHistoryNodeBySensorWithTypeAndCurDay(Sensor sensor, SensorType sensorType) {
        Query query = em.createQuery("select o.sensorValue from SensorNode o " +
                "where o.sensor = ?1 " +
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
    public List<Date> getHistoryTimeBySensorAndCurDay(Sensor sensor) {
        Query query = em.createQuery("select o.createdTime from SensorNode o " +
                "where o.sensor = ?1 " +
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
    public List<Double> getValuesBySensorAndRange(Sensor sensor, SensorType sensorType, Date startDate, Date endDate) {
        Query query = em.createQuery("select o.sensorValue from SensorNode o where " +
                "o.sensor = ?1 and " +
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
    public List<Date> getDatesBySensorAndRange(Sensor sensor, Date startDate, Date endDate) {
        Query query = em.createQuery("select o.createdTime from SensorNode o where " +
                "o.sensor = ?1 and " +
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

    @Override
    public List<Double> getLastSixNodeBySensorWithTypeAndCurDay(Sensor sensor, SensorType sensorType, int hours) {
        Date curDate = new Date();
        long curDateS = curDate.getTime();
        long sixHoursAgoDateS = (curDate.getTime() - hours*60*60*1000);
        Date sixHoursAgoDate = new Date(sixHoursAgoDateS);
        Query query = em.createQuery("select o.sensorValue from SensorNode o where " +
                "o.sensor = ?1 and " +
                "o.sensorType = ?2 and " +
                "o.createdTime between ?3 and ?4 " +
                "order by o.createdTime asc");
        query.setParameter(1, sensor);
        query.setParameter(2, sensorType);
        query.setParameter(3, sixHoursAgoDate);
        query.setParameter(4, curDate);
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
    public List<Date> getLastSixTimeBySensorAndCurDay(Sensor sensor, int hours) {
        Date curDate = new Date();
        long curDateS = curDate.getTime();
        long sixHoursAgoDateS = (curDate.getTime() - hours*60*60*1000);
        Date sixHoursAgoDate = new Date(sixHoursAgoDateS);
        Query query = em.createQuery("select o.createdTime from SensorNode o where " +
                "o.sensor = ?1 and " +
                "o.sensorType = 'TOP_TEMP_SENSOR' and " +
                "o.createdTime between ?2 and ?3 " +
                "order by o.createdTime asc");
        query.setParameter(1, sensor);
        query.setParameter(2, sixHoursAgoDate);
        query.setParameter(3, curDate);
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
