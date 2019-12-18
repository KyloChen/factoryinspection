package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Pit;
import com.loohos.factoryinspection.model.local.Row;

import java.util.List;

public interface RowService extends DAO<Row> {
    List<Row> getRowByPit(Pit p);

    Row getRowByPitAndRowCode(Pit pit, int rowCode);
}
