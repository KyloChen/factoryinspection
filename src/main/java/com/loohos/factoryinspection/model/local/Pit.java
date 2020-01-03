package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Pit {
    private String pitId;
    private int pitCode;
    private Team team;
    private String pitType = "true";

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

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    @Column
    public String getPitType() {
        return pitType;
    }

    public void setPitType(String pitType) {
        this.pitType = pitType;
    }
}
