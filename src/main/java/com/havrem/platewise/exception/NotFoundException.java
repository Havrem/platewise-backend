package com.havrem.platewise.exception;

public class NotFoundException extends ApplicationException {
    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
      return new NotFoundException("%s with id %s not found".formatted(resource, id));
    }
}
