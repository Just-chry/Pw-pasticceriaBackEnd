package it.ITSincom.WebDev.rest.model;

public class LoginResponse {
    private String message;
    private String nome;

    public LoginResponse(String message, String nome) {
        this.message = message;
        this.nome = nome;
    }

    // Getter e Setter
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
