package com.loohos.factoryinspection.service;

import com.loohos.factoryinspection.dao.DAO;
import com.loohos.factoryinspection.model.local.Pit;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.local.Team;

import java.util.List;

public interface PitService extends DAO<Pit> {
    List<Pit> getPitsByTeam(Team team);

    Pit getPitByTeamAndPitCode(Team team, int pitCode);
}
