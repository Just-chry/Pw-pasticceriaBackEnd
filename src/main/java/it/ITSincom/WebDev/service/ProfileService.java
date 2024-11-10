package it.ITSincom.WebDev.service;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import it.ITSincom.WebDev.persistence.UserRepository;
import it.ITSincom.WebDev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class ProfileService {

    private final UserRepository userRepository;
    private final Mailer mailer;

    @Inject
    public ProfileService(UserRepository userRepository, Mailer mailer) {
        this.userRepository = userRepository;
        this.mailer = mailer;
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.getEntityManager().merge(user);
    }

    public void updateEmail(User user, String newEmail) {
        user.setEmail(newEmail);
        user.setEmailVerified(false);
        String verificationTokenEmail = UUID.randomUUID().toString();
        user.setVerificationTokenEmail(verificationTokenEmail);

        String verificationLink = "http://localhost:8080/auth/verify?token=" + verificationTokenEmail + "&contact=" + user.getEmail();
        mailer.send(Mail.withHtml(user.getEmail(),
                "Conferma la tua email",
                "<h1>Ciao " + user.getName() + "!</h1>" +
                        "<p>Per favore, clicca sul link seguente per verificare il tuo nuovo indirizzo email:</p>" +
                        "<a href=\"" + verificationLink + "\">Verifica la tua email</a>"));
    }

    public void updatePhone(User user, String newPhone) {
        user.setPhone(newPhone);
    }

    public void updatePassword(User user, String oldPassword, String newPassword, AuthenticationService authService) {
        String hashedOldPassword = authService.hashPassword(oldPassword);
        if (!authService.verifyPassword(user.getPassword(), hashedOldPassword)) {
            throw new IllegalArgumentException("La vecchia password non corrisponde");
        }

        if (oldPassword.equals(newPassword)) {
            throw new IllegalArgumentException("La nuova password non può essere uguale alla vecchia");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("La nuova password non può essere vuota");
        }

        String hashedNewPassword = authService.hashPassword(newPassword);
        user.setPassword(hashedNewPassword);
    }

    public User updatePhoneNumber(User user, String newPhone) {
        if (newPhone != null) {
            user.setPhone(newPhone);
        }
        return updateUser(user);
    }

}
