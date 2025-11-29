package org.Code;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
    public LocalDate getStartDate() { return borrowDate; }

    // 🔹 NEW: احسب رسوم الـ loan
    // إذا مدة الإعارة (من start لحد due) أكبر من 28 يوم → 10 شيكل، غير هيك 0
    public double getLoanFee() {
        long days = ChronoUnit.DAYS.between(borrowDate, dueDate);
        return days > 28 ? 10.0 : 0.0;
    }
}
