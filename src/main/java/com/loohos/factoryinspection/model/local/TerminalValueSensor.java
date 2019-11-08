package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Date;

@Entity
public class TerminalValueSensor {

    private String sensorId;
    private double topTemp;
    private double midTemp;
    private double botTemp;
    private String batteryState;
    private Terminal terminalId;
    private Date createdTime;

    @Id
    @Column(length = 40, nullable = false)
    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }
    @Column(nullable = true)
    public double getTopTemp() {
        return topTemp;
    }

    public void setTopTemp(double topTemp) {
        this.topTemp = topTemp;
    }
    @Column(nullable = true)
    public double getMidTemp() {
        return midTemp;
    }

    public void setMidTemp(double midTemp) {
        this.midTemp = midTemp;
    }
    @Column(nullable = true)
    public double getBotTemp() {
        return botTemp;
    }

    public void setBotTemp(double botTemp) {
        this.botTemp = botTemp;
    }
    @Column(nullable = true)
    public String getBatteryState() {
        return batteryState;
    }

    public void setBatteryState(String batteryState) {
        this.batteryState = batteryState;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH})
    @JoinColumn(name = "terminal_id")
    public Terminal getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(Terminal terminalId) {
        this.terminalId = terminalId;
    }

    @Temporal(value = TemporalType.TIMESTAMP)
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
