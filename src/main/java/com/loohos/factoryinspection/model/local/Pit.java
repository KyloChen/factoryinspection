package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Pit {
    private String pitId;
    private int pitCode;
//    private Plant plant;
    private Team team;
    private Set<Row> rows;

    @Id
    @Column(nullable = false)
    public String getPitId() {
        return pitId;
    }

    public void setPitId(String pitId) {
        this.pitId = pitId;
    }

    @Column
    public int getPitCode() {
        return pitCode;
    }

    public void setPitCode(int pitCode) {
        this.pitCode = pitCode;
    }

    @OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy = "pit")
    public Set<Row> getRows() {
        return rows;
    }

    public void setRows(Set<Row> rows) {
        this.rows = rows;
    }

//    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
//    @JoinColumn(name = "plant_id")
//    public Plant getPlant() {
//        return plant;
//    }
//
//    public void setPlant(Plant plant) {
//        this.plant = plant;
//    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }
}
