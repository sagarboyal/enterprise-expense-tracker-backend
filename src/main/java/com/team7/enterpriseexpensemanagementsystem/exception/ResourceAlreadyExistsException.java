package com.team7.enterpriseexpensemanagementsystem.exception;

public class ResourceAlreadyExistsException  extends RuntimeException {
    public ResourceAlreadyExistsException (String message) {
        super(message);
    }
}
