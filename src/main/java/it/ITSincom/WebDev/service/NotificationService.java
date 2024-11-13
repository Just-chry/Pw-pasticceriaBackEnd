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

    private static final String ADMIN_EMAIL = "chrychen.acmilan@gmail.com";

    private final Mailer mailer;
    private final SmsService smsService;
    private final OrderService orderService;
    private final ProductRepository productRepository;

    @Inject
    public NotificationService(Mailer mailer, SmsService smsService, OrderService orderService, ProductRepository productRepository) {
        this.mailer = mailer;
        this.smsService = smsService;
        this.orderService = orderService;
        this.productRepository = productRepository;
    }

    public void sendOrderAcceptedNotification(User user, String orderId) throws Exception {
        Order order = orderService.getOrder(orderId);
        String productDetails = buildProductDetails(order);

        String message = "Il tuo ordine alla Pasticceria C'est La Vie è stato accettato con successo!<br><br>"
                + "Dettagli dell'ordine:<br>"
                + productDetails
                + "<br>Orario di ritiro: " + order.getPickupDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm")) + "<br>"
                + "Grazie per aver ordinato da noi!";

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
        String productDetails = buildProductDetails(order);
        String message = "Un nuovo ordine è stato effettuato:\n"
                + "Dettagli dell'ordine:\n"
                + productDetails
                + "\nOrario di ritiro: " + order.getPickupDateTime();

        mailer.send(Mail.withText(ADMIN_EMAIL, "Nuovo Ordine Ricevuto", message));
    }

    public void sendOrderCancelledNotificationToAdmin(Order order) {
        String productDetails = buildProductDetails(order);
        String message = "Un ordine è stato cancellato:\n"
                + "Dettagli dell'ordine:\n"
                + productDetails
                + "\nOrario di ritiro previsto: " + order.getPickupDateTime();

        mailer.send(Mail.withText(ADMIN_EMAIL, "Ordine Cancellato", message));
    }

    // Metodo privato per costruire i dettagli dei prodotti
    private String buildProductDetails(Order order) {
        StringBuilder productDetails = new StringBuilder();
        for (OrderItem item : order.getProducts()) {
            String productName = productRepository.findById(item.getProductId()).getName();
            productDetails.append("Nome prodotto: ").append(productName)
                    .append(", Quantità: ").append(item.getQuantity()).append("\n");
        }
        return productDetails.toString();
    }

    public void sendOrderRejectedNotification(User user, String orderId) throws Exception {
        Order order = orderService.getOrder(orderId);
        String productDetails = buildProductDetails(order);

        // Messaggio di notifica per l'ordine rifiutato
        String message = "Purtroppo il tuo ordine alla Pasticceria C'est La Vie è stato rifiutato.<br><br>"
                + "Dettagli dell'ordine:<br>"
                + productDetails
                + "<br>Ci dispiace per l'inconveniente. Ti invitiamo a contattarci per ulteriori informazioni.";

        // Invia una notifica via email o SMS a seconda delle informazioni disponibili
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            mailer.send(Mail.withHtml(
                    user.getEmail(),
                    "Ordine Rifiutato",
                    "<p>" + message + "</p>"
            ));
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String smsMessage = "Purtroppo il tuo ordine alla Pasticceria C'est La Vie è stato rifiutato. "
                    + "Ci dispiace per l'inconveniente. Per ulteriori informazioni, contattaci.";
            smsService.sendSms(user.getPhone(), smsMessage);
        }
    }

    public void sendOrderInCartNotification(User user, Order order) throws Exception {
        // Verifica che l'utente sia valido
        if (user == null) {
            throw new IllegalArgumentException("Utente non valido");
        }

        // Verifica che l'ordine sia valido e sia nello stato 'in_cart'
        if (order == null || !"cart".equals(order.getStatus())) {
            throw new IllegalArgumentException("L'ordine non è valido o non è nello stato 'in_cart'");
        }

        // Crea il messaggio per l'ordine in cart
        String productDetails = buildProductDetails(order);
        String message = "Il tuo ordine alla Pasticceria C'est La Vie è ancora nel carrello.<br><br>"
                + "Dettagli dell'ordine:<br>"
                + productDetails
                + "<br>Ti invitiamo a completare l'ordine per garantirti il ritiro nel giorno scelto.<br>"
                + "Grazie per averci scelto!";

        // Invia la notifica via email o SMS
        if (user.getEmail() != null && !user.getEmail().isEmpty()) {
            mailer.send(Mail.withHtml(
                    user.getEmail(),
                    "Promemoria: Ordine nel Carrello",
                    "<p>" + message + "</p>"
            ));
        } else if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String smsMessage = "Il tuo ordine alla Pasticceria C'est La Vie è ancora nel carrello. "
                    + "Completa l'ordine per garantire il ritiro.";
            smsService.sendSms(user.getPhone(), smsMessage);
        } else {
            throw new IllegalArgumentException("Non sono disponibili contatti validi per l'utente");
        }
    }

}
