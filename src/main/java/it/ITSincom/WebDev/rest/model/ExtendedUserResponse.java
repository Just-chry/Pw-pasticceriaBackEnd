package it.ITSincom.WebDev.rest.model;

public class ExtendedUserResponse extends UserResponse {
    private String id;  // Aggiungi l'ID dell'utente
    private Boolean emailVerified;
    private Boolean phoneVerified;

    public ExtendedUserResponse(String id, String name, String surname, String email, String phone, String role, Boolean emailVerified, Boolean phoneVerified) {
        super(name, surname, email, phone, role);
        this.id = id;  // Inizializza l'ID dell'utente
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public Boolean getPhoneVerified() {
        return phoneVerified;
    }

    public void setPhoneVerified(Boolean phoneVerified) {
        this.phoneVerified = phoneVerified;
    }
}
