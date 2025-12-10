package com.example.umeventplanner.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String authorId;
    private String authorName;
    private String message;
    private Timestamp timestamp;

    public Comment() {
        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
    }

    public Comment(String commentId, String authorId, String authorName, String message, Timestamp timestamp) {
        this.commentId = commentId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getCommentId() {
        return commentId;
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
