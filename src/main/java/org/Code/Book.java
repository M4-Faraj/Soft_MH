package org.Code;

import java.time.LocalDate;

public class Book extends Media {
    private String name;
    private String author;
    private String ISBN;
    private LocalDate overDue;

    public Book(String name, String author, String ISBN, boolean borrowed) {
        super(name, borrowed);  // inherited borrowed flag
        this.name = name;
        this.author = author;
        this.ISBN = ISBN;
    }

    // ---------------------- Update Methods ----------------------

    public void updateName(String name){
        this.name = name;
    }

    public void updateAuthor(String author){
        this.author = author;
    }

    public void updateISBN(String ISBN){
        this.ISBN = ISBN;
    }

    @Override
    public void updateBorrowed(boolean borrowed){
        super.updateBorrowed(borrowed);
    }

    // ---------------------- Setters ----------------------

    public void setAuthor(String author) { this.author = author; }

    public void setISBN(String ISBN) { this.ISBN = ISBN; }

    public void setName(String name) { this.name = name; }

    // ---------------------- Getters ----------------------

    public String getName(){ return name; }

    public String getAuthor(){ return author; }

    public String getISBN(){ return ISBN; }

    // ---------------------- Legacy Support ----------------------

    // ⚠ Used by your UI → keep for compatibility
    public boolean isborrowed() {
        return isBorrowed(); // map old name → new logic
    }

    // ---------------------- Borrowing Rules ----------------------

    @Override
    public int getBorrowDurationDays() { return 28; }

    @Override
    public int getLoanDuration() { return 28; }

    @Override
    public int getOverdueFine() { return 10; }

    @Override
    public double getFinePerDay() { return 10; }
}
