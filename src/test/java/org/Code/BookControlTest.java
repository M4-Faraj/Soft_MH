package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookControlTest {

    private Books books;
    private MediaCollection mediaCollection;
    private BookControl bookControl;
    private Path borrowedPath;

    @BeforeEach
    void setUp() throws Exception {
        books = new Books();
        mediaCollection = new MediaCollection();
        bookControl = new BookControl(books, mediaCollection);

        // نظف ملف Borrowed_Books.txt قبل كل تست
        borrowedPath = Paths.get(FileControler.BORROWED_PATH).toAbsolutePath();
        if (Files.exists(borrowedPath)) {
            Files.delete(borrowedPath);
        }
        if (!Files.exists(borrowedPath.getParent())) {
            Files.createDirectories(borrowedPath.getParent());
        }
    }

    // ---------------------------------------------------------
    // numberOfBorrowedBooks()
    // ---------------------------------------------------------

    @Test
    void testNumberOfBorrowedBooks_FileDoesNotExist_ReturnsZero() throws Exception {
        // تأكد إن الملف مش موجود
        if (Files.exists(borrowedPath)) {
            Files.delete(borrowedPath);
        }

        int count = bookControl.numberOfBorrowedBooks();
        assertEquals(0, count);
    }

    @Test
    void testNumberOfBorrowedBooks_WithMultipleLines() throws Exception {
        String content =
                "111,Book A,2024-01-01,user1\n" +
                        "222,Book B,2024-01-02,user2\n" +
                        "333,Book C,2024-01-03,user3\n";

        Files.writeString(borrowedPath, content);

        int count = bookControl.numberOfBorrowedBooks();
        assertEquals(3, count);
    }

    // ---------------------------------------------------------
    // borrowMedia()
    // ---------------------------------------------------------

    @Test
    void testBorrowMedia_SuccessfulBorrowBook() {
        // User بدون غرامات أو تأخير
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        // Book متاح
        Book book = new Book("Clean Code", "Robert Martin", "111", false);
        mediaCollection.addItem(book);

        bookControl.borrowMedia(user, "Clean Code");

        List<Loan> loans = bookControl.getLoans();
        assertEquals(1, loans.size());

        Loan loan = loans.get(0);
        assertEquals(book, loan.getItem());
        assertEquals(user, loan.getUser());
        assertTrue(book.isBorrowed());
        assertEquals(LocalDate.now().plusDays(28), loan.getDueDate());
    }

    @Test
    void testBorrowMedia_ItemNotFound_DoesNotCreateLoan() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        // ما في أي كتب/ميديا في الكولكشن
        bookControl.borrowMedia(user, "Something");

        assertTrue(bookControl.getLoans().isEmpty());
    }

    @Test
    void testBorrowMedia_ItemAlreadyBorrowed_DoesNotCreateLoan() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        Book book = new Book("Clean Code", "Robert Martin", "111", true); // already borrowed
        mediaCollection.addItem(book);

        bookControl.borrowMedia(user, "Clean Code");

        assertTrue(bookControl.getLoans().isEmpty());
    }

    @Test
    void testBorrowMedia_UserHasOutstandingFineFromFile_Blocked() throws Exception {
        // نكتب سطر في Borrowed_Books.txt أقدم من 28 يوم لنفس اليوزر
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        LocalDate borrowDate = LocalDate.now().minusDays(29);
        String line = "111,Old Book," + borrowDate + "," + user.getUsername() + "\n";
        Files.writeString(borrowedPath, line);

        Book book = new Book("Clean Code", "Robert Martin", "111", false);
        mediaCollection.addItem(book);

        bookControl.borrowMedia(user, "Clean Code");

        // لازم ما ينضاف ولا Loan جديد
        assertTrue(bookControl.getLoans().isEmpty());
        // والكتاب لازم يظل مش مُستعار
        assertFalse(book.isBorrowed());
    }

    @Test
    void testBorrowMedia_UserHasOverdueLoanInMemory_Blocked() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        // كتاب جديد متاح
        Book book = new Book("Clean Code", "Robert Martin", "111", false);
        mediaCollection.addItem(book);

        // Loan قديم ومتأخر لنفس اليوزر
        LocalDate oldBorrow = LocalDate.now().minusDays(40);
        Loan oldLoan = new Loan(book, user, oldBorrow, 28);
        bookControl.getLoans().add(oldLoan);

        bookControl.borrowMedia(user, "Clean Code");

        // ما ينضافش Loan جديد
        assertEquals(1, bookControl.getLoans().size());
    }

    // ---------------------------------------------------------
    // getOverDueBooks()
    // ---------------------------------------------------------

    @Test
    void testGetOverDueBooks_ReturnsOnlyOverdueLoans() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        Book book1 = new Book("Book1", "A", "111", false);
        Book book2 = new Book("Book2", "B", "222", false);

        // Loan 1: متأخر
        LocalDate borrow1 = LocalDate.now().minusDays(40);
        Loan overdue = new Loan(book1, user, borrow1, 28); // due قبل 12 يوم

        // Loan 2: غير متأخر
        LocalDate borrow2 = LocalDate.now().minusDays(10);
        Loan notOverdue = new Loan(book2, user, borrow2, 28);

        bookControl.getLoans().add(overdue);
        bookControl.getLoans().add(notOverdue);

        List<Loan> result = bookControl.getOverDueBooks(LocalDate.now());
        assertEquals(1, result.size());
        assertTrue(result.contains(overdue));
        assertFalse(result.contains(notOverdue));
    }

    // ---------------------------------------------------------
    // payFine()
    // ---------------------------------------------------------

    @Test
    void testPayFine_ReducesFineOnUser() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");
        user.addFine(50);

        bookControl.payFine(user, 20);

        assertEquals(30, user.getFine(), 0.0001);
    }

    @Test
    void testPayFine_CannotGoBelowZero() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");
        user.addFine(10);

        bookControl.payFine(user, 20);

        assertEquals(0, user.getFine(), 0.0001);
    }

    // ---------------------------------------------------------
    // unregisterUser()
    // ---------------------------------------------------------

    @Test
    void testUnregisterUser_NonAdminCannotUnregister() {
        User admin = new User("Admin", "X", "admin", "a@mail.com", "pass");
        admin.setAdmin(false); // مش أدمِن

        User target = new User("User", "Y", "u1", "u1@mail.com", "pass");

        Users users = new Users();
        users.addUser(target);
        bookControl.setUsers(users);

        bookControl.unregisterUser(admin, target);

        assertEquals(1, users.getAllUsers().size());
        assertTrue(users.getAllUsers().contains(target));
    }

    @Test
    void testUnregisterUser_AdminButUserHasFine_CannotUnregister() {
        User admin = new User("Admin", "X", "admin", "a@mail.com", "pass");
        admin.setAdmin(true);

        User target = new User("User", "Y", "u1", "u1@mail.com", "pass");
        target.addFine(30);

        Users users = new Users();
        users.addUser(target);
        bookControl.setUsers(users);

        bookControl.unregisterUser(admin, target);

        assertTrue(users.getAllUsers().contains(target));
    }

    @Test
    void testUnregisterUser_AdminButUserHasActiveLoan_CannotUnregister() {
        User admin = new User("Admin", "X", "admin", "a@mail.com", "pass");
        admin.setAdmin(true);

        User target = new User("User", "Y", "u1", "u1@mail.com", "pass");

        Users users = new Users();
        users.addUser(target);
        bookControl.setUsers(users);

        // Loan غير مُعاد لنفس اليوزر
        Book book = new Book("Book1", "A", "111", true);
        Loan loan = new Loan(book, target, LocalDate.now().minusDays(5), 28);
        bookControl.getLoans().add(loan);

        bookControl.unregisterUser(admin, target);

        assertTrue(users.getAllUsers().contains(target));
    }

    @Test
    void testUnregisterUser_AdminNoFineNoLoans_UserRemoved() {
        User admin = new User("Admin", "X", "admin", "a@mail.com", "pass");
        admin.setAdmin(true);

        User target = new User("User", "Y", "u1", "u1@mail.com", "pass");

        Users users = new Users();
        users.addUser(target);
        bookControl.setUsers(users);

        // لا غرامات، لا Loans نشطة
        bookControl.unregisterUser(admin, target);

        assertFalse(users.getAllUsers().contains(target));
    }

    // ---------------------------------------------------------
    // calculateOverdueFine()
    // ---------------------------------------------------------

    @Test
    void testCalculateOverdueFine_MixedMedia() {
        User user = new User("First", "Last", "user1", "u1@mail.com", "pass");

        LocalDate today = LocalDate.now();

        // Book: متأخر 3 أيام (غرامة 10 NIS / يوم)
        Book book = new Book("Book1", "A", "111", true);
        LocalDate borrowBook = today.minusDays(31); // due = today - 3
        Loan loanBook = new Loan(book, user, borrowBook, 28);

        // CD: متأخر 2 يوم (غرامة 20 NIS / يوم)
        CD cd = new CD("Album1", "Artist", true);
        LocalDate borrowCd = today.minusDays(9); // due = today - 2
        Loan loanCd = new Loan(cd, user, borrowCd, 7);

        // Loan غير متأخر
        Book okBook = new Book("Book2", "B", "222", true);
        LocalDate borrowOk = today.minusDays(5); // not overdue
        Loan loanOk = new Loan(okBook, user, borrowOk, 28);

        bookControl.getLoans().add(loanBook);
        bookControl.getLoans().add(loanCd);
        bookControl.getLoans().add(loanOk);

        double fine = bookControl.calculateOverdueFine(user, today);

        // Book: 3 * 10 = 30
        // CD:   2 * 20 = 40
        // Total = 70
        assertEquals(70.0, fine, 0.0001);
    }

    // ---------------------------------------------------------
    // getAllOverdueMedia()
    // ---------------------------------------------------------

    @Test
    void testGetAllOverdueMedia_FiltersByUserAndOverdue() {
        User user1 = new User("First", "Last", "user1", "u1@mail.com", "pass");
        User user2 = new User("Other", "User", "user2", "u2@mail.com", "pass");

        LocalDate today = LocalDate.now();

        Book book1 = new Book("Book1", "A", "111", true);
        Book book2 = new Book("Book2", "B", "222", true);

        // user1 overdue
        Loan loan1 = new Loan(book1, user1, today.minusDays(40), 28);

        // user1 not overdue
        Loan loan2 = new Loan(book2, user1, today.minusDays(10), 28);

        // user2 overdue
        Loan loan3 = new Loan(book1, user2, today.minusDays(40), 28);

        bookControl.getLoans().add(loan1);
        bookControl.getLoans().add(loan2);
        bookControl.getLoans().add(loan3);

        List<Loan> result = bookControl.getAllOverdueMedia(user1, today);

        assertEquals(1, result.size());
        assertTrue(result.contains(loan1));
        assertFalse(result.contains(loan2));
        assertFalse(result.contains(loan3));
    }
}
