package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.enumeration.CameraLocation;
import com.loohos.factoryinspection.model.local.Camera;
import com.loohos.factoryinspection.service.CameraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Transactional
@Service
public class CameraServiceImpl extends DaoSupport<Camera> implements CameraService {
    Logger logger = LoggerFactory.getLogger(CellarServiceImpl.class);

    @Override
    public List<Camera> getCamerasByPlantCode(int plantCode) {
        Query query = em.createQuery("select o from Camera o where o.plantCode = ?1 and o.cameraLocation = ?2");
        query.setParameter(1, plantCode);
        query.setParameter(2, CameraLocation.PLANT_CAMERA);
        try{
            List<Camera> cameras = query.getResultList();
            if (cameras.size() > 0) {
                return cameras;
            } else {
                logger.info("本车间无摄像头！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Camera getUrlExistedCamera(String cameraUrl) {
        Query query = em.createQuery("select o from Camera o where o.cameraUrl = ?1");
        query.setParameter(1, cameraUrl);
        try {
            return (Camera) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Camera getSerialExistedCamera(String cameraSerialNum) {
        Query query = em.createQuery("select o from Camera o where o.cameraSerialNum = ?1");
        query.setParameter(1, cameraSerialNum);
        try {
            return (Camera) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Camera getCamerasByPlantAndPit(int plantCode, int pitCode) {
        Query query = em.createQuery("select o from Camera o where o.plantCode = ?1 and o.pitCode = ?2");
        query.setParameter(1, plantCode);
        query.setParameter(2, pitCode);
        query.setMaxResults(1);
        try {
            return (Camera) query.getSingleResult();
        }catch (Exception e){
            return new Camera();
        }
    }
}
