package org.Code;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    // ---------------------------------------------------------
    // Constructor & basic getters
    // ---------------------------------------------------------

    @Test
    void testConstructorAndGetters() {
        Book book = new Book("Clean Code", "Robert Martin", "111", false);
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        LocalDate borrowDate = LocalDate.now().minusDays(3);
        int loanDays = 28;

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertSame(book, loan.getItem());
        assertSame(user, loan.getUser());
        assertEquals(borrowDate, loan.getBorrowDate());
        assertEquals(borrowDate, loan.getStartDate());   // alias
        assertEquals(borrowDate.plusDays(loanDays), loan.getDueDate());
        assertFalse(loan.isReturned());
    }

    @Test
    void testSetReturned() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");
        Loan loan = new Loan(book, user, LocalDate.now(), 7);

        assertFalse(loan.isReturned());

        loan.setReturned(true);
        assertTrue(loan.isReturned());

        loan.setReturned(false);
        assertFalse(loan.isReturned());
    }

    // ---------------------------------------------------------
    // isOverdue()
    // ---------------------------------------------------------

    @Test
    void testIsOverdue_FalseBeforeDueDate() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        LocalDate borrowDate = today.minusDays(5);
        int loanDays = 10; // due = today + 5

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertFalse(loan.isOverdue());
    }

    @Test
    void testIsOverdue_TrueAfterDueDate() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        LocalDate borrowDate = today.minusDays(15);
        int loanDays = 10; // due = today - 5

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertTrue(loan.isOverdue());
    }

    // ---------------------------------------------------------
    // getDaysOverdue() (uses LocalDate.now() inside)
    // ---------------------------------------------------------

    @Test
    void testGetDaysOverdue_ZeroWhenNotOverdue() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        LocalDate borrowDate = today.minusDays(3);
        int loanDays = 10; // due = today + 7

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertFalse(loan.isOverdue());
        assertEquals(0, loan.getDaysOverdue());
    }

    @Test
    void testGetDaysOverdue_PositiveWhenOverdue() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        // 10 days ago, period 5 -> due 5 days ago
        LocalDate borrowDate = today.minusDays(10);
        int loanDays = 5;

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertTrue(loan.isOverdue());
        assertEquals(5, loan.getDaysOverdue());
    }

    // ---------------------------------------------------------
    // daysOverdue(LocalDate currentDate)
    // ---------------------------------------------------------

    @Test
    void testDaysOverdue_ZeroBeforeDueDate() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate borrowDate = LocalDate.of(2025, 1, 1);
        int loanDays = 10; // due = 2025-01-11
        Loan loan = new Loan(book, user, borrowDate, loanDays);

        LocalDate current = LocalDate.of(2025, 1, 10); // one day before due
        assertEquals(0, loan.daysOverdue(current));
    }

    @Test
    void testDaysOverdue_ZeroExactlyOnDueDate() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate borrowDate = LocalDate.of(2025, 1, 1);
        int loanDays = 10; // due = 2025-01-11
        Loan loan = new Loan(book, user, borrowDate, loanDays);

        LocalDate current = LocalDate.of(2025, 1, 11);
        assertEquals(0, loan.daysOverdue(current));
    }

    @Test
    void testDaysOverdue_PositiveAfterDueDate() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate borrowDate = LocalDate.of(2025, 1, 1);
        int loanDays = 10; // due = 2025-01-11
        Loan loan = new Loan(book, user, borrowDate, loanDays);

        LocalDate current = LocalDate.of(2025, 1, 15); // 4 days late
        assertEquals(4, loan.daysOverdue(current));
    }

    // ---------------------------------------------------------
    // getLoanFee()  (10 NIS if totalDays > 28)
    // ---------------------------------------------------------

    @Test
    void testGetLoanFee_ZeroWhenTotalDaysLessOrEqual28() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();

        // exactly 28 days ago
        Loan loanExact = new Loan(book, user, today.minusDays(28), 28);
        assertEquals(0.0, loanExact.getLoanFee(), 0.0001);

        // 10 days ago
        Loan loanShort = new Loan(book, user, today.minusDays(10), 28);
        assertEquals(0.0, loanShort.getLoanFee(), 0.0001);
    }

    @Test
    void testGetLoanFee_10WhenTotalDaysMoreThan28() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        // 29 days ago
        Loan loan = new Loan(book, user, today.minusDays(29), 28);

        assertEquals(10.0, loan.getLoanFee(), 0.0001);
    }

    // ---------------------------------------------------------
    // renew()
    // ---------------------------------------------------------

    @Test
    void testRenew_UpdatesBorrowAndDueDates() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate oldBorrowDate = LocalDate.now().minusDays(20);
        int loanDays = 28;

        Loan loan = new Loan(book, user, oldBorrowDate, loanDays);

        LocalDate beforeRenew = LocalDate.now();
        loan.renew();
        LocalDate afterRenewBorrowDate = loan.getBorrowDate();

        // borrowDate should now be "today" (بين before و now تقريباً نفس اليوم)
        assertFalse(afterRenewBorrowDate.isBefore(beforeRenew));
        assertEquals(afterRenewBorrowDate.plusDays(loanDays), loan.getDueDate());
    }

    @Test
    void testRenew_ClearsOverdueStatus() {
        Book book = new Book("Book", "Author", "111", false);
        User user = new User("F", "L", "u", "u@mail.com", "p");

        LocalDate today = LocalDate.now();
        LocalDate borrowDate = today.minusDays(40);
        int loanDays = 28;

        Loan loan = new Loan(book, user, borrowDate, loanDays);

        assertTrue(loan.isOverdue());

        loan.renew();

        assertFalse(loan.isOverdue());
    }
}
