package com.example.umeventplanner;

public class LeaderboardEntry {

    private String eventId;
    private String eventName;
    private String bannerUrl;
    private double greenScore;
    private double userRating;
    private double finalScore;
    private int rank;

    public LeaderboardEntry(String eventId, String eventName, String bannerUrl, double greenScore, double userRating, double finalScore) {
        this.eventId = eventId;
        this.eventName = eventName;
        this.bannerUrl = bannerUrl;
        this.greenScore = greenScore;
        this.userRating = userRating;
        this.finalScore = finalScore;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public double getGreenScore() {
        return greenScore;
    }

    public void setGreenScore(double greenScore) {
        this.greenScore = greenScore;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }
}
