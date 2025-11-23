package com.example.umeventplanner;

// A helper class to hold both an Event and its registration status for the UI
public class Ticket {
    private Event event;
    private String registrationStatus;

    public Ticket(Event event, String registrationStatus) {
        this.event = event;
        this.registrationStatus = registrationStatus;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }
}
