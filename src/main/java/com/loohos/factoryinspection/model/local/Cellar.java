package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Cellar {
    private String cellarId;
    private int cellarCode;
    private Sensor sensor;
    private Row row;
    private String cellarType = "true";

    @Id
    @Column(nullable = false)
    public String getCellarId() {
        return cellarId;
    }

    public void setCellarId(String cellarId) {
        this.cellarId = cellarId;
    }

    @Column
    public int getCellarCode() {
        return cellarCode;
    }

    public void setCellarCode(int cellarCode) {
        this.cellarCode = cellarCode;
    }

    @OneToOne(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy = "cellar")
    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "row_id")
    public Row getRow() {
        return row;
    }

    public void setRow(Row row) {
        this.row = row;
    }

    @Column
    public String getCellarType() {
        return cellarType;
    }

    public void setCellarType(String cellarType) {
        this.cellarType = cellarType;
    }
}
