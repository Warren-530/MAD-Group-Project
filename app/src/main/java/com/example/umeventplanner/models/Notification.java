package com.example.umeventplanner.models;

import com.google.firebase.Timestamp;

public class Notification {

    private String notificationId;
    private String eventId;
    private String eventTitle;
    private String message;
    private Timestamp timestamp;
    private NotificationType type;

    public enum NotificationType {
        NEW_ANNOUNCEMENT,
        EVENT_REMINDER
    }

    public Notification() { }

    public Notification(String notificationId, String eventId, String eventTitle, String message, Timestamp timestamp, NotificationType type) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.message = message;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and Setters
    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }
}
