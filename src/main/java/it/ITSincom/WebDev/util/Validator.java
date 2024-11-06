package it.ITSincom.WebDev.util;

import java.util.regex.Pattern;

public class Validator {
    // Regex pattern for validating email
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Regex pattern for validating phone numbers (international format)
    private static final String PHONE_PATTERN = "^\\+[0-9]{10,15}$";

    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);

    /**
     * Validates the given email address against the pattern.
     *
     * @param email the email address to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return emailPattern.matcher(email).matches();
    }

    /**
     * Validates the given phone number against the pattern.
     *
     * @param phone the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return false;
        }
        return phonePattern.matcher(phone).matches();
    }
}