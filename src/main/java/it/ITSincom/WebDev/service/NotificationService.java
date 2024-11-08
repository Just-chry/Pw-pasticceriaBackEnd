package it.ITSincom.WebDev.service;

import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import it.ITSincom.WebDev.persistence.model.Order;
import it.ITSincom.WebDev.persistence.model.OrderItem;
import it.ITSincom.WebDev.persistence.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;

@ApplicationScoped
public class NotificationService {

    private final Mailer mailer;
    private final SmsService smsService;
    private final OrderService orderService;

    @Inject
    public NotificationService(Mailer mailer, SmsService smsService, OrderService orderService) {
        this.mailer = mailer;
        this.smsService = smsService;
        this.orderService = orderService;
    }


    public void sendOrderAcceptedNotification(User user, String orderId) throws Exception {
        // Recupera i dettagli dell'ordine
        Order order = orderService.getOrder(orderId); // Assicurati di avere questo metodo in OrderService
        StringBuilder productDetails = new StringBuilder();

        for (OrderItem item : order.getProducts()) {
            productDetails.append(item.getQuantity())
                    .append("x ")
                    .append(item.getProductName())
                    .append("<br>");
        }

        String message = "Il tuo ordine alla Pasticceria C'est La Vie è stato accettato con successo!<br><br>"
                + "Dettagli dell'ordine:<br>"
                + productDetails.toString()
                + "<br>Orario di ritiro: " + order.getPickupDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")) + "<br>"
                + "Grazie per aver ordinato da noi!";

        // Invia la notifica via email se l'utente ha un'email
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            mailer.send(Mail.withHtml(
                    user.getEmail(),
                    "Ordine Accettato",
                    "<p>" + message + "</p>"
            ));
            System.out.println("Email inviata a: " + user.getEmail());
        }

        // Invia la notifica via SMS se l'utente ha un numero di telefono
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            try {
                String smsMessage = "Il tuo ordine alla Pasticceria C'est La Vie è stato accettato con successo! "
                        + "Ritira il tuo ordine il " + order.getPickupDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")) + ".";
                smsService.sendSms(user.getPhone(), smsMessage);
                System.out.println("SMS inviato a: " + user.getPhone());
            } catch (Exception e) {
                System.err.println("Errore durante l'invio dell'SMS: " + e.getMessage());
            }
        }
    }

}
