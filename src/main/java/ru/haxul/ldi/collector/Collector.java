package ru.haxul.ldi.collector;

import java.util.List;

public interface Collector<T,E> {

    List<T> find(E entryPoint);
}
