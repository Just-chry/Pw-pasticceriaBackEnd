package it.ITSincom.WebDev.service.exception;

public class SessionAlreadyExistsException extends Exception {
    public SessionAlreadyExistsException(String message) {
        super(message);
    }
}
