package it.ITSincom.WebDev.persistence.repository;

import it.ITSincom.WebDev.persistence.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByTelefono(String telefono) {
        return find("telefono", telefono).firstResultOptional();
    }
}
