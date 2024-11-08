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
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;
import java.util.List;
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
            telefonoInUse = userRepository.findByPhone(request.getPhone()).isPresent();
        }

        if (emailInUse && telefonoInUse) {
            throw new UserCreationException("Sia l'email che il numero di telefono sono già in uso.");
        } else if (emailInUse) {
            throw new UserCreationException("L'email è già in uso.");
        } else if (telefonoInUse) {
            throw new UserCreationException("Il numero di telefono è già in uso.");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPassword(hashPassword(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            String verificationToken = UUID.randomUUID().toString();
            user.setVerificationTokenEmail(verificationToken);
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String otp = generateOtp();
            user.setVerificationTokenPhone(otp);
        }

        userRepository.persist(user);
        return user;
    }

    @Transactional
    public Response verifyContact(String token, String contact) {
        Optional<User> optionalUser;
        if (contact.contains("@")) {
            optionalUser = findUserByEmail(contact);
        } else {
            optionalUser = findUserByPhone(contact);
        }

        if (optionalUser.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Utente non trovato.").build();
        }

        User user = optionalUser.get();

        if (contact.contains("@")) {
            if (user.getVerificationTokenEmail() == null || !user.getVerificationTokenEmail().equals(token)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Token non valido.").build();
            }
            user.setEmailVerified(true);
            user.setVerificationTokenEmail(null);
        } else {
            if (user.getVerificationTokenPhone() == null || !user.getVerificationTokenPhone().equals(token)) {
                return Response.status(Response.Status.BAD_REQUEST).entity("Token non valido.").build();
            }
            user.setPhoneVerified(true);
            user.setVerificationTokenPhone(null);
        }

        userRepository.getEntityManager().merge(user);
        return Response.ok("Contatto verificato con successo! Ora puoi accedere.").build();
    }

    @Transactional
    public String login(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        if (request == null || request.getEmailOrPhone() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Email/Telefono e password sono obbligatori.");
        }

        Optional<User> optionalUser = userRepository.findUserByEmailOrPhone(request.getEmailOrPhone());
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
        userSession.setUser(user);
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
        User user = session.getUser();
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            throw new UserSessionNotFoundException("Accesso non autorizzato: l'utente non è un amministratore");
        }
        return true;
    }

    public User findUserById(String userId) {
        return userRepository.findById(userId);
    }


    public UserSession findUserSessionBySessionId(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId).orElse(null);
    }

    public Optional<User> findUserByEmailOrPhone(String emailOrTelefono) {
        return userRepository.findUserByEmailOrPhone(emailOrTelefono);
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.find("email", email).firstResultOptional();
    }

    public Optional<User> findUserByPhone(String contact) {
        return userRepository.find("phone", contact).firstResultOptional();
    }
    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public String hashPassword(String password) {
        return hashCalculator.calculateHash(password);
    }

    public boolean verifyPassword(String actualPassword, String providedPassword) {
        return actualPassword != null && actualPassword.equals(providedPassword);
    }

    public List<User> getAllUsers() {
        return userRepository.listAll();
    }
}
