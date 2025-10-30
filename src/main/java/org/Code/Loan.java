package org.Code;

import java.time.LocalDate;

public class Loan {
    private Book book;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public Loan(Book book, User user, LocalDate borrowDate, int loanPeriodDays) {
        this.book = book;
        this.user = user;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(loanPeriodDays);
        this.returned = false;
    }

    public Book getBook() { return book; }
    public User getUser() { return user; }
    public LocalDate getDueDate() { return dueDate; }
    public boolean isReturned() { return returned; }
    public void setReturned(boolean returned) { this.returned = returned; }
}