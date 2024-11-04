package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProfileService {

    @Transactional
    public User updateUser(User user) {
        return User.getEntityManager().merge(user);
    }
}
