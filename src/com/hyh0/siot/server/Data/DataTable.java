package com.hyh0.siot.server.Data;

import com.hyh0.siot.server.Listener;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DataTable<K, V> extends HashMap<K, V>{
    ConcurrentHashMap<K, Set<Listener<K, V>>> listenerLists = new ConcurrentHashMap<>();

    @Override
    public V put(K key, V value) {
        super.put(key, value);
        Set<Listener<K, V>> listeners = listenerLists.get(key);
        if (listeners !=null) {
            for (Listener<K, V> l : listeners) {
                l.handler(key, value);
            }
        }
        return value;
    }

    @Override
    public V remove(Object key) {
        super.remove(key);
        listenerLists.remove(key);
        return null;
    }

    public void addListener(K key, Listener<K, V> listener) {
        if (listenerLists.get(key) == null) {
            Set<Listener<K, V>> set = new HashSet<>();
            Collections.synchronizedSet(set);
            listenerLists.put(key, set);
        }
        listenerLists.get(key).add(listener);
    }

    public void removeListener(K key, Listener<K, V> listener) {
        Set<Listener<K, V>> listeners = listenerLists.get(key);
        if (listeners != null)
            listeners.remove(listener);
    }

    public void removeListener(K key) {
        Set<Listener<K, V>> listeners = listenerLists.get(key);
        if (listeners != null)
            listeners.clear();
    }

    public void removeListener(Listener<K, V> listener) {
        this.forEach((key, value) -> removeListener(key, listener));
    }
}
