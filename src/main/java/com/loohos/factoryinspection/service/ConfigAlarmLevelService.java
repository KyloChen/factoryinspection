package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;

public interface ConfigAlarmLevelService extends DAO<ConfigAlarmLevel> {
    int getLevelByTemp(double topTemp);

    ConfigAlarmLevel getLevelByLevel(int alarmLevel);

    int getIsOrNotUnderRange(double value);

    ConfigAlarmLevel getLevelById(String alarmId);
}
