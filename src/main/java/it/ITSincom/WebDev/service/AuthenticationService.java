package it.ITSincom.WebDev.service;


import it.ITSincom.WebDev.persistence.model.UserSession;
import it.ITSincom.WebDev.persistence.repository.UserSessionRepository;
import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.repository.UserRepository;
import it.ITSincom.WebDev.rest.model.LoginRequest;
import it.ITSincom.WebDev.service.exception.AuthenticationException;
import it.ITSincom.WebDev.service.exception.UserCreationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AuthenticationService {

    private final UserRepository utenteRepository;
    private final HashCalculator hashCalculator;
    private final UserSessionRepository userSessionRepository;

    @Inject
    public AuthenticationService(UserRepository utenteRepository, HashCalculator hashCalculator, UserSessionRepository userSessionRepository){
        this.utenteRepository = utenteRepository;
        this.hashCalculator = hashCalculator;
        this.userSessionRepository = userSessionRepository;
    }

    @Transactional
    public void register(CreateUserRequest request) throws UserCreationException {
        if (request == null) {
            throw new UserCreationException("La richiesta non può essere vuota. Nome, cognome, password e almeno un contatto sono obbligatori.");
        }
        if (!request.hasValidNameAndSurname()) {
            throw new UserCreationException("Nome e cognome sono obbligatori.");
        }
        if (!request.hasValidContact()) {
            throw new UserCreationException("È necessario inserire almeno un'email o un numero di telefono.");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            Optional<User> existingUserByEmail = utenteRepository.findByEmail(request.getEmail());
            if (existingUserByEmail.isPresent()) {
                throw new UserCreationException("L'email è già in uso.");
            }
        }
        if (request.getTelefono() != null && !request.getTelefono().isBlank()) {
            Optional<User> existingUserByTelefono = utenteRepository.findByTelefono(request.getTelefono());
            if (existingUserByTelefono.isPresent()) {
                throw new UserCreationException("Il numero di telefono è già in uso.");
            }
        }

        if (request.getTelefono() != null) {
            Optional<User> existingUserByTelefono = utenteRepository.findByTelefono(request.getTelefono());
            if (existingUserByTelefono.isPresent()) {
                throw new UserCreationException("Il numero di telefono è già in uso.");
            }
        }
        String hashedPassword = hashCalculator.calculateHash(request.getPassword());
        User user = new User();
        user.setNome(request.getNome());
        user.setCognome(request.getCognome());
        user.setPassword(hashedPassword);
        user.setEmail(request.getEmail());
        user.setTelefono(request.getTelefono());
        utenteRepository.persist(user);
    }

    @Transactional
    public String login(LoginRequest request) throws AuthenticationException {
        if (request == null || request.getEmailOrTelefono() == null || request.getPassword() == null) {
            throw new AuthenticationException("Email/Telefono e password sono obbligatori.");
        }

        Optional<User> optionalUser = utenteRepository.findByEmailOrTelefono(request.getEmailOrTelefono());
        if (optionalUser.isEmpty()) {
            throw new AuthenticationException("Utente non trovato.");
        }

        User user = optionalUser.get();
        String hashedPassword = hashCalculator.calculateHash(request.getPassword());
        if (!user.getPassword().equals(hashedPassword)) {
            throw new AuthenticationException("Password errata.");
        }

        String sessionId = UUID.randomUUID().toString();
        UserSession userSession = new UserSession();
        userSession.setSessionId(sessionId);
        userSession.setUserId(user.getId());

        userSessionRepository.persist(userSession);

        return sessionId;
    }

}
