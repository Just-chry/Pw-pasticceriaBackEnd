package it.ITSincom.WebDev.persistence.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "session")
public class UserSession {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public UserSession() {
        this.sessionId = UUID.randomUUID().toString(); // Genera automaticamente un UUID per la sessione
    }

    // Getter e Setter
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
