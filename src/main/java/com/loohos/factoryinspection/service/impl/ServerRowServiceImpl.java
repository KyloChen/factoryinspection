package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.server.ServerPit;
import com.loohos.factoryinspection.model.server.ServerRow;
import com.loohos.factoryinspection.service.ServerRowService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class ServerRowServiceImpl extends DaoSupport<ServerRow> implements ServerRowService {
    private Logger logger = LoggerFactory.getLogger(ServerRowServiceImpl.class);

    @Override
    public ServerRow getServerRowByLocalId(String localRowId) {
        Query query = em.createQuery("select o from ServerRow o where o.localRowId = ?1");
        query.setParameter(1, localRowId);
        query.setMaxResults(1);
        try {
            return (ServerRow) query.getSingleResult();
        }catch (Exception e){
            logger.info("server error get row by code");
            return null;
        }

    }

    @Override
    public List<ServerRow> getRowByPit(ServerPit pit) {
//        Query query = em.createQuery("select o from ServerRow o where o.serverPit = ?1 order by o.rowCode asc");
        Query query = em.createQuery("select o from ServerRow o where o.serverPit = ?1");
        query.setParameter(1, pit);
        try{
            List<ServerRow> rows = query.getResultList();
            if (rows.size() > 0) {
                return rows;
            } else {
                logger.info("服务器无排信息.");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
