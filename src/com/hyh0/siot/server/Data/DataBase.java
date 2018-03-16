package com.hyh0.siot.server.Data;

import com.hyh0.siot.server.Listener;

import java.util.concurrent.ConcurrentHashMap;

public class DataBase<K, V> {
    ConcurrentHashMap<K, DataTable<K, V>> tables = new ConcurrentHashMap<>();

    public DataTable<K, V> createTable(K key) {
        DataTable<K, V> table = new DataTable<>();
        tables.put(key, table);
        return table;
    }

    public void deleteTable(K key) {
        tables.remove(key);
    }

    public void removeListener(Listener<K, V> l) {
        tables.forEach((key, value) -> {value.removeListener(l);});
    }

    public DataTable<K, V> getTable(K key) {
        DataTable<K, V> table = tables.get(key);
        if (table != null)
            return table;
        else
            return createTable(key);
    }
}
