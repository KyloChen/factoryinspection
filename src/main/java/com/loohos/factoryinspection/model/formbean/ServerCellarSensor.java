package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.server.ServerCellar;
import com.loohos.factoryinspection.model.server.ServerSensor;

public class ServerCellarSensor {
    private ServerCellar serverCellar;
    private ServerSensor serverSensor;

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
}
