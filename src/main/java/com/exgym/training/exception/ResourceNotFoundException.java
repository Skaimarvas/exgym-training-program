package com.exgym.training.exception;


public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceType, Long id) {
        super(String.format("%s not found with id: %d", resourceType, id));
    }

    public ResourceNotFoundException(String resourceType, String identifier, String value) {
        super(String.format("%s not found with %s: %s", resourceType, identifier, value));
    }
}
