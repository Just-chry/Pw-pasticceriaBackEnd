package it.ITSincom.WebDev.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@ApplicationScoped
public class SmsService {

    @ConfigProperty(name = "twilio.account.sid")
    String accountSid;

    @ConfigProperty(name = "twilio.auth.token")
    String authToken;

    @ConfigProperty(name = "twilio.phone.number")
    String fromPhoneNumber;

    // Metodo per l'invio di SMS tramite Twilio
    public void sendSms(String toPhoneNumber, String messageBody) {
        // Inizializza Twilio (questo può essere fatto una volta sola)
        Twilio.init(accountSid, authToken);

        // Crea il messaggio
        Message message = Message.creator(
                new PhoneNumber(toPhoneNumber), // Numero di destinazione
                new PhoneNumber(fromPhoneNumber), // Numero Twilio da cui inviare l'SMS
                messageBody // Contenuto del messaggio
        ).create();
    }
}
