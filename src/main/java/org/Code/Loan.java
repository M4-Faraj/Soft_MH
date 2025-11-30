package org.Code;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Loan {
    private Book book;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;
    private int loanPeriodDays;

    public Loan(Book book, User user, LocalDate borrowDate, int loanPeriodDays) {
        this.book = book;
        this.user = user;
        this.borrowDate = borrowDate;
        this.loanPeriodDays = loanPeriodDays;
        this.dueDate = borrowDate.plusDays(loanPeriodDays);
        this.returned = false;
    }

    public Book getBook() { return book; }
    public User getUser() { return user; }

    public LocalDate getStartDate() {   // عشان الكود القديم
        return borrowDate;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void setReturned(boolean returned) {
        this.returned = returned;
    }

    // هل متأخر؟
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate);
    }

    public long getDaysOverdue() {
        if (!isOverdue()) return 0;
        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
    }

    // حسب شرطك: إذا المدة الفعلية من تاريخ الاستعارة > 28 يوم → 10 شيكل
    public double getLoanFee() {
        long totalDays = ChronoUnit.DAYS.between(borrowDate, LocalDate.now());
        return (totalDays > 28) ? 10.0 : 0.0;
    }

    // تجديد: نعيد تاريخ الاستعارة لليوم
    public void renew() {
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(loanPeriodDays);
    }
}
