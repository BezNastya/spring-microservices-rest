package com.example.bookmodule.exception;

public class NoSuchOrderException extends RuntimeException {

    public NoSuchOrderException(String message) {
        super(message);
    }
}
