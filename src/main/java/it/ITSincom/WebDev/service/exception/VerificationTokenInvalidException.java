package it.ITSincom.WebDev.service.exception;

public class VerificationTokenInvalidException extends RuntimeException {
    public VerificationTokenInvalidException(String message) {
        super(message);
    }
}
