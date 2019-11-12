package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;

public interface ConfigAlarmLevelService extends DAO<ConfigAlarmLevel> {
    ConfigAlarmLevel getLevelByTemp(double topTemp);

    ConfigAlarmLevel getLevelByLevel(int alarmLevel);
}
