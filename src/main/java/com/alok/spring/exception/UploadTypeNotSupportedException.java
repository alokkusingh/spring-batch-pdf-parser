package com.alok.spring.exception;

public class UploadTypeNotSupportedException extends RuntimeException {
    public UploadTypeNotSupportedException(String message) {
        super(message);
    }

    public UploadTypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }
}
