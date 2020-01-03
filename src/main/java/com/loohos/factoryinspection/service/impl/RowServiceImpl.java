package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Pit;
import com.loohos.factoryinspection.model.local.Row;
import com.loohos.factoryinspection.service.RowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class RowServiceImpl extends DaoSupport<Row> implements RowService {
    Logger logger = LoggerFactory.getLogger(RowServiceImpl.class);
    @Override
    public List<Row> getRowByPit(Pit pit) {
//        Query query = em.createQuery("select o from Row o where o.pit = ?1 order by o.rowCode asc");
        Query query = em.createQuery("select o from Row o where o.pit = ?1");
        query.setParameter(1, pit);
        try{
            List<Row> rows = query.getResultList();
            if (rows.size() > 0) {
                return rows;
            } else {
                logger.info("本");
                return new ArrayList<>();
            }
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public Row getRowByPitAndRowCode(Pit pit, int rowCode) {
        Query query = em.createQuery("select o from Row o where o.pit = ?1 and o.rowCode = ?2");
        query.setParameter(1, pit);
        query.setParameter(2, rowCode);
        try {
            return (Row) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }
}
