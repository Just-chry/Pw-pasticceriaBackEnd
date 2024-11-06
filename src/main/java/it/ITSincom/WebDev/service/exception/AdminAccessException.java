package it.ITSincom.WebDev.service.exception;

public class AdminAccessException extends RuntimeException {
    public AdminAccessException(String message) {
        super(message);
    }
}
