package it.ITSincom.WebDev.persistence.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "user")
public class User {

    @Id
    @Column(columnDefinition = "CHAR(36)")
    private String id;

    private String email;
    private String password;
    private String name;
    private String surname;
    private String phone;
    private Boolean emailVerified = false;
    private Boolean phoneVerified = false;
    private String role = "user";
    private String verificationTokenEmail;
    private String verificationTokenPhone;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getVerificationTokenEmail() {
        return verificationTokenEmail;
    }

    public void setVerificationTokenEmail(String verificationTokenEmail) {
        this.verificationTokenEmail = verificationTokenEmail;
    }

    public String getVerificationTokenPhone() {
        return verificationTokenPhone;
    }

    public void setVerificationTokenPhone(String verificationTokenPhone) {
        this.verificationTokenPhone = verificationTokenPhone;
    }
}
