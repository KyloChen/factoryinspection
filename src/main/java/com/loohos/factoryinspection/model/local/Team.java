package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Team {
    private String teamId;
    private int teamCode;
    private Territory territory;

    @Id
    @Column
    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    @Column
    public int getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(int teamCode) {
        this.teamCode = teamCode;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "territory_id")
    public Territory getTerritory() {
        return territory;
    }

    public void setTerritory(Territory territory) {
        this.territory = territory;
    }

}
