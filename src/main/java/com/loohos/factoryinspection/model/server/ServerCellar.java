package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerCellar {
    private String cellarId;
    private int cellarCode;
    private ServerSensor serverSensor;
    private ServerRow serverRow;
    private String localCellarId;
    private String cellarType = "true";

    @Id
    @Column
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

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "row_id")
    public ServerRow getServerRow() {
        return serverRow;
    }

    public void setServerRow(ServerRow serverRow) {
        this.serverRow = serverRow;
    }

    @Column
    public String getLocalCellarId() {
        return localCellarId;
    }

    public void setLocalCellarId(String localCellarId) {
        this.localCellarId = localCellarId;
    }

    @Column
    public String getCellarType() {
        return cellarType;
    }

    public void setCellarType(String cellarType) {
        this.cellarType = cellarType;
    }
}
