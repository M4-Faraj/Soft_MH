package org.Code;

import java.time.LocalDate;

public class Book extends Media {
     private String name;
     private String author;
    private String ISBN;
    private boolean borrowed;
    private LocalDate overDue;

    public Book(String Name, String author, String ISBN, boolean borrowed) {
        super(Name , borrowed);
        this.author = author;
        this.ISBN = ISBN;
    }
    public void updateName(String Name){
        this.name=Name;
    }
    public void updateAuthor(String author){
        this.author=author;
    }
    public void updateISBN(String ISBN){
        this.ISBN=ISBN;
    }
    public void updateBorrowed(boolean borrowed){
        this.borrowed=borrowed;
    }

    public boolean isBorrowed() {
        return borrowed;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
 public void setISBN(String ISBN) {
        this.ISBN = ISBN;
 }
 public void setborrowed(boolean borrowed) {
        this.borrowed=borrowed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return name;
    }
    public String getAuthor(){
        return author;
    }
    public String getISBN(){
        return ISBN;
    }
    public boolean isborrowed(){
        return borrowed;
    }

    @Override
    public int getBorrowDurationDays() {
        return 28;
    }

    @Override
    public int getLoanDuration() {
        return 28; // Books are borrowed for 28 days
    }
    @Override
    public int getOverdueFine() {
        return 10; // 10 NIS per overdue book
    }
}
