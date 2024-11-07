package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.UserSession;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import java.util.Optional;

@ApplicationScoped
public class UserSessionRepository implements PanacheRepository<UserSession> {

    public Optional<UserSession> findBySessionId(String sessionId) {
        return find("sessionId", sessionId).firstResultOptional();
    }
    public Optional<UserSession> findByUserId(String userId) {
        return find("user.id", userId).firstResultOptional();
    }

}
