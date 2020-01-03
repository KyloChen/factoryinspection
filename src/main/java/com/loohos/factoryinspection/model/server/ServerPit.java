package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerPit {
    private String pitId;
    private int pitCode;
    private ServerTeam serverTeam;
    private String localPitId;
    private String pitType = "true";

    @Id
    @Column
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
    public ServerTeam getServerTeam() {
        return serverTeam;
    }

    public void setServerTeam(ServerTeam serverTeam) {
        this.serverTeam = serverTeam;
    }

    @Column
    public String getLocalPitId() {
        return localPitId;
    }

    public void setLocalPitId(String localPitId) {
        this.localPitId = localPitId;
    }

    @Column
    public String getPitType() {
        return pitType;
    }

    public void setPitType(String pitType) {
        this.pitType = pitType;
    }
}
