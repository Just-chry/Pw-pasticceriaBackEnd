package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.model.Session;
import it.ITSincom.WebDev.persistence.repository.SessionRepository;
import it.ITSincom.WebDev.rest.model.CreateUserRequest;
import it.ITSincom.WebDev.persistence.model.User;
import it.ITSincom.WebDev.persistence.repository.UserRepository;
import it.ITSincom.WebDev.rest.model.LoginRequest;
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


    @Inject
    public AuthenticationService(UserRepository utenteRepository, HashCalculator hashCalculator, SessionRepository sessionRepository) {
        this.utenteRepository = utenteRepository;
        this.hashCalculator = hashCalculator;

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


}
