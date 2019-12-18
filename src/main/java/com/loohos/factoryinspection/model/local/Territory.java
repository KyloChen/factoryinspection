package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Territory {
    private String territoryId;
    private int territoryCode;
    private Plant plant;
    private Set<Team> teams;


    @Id
    @Column(nullable = false)
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
    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    @OneToMany(cascade = {CascadeType.REMOVE},fetch = FetchType.EAGER, mappedBy = "territory")
    public Set<Team> getTeams() {
        return teams;
    }

    public void setTeams(Set<Team> teams) {
        this.teams = teams;
    }
}
