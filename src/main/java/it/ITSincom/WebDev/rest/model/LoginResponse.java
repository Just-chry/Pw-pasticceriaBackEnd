package it.ITSincom.WebDev.rest.model;

public class LoginResponse {
    private String message;
    private String name;

    public LoginResponse(String message, String nome) {
        this.message = message;
        this.name = nome;
    }

    // Getter e Setter
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
}
