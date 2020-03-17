package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.server.ServerPlant;

public class ServerPlantPit extends BaseFormBean{
    private String serverPlantId;
    private String serverPitId;

    public String getServerPlantId() {
        return serverPlantId;
    }

    public void setServerPlantId(String serverPlantId) {
        this.serverPlantId = serverPlantId;
    }

    public String getServerPitId() {
        return serverPitId;
    }

    public void setServerPitId(String serverPitId) {
        this.serverPitId = serverPitId;
    }
}
