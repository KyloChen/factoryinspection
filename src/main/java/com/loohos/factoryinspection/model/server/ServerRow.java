package com.loohos.factoryinspection.model.server;

import javax.persistence.*;
import java.util.Set;

@Entity
public class ServerRow {
    private String rowId;
    private int rowCode;
    private ServerPit serverPit;
    private String localRowId;
    private String rowType = "true";


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

    @Column
    public String getLocalRowId() {
        return localRowId;
    }

    public void setLocalRowId(String localRowId) {
        this.localRowId = localRowId;
    }

    @Column
    public String getRowType() {
        return rowType;
    }

    public void setRowType(String rowType) {
        this.rowType = rowType;
    }
}
