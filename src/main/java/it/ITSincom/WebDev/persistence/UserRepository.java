package it.ITSincom.WebDev.persistence;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import it.ITSincom.WebDev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public Optional<User> findByEmail(String email) {
        return find("email", email).firstResultOptional();
    }

    public Optional<User> findByPhone(String phone) {
        return find("phone", phone).firstResultOptional();
    }

    public Optional<User> findUserByEmailOrPhone(String emailOrTelefono) {
        Optional<User> userByEmail = findByEmail(emailOrTelefono);
        if (userByEmail.isPresent()) {
            return userByEmail;
        }
        return findByPhone(emailOrTelefono);
    }

}
