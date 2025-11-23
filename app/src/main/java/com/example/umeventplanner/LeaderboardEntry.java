package com.example.umeventplanner;

public class LeaderboardEntry {

    private String eventId;
    private String eventName;
    private double score;
    private int rank;

    public LeaderboardEntry(String eventId, String eventName, double score) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.score = score;
    }

    // Getters
    public String getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public double getScore() {
        return score;
    }

    public int getRank() {
        return rank;
    }

    // Setters
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
