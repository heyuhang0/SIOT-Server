package com.hyh0.siot.server;

public interface Listener<K, V> {
    void handler(K key, V value);
}
