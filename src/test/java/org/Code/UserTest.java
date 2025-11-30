package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Path borrowedPath;

    @BeforeEach
    void setUp() throws Exception {
        borrowedPath = Paths.get(FileControler.BORROWED_PATH).toAbsolutePath();

        if (!Files.exists(borrowedPath.getParent())) {
            Files.createDirectories(borrowedPath.getParent());
        }

        Files.writeString(
                borrowedPath,
                "",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // ---------------------------------------------------------
    // Constructors & basic fields
    // ---------------------------------------------------------

    @Test
    void testFullConstructor_SetsAllFields() {
        String[] books = {"Book1", "Book2"};
        User u = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass123", books);

        assertEquals("John", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals("jdoe", u.getUsername());
        assertEquals("jdoe@mail.com", u.getEmail());
        assertEquals("pass123", u.getPassword());

        assertArrayEquals(books, u.getBooks());
        assertFalse(u.isAdmin());
        assertFalse(u.isLibrarian());
        assertEquals(0.0, u.getFine(), 0.0001);
    }

    @Test
    void testConstructorWithoutBooks_CreatesEmptyBooksArray() {
        User u = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pwd");

        assertEquals("Jane", u.getFirstName());
        assertEquals("Smith", u.getLastName());
        assertEquals("jsmith", u.getUsername());
        assertEquals("jsmith@mail.com", u.getEmail());
        assertEquals("pwd", u.getPassword());

        assertNotNull(u.getBooks());
        assertEquals(0, u.getBooks().length);
    }

    @Test
    void testEmptyConstructor_InitialValues() {
        User u = new User();

        assertNotNull(u.getFirstName());
        assertNotNull(u.getLastName());
        assertNotNull(u.getUsername());
        assertNotNull(u.getEmail());
        assertNotNull(u.getPassword());
        assertNotNull(u.getBooks());

        assertEquals(0, u.getBooks().length);
        assertFalse(u.isAdmin());
        assertFalse(u.isLibrarian());
        assertEquals(0.0, u.getFine(), 0.0001);
    }

    // ---------------------------------------------------------
    // Setters / Getters
    // ---------------------------------------------------------

    @Test
    void testSetters_UpdateFieldsCorrectly() {
        User u = new User();

        u.setFirstName("Ali");
        u.setLastName("Ahmad");
        u.setUsername("ali123");
        u.setEmail("ali@example.com");
        u.setPassword("secret");
        u.setBooks(new String[]{"B1", "B2"});

        assertEquals("Ali", u.getFirstName());
        assertEquals("Ahmad", u.getLastName());
        assertEquals("ali123", u.getUsername());
        assertEquals("ali@example.com", u.getEmail());
        assertEquals("secret", u.getPassword());
        assertArrayEquals(new String[]{"B1", "B2"}, u.getBooks());
    }

    @Test
    void testSetBooks_WithNullCreatesEmptyArray() {
        User u = new User();
        u.setBooks(null);

        assertNotNull(u.getBooks());
        assertEquals(0, u.getBooks().length);
    }

    // ---------------------------------------------------------
    // Fine handling: addFine / payFine
    // ---------------------------------------------------------

    @Test
    void testAddFine_AccumulatesFine() {
        User u = new User();
        assertEquals(0.0, u.getFine(), 0.0001);

        u.addFine(10);
        assertEquals(10.0, u.getFine(), 0.0001);

        u.addFine(5.5);
        assertEquals(15.5, u.getFine(), 0.0001);
    }

    @Test
    void testPayFine_ReducesFineButNotBelowZero() {
        User u = new User();
        u.addFine(20);

        u.payFine(5);
        assertEquals(15.0, u.getFine(), 0.0001);

        // paying more than remaining should not go below zero
        u.payFine(100);
        assertEquals(0.0, u.getFine(), 0.0001);
    }

    @Test
    void testPayFine_InvalidAmountDoesNotChangeFine() {
        User u = new User();
        u.addFine(10);

        u.payFine(0);
        assertEquals(10.0, u.getFine(), 0.0001);

        u.payFine(-5);
        assertEquals(10.0, u.getFine(), 0.0001);
    }

    // ---------------------------------------------------------
    // Admin / Librarian flags
    // ---------------------------------------------------------

    @Test
    void testAdminFlag() {
        User u = new User();
        assertFalse(u.isAdmin());

        u.setAdmin(true);
        assertTrue(u.isAdmin());

        u.setAdmin(false);
        assertFalse(u.isAdmin());
    }

    @Test
    void testLibrarianFlag() {
        User u = new User();
        assertFalse(u.isLibrarian());

        u.setLibrarian(true);
        assertTrue(u.isLibrarian());

        u.setLibrarian(false);
        assertFalse(u.isLibrarian());
    }

    // ---------------------------------------------------------
    // hasOutstandingFine() → uses FileControler.hasOverdueBooks(username)
    // ---------------------------------------------------------

    @Test
    void testHasOutstandingFine_TrueWhenUserHasOverdueBorrowedBook() throws Exception {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass");

        // overdue borrow record: more than 28 days ago
        LocalDate borrowDate = LocalDate.now().minusDays(35);
        String line = "111,Some Book," + borrowDate + "," + u.getUsername() + "\n";

        Files.writeString(
                borrowedPath,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        assertTrue(u.hasOutstandingFine());
    }

    @Test
    void testHasOutstandingFine_FalseWhenNoBorrowedOrNotOverdue() throws Exception {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass");

        // within 28 days → NOT overdue
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        String line = "111,Some Book," + borrowDate + "," + u.getUsername() + "\n";

        Files.writeString(
                borrowedPath,
                line,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        assertFalse(u.hasOutstandingFine());

        // test with empty file
        Files.writeString(
                borrowedPath,
                "",
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
        );
        assertFalse(u.hasOutstandingFine());
    }

    // ---------------------------------------------------------
    // applyFineForLoan()
    // ---------------------------------------------------------

    @Test
    void testApplyFineForLoan_AddsFineWhenLoanIsOverdue_Book() {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass");

        Book book = new Book("Book A", "Author", "111", true);
        // loaned 35 days ago for 28 days → overdue 7 days
        LocalDate borrowDate = LocalDate.now().minusDays(35);
        Loan loan = new Loan(book, u, borrowDate, 28);

        u.applyFineForLoan(loan, LocalDate.now());

        // Book fine per day = getOverdueFine() = 10
        // 7 days * 10 = 70
        assertEquals(70.0, u.getFine(), 0.0001);
    }

    @Test
    void testApplyFineForLoan_AddsFineWhenLoanIsOverdue_CD() {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass");

        CD cd = new CD("Album", "Artist", true);
        // loaned 12 days ago for 7 days → overdue 5 days
        LocalDate borrowDate = LocalDate.now().minusDays(12);
        Loan loan = new Loan(cd, u, borrowDate, 7);

        u.applyFineForLoan(loan, LocalDate.now());

        // CD fine per day = getOverdueFine() = 20
        // 5 days * 20 = 100
        assertEquals(100.0, u.getFine(), 0.0001);
    }

    @Test
    void testApplyFineForLoan_NoFineIfNotOverdue() {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass");

        Book book = new Book("Book A", "Author", "111", true);
        // loaned 5 days ago for 28 days → NOT overdue
        LocalDate borrowDate = LocalDate.now().minusDays(5);
        Loan loan = new Loan(book, u, borrowDate, 28);

        u.applyFineForLoan(loan, LocalDate.now());

        assertEquals(0.0, u.getFine(), 0.0001);
    }

    // ---------------------------------------------------------
    // allowedBooks()
    // ---------------------------------------------------------

    @Test
    void testAllowedBooks_BasedOnBooksArrayLength() {
        User u = new User("First", "Last", "user1", "u1@mail.com", "pass",
                new String[0]);
        assertFalse(u.allowedBooks()); // length = 0

        u.setBooks(new String[]{"B1"});
        assertFalse(u.allowedBooks()); // length = 1

        u.setBooks(new String[]{"B1", "B2"});
        assertFalse(u.allowedBooks()); // length = 2

        u.setBooks(new String[]{"B1", "B2", "B3"});
        assertTrue(u.allowedBooks());  // length > 2 (per current logic)
    }

    @Test
    void testAllowedBooks_WithNullBooksReturnsFalse() {
        User u = new User();
        u.setBooks(null); // will internally become empty array

        assertFalse(u.allowedBooks());
    }
}
