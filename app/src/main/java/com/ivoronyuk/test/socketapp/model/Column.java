package com.ivoronyuk.test.socketapp.model;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class Column {

    private ColumnType type;

    public Column(ColumnType type) {
        this.type = type;
    }

    public ColumnType getType() {
        return type;
    }

    public void setType(ColumnType type) {
        this.type = type;
    }
}
