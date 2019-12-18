package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.server.*;

public class LocalToServerSensorAdding extends BaseFormBean {
    private ServerPlant serverPlant;
    private ServerTerritory serverTerritory;
    private ServerTeam serverTeam;
    private ServerPit serverPit;
    private ServerRow serverRow;
    private ServerCellar serverCellar;
    private ServerSensor serverSensor;
    private ServerSensorNode topSensorNode;
    private ServerSensorNode midSensorNode;
    private ServerSensorNode botSensorNode;

    public ServerPlant getServerPlant() {
        return serverPlant;
    }

    public void setServerPlant(ServerPlant serverPlant) {
        this.serverPlant = serverPlant;
    }

    public ServerTerritory getServerTerritory() {
        return serverTerritory;
    }

    public void setServerTerritory(ServerTerritory serverTerritory) {
        this.serverTerritory = serverTerritory;
    }

    public ServerTeam getServerTeam() {
        return serverTeam;
    }

    public void setServerTeam(ServerTeam serverTeam) {
        this.serverTeam = serverTeam;
    }

    public ServerPit getServerPit() {
        return serverPit;
    }

    public void setServerPit(ServerPit serverPit) {
        this.serverPit = serverPit;
    }

    public ServerRow getServerRow() {
        return serverRow;
    }

    public void setServerRow(ServerRow serverRow) {
        this.serverRow = serverRow;
    }

    public ServerCellar getServerCellar() {
        return serverCellar;
    }

    public void setServerCellar(ServerCellar serverCellar) {
        this.serverCellar = serverCellar;
    }

    public ServerSensor getServerSensor() {
        return serverSensor;
    }

    public void setServerSensor(ServerSensor serverSensor) {
        this.serverSensor = serverSensor;
    }

    public ServerSensorNode getTopSensorNode() {
        return topSensorNode;
    }

    public void setTopSensorNode(ServerSensorNode topSensorNode) {
        this.topSensorNode = topSensorNode;
    }

    public ServerSensorNode getMidSensorNode() {
        return midSensorNode;
    }

    public void setMidSensorNode(ServerSensorNode midSensorNode) {
        this.midSensorNode = midSensorNode;
    }

    public ServerSensorNode getBotSensorNode() {
        return botSensorNode;
    }

    public void setBotSensorNode(ServerSensorNode botSensorNode) {
        this.botSensorNode = botSensorNode;
    }
}
