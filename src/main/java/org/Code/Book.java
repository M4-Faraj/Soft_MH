package org.Code;

import java.time.LocalDate;
/**
 * Represents a book or CD item in the library system.
 * A book extends the {@link Media} class and adds bibliographic information
 * such as name, author, ISBN, and category. It also defines borrowing rules
 * specific to books (28-day loan duration, fixed overdue fine).
 *
 * This class supports multiple constructors to accommodate different data-loading
 * formats used throughout the system and maintains backward compatibility
 * with legacy UI components.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class Book extends Media {
    private String name;
    private String author;
    private String ISBN;
    private LocalDate overDue;
    private String category; // "Book" أو "CD"

    public Book(String name, String author, String ISBN, boolean borrowed) {
        super(name, borrowed);  // inherited borrowed flag
        this.name = name;
        this.author = author;
        this.ISBN = ISBN;
    }
    public Book(String name, String author, String ISBN,
                boolean borrowed, String mediaType, String category) {
        super(name, borrowed);
        this.author = author;
        this.ISBN = ISBN;
        this.mediaType = (mediaType == null || mediaType.isBlank())
                ? "BOOK"
                : mediaType.toUpperCase();
        this.category = (category == null || category.isBlank())
                ? "Other"
                : category;
    }
    public Book(String name, String author, String ISBN, boolean borrowed, String category) {
        super(name,borrowed);
        this.author = author;
        this.ISBN = ISBN;
        this.category = (category == null || category.isBlank()) ? "Book" : category;
    }
    private String mediaType;  // "BOOK" or "CD"

    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

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
    @Override
    public boolean isBorrowed() {
        return super.isBorrowed(); // map old name → new logic
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
