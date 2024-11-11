package it.ITSincom.WebDev.service;


import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.persistence.UserSessionRepository;
import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.rest.model.LoginResponse;
import it.ITSincom.WebDev.rest.model.UserResponse;
import it.ITSincom.WebDev.service.exception.*;
import it.ITSincom.WebDev.util.Validation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationService {

    private final UserRepository userRepository;
    private final HashCalculator hashCalculator;
    private final UserSessionRepository userSessionRepository;
    private final Mailer mailer;
    private final SmsService smsService;

    @Inject
    public AuthenticationService(UserRepository userRepository, HashCalculator hashCalculator, UserSessionRepository userSessionRepository, Mailer mailer, SmsService smsService) {
        this.userRepository = userRepository;
        this.hashCalculator = hashCalculator;
        this.userSessionRepository = userSessionRepository;
        this.mailer = mailer;
        this.smsService = smsService;
    }

    @Transactional
    public User register(CreateUserRequest request) throws UserCreationException {
        Validation.validateUserRequest(request);
        checkIfEmailOrPhoneExists(request);

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setName(request.getName());
        user.setSurname(request.getSurname());
        user.setPassword(hashPassword(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());

        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            user.setVerificationTokenEmail(UUID.randomUUID().toString());
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            user.setVerificationTokenPhone(generateOtp());
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
    public LoginResponse handleLogin(LoginRequest request) throws UserNotFoundException, WrongPasswordException, SessionAlreadyExistsException {
        // Validazione della richiesta
        Validation.validateLoginRequest(request);

        // Cerca l'utente nel database
        Optional<User> optionalUser = userRepository.findUserByEmailOrPhone(request.getEmailOrPhone());
        User user = optionalUser.orElseThrow(() -> new UserNotFoundException("Utente non trovato."));

        // Controlla se l'utente ha verificato almeno un contatto
        if (!user.getEmailVerified() && !user.getPhoneVerified()) {
            throw new UnauthorizedAccessException("Contatto non verificato. Per favore, verifica il tuo indirizzo email o il tuo numero di telefono.");
        }

        // Verifica della password
        String hashedProvidedPassword = hashPassword(request.getPassword());
        if (!verifyPassword(user.getPassword(), hashedProvidedPassword)) {
            throw new WrongPasswordException("Password errata.");
        }

        // Controlla se la sessione esiste già
        checkIfSessionExists(user.getId());

        // Crea una nuova sessione
        String sessionId = createSession(user);
        return new LoginResponse("Login avvenuto con successo", user.getName(), sessionId);
    }

    @Transactional
    public void logout(String sessionId) throws UserSessionNotFoundException {
        Optional<UserSession> optionalSession = userSessionRepository.findBySessionId(sessionId);
        if (optionalSession.isEmpty()) {
            throw new UserSessionNotFoundException("Sessione non valida.");
        }
        userSessionRepository.delete(optionalSession.get());
    }

    public void isAdmin(String sessionId) throws UserSessionNotFoundException, UnauthorizedAccessException {
        UserSession session = findUserSessionBySessionId(sessionId);
        User user = (session != null) ? session.getUser() : null;
        Validation.validateSessionAndUser(sessionId, session, user);
        if (!"admin".equalsIgnoreCase(user.getRole())) {
            throw new UnauthorizedAccessException("Accesso non autorizzato: l'utente non è un amministratore");
        }
    }



    private void checkIfEmailOrPhoneExists(CreateUserRequest request) throws UserCreationException {
        boolean emailInUse = request.getEmail() != null && userRepository.findByEmail(request.getEmail()).isPresent();
        boolean phoneInUse = request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent();

        if (emailInUse && phoneInUse) {
            throw new UserCreationException("Sia l'email che il numero di telefono sono già in uso.");
        } else if (emailInUse) {
            throw new UserCreationException("L'email è già in uso.");
        } else if (phoneInUse) {
            throw new UserCreationException("Il numero di telefono è già in uso.");
        }
    }

    private String createSession(User user) {
        String sessionId = UUID.randomUUID().toString();
        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUser(user);
        userSessionRepository.persist(userSession);
        return sessionId;
    }

    private void checkIfSessionExists(String userId) throws SessionAlreadyExistsException {
        Optional<UserSession> existingSession = userSessionRepository.findByUserId(userId);
        if (existingSession.isPresent()) {
            throw new SessionAlreadyExistsException("Utente ha già una sessione attiva.");
        }
    }

    public UserSession findUserSessionBySessionId(String sessionId) {
        return userSessionRepository.findBySessionId(sessionId).orElse(null);
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

    public void sendVerificationEmail(User user, String verificationLink) {
        mailer.send(Mail.withHtml(user.getEmail(),
                "Conferma la tua registrazione",
                "<h1>Benvenuto " + user.getName() + " " + user.getSurname() + "!</h1>" +
                        "<p>Per favore, clicca sul link seguente per verificare il tuo indirizzo email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));
    }

    public void sendVerificationSms(User user) throws UserCreationException {
        String otp = user.getVerificationTokenPhone();
        try {
            smsService.sendSms(user.getPhone(), "Il tuo codice OTP è: " + otp);
        } catch (SmsSendingException e) {
            throw new UserCreationException("Errore durante l'invio dell'OTP: " + e.getMessage());
        }
    }

    public List<UserResponse> getAllUserResponses() {
        List<User> users = getAllUsers(); // Ottieni tutti gli utenti
        List<UserResponse> userResponses = new ArrayList<>();

        for (User user : users) {
            // Converte ogni User in un UserResponse
            UserResponse response = new UserResponse(
                    user.getName(),
                    user.getSurname(),
                    user.getEmail(),
                    user.getPhone()
            );
            userResponses.add(response);
        }

        return userResponses;
    }
    public List<User> getAllUsers() {
        return userRepository.findAll().list();
    }
}
