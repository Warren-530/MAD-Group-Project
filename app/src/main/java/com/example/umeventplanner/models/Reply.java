package com.example.umeventplanner.models;

import com.google.firebase.Timestamp;

public class Reply {
    private String replyId;
    private String authorId;
    private String authorName;
    private String message;
    private Timestamp timestamp;

    public Reply() {
        // Default constructor required for calls to DataSnapshot.getValue(Reply.class)
    }

    public Reply(String replyId, String authorId, String authorName, String message, Timestamp timestamp) {
        this.replyId = replyId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getReplyId() {
        return replyId;
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
