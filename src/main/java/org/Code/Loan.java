package org.Code;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Represents a loan record in the library system.
 * <p>
 * A {@code Loan} tracks:
 * <ul>
 *     <li>The borrowed media item (Book or CD)</li>
 *     <li>The user who borrowed it</li>
 *     <li>The borrow date and calculated due date</li>
 *     <li>Whether the item has been returned</li>
 *     <li>Overdue calculations and renewal status</li>
 * </ul>
 *
 * Each loan stores a fixed loan duration (in days), which determines the due date.
 * The class also provides helper methods for computing overdue days, loan fees,
 * and checking if a renewal has been requested.
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class Loan {
    private Media item;
    private User user;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;
    private int loanPeriodDays;

    public Loan(Media item, User user, LocalDate borrowDate, int loanPeriodDays) {
        this.item = item;
        this.user = user;
        this.borrowDate = borrowDate;
        this.loanPeriodDays = loanPeriodDays;
        this.dueDate = borrowDate.plusDays(loanPeriodDays);
        this.returned = false;
    }

    public Media getItem() {
        return item;
    }
    public Book getBook() {
        if (item instanceof Book) {
            return (Book) item;
        }
        return null;   // or throw exception if you prefer
    }
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

    public long daysOverdue(LocalDate currentDate) {
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, currentDate);
        return Math.max(daysOverdue, 0); // If not overdue, return 0
    }
    private boolean renewalRequested = false;

    public boolean isRenewalRequested() {
        return renewalRequested;
    }

    public void setRenewalRequested(boolean renewalRequested) {
        this.renewalRequested = renewalRequested;
    }


}
