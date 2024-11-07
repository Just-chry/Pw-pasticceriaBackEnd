package it.ITSincom.WebDev.persistence.model;

import java.util.List;

public class PickupSlot {

    private String dayOfWeek;
    private List<Slot> slots;

    public PickupSlot() {
    }

    public PickupSlot(String dayOfWeek, List<Slot> slots) {
        this.dayOfWeek = dayOfWeek;
        this.slots = slots;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public List<Slot> getSlots() {
        return slots;
    }

    public void setSlots(List<Slot> slots) {
        this.slots = slots;
    }

    public static class Slot {
        private String time;
        private boolean isAvailable;

        public Slot() {
        }

        public Slot(String time, boolean isAvailable) {
            this.time = time;
            this.isAvailable = isAvailable;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public boolean isAvailable() {
            return isAvailable;
        }

        public void setAvailable(boolean available) {
            isAvailable = available;
        }
    }
}
