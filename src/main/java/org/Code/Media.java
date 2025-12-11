package org.Code;

/**
 * Represents a generic media item in the library system.
 * <p>
 * This abstract class provides the basic properties shared by all media types
 * (e.g., books, CDs), including title and borrowed status. Concrete subclasses
 * must define borrowing rules such as loan duration and overdue fines.
 *
 * <p>Subclasses include:
 * <ul>
 *     <li>{@link Book}</li>
 *     <li>{@link CD}</li>
 * </ul>
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public abstract class Media {
    private String title;
    private boolean borrowed;

    public Media(String title, boolean borrowed) {
        this.title = title;
        this.borrowed = borrowed;
    }

    public String getTitle() { return title; }
    public boolean isBorrowed() { return borrowed; }
    public void setBorrowed(boolean b) { borrowed = b; }
    public void updateBorrowed(boolean status) {
        borrowed = status;
    }
    public abstract int getBorrowDurationDays();
    public abstract int getLoanDuration();
    public abstract int getOverdueFine();
    public abstract double getFinePerDay();

}

