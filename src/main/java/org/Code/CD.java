package org.Code;

public class CD extends Media {
    private String artist;

    public CD(String title, String artist, boolean borrowed) {
        super(title, borrowed);
        this.artist = artist;
    }

    @Override
    public int getBorrowDurationDays() {
        return 7;   // requirement
    }

    public String getArtist() { return artist; }
    @Override
    public int getLoanDuration() {
        return 7; // CDs are borrowed for 7 days
    }
    @Override
    public int getOverdueFine() {
        return 20; // 20 NIS per overdue CD
    }
    @Override
    public double getFinePerDay() { return 20; }
}
