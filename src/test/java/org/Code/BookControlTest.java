package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BookControlTest {
    private BookControl control;
    private Books books;
    private User user;
    private User adminUser;
    private Users allUsers;

    @BeforeEach
    void setup() {
        books = new Books();
        books.availableBooks.add(new Book("Harry Potter", "J.K. Rowling", "123", false));
        books.availableBooks.add(new Book("The Hobbit", "Tolkien", "456", false));

        control = new BookControl(books);

        user = new User("John", "Doe", "johndoe", "john@example.com", "password");
        adminUser = new User("Admin", "User", "admin", "admin@example.com", "adminpass");
        adminUser.setAdmin(true);

        allUsers = new Users();
        allUsers.addUser(user);
        allUsers.addUser(adminUser);
        control.setUsers(allUsers);

    }

    @Test
    void testBorrowBookWithFine() {
        user.addFine(50);
        control.borrowBook(user, "Harry Potter");

        // Book should not be borrowed
        Book book = books.searchBook("Harry Potter");
        assertFalse(book.isborrowed(), "Book should not be borrowed if user has a fine");
    }

    @Test
    void testBorrowBookWithOverdueBooks() {
        Book book = books.searchBook("Harry Potter");
        Loan overdueLoan = new Loan(book, user, LocalDate.now().minusDays(30), 28);
        control.getLoans().add(overdueLoan); // manually add overdue loan

        control.borrowBook(user, "The Hobbit");

        Book hobbit = books.searchBook("The Hobbit");
        assertFalse(hobbit.isborrowed(), "User with overdue books cannot borrow another book");
    }

    @Test
    void testBorrowBookNotFound() {
        control.borrowBook(user, "Nonexistent Book");
        // No exception, nothing borrowed
        assertTrue(books.availableBooks.stream().allMatch(b -> !b.isborrowed()));
    }

    @Test
    void testBorrowBookAlreadyBorrowed() {
        Book book = books.searchBook("Harry Potter");
        book.updateBorrowed(true); // already borrowed

        control.borrowBook(user, "Harry Potter");
        assertTrue(book.isborrowed(), "Book remains borrowed, cannot borrow again");
    }

    @Test
    void testSuccessfulBorrow() {
        control.borrowBook(user, "Harry Potter");
        Book book = books.searchBook("Harry Potter");

        assertTrue(book.isborrowed(), "Book should be marked as borrowed");
        assertEquals(1, control.getLoans().size(), "Loan should be added to loans list");
    }
    //getOverDueBooks
    @Test
    void testGetOverDueBooks_NoLoans() {
        // No books have been borrowed yet
        List<Loan> overdue = control.getOverDueBooks(LocalDate.now());
        assertTrue(overdue.isEmpty(), "Expected no overdue books when none are borrowed");
    }

    @Test
    void testGetOverDueBooks_WithOverdueAndNotOverdue() throws Exception {
        // Borrow a book in the past to simulate overdue
        Book overdueBook = books.searchBook("Harry Potter");
        Loan overdueLoan = new Loan(overdueBook, new User(), LocalDate.now().minusDays(30), 28);

        // Borrow a book today → not overdue
        Book notOverdueBook = books.searchBook("The Hobbit");
        Loan notOverdueLoan = new Loan(notOverdueBook, new User(), LocalDate.now(), 28);

        // Add loans using reflection since loans is private
        java.lang.reflect.Field loansField = BookControl.class.getDeclaredField("loans");
        loansField.setAccessible(true);
        List<Loan> loans = (List<Loan>) loansField.get(control);
        loans.add(overdueLoan);
        loans.add(notOverdueLoan);

        // Test getOverDueBooks
        List<Loan> overdue = control.getOverDueBooks(LocalDate.now());
        assertEquals(1, overdue.size(), "Expected only one overdue book");
        assertEquals(overdueBook, overdue.getFirst().getBook(), "The overdue book should be Harry Potter");
    }


    @Test
    void testPayFine_ReduceFineAmount() {
        // Arrange
        user.addFine(50.0); // user owes 50
        assertEquals(50.0, user.getFine());

        // Act
        control.payFine(user, 20.0);

        // Assert
        assertEquals(30.0, user.getFine(), "Fine should be reduced by 20");
    }

    @Test
    void testPayFine_ExactPayment() {
        user.addFine(25.0);

        control.payFine(user, 25.0);

        assertEquals(0.0, user.getFine(), "Fine should be fully paid");
    }

    @Test
    void testPayFine_OverPayment() {
        user.addFine(10.0);

        control.payFine(user, 20.0);

        assertEquals(0.0, user.getFine(), "Fine should not be negative");
    }

    @Test
    void testPayFine_NegativeOrZeroPayment() {
        user.addFine(30.0);

        control.payFine(user, -5.0); // invalid
        assertEquals(30.0, user.getFine(), "Fine should remain unchanged for negative payment");

        control.payFine(user, 0.0); // zero
        assertEquals(30.0, user.getFine(), "Fine should remain unchanged for zero payment");
    }



    @Test
    void testUnregisterUser_NotAdmin() {
        control.unregisterUser(user, user); // user tries to unregister themselves
        assertTrue(allUsers.getAllUsers().contains(user), "Non-admin cannot unregister user");
    }

    @Test
    void testUnregisterUser_UserHasFines() {
        user.addFine(10.0);

        control.unregisterUser(adminUser, user);
        assertTrue(allUsers.getAllUsers().contains(user), "User with fines cannot be unregistered");
    }

    @Test
    void testUnregisterUser_UserHasActiveLoans() {
        Book book = books.searchBook("Harry Potter");
        Loan loan = new Loan(book, user, LocalDate.now(), 28);
        control.getLoans().add(loan); // assuming getter for loans

        control.unregisterUser(adminUser, user);
        assertTrue(allUsers.getAllUsers().contains(user), "User with active loans cannot be unregistered");
    }

    @Test
    void testUnregisterUser_Success() {
        control.unregisterUser(adminUser, user);
        assertFalse(allUsers.getAllUsers().contains(user), "User should be successfully unregistered");
    }
}
