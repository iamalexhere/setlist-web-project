package com.tubes.setlist.admin.model;

public class Statistics {
    private int totalUsers;
    private int totalArtists;
    private int totalSetlists;
    private double userGrowth;
    private double artistGrowth;
    private double setlistGrowth;

    // Getters and Setters
    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalArtists() {
        return totalArtists;
    }

    public void setTotalArtists(int totalArtists) {
        this.totalArtists = totalArtists;
    }

    public int getTotalSetlists() {
        return totalSetlists;
    }

    public void setTotalSetlists(int totalSetlists) {
        this.totalSetlists = totalSetlists;
    }

    public double getUserGrowth() {
        return userGrowth;
    }

    public void setUserGrowth(double userGrowth) {
        this.userGrowth = userGrowth;
    }

    public double getArtistGrowth() {
        return artistGrowth;
    }

    public void setArtistGrowth(double artistGrowth) {
        this.artistGrowth = artistGrowth;
    }

    public double getSetlistGrowth() {
        return setlistGrowth;
    }

    public void setSetlistGrowth(double setlistGrowth) {
        this.setlistGrowth = setlistGrowth;
    }
}
