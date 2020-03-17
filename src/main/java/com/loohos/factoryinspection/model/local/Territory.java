package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Territory {
    private String territoryId;
    private int territoryCode;
    private Plant plant;


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

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "plant_id")
    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }
}
