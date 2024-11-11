package it.ITSincom.WebDev.service;

import io.quarkus.mailer.Mailer;
import io.quarkus.mailer.Mail;
import it.ITSincom.WebDev.persistence.ProductRepository;
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
    private final ProductRepository productRepository; // Modifica in base alla tua implementazione

    @Inject
    public NotificationService(Mailer mailer, SmsService smsService, OrderService orderService, ProductRepository productRepository) {
        this.mailer = mailer;
        this.smsService = smsService;
        this.orderService = orderService;
        this.productRepository = productRepository;
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
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String smsMessage = "Il tuo ordine alla Pasticceria C'est La Vie è stato accettato con successo! "
                    + "Ritira il tuo ordine il " + order.getPickupDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")) + ".";
            smsService.sendSms(user.getPhone(), smsMessage);
        }
    }

    public void sendNewOrderNotificationToAdmin(Order order) {
        String adminEmail = "chrychen.acmilan@gmail.com";
        // Costruisci i dettagli dei prodotti
        StringBuilder productDetails = new StringBuilder();
        for (OrderItem item : order.getProducts()) {
            // Supponendo che tu possa ottenere il nome del prodotto usando un metodo del repository
            String productName = productRepository.findById(item.getProductId()).getName(); // Modifica in base alla tua implementazione
            productDetails.append("Nome prodotto: ").append(productName)
                    .append(", Quantità: ").append(item.getQuantity()).append("\n");
        }
        // Messaggio con i dettagli dei prodotti
        String message = "Un nuovo ordine è stato effettuato:\n"
                + "Dettagli dell'ordine:\n"
                + productDetails.toString()
                + "\nOrario di ritiro: " + order.getPickupDateTime();
        // Invia notifica via email
        mailer.send(Mail.withText(adminEmail, "Nuovo Ordine Ricevuto", message));
    }
}

