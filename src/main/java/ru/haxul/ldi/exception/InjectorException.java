package ru.haxul.ldi.exception;

public class InjectorException extends RuntimeException {
    public InjectorException(final String msg) {
        super(msg);
    }

    public InjectorException(final String msg, Exception ex) {
        super(msg, ex);
    }
}
