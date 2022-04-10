package ru.haxul.ldi.exception;

public class SingletonNotFoundException extends RuntimeException {
    public SingletonNotFoundException(String name) {
        super(name);
    }
}
