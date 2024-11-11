package it.ITSincom.WebDev.rest.model;

import it.ITSincom.WebDev.util.Validator;

import javax.xml.bind.ValidationException;

public class CreateUserRequest {
    private String name;
    private String surname;
    private String password;
    private String email;
    private String phone;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSurname() {
        return surname;
    }
    public void setSurname(String surname) {
        this.surname = surname;
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
    public void setEmail(String email) throws ValidationException {
        if (email != null && !email.trim().isEmpty()) {
            if (Validator.isValidEmail(email)) {
                this.email = email;
            } else {
                throw new ValidationException("Email non valida");
            }
        } else {
            this.email = null; // Imposta email a null se è una stringa vuota
        }
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws ValidationException {
        if (phone != null && !phone.trim().isEmpty()) {
            // Aggiungi il prefisso +39 se non è già presente
            if (!phone.startsWith("+39")) {
                phone = "+39" + phone;
            }

            if (Validator.isValidPhone(phone)) {
                this.phone = phone;
            } else {
                throw new ValidationException("Numero di telefono non valido");
            }
        } else {
            this.phone = null; // Imposta il telefono a null se è una stringa vuota
        }
    }



    public boolean hasValidNameAndSurname() {
        return name != null && !name.trim().isEmpty() &&
                surname != null && !surname.trim().isEmpty();
    }

    public boolean hasValidContact() {
        return (email != null && !email.trim().isEmpty()) || (phone != null && !phone.trim().isEmpty());
    }




}
