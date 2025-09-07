package com.johnmessaging.application.exception;

public class PublishException extends RuntimeException {
    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }
}