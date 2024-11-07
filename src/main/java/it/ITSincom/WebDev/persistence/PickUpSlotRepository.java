package it.ITSincom.WebDev.persistence;

import it.ITSincom.WebDev.persistence.model.PickupSlot;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class PickUpSlotRepository implements PanacheMongoRepository<PickupSlot> {


    private Map<LocalDateTime, Boolean> slotAvailability = new HashMap<>();

    public PickupSlot findPickupSlot(String dayOfWeek, String time) {
        return find("dayOfWeek = ?1 and slots.time = ?2", dayOfWeek, time).firstResult();
    }

    public void updateSlotAvailability(String dayOfWeek, String time, boolean isAvailable) {
        PickupSlot slot = findPickupSlot(dayOfWeek, time);
        if (slot != null) {
            slot.getSlots().stream()
                    .filter(s -> s.getTime().equals(time))
                    .findFirst()
                    .ifPresent(s -> s.setAvailable(isAvailable));
            persist(slot);
        }
    }

    public PickUpSlotRepository() {
        // Inizializza la disponibilità degli slot per un determinato periodo
        initializeSlots();
    }

    private void initializeSlots() {
        // Esempio: genera slot ogni 10 minuti tra 9:00 e 19:00 per una settimana
        LocalDateTime start = LocalDateTime.now().withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusDays(7);

        while (start.isBefore(end)) {
            if (start.getHour() >= 9 && (start.getHour() < 13 || (start.getHour() >= 15 && start.getHour() < 19))) {
                slotAvailability.put(start, true);
            }
            start = start.plusMinutes(10);
        }
    }

    public boolean isSlotAvailable(String pickupDate, String pickupTime) {
        LocalDateTime requestedSlot = LocalDateTime.parse(pickupDate + "T" + pickupTime);

        // Verifica se lo slot richiesto è disponibile
        return slotAvailability.getOrDefault(requestedSlot, false);
    }

    public void bookSlot(String pickupDate, String pickupTime) {
        LocalDateTime requestedSlot = LocalDateTime.parse(pickupDate + "T" + pickupTime);

        // Aggiorna lo slot come non disponibile
        if (slotAvailability.containsKey(requestedSlot) && slotAvailability.get(requestedSlot)) {
            slotAvailability.put(requestedSlot, false);
        } else {
            throw new IllegalArgumentException("Slot non disponibile per il booking");
        }
    }

}
