package com.example.umeventplanner.models;

import com.google.firebase.Timestamp;

public class Announcement {
    private String announcementId;
    private String authorId;
    private String authorName;
    private String message;
    private Timestamp timestamp;

    public Announcement() {
        // Default constructor required for calls to DataSnapshot.getValue(Announcement.class)
    }

    public Announcement(String announcementId, String authorId, String authorName, String message, Timestamp timestamp) {
        this.announcementId = announcementId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
