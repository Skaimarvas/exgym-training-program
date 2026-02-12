package com.exgym.training.exception;


public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }

    public AlreadyExistsException(String resourceType, String fieldName, String value) {
        super(String.format("%s already exists with %s: %s", resourceType, fieldName, value));
    }
}
