package it.ITSincom.WebDev.persistence.repository;

import it.ITSincom.WebDev.persistence.model.UserSession;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class UserSessionRepository implements PanacheRepository<UserSession> {
}
