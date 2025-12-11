package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class FileControlerTest {

    private final Path baseDir       = Paths.get("src/main/InfoBase");
    private final Path booksPath     = Paths.get(FileControler.BOOKS_PATH);
    private final Path usersPath     = Paths.get(FileControler.USERS_PATH);
    private final Path borrowedPath  = Paths.get(FileControler.BORROWED_PATH);
    private final Path librarianPath = Paths.get(FileControler.LIBRARIANS_PATH);

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(baseDir);

        deleteIfExists(booksPath);
        deleteIfExists(usersPath);
        deleteIfExists(borrowedPath);
        deleteIfExists(librarianPath);
        deleteIfExists(Paths.get(FileControler.PRICES_PATH));
        deleteIfExists(Paths.get(FileControler.MAILS_PATH));
        deleteIfExists(Paths.get(FileControler.LOANS_PATH));
        deleteIfExists(Paths.get(FileControler.RENEW_REQUESTS_PATH));

        FileControler.BooksList.clear();
        FileControler.UserList.clear();
        FileControler.LibrarianList.clear();
        FileControler.BorrowedList.clear();
        FileControler.LoanedList.clear();
    }

    private void deleteIfExists(Path p) throws IOException {
        if (Files.exists(p)) {
            Files.delete(p);
        }
    }

    private void deleteIfNoThrow(Path p) {
        try {
            if (Files.exists(p)) Files.delete(p);
        } catch (Exception ignored) {}
    }

    // ---------------------------------------------------------
    // constructor / basic
    // ---------------------------------------------------------

    @Test
    void constructor_StartsAsyncLoaders_NoCrash() {
        assertDoesNotThrow(FileControler::new);
    }

    // ---------------------------------------------------------
    // addBookSync + addBookAsync
    // ---------------------------------------------------------

    @Test
    void addBookSync_WritesCorrectRecordToBooksFile_DefaultTypeAndCategory() throws Exception {
        Book book = new Book("Clean Code", "Robert Martin", "111", false);

        FileControler.addBookSync(book);

        assertTrue(Files.exists(booksPath));
        List<String> lines = Files.readAllLines(booksPath);
        assertEquals(1, lines.size());
        assertEquals("Clean Code,Robert Martin,111,false,BOOK,Other", lines.get(0));
    }

    @Test
    void addBookSync_RespectsMediaTypeAndCategoryWhenPresent() throws Exception {
        Book book = new Book("Some CD", "Artist", "CD-1", false);
        book.setMediaType("cd");
        book.setCategory("Music");

        FileControler.addBookSync(book);

        List<String> lines = Files.readAllLines(booksPath);
        assertEquals("Some CD,Artist,CD-1,false,CD,Music", lines.get(0));
    }

    @Test
    void addBookAsync_WritesRecord_Asynchronously() throws Exception {
        Book book = new Book("Refactoring", "Martin Fowler", "222", true);

        FileControler.addBookAsync(book);
        Thread.sleep(200);

        assertTrue(Files.exists(booksPath));
        List<String> lines = Files.readAllLines(booksPath);
        assertEquals(1, lines.size());
        assertEquals("Refactoring,Martin Fowler,222,true,BOOK,Other", lines.get(0));
    }

    // ---------------------------------------------------------
    // addBorrowedBook / addBorrowedCD / unBorrowIfNoFine
    // ---------------------------------------------------------

    @Test
    void addBorrowedBook_AppendsLineAndSetsBookFlagTrue() throws Exception {
        Files.writeString(booksPath, "Clean Code,Robert,111,false\n");

        FileControler.addBorrowedBook("111", "Clean Code", "user1");

        assertTrue(Files.exists(borrowedPath));
        List<String> borrowedLines = Files.readAllLines(borrowedPath);
        assertEquals(1, borrowedLines.size());
        assertTrue(borrowedLines.get(0).startsWith("111,Clean Code,"));

        List<String> booksLines = Files.readAllLines(booksPath);
        assertEquals("Clean Code,Robert,111,true", booksLines.get(0));
    }

    @Test
    void addBorrowedCD_AppendsLineWithTypeCD_AndSetsBookFlagTrue() throws Exception {
        Files.writeString(booksPath, "Some CD,Artist,CD-001,false\n");

        FileControler.addBorrowedCD("CD-001", "Some CD", "user1");

        List<String> borrowedLines = Files.readAllLines(borrowedPath);
        assertEquals(1, borrowedLines.size());
        String[] parts = borrowedLines.get(0).split(",");
        assertEquals("CD-001", parts[0].trim());
        assertEquals("Some CD", parts[1].trim());
        assertEquals("user1", parts[3].trim());
        assertEquals("CD", parts[4].trim());

        List<String> booksLines = Files.readAllLines(booksPath);
        assertEquals("Some CD,Artist,CD-001,true", booksLines.get(0));
    }

    @Test
    void unBorrowIfNoFine_RemovesLineAndSetsBookFlagFalse_WhenWithin28Days() throws Exception {
        LocalDate borrowDate = LocalDate.now().minusDays(5);
        Files.writeString(borrowedPath, "111,Clean Code," + borrowDate + ",user1\n");
        Files.writeString(booksPath, "Clean Code,Robert,111,true\n");

        boolean result = FileControler.unBorrowIfNoFine("111", "user1");
        assertTrue(result);

        List<String> borrowedLines = Files.readAllLines(borrowedPath);
        assertTrue(borrowedLines.isEmpty());

        List<String> booksLines = Files.readAllLines(booksPath);
        assertEquals("Clean Code,Robert,111,false", booksLines.get(0));
    }

    @Test
    void unBorrowIfNoFine_ReturnsFalse_WhenOverdueOrNotFound() throws Exception {
        LocalDate lateDate = LocalDate.now().minusDays(40);
        Files.writeString(borrowedPath, "111,Clean Code," + lateDate + ",user1\n");
        Files.writeString(booksPath, "Clean Code,Robert,111,true\n");

        assertFalse(FileControler.unBorrowIfNoFine("111", "user1"));

        Files.writeString(borrowedPath,
                "222,Other Book," + LocalDate.now() + ",otherUser\n");
        assertFalse(FileControler.unBorrowIfNoFine("111", "user1"));
    }

    @Test
    void unBorrowIfNoFine_IgnoresEmptyAndInvalidLines() throws Exception {
        Files.writeString(borrowedPath,
                "\n" +
                        "bad,line\n" +
                        "111,B," + LocalDate.now() + ",user1\n");
        Files.writeString(booksPath, "B,A,111,true\n");

        assertTrue(FileControler.unBorrowIfNoFine("111", "user1"));
    }

    @Test
    void unBorrowIfNoFine_InvalidDateIsTreatedAsFineAndReturnsFalse() throws Exception {
        Files.writeString(borrowedPath, "111,B,not-a-date,user1\n");
        Files.writeString(booksPath, "B,A,111,true\n");

        assertFalse(FileControler.unBorrowIfNoFine("111", "user1"));
    }

    // ---------------------------------------------------------
    // addUser / searchUser / rewriteUsersFile
    // ---------------------------------------------------------

    @Test
    void addUserAsync_And_searchUser_WorksCorrectly() throws Exception {
        User u = new User("Ali", "Ahmad", "ali1",
                "ali@mail.com", "1234", new String[]{"B1", "B2"});
        u.setAdmin(true);

        FileControler.addUserAsync(u);
        Thread.sleep(200);

        assertTrue(Files.exists(usersPath));
        List<String> lines = Files.readAllLines(usersPath);
        assertEquals(1, lines.size());
        assertEquals("Ali,Ahmad,ali1,ali@mail.com,1234,B1;B2,true", lines.get(0));

        assertTrue(FileControler.searchUser("ali1"));
        assertFalse(FileControler.searchUser("unknown"));
    }

    @Test
    void searchUserAsync_ReturnsTrue_WhenUserExists() throws Exception {
        Files.writeString(usersPath,
                "F,L,userX,ux@mail.com,pass,,false\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean found = new AtomicBoolean(false);

        FileControler.searchUserAsync("userX", result -> {
            found.set(result);
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(found.get());
    }

    @Test
    void searchUserAsync_ReturnsFalse_WhenUserMissing() throws Exception {
        Files.writeString(usersPath,
                "F,L,other,mail@mail.com,p\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean found = new AtomicBoolean(true);

        FileControler.searchUserAsync("missingUser", result -> {
            found.set(result);
            latch.countDown();
        });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertFalse(found.get());
    }

    @Test
    void rewriteUsersFile_RewritesFromStaticUserList() throws Exception {
        User u1 = new User("A", "One", "u1", "u1@mail.com", "p1", new String[]{"B1"});
        u1.setAdmin(false);
        User u2 = new User("B", "Two", "u2", "u2@mail.com", "p2", new String[0]);
        u2.setAdmin(true);

        FileControler.UserList.add(u1);
        FileControler.UserList.add(u2);

        FileControler.rewriteUsersFile();

        List<String> lines = Files.readAllLines(usersPath);
        assertEquals(2, lines.size());
        assertEquals("A,One,u1,u1@mail.com,p1,B1,false", lines.get(0));
        assertEquals("B,Two,u2,u2@mail.com,p2,,true", lines.get(1));
    }

    // ---------------------------------------------------------
    // getEmailForUser
    // ---------------------------------------------------------

    @Test
    void getEmailForUser_ReturnsEmailWhenFound() throws Exception {
        String content =
                "F1,L1,user1,u1@mail.com,p1\n" +
                        "F2,L2,user2,u2@mail.com,p2\n";
        Files.writeString(usersPath, content);

        assertEquals("u1@mail.com", FileControler.getEmailForUser("user1"));
        assertEquals("u2@mail.com", FileControler.getEmailForUser("user2"));
    }

    @Test
    void getEmailForUser_ReturnsNullWhenNotFoundOrFileMissing() throws Exception {
        assertNull(FileControler.getEmailForUser("nope"));

        Files.writeString(usersPath, "F,L,xx,xx@mail.com,p\n");
        assertNull(FileControler.getEmailForUser("other"));
    }

    // ---------------------------------------------------------
    // renewLoan
    // ---------------------------------------------------------

    @Test
    void renewLoan_UpdatesBorrowDate_WhenMatchFound() throws Exception {
        LocalDate oldDate = LocalDate.now().minusDays(10);
        Files.writeString(borrowedPath,
                "111,Clean Code," + oldDate + ",user1\n");

        assertTrue(FileControler.renewLoan("111", "user1"));

        List<String> lines = Files.readAllLines(borrowedPath);
        assertEquals(1, lines.size());
        String[] p = lines.get(0).split(",");
        assertEquals(LocalDate.now().toString(), p[2].trim());
    }

    @Test
    void renewLoan_ReturnsFalse_WhenNoMatch() throws Exception {
        Files.writeString(borrowedPath,
                "222,Other Book," + LocalDate.now() + ",otherUser\n");
        assertFalse(FileControler.renewLoan("111", "user1"));
    }

    @Test
    void renewLoan_SkipsMalformedAndEmptyLines() throws Exception {
        Files.writeString(borrowedPath,
                "\n" +
                        "bad,line\n" +
                        "111,Title," + LocalDate.now() + ",user1\n");

        assertTrue(FileControler.renewLoan("111", "user1"));

        List<String> lines = Files.readAllLines(borrowedPath);
        String[] p = lines.get(2).split(",");
        assertEquals(LocalDate.now().toString(), p[2]);
    }

    @Test
    void renewLoan_OnlyFirstMatchIsUpdated() throws Exception {
        String old = LocalDate.now().minusDays(10).toString();
        Files.writeString(borrowedPath,
                "111,B," + old + ",user1\n" +
                        "111,B," + old + ",user1\n");

        FileControler.renewLoan("111", "user1");

        List<String> lines = Files.readAllLines(borrowedPath);
        assertNotEquals(old, lines.get(0).split(",")[2]);
        assertEquals(old, lines.get(1).split(",")[2]);
    }

    // ---------------------------------------------------------
    // hasOverdueBooks
    // ---------------------------------------------------------

    @Test
    void hasOverdueBooks_ReturnsTrue_WhenAnyLoanOver28Days() throws Exception {
        LocalDate lateDate = LocalDate.now().minusDays(35);
        String content =
                "111,B1," + lateDate + ",user1\n" +
                        "222,B2," + LocalDate.now() + ",user1\n";
        Files.writeString(borrowedPath, content);
        assertTrue(FileControler.hasOverdueBooks("user1"));
    }

    @Test
    void hasOverdueBooks_ReturnsFalse_WhenNoOverdueOrDifferentUser() throws Exception {
        LocalDate within = LocalDate.now().minusDays(10);
        Files.writeString(borrowedPath, "111,B1," + within + ",user1\n");

        assertFalse(FileControler.hasOverdueBooks("otherUser"));
        assertFalse(FileControler.hasOverdueBooks("user1"));
    }

    // ---------------------------------------------------------
    // loadLoansForUser / loadLoansFromFile
    // ---------------------------------------------------------

    @Test
    void loadLoansForUser_LoadsLoansAndUsesBooksListIfAvailable() throws Exception {
        Book b = new Book("Clean Code", "Robert", "111", true);
        FileControler.BooksList.add(b);

        LocalDate d1 = LocalDate.now().minusDays(3);
        LocalDate d2 = LocalDate.now().minusDays(10);

        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.createDirectories(loansPath.getParent());
        String content =
                "user1,111,Clean Code," + d1 + "," + d1.plusDays(28) + ",BORROWED,0.0\n" +
                        "user1,333,Missing Book," + d2 + "," + d2.plusDays(28) + ",BORROWED,0.0\n" +
                        "otherUser,111,Clean Code," + d1 + "," + d1.plusDays(28) + ",BORROWED,0.0\n";
        Files.writeString(loansPath, content, StandardCharsets.UTF_8);

        User user = new User("F", "L", "user1", "u@mail.com", "p");
        List<Loan> loans = FileControler.loadLoansForUser(user);

        assertEquals(2, loans.size());
        assertEquals("Clean Code", loans.get(0).getItem().getTitle());
        assertEquals(d1, loans.get(0).getBorrowDate());
        assertEquals("Missing Book", loans.get(1).getItem().getTitle());
        assertEquals(d2, loans.get(1).getBorrowDate());
    }

    @Test
    void loadLoansFromFile_IgnoresInvalidLines() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        String content =
                "invalid,line\n" +
                        "user1,111,Clean Code,2025-01-01,2025-01-29,BORROWED,0.0\n";
        Files.writeString(loansPath, content, StandardCharsets.UTF_8);

        List<FileControler.LoanRecord> records = FileControler.loadLoansFromFile();
        assertEquals(1, records.size());
        assertEquals("user1", records.get(0).username);
    }

    // ---------------------------------------------------------
    // Librarians
    // ---------------------------------------------------------

    @Test
    void getLibrarian_ReturnsNull_WhenFileMissingOrNoMatch() {
        assertNull(FileControler.getLibrarian("lib", "pass"));
    }

    @Test
    void getLibrarian_ReturnsUser_WhenMatchFound() throws Exception {
        String content = "lib1,pass1\nlib2,pass2\n";
        Files.writeString(librarianPath, content);

        User u = FileControler.getLibrarian("lib2", "pass2");
        assertNotNull(u);
        assertEquals("lib2", u.getUsername());
        assertTrue(u.isLibrarian());
        assertFalse(u.isAdmin());
    }

    @Test
    void getLibrarian_ReturnsNull_WhenWrongCredentials() throws Exception {
        Files.writeString(librarianPath, "lib1,pass1\n");
        assertNull(FileControler.getLibrarian("lib1", "x"));
    }

    @Test
    void fillLibrariansDataAsync_PopulatesLibrarianList() throws Exception {
        String content =
                "F1,L1,lib1,l1@mail.com,p1\n" +
                        "invalid line\n";
        Files.writeString(librarianPath, content);

        FileControler.fillLibrariansDataAsync();
        Thread.sleep(200);

        assertEquals(1, FileControler.LibrarianList.size());
        assertEquals("lib1", FileControler.LibrarianList.get(0).getUsername());
    }

    @Test
    void fillLibrariansDataAsync_NoFile_DoesNotThrow() {
        deleteIfNoThrow(librarianPath);
        assertDoesNotThrow(FileControler::fillLibrariansDataAsync);
    }

    // ---------------------------------------------------------
    // updateBookBorrowFlag / syncBorrowedStatusOnce
    // ---------------------------------------------------------

    @Test
    void updateBookBorrowFlag_UpdatesOnlyFirstMatchingBook() throws Exception {
        String content =
                "B1,A1,111,false\n" +
                        "B2,A2,111,false\n" +
                        "B3,A3,222,true\n";
        Files.writeString(booksPath, content);

        FileControler.updateBookBorrowFlag("111", true);

        List<String> lines = Files.readAllLines(booksPath);
        assertEquals("B1,A1,111,true", lines.get(0));
        assertEquals("B2,A2,111,false", lines.get(1));
        assertEquals("B3,A3,222,true", lines.get(2));
    }

    @Test
    void syncBorrowedStatusOnce_SetsBorrowedFlagsBasedOnBorrowedFile() throws Exception {
        String borrowedContent =
                "111,B1,2025-01-01,user1\n" +
                        "111,B1,2025-01-02,user2\n" +
                        "222,B2,2025-01-03,user3\n";
        Files.writeString(borrowedPath, borrowedContent);

        String booksContent =
                "B1,A1,111,false\n" +
                        "B1,A1,111,false\n" +
                        "B2,A2,222,false\n" +
                        "B3,A3,333,true\n";
        Files.writeString(booksPath, booksContent);

        FileControler.syncBorrowedStatusOnce();

        List<String> lines = Files.readAllLines(booksPath);
        assertEquals("B1,A1,111,true", lines.get(0));
        assertEquals("B1,A1,111,true", lines.get(1));
        assertEquals("B2,A2,222,true", lines.get(2));
        assertEquals("B3,A3,333,false", lines.get(3));
    }

    // ---------------------------------------------------------
    // fillBorrowedBookAsync / fillBooksDataAsync / fillUsersDataAsync
    // ---------------------------------------------------------

    @Test
    void fillBorrowedBookAsync_ReadsBorrowedBooksIntoList() throws Exception {
        String content =
                "111,B1,2025-01-01,user1\n" +
                        "222,B2,2025-01-02,user2\n";
        Files.writeString(borrowedPath, content);

        ArrayList<Book> list = new ArrayList<>();
        ArrayList<Book> result = FileControler.fillBorrowedBookAsync(list);

        assertEquals(2, result.size());
        assertEquals("B1", result.get(0).getName());
        assertEquals("111", result.get(0).getISBN());
        assertTrue(result.get(0).isBorrowed());
    }

    @Test
    void fillBorrowedBookAsync_HandlesMissingFileGracefully() {
        deleteIfNoThrow(borrowedPath);
        ArrayList<Book> list = new ArrayList<>();
        assertTrue(FileControler.fillBorrowedBookAsync(list).isEmpty());
    }

    @Test
    void fillBorrowedBookAsync_SkipsInvalidLines() throws Exception {
        String content =
                "invalid\n" + // ignored
                        "444,GoodBook,2025-01-03,userX\n"; // valid
        Files.writeString(borrowedPath, content);

        ArrayList<Book> list = new ArrayList<>();
        ArrayList<Book> result = FileControler.fillBorrowedBookAsync(list);
        assertEquals(1, result.size());
        assertEquals("GoodBook", result.get(0).getName());
    }

    @Test
    void fillBooksDataAsync_PopulatesBooksListFromFile() throws Exception {
        String content =
                "B1,A1,111,true\n" +
                        "B2,A2,222,false,CD\n" +
                        "invalid line\n";
        Files.writeString(booksPath, content);

        FileControler.fillBooksDataAsync();
        Thread.sleep(200);

        assertEquals(2, FileControler.BooksList.size());
        assertEquals("B1", FileControler.BooksList.get(0).getName());
        assertTrue(FileControler.BooksList.get(0).isBorrowed());
        assertEquals("CD", FileControler.BooksList.get(1).getMediaType());
    }

    @Test
    void fillUsersDataAsync_PopulatesUserListFromFile() throws Exception {
        String content =
                "F1,L1,u1,u1@mail.com,p1,B1;B2,true\n" +
                        "F2,L2,u2,u2@mail.com,p2,,false\n" +
                        "invalid\n";
        Files.writeString(usersPath, content);

        FileControler.fillUsersDataAsync();
        Thread.sleep(200);

        assertEquals(2, FileControler.UserList.size());
        User u1 = FileControler.UserList.get(0);
        assertEquals("u1", u1.getUsername());
        assertTrue(u1.isAdmin());
        assertEquals(2, u1.getBooks().length);
    }

    // ---------------------------------------------------------
    // searchBooksContains
    // ---------------------------------------------------------

    @Test
    void searchBooksContains_ReturnsEmpty_WhenKeywordNullOrEmptyOrFileMissing() {
        assertTrue(FileControler.searchBooksContains(null).isEmpty());
        assertTrue(FileControler.searchBooksContains("   ").isEmpty());
        deleteIfNoThrow(booksPath);
        assertTrue(FileControler.searchBooksContains("any").isEmpty());
    }

    @Test
    void searchBooksContains_SearchesByNameAuthorAndIsbn() throws Exception {
        String content =
                "Clean Code,Robert,111,true\n" +
                        "Refactoring,Martin,222,false\n";
        Files.writeString(booksPath, content);

        ArrayList<Book> byName = FileControler.searchBooksContains("clean");
        assertEquals(1, byName.size());
        assertEquals("Clean Code", byName.get(0).getName());

        ArrayList<Book> byAuthor = FileControler.searchBooksContains("martin");
        assertEquals(1, byAuthor.size());
        assertEquals("Refactoring", byAuthor.get(0).getName());

        ArrayList<Book> byIsbn = FileControler.searchBooksContains("222");
        assertEquals(1, byIsbn.size());
        assertEquals("Refactoring", byIsbn.get(0).getName());
    }

    // ---------------------------------------------------------
    // findBookByIsbn / markLoanReturned / userHasActiveLoansOrFines
    // ---------------------------------------------------------

    @Test
    void findBookByIsbn_ReturnsMatchingBookOrNull() {
        Book b1 = new Book("B1", "A1", "111", true);
        Book b2 = new Book("B2", "A2", "222", false);
        FileControler.BooksList.add(b1);
        FileControler.BooksList.add(b2);

        assertSame(b1, FileControler.findBookByIsbn("111"));
        assertSame(b2, FileControler.findBookByIsbn("222"));
        assertNull(FileControler.findBookByIsbn("333"));
    }

    @Test
    void markLoanReturned_RemovesLineAndUpdatesBookAndSyncs() throws Exception {
        String content =
                "111,B1," + LocalDate.now() + ",user1\n" +
                        "222,B2," + LocalDate.now() + ",user2\n";
        Files.writeString(borrowedPath, content);

        Book b1 = new Book("B1", "A1", "111", true);
        FileControler.BooksList.add(b1);

        Files.writeString(booksPath, "B1,A1,111,true\n");

        assertTrue(FileControler.markLoanReturned("user1", "111"));

        List<String> newLines = Files.readAllLines(borrowedPath);
        assertEquals(1, newLines.size());
        assertTrue(newLines.get(0).startsWith("222,"));
        assertFalse(b1.isBorrowed());
    }

    @Test
    void markLoanReturned_ReturnsFalse_WhenNoMatch() throws Exception {
        Files.writeString(borrowedPath,
                "111,B1," + LocalDate.now() + ",user1\n");
        assertFalse(FileControler.markLoanReturned("user2", "222"));
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsFalse_WhenUserNullOrNoFile() {
        assertFalse(FileControler.userHasActiveLoansOrFines(null));
        User u = new User("F", "L", "user1", "e", "p");
        assertFalse(FileControler.userHasActiveLoansOrFines(u));
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsTrue_WhenActiveOrOverdueOrBadDate() throws Exception {
        User u = new User("F", "L", "user1", "e", "p");

        LocalDate d1 = LocalDate.now().minusDays(5);
        Files.writeString(borrowedPath, "111,B1," + d1 + ",user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));

        Files.writeString(borrowedPath, "111,B1,not-a-date,user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));

        LocalDate dLate = LocalDate.now().minusDays(40);
        Files.writeString(borrowedPath, "111,B1," + dLate + ",user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsFalse_WhenNoRowForUser() throws Exception {
        User u = new User("F", "L", "user1", "e", "p");
        Files.writeString(borrowedPath,
                "111,B1," + LocalDate.now() + ",otherUser\n");
        assertFalse(FileControler.userHasActiveLoansOrFines(u));
    }

    // ---------------------------------------------------------
    // fines: addFine / getTotalFineForUser / hasOutstandingFine / clearFineForUserAndBook
    // ---------------------------------------------------------

    @Test
    void addFine_And_getTotalFineForUser_WorkCorrectly() throws Exception {
        Path pricesPath = Paths.get(FileControler.PRICES_PATH);
        deleteIfNoThrow(pricesPath);

        FileControler.addFine("user1", "111", 10.0);
        FileControler.addFine("user1", "222", 5.5);
        FileControler.addFine("other", "333", 7.0);

        assertEquals(15.5, FileControler.getTotalFineForUser("user1"), 0.0001);
        assertEquals(7.0,  FileControler.getTotalFineForUser("other"), 0.0001);
        assertEquals(0.0,  FileControler.getTotalFineForUser("nope"), 0.0001);

        assertTrue(FileControler.hasOutstandingFine("user1"));
        assertFalse(FileControler.hasOutstandingFine("nope"));
    }

    @Test
    void clearFineForUserAndBook_RemovesOnlyMatchingFine() throws Exception {
        Path pricesPath = Paths.get(FileControler.PRICES_PATH);
        deleteIfNoThrow(pricesPath);

        String content =
                "user1,111,10.0\n" +
                        "user1,222,5.0\n" +
                        "user2,111,7.0\n";
        Files.writeString(pricesPath, content);

        FileControler.clearFineForUserAndBook("user1", "111");

        List<String> lines = Files.readAllLines(pricesPath);
        assertEquals(2, lines.size());
        assertTrue(lines.contains("user1,222,5.0"));
        assertTrue(lines.contains("user2,111,7.0"));
    }

    // ---------------------------------------------------------
    // mails log
    // ---------------------------------------------------------

    @Test
    void logMail_AppendsMailRecordToFile() throws Exception {
        Path mailsPath = Paths.get(FileControler.MAILS_PATH);
        deleteIfNoThrow(mailsPath);

        FileControler.logMail("user1", "u1@mail.com", "Test Subject");

        assertTrue(Files.exists(mailsPath));
        List<String> lines = Files.readAllLines(mailsPath);
        assertEquals(1, lines.size());

        String[] p = lines.get(0).split(",");
        assertEquals("user1", p[0].trim());
        assertEquals("u1@mail.com", p[1].trim());
        assertEquals("Test Subject", p[2].trim());
        assertTrue(p[3].trim().length() > 0);
    }

    // ---------------------------------------------------------
    // Loan raw APIs: appendLoanRecord / save+load / updateLoanStatus
    // ---------------------------------------------------------

    @Test
    void appendLoanRecord_And_loadLoansFromFile_CreateAndReadLoanRecords() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now();
        LocalDate due = start.plusDays(28);

        FileControler.appendLoanRecord("user1", "111", "Clean Code", start, due);

        List<FileControler.LoanRecord> records = FileControler.loadLoansFromFile();
        assertEquals(1, records.size());
        FileControler.LoanRecord r = records.get(0);
        assertEquals("user1", r.username);
        assertEquals("111", r.isbn);
        assertEquals("Clean Code", r.title);
        assertEquals(start, r.startDate);
        assertEquals(due, r.dueDate);
        assertEquals("BORROWED", r.status);
        assertEquals(0.0, r.fee, 0.0001);
    }

    @Test
    void updateLoanStatus_ChangesStatusAndFee_WhenLoanMatches() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertTrue(FileControler.updateLoanStatus("user1", "111", start, "OVERDUE", 15.0));

        List<FileControler.LoanRecord> after = FileControler.loadLoansFromFile();
        assertEquals(1, after.size());
        FileControler.LoanRecord r = after.get(0);
        assertEquals("OVERDUE", r.status);
        assertEquals(15.0, r.fee, 0.0001);
    }

    @Test
    void updateLoanStatus_ReturnsFalse_WhenNoMatchingLoan() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertFalse(FileControler.updateLoanStatus("user1", "222", start, "RETURNED", 0.0));
    }

    // ---------------------------------------------------------
    // markLoanReturnedInFile / markLoanOverdueInFile
    // ---------------------------------------------------------

    @Test
    void markLoanReturnedInFile_UpdatesStatusToReturned() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertTrue(FileControler.markLoanReturnedInFile("user1", "111", start.toString(), due.toString()));

        List<FileControler.LoanRecord> list = FileControler.loadLoansFromFile();
        assertEquals(1, list.size());
        assertEquals("RETURNED", list.get(0).status);
    }

    @Test
    void markLoanReturnedInFile_ReturnsFalse_WhenBadStartDateOrNoMatch() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertFalse(FileControler.markLoanReturnedInFile("user1", "111", "not-a-date", due.toString()));
        assertFalse(FileControler.markLoanReturnedInFile("other", "111", start.toString(), due.toString()));
    }

    @Test
    void markLoanOverdueInFile_UpdatesStatusToOverdueAndFee() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(40);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertTrue(FileControler.markLoanOverdueInFile("user1", "Clean Code", start.toString(), 20.0));

        List<FileControler.LoanRecord> list = FileControler.loadLoansFromFile();
        FileControler.LoanRecord r = list.get(0);
        assertEquals("OVERDUE", r.status);
        assertEquals(20.0, r.fee, 0.0001);
    }

    @Test
    void markLoanOverdueInFile_ReturnsFalse_WhenBadStartDateOrNoMatch() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate due = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        assertFalse(FileControler.markLoanOverdueInFile("user1", "111", "not-a-date", 10.0));
        assertFalse(FileControler.markLoanOverdueInFile("other", "111", start.toString(), 10.0));
    }

    // ---------------------------------------------------------
    // Admin view: loadAllLoansRows / searchLoansRows
    // ---------------------------------------------------------

    @Test
    void loadAllLoansRows_ReadsRowsFromLoanFile() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        String content =
                "user1,111,Clean Code,2025-01-01,2025-01-29,BORROWED,0.0\n" +
                        "user2,222,Refactoring,2025-01-02,2025-01-30,OVERDUE,10.0\n";
        Files.writeString(loansPath, content, StandardCharsets.UTF_8);

        List<GAdminControl.LoanRow> rows = FileControler.loadAllLoansRows();
        assertEquals(2, rows.size());
        assertEquals("user1", rows.get(0).getUser());
        assertEquals("Clean Code", rows.get(0).getBook());
        assertEquals("borrowed", rows.get(0).getStatus().toLowerCase());
    }

    @Test
    void loadAllLoansRows_ReturnsEmptyList_WhenFileMissing() {
        deleteIfNoThrow(Paths.get(FileControler.LOANS_PATH));
        List<GAdminControl.LoanRow> rows = FileControler.loadAllLoansRows();
        assertNotNull(rows);
        assertTrue(rows.isEmpty());
    }

    @Test
    void searchLoansRows_FiltersByUserBookOrStatus() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        String content =
                "user1,111,Clean Code,2025-01-01,2025-01-29,BORROWED,0.0\n" +
                        "user2,222,Refactoring,2025-01-02,2025-01-30,OVERDUE,10.0\n";
        Files.writeString(loansPath, content, StandardCharsets.UTF_8);

        List<GAdminControl.LoanRow> byUser = FileControler.searchLoansRows("user2");
        assertEquals(1, byUser.size());
        assertEquals("user2", byUser.get(0).getUser());

        List<GAdminControl.LoanRow> byBook = FileControler.searchLoansRows("clean");
        assertEquals(1, byBook.size());
        assertEquals("Clean Code", byBook.get(0).getBook());

        List<GAdminControl.LoanRow> byStatus = FileControler.searchLoansRows("overdue");
        assertEquals(1, byStatus.size());
        assertEquals("Refactoring", byStatus.get(0).getBook());
    }

    @Test
    void searchLoansRows_ReturnsEmptyList_WhenNoMatches() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);
        Files.writeString(loansPath,
                "user1,111,Clean Code,2025-01-01,2025-01-29,BORROWED,0.0\n",
                StandardCharsets.UTF_8);

        assertTrue(FileControler.searchLoansRows("xyz").isEmpty());
    }

    // ---------------------------------------------------------
    // autoUpdateOverdueLoans
    // ---------------------------------------------------------

    @Test
    void autoUpdateOverdueLoans_SetsStatusOverdueOrBorrowedCorrectly() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate today = LocalDate.now();

        FileControler.LoanRecord r1 =
                new FileControler.LoanRecord(
                        "user1", "111", "Old Book",
                        today.minusDays(40),
                        today.minusDays(5),
                        "BORROWED", 0.0);

        FileControler.LoanRecord r2 =
                new FileControler.LoanRecord(
                        "user2", "222", "New Book",
                        today.minusDays(3),
                        today.plusDays(10),
                        "BORROWED", 0.0);

        FileControler.saveLoansToFile(List.of(r1, r2));
        FileControler.autoUpdateOverdueLoans();

        List<FileControler.LoanRecord> after = FileControler.loadLoansFromFile();
        assertEquals(2, after.size());
        assertEquals("OVERDUE", after.get(0).status);
        assertTrue(after.get(0).fee > 0.0);
        assertEquals("BORROWED", after.get(1).status);
    }

    @Test
    void autoUpdateOverdueLoans_DoesNotChangeReturnedLoans() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate today = LocalDate.now();
        FileControler.LoanRecord r =
                new FileControler.LoanRecord(
                        "user1", "111", "Book",
                        today.minusDays(40),
                        today.minusDays(10),
                        "RETURNED", 5.0);

        FileControler.saveLoansToFile(List.of(r));
        FileControler.autoUpdateOverdueLoans();

        List<FileControler.LoanRecord> after = FileControler.loadLoansFromFile();
        assertEquals(1, after.size());
        assertEquals("RETURNED", after.get(0).status);
        assertEquals(5.0, after.get(0).fee, 0.0001);
    }

    // ---------------------------------------------------------
    // hasOverdueCDs
    // ---------------------------------------------------------

    @Test
    void hasOverdueCDs_ReturnsTrue_WhenUserHasOverdueCDLoan() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate due = start.plusDays(7);

        String line = String.join(",",
                "user1",
                "CD-001",
                "Some CD",
                start.toString(),
                due.toString(),
                "BORROWED",
                "0.0");
        Files.writeString(loansPath, line + System.lineSeparator(), StandardCharsets.UTF_8);

        Book cd = new Book("Some CD", "Artist", "CD-001", true);
        cd.setMediaType("CD");
        FileControler.BooksList.add(cd);

        User u = new User("F", "L", "user1", "e", "p");
        assertTrue(FileControler.hasOverdueCDs(u));
    }

    @Test
    void hasOverdueCDs_ReturnsFalse_WhenNoCDOrNotOverdue() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        deleteIfNoThrow(loansPath);

        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate due = start.plusDays(7);

        String line = String.join(",",
                "user1",
                "111",
                "Clean Code",
                start.toString(),
                due.toString(),
                "BORROWED",
                "0.0");
        Files.writeString(loansPath, line + System.lineSeparator(), StandardCharsets.UTF_8);

        Book b = new Book("Clean Code", "Robert", "111", true);
        b.setMediaType("BOOK");
        FileControler.BooksList.add(b);

        User u = new User("F", "L", "user1", "e", "p");
        assertFalse(FileControler.hasOverdueCDs(u));
    }

    @Test
    void hasOverdueCDs_ReturnsFalse_WhenUserNullOrNoLoans() {
        assertFalse(FileControler.hasOverdueCDs(null));
        User u = new User("F", "L", "user1", "e", "p");
        deleteIfNoThrow(Paths.get(FileControler.LOANS_PATH));
        assertFalse(FileControler.hasOverdueCDs(u));
    }

    // ---------------------------------------------------------
    // Renew requests: addRenewRequest / hasRenewRequest / clearRenewRequest
    // ---------------------------------------------------------

    @Test
    void addRenewRequest_Then_HasAndClearRenewRequest_Works() throws Exception {
        Path renewPath = Paths.get(FileControler.RENEW_REQUESTS_PATH);
        deleteIfNoThrow(renewPath);

        User u = new User("F", "L", "user1", "e", "p");
        Book b = new Book("Clean Code", "Robert", "111", true);
        LocalDate borrowDate = LocalDate.now().minusDays(10);
        Loan loan = new Loan(b, u, borrowDate, 28);

        FileControler.addRenewRequest(u, loan);
        assertTrue(Files.exists(renewPath));
        assertTrue(FileControler.hasRenewRequest("user1", "111", borrowDate));

        FileControler.clearRenewRequest("user1", "111", borrowDate);
        assertFalse(FileControler.hasRenewRequest("user1", "111", borrowDate));
    }

    @Test
    void hasRenewRequest_ReturnsFalse_WhenFileMissingOrNoMatch() {
        deleteIfNoThrow(Paths.get(FileControler.RENEW_REQUESTS_PATH));
        assertFalse(FileControler.hasRenewRequest("u", "111", LocalDate.now()));
    }

    @Test
    void hasRenewRequest_IgnoresMalformedLinesAndParseErrors() throws Exception {
        Path p = Paths.get(FileControler.RENEW_REQUESTS_PATH);
        deleteIfNoThrow(p);

        Files.writeString(p,
                "bad\n" +
                        "u,111,title,notADate,2025-01-01\n" +
                        "u,222,title," + LocalDate.now() + "," + LocalDate.now().plusDays(3) + "\n",
                StandardCharsets.UTF_8);

        assertFalse(FileControler.hasRenewRequest("u", "111", LocalDate.now()));
    }

    @Test
    void clearRenewRequest_IgnoresMalformedLines() throws Exception {
        Path p = Paths.get(FileControler.RENEW_REQUESTS_PATH);
        deleteIfNoThrow(p);

        Files.writeString(p,
                "badLine\n" +
                        "u,111,title,notADate,2025-01-01\n",
                StandardCharsets.UTF_8);

        FileControler.clearRenewRequest("u", "111", LocalDate.now());

        List<String> lines = Files.readAllLines(p);
        // الدالتين الاثنين بيبقوا لأن الأولى length<5 والثانية parse fails
        assertEquals(2, lines.size());
    }

    @Test
    void clearRenewRequest_DoesNotRemoveWhenNotMatching() throws Exception {
        Path p = Paths.get(FileControler.RENEW_REQUESTS_PATH);
        deleteIfNoThrow(p);

        LocalDate d = LocalDate.now().minusDays(3);
        String line = "userA,111,Book," + d + "," + d.plusDays(20);
        Files.writeString(p, line, StandardCharsets.UTF_8);

        FileControler.clearRenewRequest("otherUser", "111", d);
        assertEquals(line, Files.readAllLines(p).get(0));
    }

    @Test
    void clearRenewRequest_NoFile_DoesNotThrow() {
        deleteIfNoThrow(Paths.get(FileControler.RENEW_REQUESTS_PATH));
        assertDoesNotThrow(() ->
                FileControler.clearRenewRequest("u", "111", LocalDate.now()));
    }

    // ---------------------------------------------------------
    // updateUserInFile / updateLibrarianInFile
    // ---------------------------------------------------------

    @Test
    void updateUserInFile_UpdatesMatchingLine_AndSkipsEmptyAndMalformed() throws Exception {
        Files.writeString(usersPath,
                "\n" +
                        "bad,line\n" +
                        "F,L,u1,u1@mail.com,p,X,false\n",
                StandardCharsets.UTF_8);

        User u = new User("NF", "NL", "u1", "ne@mail", "newP", new String[]{"B"});
        u.setAdmin(true);

        assertTrue(FileControler.updateUserInFile(u));

        List<String> lines = Files.readAllLines(usersPath);
        assertEquals("NF,NL,u1,ne@mail,newP,B,true", lines.get(2));
    }

    @Test
    void updateUserInFile_ReturnsFalse_WhenFileMissing() {
        deleteIfNoThrow(usersPath);
        User updated = new User("F", "L", "u1", "e", "p", new String[0]);
        assertFalse(FileControler.updateUserInFile(updated));
    }

    @Test
    void updateLibrarianInFile_UpdatesMatchingLibrarianLine() throws Exception {
        Path libsPath = Paths.get(FileControler.LIBRARIANS_PATH);
        deleteIfNoThrow(libsPath);

        String content =
                "F1,L1,lib1,l1@mail.com,p1\n" +
                        "F2,L2,lib2,l2@mail.com,p2\n";
        Files.writeString(libsPath, content, StandardCharsets.UTF_8);

        User upd = new User("NF", "NL", "lib2", "new@mail.com", "newPass", new String[0]);
        assertTrue(FileControler.updateLibrarianInFile(upd));

        List<String> lines = Files.readAllLines(libsPath);
        assertEquals("NF,NL,lib2,new@mail.com,newPass", lines.get(1));
    }

    @Test
    void updateLibrarianInFile_ReturnsFalse_WhenFileMissing() {
        deleteIfNoThrow(Paths.get(FileControler.LIBRARIANS_PATH));
        User upd = new User("F", "L", "lib", "e", "p", new String[0]);
        assertFalse(FileControler.updateLibrarianInFile(upd));
    }

    // ---------------------------------------------------------
    // Background sync
    // ---------------------------------------------------------

    @Test
    void startBackgroundSync_CanBeCalledTwiceWithoutException() throws InterruptedException {
        assertDoesNotThrow(FileControler::startBackgroundSync);
        assertDoesNotThrow(FileControler::startBackgroundSync);
        Thread.sleep(100);
    }
    @Test
    void markLoanReturned_ReturnsFalse_WhenBorrowedFileMissing() {
        // نتأكد إن ملف Borrowed_Books.txt مش موجود
        deleteIfNoThrow(borrowedPath);

        boolean result = FileControler.markLoanReturned("user1", "111");

        // لما ما يكون في ملف أصلاً → الدالة لازم ترجع false من الفرع الأول
        assertFalse(result);
    }

    @Test
    void markLoanReturned_LeavesMalformedLinesAndOnlyRemovesFirstMatch() throws Exception {

        String content =
                "\n" +
                        "malformed-line\n" +
                        "111,B1," + LocalDate.now() + ",user1\n" +
                        "111,B2," + LocalDate.now() + ",user1\n" +
                        "222,B3," + LocalDate.now() + ",other\n";

        Files.writeString(borrowedPath, content);

        // نخلي BooksList فاضية عشان نغطي فرع (b == null) داخل markLoanReturned
        FileControler.BooksList.clear();

        boolean ok = FileControler.markLoanReturned("user1", "111");
        assertTrue(ok);

        // السطر الفاضي انمسح، أول match انمسح، والباقي موجود
        List<String> lines = Files.readAllLines(borrowedPath);
        assertEquals(3, lines.size());

        assertEquals("malformed-line", lines.get(0));          // سطر معطوب محفوظ كما هو
        assertTrue(lines.get(1).startsWith("111,B2,"));        // ثاني match لم يُحذف
        assertTrue(lines.get(2).startsWith("222,B3,"));        // سطر اليوزر الآخر
    }

}
