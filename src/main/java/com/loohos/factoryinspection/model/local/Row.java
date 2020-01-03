package com.loohos.factoryinspection.model.local;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Row {
    private String rowId;
    private int rowCode;
    private Pit pit;
    private String rowType = "true";

    @Id
    @Column(nullable = false)
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
    public Pit getPit() {
        return pit;
    }

    public void setPit(Pit pit) {
        this.pit = pit;
    }

    @Column
    public String getRowType() {
        return rowType;
    }

    public void setRowType(String rowType) {
        this.rowType = rowType;
    }
}
