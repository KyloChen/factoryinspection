package com.loohos.factoryinspection.model.formbean;

public class SensorPosInfo {
    private int plantCode;
    private int territoryCode;
    private int teamCode;
    private int pitCode;
    private int rowCode;
    private int cellarCode;
    private String sensorCode;
    private String sensorId;

    public int getPlantCode() {
        return plantCode;
    }

    public void setPlantCode(int plantCode) {
        this.plantCode = plantCode;
    }

    public int getTerritoryCode() {
        return territoryCode;
    }

    public void setTerritoryCode(int territoryCode) {
        this.territoryCode = territoryCode;
    }

    public int getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(int teamCode) {
        this.teamCode = teamCode;
    }

    public int getPitCode() {
        return pitCode;
    }

    public void setPitCode(int pitCode) {
        this.pitCode = pitCode;
    }

    public int getRowCode() {
        return rowCode;
    }

    public void setRowCode(int rowCode) {
        this.rowCode = rowCode;
    }

    public int getCellarCode() {
        return cellarCode;
    }

    public void setCellarCode(int cellarCode) {
        this.cellarCode = cellarCode;
    }

    public String getSensorCode() {
        return sensorCode;
    }

    public void setSensorCode(String sensorCode) {
        this.sensorCode = sensorCode;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
}
