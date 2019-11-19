package com.loohos.factoryinspection.service.impl;

import com.loohos.factoryinspection.dao.DaoSupport;
import com.loohos.factoryinspection.model.local.Factory;
import com.loohos.factoryinspection.model.local.Terminal;
import com.loohos.factoryinspection.service.TerminalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.util.List;

@SuppressWarnings("JpaQlInspection")
@Service
@Transactional
public class TerminalServiceImpl extends DaoSupport<Terminal> implements TerminalService  {

    @Override
    public List<Terminal> getTerminalsByFactory(Factory factoryId) {
        Query query = em.createQuery("select o from Terminal o where o.factoryId = ?1 and o.inUsing = true");
        query.setParameter(1, factoryId);
        try{
            List<Terminal> terminals = query.getResultList();
            if (terminals.size() > 0) {
                return terminals;
            } else {
                System.out.println("无终端！");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Terminal getIdByCode(String terminalCode) {
        Query query = em.createQuery("select o from Terminal o where o.terminalCode = ?1 and o.inUsing = true");
        query.setParameter(1, terminalCode);
        query.setMaxResults(1);
        try {
            return (Terminal) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Terminal getLastestTerminal() {
        Query query = em.createQuery("select o from Terminal o order by o.createdTime desc");
        query.setMaxResults(1);
        try {
            return (Terminal) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

    @Override
    public Terminal getTerminalById(String terminalId) {
        Query query = em.createQuery("select o from Terminal o where o.terminalId = ?1");
        query.setParameter(1, terminalId);
        query.setMaxResults(1);
        try {
            return (Terminal) query.getSingleResult();
        }catch (Exception e){
            return null;
        }
    }

}
