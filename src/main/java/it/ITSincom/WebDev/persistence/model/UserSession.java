package it.ITSincom.WebDev.persistence.model;

import jakarta.persistence.*;

@Entity
@Table(name = "session")
public class UserSession {

    @Id
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Costruttore vuoto per JPA
    public UserSession() {}

    // Getter e Setter
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
