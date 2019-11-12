package com.loohos.factoryinspection.model.formbean;

import com.loohos.factoryinspection.model.config.ConfigAlarmLevel;
import com.loohos.factoryinspection.model.formbean.BaseFormBean;
import com.loohos.factoryinspection.model.local.Terminal;

public class TerminalGroup extends BaseFormBean {
    private Terminal terminal;
    private double topTemp;
    private double midTemp;
    private double botTemp;
    private String batteryState;
    private ConfigAlarmLevel topAlarmLevel;
    private ConfigAlarmLevel midAlarmLevel;
    private ConfigAlarmLevel botAlarmLevel;

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

    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    public ConfigAlarmLevel getTopAlarmLevel() {
        return topAlarmLevel;
    }

    public void setTopAlarmLevel(ConfigAlarmLevel topAlarmLevel) {
        this.topAlarmLevel = topAlarmLevel;
    }

    public ConfigAlarmLevel getMidAlarmLevel() {
        return midAlarmLevel;
    }

    public void setMidAlarmLevel(ConfigAlarmLevel midAlarmLevel) {
        this.midAlarmLevel = midAlarmLevel;
    }

    public ConfigAlarmLevel getBotAlarmLevel() {
        return botAlarmLevel;
    }

    public void setBotAlarmLevel(ConfigAlarmLevel botAlarmLevel) {
        this.botAlarmLevel = botAlarmLevel;
    }
}
