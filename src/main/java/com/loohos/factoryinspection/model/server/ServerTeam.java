package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerTeam {
    private String teamId;
    private int teamCode;
    private ServerTerritory serverTerritory;
    private String localTeamId;

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
    public ServerTerritory getServerTerritory() {
        return serverTerritory;
    }

    public void setServerTerritory(ServerTerritory serverTerritory) {
        this.serverTerritory = serverTerritory;
    }

    @Column
    public String getLocalTeamId() {
        return localTeamId;
    }

    public void setLocalTeamId(String localTeamId) {
        this.localTeamId = localTeamId;
    }
}
