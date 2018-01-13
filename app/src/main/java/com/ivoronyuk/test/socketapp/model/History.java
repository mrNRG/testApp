package com.ivoronyuk.test.socketapp.model;

import java.util.HashMap;
import java.util.List;

/**
 * Created by igorvoronyuk on 1/12/18.
 */

public class History {

    private static final int MAX_COUNT = 10;

    private int id;
    private HashMap<ColumnType, List<Column>> map;

    public History() {
    }

    public History(int id, HashMap<ColumnType, List<Column>> map) {
        this.id = id;
        this.map = new HashMap<>();
        this.map.putAll(map);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public HashMap<ColumnType, List<Column>> getMap() {
        return map;
    }

    public void setMap(HashMap<ColumnType, List<Column>> map) {
        this.map = map;
    }

    public List<Column> getColumnsByType(ColumnType type) {
        return map.get(type);
    }

    public void addColumnsByType(ColumnType type) {
        List<Column> list = getColumnsByType(type);
        if (list.size() < MAX_COUNT) {
            getColumnsByType(type).add(new Column(type));
        }
    }

    public void removeColumnAAA() {
        List<Column> list = map.get(ColumnType.AAA);
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
    }

    public void removeColumnBBB() {
        List<Column> list = map.get(ColumnType.BBB);
        if (!list.isEmpty()) {
            list.remove(list.size() - 1);
        }
    }

    public void clearAll() {
        List<Column> listAAA = map.get(ColumnType.AAA);
        List<Column> listBBB = map.get(ColumnType.BBB);

        listAAA.clear();
        listBBB.clear();
    }
}
