package com.loohos.factoryinspection.model.server;


import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerTerritory {
    private String territoryId;
    private int territoryCode;
    private ServerPlant serverPlant;
    private Set<ServerTeam> serverTeams;
    private String localTerritoryId;

    @Id
    @Column
    public String getTerritoryId() {
        return territoryId;
    }

    public void setTerritoryId(String territoryId) {
        this.territoryId = territoryId;
    }

    @Column
    public int getTerritoryCode() {
        return territoryCode;
    }

    public void setTerritoryCode(int territoryCode) {
        this.territoryCode = territoryCode;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "plant_id")
    public ServerPlant getServerPlant() {
        return serverPlant;
    }

    public void setServerPlant(ServerPlant serverPlant) {
        this.serverPlant = serverPlant;
    }

    @OneToMany(cascade = {CascadeType.REMOVE},fetch = FetchType.EAGER, mappedBy = "serverTerritory")
    public Set<ServerTeam> getServerTeams() {
        return serverTeams;
    }

    public void setServerTeams(Set<ServerTeam> serverTeams) {
        this.serverTeams = serverTeams;
    }

    @Column
    public String getLocalTerritoryId() {
        return localTerritoryId;
    }

    public void setLocalTerritoryId(String localTerritoryId) {
        this.localTerritoryId = localTerritoryId;
    }
}
