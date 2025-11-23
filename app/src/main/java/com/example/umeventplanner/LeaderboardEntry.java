package com.example.umeventplanner;

public class LeaderboardEntry {

    private String organizerName;
    private String organizerId;
    private double totalScore;
    private int eventCount;
    private int rank;

    public LeaderboardEntry(String organizerName, String organizerId, double totalScore, int eventCount) {
        this.organizerName = organizerName;
        this.organizerId = organizerId;
        this.totalScore = totalScore;
        this.eventCount = eventCount;
    }

    // Getters
    public String getOrganizerName() {
        return organizerName;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public int getEventCount() {
        return eventCount;
    }

    public int getRank() {
        return rank;
    }

    // Setters
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public void setTotalScore(double totalScore) {
        this.totalScore = totalScore;
    }

    public void setEventCount(int eventCount) {
        this.eventCount = eventCount;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
