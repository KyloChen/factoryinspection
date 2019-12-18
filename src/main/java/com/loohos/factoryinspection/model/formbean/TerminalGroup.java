package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.local.Cellar;
import com.loohos.factoryinspection.model.local.Plant;
import com.loohos.factoryinspection.model.local.Terminal;

public class TerminalGroup extends BaseFormBean {
    private Terminal terminal;
    private double topTemp;
    private double midTemp;
    private double botTemp;
    private int topAlarmLevel;
    private int midAlarmLevel;
    private int botAlarmLevel;
    private Plant plant;
    private String batteryState;

    public Terminal getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal terminal) {
        this.terminal = terminal;
    }

    public double getTopTemp() {
        return topTemp;
    }

    public void setTopTemp(double topTemp) {
        this.topTemp = topTemp;
    }

    public double getMidTemp() {
        return midTemp;
    }

    public void setMidTemp(double midTemp) {
        this.midTemp = midTemp;
    }

    public double getBotTemp() {
        return botTemp;
    }

    public void setBotTemp(double botTemp) {
        this.botTemp = botTemp;
    }

    public int getTopAlarmLevel() {
        return topAlarmLevel;
    }

    public void setTopAlarmLevel(int topAlarmLevel) {
        this.topAlarmLevel = topAlarmLevel;
    }

    public int getMidAlarmLevel() {
        return midAlarmLevel;
    }

    public void setMidAlarmLevel(int midAlarmLevel) {
        this.midAlarmLevel = midAlarmLevel;
    }

    public int getBotAlarmLevel() {
        return botAlarmLevel;
    }

    public void setBotAlarmLevel(int botAlarmLevel) {
        this.botAlarmLevel = botAlarmLevel;
    }

    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }
}
