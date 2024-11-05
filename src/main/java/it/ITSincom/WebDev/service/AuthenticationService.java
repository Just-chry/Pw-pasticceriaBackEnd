package it.ITSincom.WebDev.service;


import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.service.exception.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationService {

    private final UserRepository userRepository;
    private final HashCalculator hashCalculator;
    private final UserSessionRepository userSessionRepository;

    @Inject
    public AuthenticationService(UserRepository userRepository, HashCalculator hashCalculator, UserSessionRepository userSessionRepository) {
        this.userRepository = userRepository;
        this.hashCalculator = hashCalculator;
        this.userSessionRepository = userSessionRepository;
    }
    @Transactional
    public User register(CreateUserRequest request) throws UserCreationException {
        if (request == null) {
            throw new UserCreationException("La richiesta non può essere vuota. Nome, cognome, password e almeno un contatto sono obbligatori.");
        }
        if (!request.hasValidNameAndSurname()) {
            throw new UserCreationException("Nome e cognome sono obbligatori.");
        }
        if (!request.hasValidContact()) {
            throw new UserCreationException("È necessario inserire almeno un'email o un numero di telefono.");
        }

        boolean emailInUse = false;
        boolean telefonoInUse = false;

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            emailInUse = userRepository.findByEmail(request.getEmail()).isPresent();
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            telefonoInUse = userRepository.findByTelefono(request.getPhone()).isPresent();
        }

        if (emailInUse && telefonoInUse) {
            throw new UserCreationException("Sia l'email che il numero di telefono sono già in uso.");
        } else if (emailInUse) {
            throw new UserCreationException("L'email è già in uso.");
        } else if (telefonoInUse) {
            throw new UserCreationException("Il numero di telefono è già in uso.");
        }

        String hashedPassword = hashCalculator.calculateHash(request.getPassword());
        User user = new User();
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPassword(hashedPassword);
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        String verificationToken = UUID.randomUUID().toString();
        user.setVerificationToken(verificationToken);

        userRepository.persist(user);
        return user;
    }

    @Transactional
    public String login(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        if (request == null || request.getEmailOrPhone() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email/Telefono e password sono obbligatori.");
        }

        Optional<User> optionalUser = userRepository.findByEmailOrTelefono(request.getEmailOrPhone());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("Utente non trovato.");
        }

        User user = optionalUser.get();
        String hashedPassword = hashCalculator.calculateHash(request.getPassword());
        if (!user.getPassword().equals(hashedPassword)) {
            throw new WrongPasswordException("Password errata.");
        }

        Optional<UserSession> existingSession = userSessionRepository.findByUserId(user.getId());
        if (existingSession.isPresent()) {
            throw new SessionAlreadyExistsException("Utente ha già una sessione attiva.");
        }

        String sessionId = UUID.randomUUID().toString();
        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUserId(user.getId());

        userSessionRepository.persist(userSession);

        return sessionId;
    }

    @Transactional
    public void logout(String sessionId) throws UserSessionNotFoundException {
        Optional<UserSession> optionalSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non valida.");
        }
        userSessionRepository.delete(optionalSession.get());
    }

    public boolean isAdmin(String sessionId) throws UserSessionNotFoundException {
        UserSession session = findUserSessionBySessionId(sessionId);
        if (session == null) {
            throw new UserSessionNotFoundException("Sessione non valida");
        }
        User user = findUserById(session.getUserId());
        return "admin".equalsIgnoreCase(user.getRole());
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    public UserSession findUserSessionBySessionId(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId).orElse(null);
    }

    public Optional<User> findUserByEmailOrTelefono(String emailOrTelefono) {
        return userRepository.findByEmailOrTelefono(emailOrTelefono);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.find("email", email).firstResultOptional();
    }
}
