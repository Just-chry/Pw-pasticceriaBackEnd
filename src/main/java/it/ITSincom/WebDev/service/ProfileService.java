package it.ITSincom.WebDev.service;

import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ProfileService {


    private final UserRepository userRepository;

    @Inject
    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Transactional
    public User updateUser(User user) {
        if (userRepository.isPersistent(user)) {
            userRepository.getEntityManager().merge(user);
        } else {
            userRepository.persist(user);
        }
        return user;
    }
}
