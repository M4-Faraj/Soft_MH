package org.Code;

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

}

