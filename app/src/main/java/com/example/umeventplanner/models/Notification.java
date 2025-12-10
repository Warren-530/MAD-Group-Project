package com.example.umeventplanner.models;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Notification {
    private String notificationId;
    private String eventId;
    private String message;
    private Date timestamp;
    private boolean read;
    private NotificationType type; // Changed from notificationType to type

    public enum NotificationType {
        EVENT_REMINDER,
        NEW_ANNOUNCEMENT,
        EVENT_INVITATION,
        EVENT_REMOVAL // Added this line
    }

    public Notification() {
        // Default constructor required for calls to DataSnapshot.getValue(Notification.class)
    }

    public Notification(String notificationId, String eventId, String message, Date timestamp, boolean read, NotificationType type) {
        this.notificationId = notificationId;
        this.eventId = eventId;
        this.message = message;
        this.timestamp = timestamp;
        this.read = read;
        this.type = type;
    }

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @ServerTimestamp
    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public NotificationType getType() { // Changed from getNotificationType to getType
        return type;
    }

    public void setType(NotificationType type) { // Changed from setNotificationType to setType
        this.type = type;
    }
}
