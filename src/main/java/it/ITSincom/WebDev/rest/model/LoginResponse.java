package it.ITSincom.WebDev.rest.model;

public class LoginResponse {
    private String message;
    private String name;
    private String sessionId;

    public LoginResponse(String message, String name, String sessionId) {
        this.message = message;
        this.name = name;
        this.sessionId = sessionId;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
