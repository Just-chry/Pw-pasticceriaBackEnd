package it.ITSincom.WebDev.rest.model;

public class LoginRequest {
    private String emailOrTelefono;
    private String password;

    public String getEmailOrTelefono() {
        return emailOrTelefono;
    }

    public void setEmailOrTelefono(String emailOrTelefono) {
        this.emailOrTelefono = emailOrTelefono;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
