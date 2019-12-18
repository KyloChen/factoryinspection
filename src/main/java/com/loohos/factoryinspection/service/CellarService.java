package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Row;

import java.util.List;

public interface CellarService extends DAO<Cellar> {
    List<Cellar> getCellarByRow(Row row);

    Cellar getCellarByRowAndCellarCode(Row row, int cellarCode);

    List<Cellar> getCellarByRowDesc(Row row);
}
