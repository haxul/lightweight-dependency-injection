package ru.haxul.ldi.collector;

public interface FileTypeDefiner<T> {

    FileType define(final T file);
}
