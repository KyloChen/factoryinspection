package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerRow {
    private String rowId;
    private int rowCode;
    private ServerPit serverPit;
    private Set<ServerCellar> serverCellars;
    private String localRowId;


    @Id
    @Column
    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    @Column
    public int getRowCode() {
        return rowCode;
    }

    public void setRowCode(int rowCode) {
        this.rowCode = rowCode;
    }

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinColumn(name = "pit_id")
    public ServerPit getServerPit() {
        return serverPit;
    }

    public void setServerPit(ServerPit serverPit) {
        this.serverPit = serverPit;
    }

    @OneToMany(cascade = {CascadeType.REMOVE}, fetch = FetchType.EAGER, mappedBy = "serverRow")
    public Set<ServerCellar> getServerCellars() {
        return serverCellars;
    }

    public void setServerCellars(Set<ServerCellar> serverCellars) {
        this.serverCellars = serverCellars;
    }

    @Column
    public String getLocalRowId() {
        return localRowId;
    }

    public void setLocalRowId(String localRowId) {
        this.localRowId = localRowId;
    }
}
