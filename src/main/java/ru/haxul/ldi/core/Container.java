package ru.haxul.ldi.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Container {

    private static Map<Class<?>, Object> storage = new HashMap<>();
    private boolean locked = false;

    public Object get(Class<?> type) {
        return storage.get(type);
    }

    public void put(Class<?> key, Object val) {
        if (!locked) storage.put(key, val);
    }

    public boolean containsKey(Class<?> key) {
        return storage.containsKey(key);
    }

    public void becameImmutable() {
        if (locked) return;
        locked = true;
        storage = Collections.unmodifiableMap(storage);
    }
}
