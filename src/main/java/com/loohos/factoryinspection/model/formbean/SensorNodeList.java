package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.local.SensorNode;

public class SensorNodeList {
    private SensorNode topNode;
    private SensorNode midNode;
    private SensorNode botNode;

    public SensorNode getTopNode() {
        return topNode;
    }

    public void setTopNode(SensorNode topNode) {
        this.topNode = topNode;
    }

    public SensorNode getMidNode() {
        return midNode;
    }

    public void setMidNode(SensorNode midNode) {
        this.midNode = midNode;
    }

    public SensorNode getBotNode() {
        return botNode;
    }

    public void setBotNode(SensorNode botNode) {
        this.botNode = botNode;
    }
}
