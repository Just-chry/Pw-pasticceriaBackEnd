package it.ITSincom.WebDev.rest.model;

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
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean hasValidNameAndSurname() {
        return name != null && !name.trim().isEmpty() &&
                surname != null && !surname.trim().isEmpty();
    }

    public boolean hasValidContact() {
        return (email != null && !email.trim().isEmpty()) ||
                (phone != null && !phone.trim().isEmpty());
    }


}
