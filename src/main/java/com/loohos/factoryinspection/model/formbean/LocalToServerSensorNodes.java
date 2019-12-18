package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.local.SensorNode;
import com.loohos.factoryinspection.model.server.ServerSensor;
import com.loohos.factoryinspection.model.server.ServerSensorNode;

import java.util.Date;

public class LocalToServerSensorNodes extends BaseFormBean {
    private String localSensorId;
    private ServerSensorNode topNode;
    private ServerSensorNode midNode;
    private ServerSensorNode botNode;

    public String getLocalSensorId() {
        return localSensorId;
    }

    public void setLocalSensorId(String localSensorId) {
        this.localSensorId = localSensorId;
    }

    public ServerSensorNode getTopNode() {
        return topNode;
    }

    public void setTopNode(ServerSensorNode topNode) {
        this.topNode = topNode;
    }

    public ServerSensorNode getMidNode() {
        return midNode;
    }

    public void setMidNode(ServerSensorNode midNode) {
        this.midNode = midNode;
    }

    public ServerSensorNode getBotNode() {
        return botNode;
    }

    public void setBotNode(ServerSensorNode botNode) {
        this.botNode = botNode;
    }

}
