package it.ITSincom.WebDev.rest.model;

public class CreateUserRequest {
    private String nome;
    private String cognome;
    private String password;
    private String email;
    private String telefono;

    public String getNome() {
        return nome;
    }
    public void setNome(String nome) {
        this.nome = nome;
    }
    public String getCognome() {
        return cognome;
    }
    public void setCognome(String cognome) {
        this.cognome = cognome;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getTelefono() {
        return telefono;
    }
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean hasValidNameAndSurname() {
        return nome != null && !nome.trim().isEmpty() &&
                cognome != null && !cognome.trim().isEmpty();
    }

    public boolean hasValidContact() {
        return (email != null && !email.trim().isEmpty()) ||
                (telefono != null && !telefono.trim().isEmpty());
    }


}
