package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Terminal {
    private String terminalId;
    private String terminalCode;
    private boolean inUsing = true;
    private Factory factoryId;
    private String plantCode;
    private String territoryCode;
    private String teamCode;
    private String pitCode;
    private String rowCode;
    private String cellarCode;
    private String frenquency;
    private String power;
    private String activationCycle;
    private String workingHour;
    private String batteryState;
    private Date createdTime;
    private String createdBy;

    //@GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column(length = 40, nullable = false)
    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    @Column(length = 40, nullable = false)
    public String getTerminalCode() {
        return terminalCode;
    }

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = terminalCode;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH, CascadeType.DETACH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "factory_id")
    public Factory getFactoryId() {
        return factoryId;
    }

    public void setFactoryId(Factory factoryId) {
        this.factoryId = factoryId;
    }

    @Column(nullable = false)
    public String getPlantCode() { return plantCode; }

    public void setPlantCode(String plantCode) { this.plantCode = plantCode; }

    @Column(nullable = false)
    public String getTerritoryCode() { return territoryCode; }

    public void setTerritoryCode(String territoryCode) { this.territoryCode = territoryCode; }

    @Column(nullable = false)
    public String getTeamCode() { return teamCode; }

    public void setTeamCode(String teamCode) { this.teamCode = teamCode; }

    @Column(nullable = false)
    public String getPitCode() { return pitCode; }

    public void setPitCode(String pitCode) { this.pitCode = pitCode; }

    @Column(nullable = false)
    public String getRowCode() { return rowCode; }

    public void setRowCode(String rowCode) { this.rowCode = rowCode; }

    @Column(nullable = false)
    public String getCellarCode() { return cellarCode; }

    public void setCellarCode(String cellarCode) { this.cellarCode = cellarCode; }

    @Column(nullable = true)
    public String getFrenquency() { return frenquency; }

    public void setFrenquency(String frenquency) { this.frenquency = frenquency; }

    @Column(nullable = true)
    public String getPower() { return power; }

    public void setPower(String power) { this.power = power; }

    @Column(nullable = true)
    public String getActivationCycle() { return activationCycle; }

    public void setActivationCycle(String activationCycle) { this.activationCycle = activationCycle; }

    @Column(nullable = true)
    public String getWorkingHour() { return workingHour; }

    public void setWorkingHour(String workingHour) { this.workingHour = workingHour; }

    @Column(nullable = true)
    public String getBatteryState() { return batteryState; }

    public void setBatteryState(String batteryState) { this.batteryState = batteryState; }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreatedTime() { return createdTime; }

    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    @Column(nullable = true)
    public String getCreatedBy() { return createdBy; }

    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @Column(nullable = false)
    public boolean getInUsing() {
        return inUsing;
    }

    public void setInUsing(boolean inUsing) {
        this.inUsing = inUsing;
    }
}
