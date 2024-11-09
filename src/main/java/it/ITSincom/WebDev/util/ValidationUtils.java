package it.ITSincom.WebDev.util;

import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.service.exception.UserCreationException;
import it.ITSincom.WebDev.service.exception.UserSessionNotFoundException;
import it.ITSincom.WebDev.service.exception.VerificationTokenInvalidException;

public class ValidationUtils {

    public static void validateUserRequest(CreateUserRequest request) throws UserCreationException {
        if (request == null) {
            throw new UserCreationException("La richiesta non può essere vuota. Nome, cognome, password e almeno un contatto sono obbligatori.");
        }
        if (!request.hasValidNameAndSurname()) {
            throw new UserCreationException("Nome e cognome sono obbligatori.");
        }
        if (!request.hasValidContact()) {
            throw new UserCreationException("È necessario inserire almeno un'email o un numero di telefono.");
        }
    }

    public static void validateSessionId(String sessionId) throws UserSessionNotFoundException {
        if (sessionId == null || sessionId.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non trovata o non valida. Effettua il login.");
        }
    }

    public static void validateToken(String actualToken, String providedToken) throws VerificationTokenInvalidException {
        if (actualToken == null || !actualToken.equals(providedToken)) {
            throw new VerificationTokenInvalidException("Token non valido.");
        }
    }

    public static void validateLoginRequest(LoginRequest request) throws IllegalArgumentException {
        if (request == null) {
            throw new IllegalArgumentException("La richiesta di login non può essere nulla.");
        }
        if (request.getEmailOrPhone() == null || request.getEmailOrPhone().isEmpty()) {
            throw new IllegalArgumentException("Email o numero di telefono è obbligatorio.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("La password è obbligatoria.");
        }
    }
}
