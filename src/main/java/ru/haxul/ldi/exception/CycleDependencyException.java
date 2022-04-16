package ru.haxul.ldi.exception;

public class CycleDependencyException extends RuntimeException {
    public CycleDependencyException(final String msg) {
        super(msg);
    }
}
