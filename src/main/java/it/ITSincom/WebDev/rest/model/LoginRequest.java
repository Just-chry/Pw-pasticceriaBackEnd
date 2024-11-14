package it.ITSincom.WebDev.rest.model;

public class LoginRequest {
    private String emailOrPhone;
    private String password;

    public String getEmailOrPhone() {
        return emailOrPhone;
    }

    public void setEmailOrPhone(String emailOrPhone) {
        if (emailOrPhone != null && !emailOrPhone.trim().isEmpty() && emailOrPhone.matches("\\d+")) {
            if (!emailOrPhone.startsWith("+39")) {
                emailOrPhone = "+39" + emailOrPhone;
            }
        }
        this.emailOrPhone = emailOrPhone;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
