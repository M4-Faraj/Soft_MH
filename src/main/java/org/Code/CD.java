package org.Code;
/**
 * Represents a CD (Compact Disc) in the library system.
 * <p>
 * A CD is a type of {@link Media} that has a shorter borrowing duration
 * and higher overdue fines compared to books. This class stores CD-specific
 * attributes such as the artist name and implements borrowing rules defined
 * by the system.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */

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
