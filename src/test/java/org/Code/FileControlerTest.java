package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
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
    private final Path librarianPath = Paths.get(FileControler.LIBRARIANS_PATH); // same as LIBRARIAN_PATH

    @BeforeEach
    void setUp() throws Exception {
        // تأكد إن الفولدر موجود
        Files.createDirectories(baseDir);

        // نظّف الملفات
        deleteIfExists(booksPath);
        deleteIfExists(usersPath);
        deleteIfExists(borrowedPath);
        deleteIfExists(librarianPath);

        // نظف كل القوائم الستاتيكية
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

    // ---------------------------------------------------------
    // addBookSync + addBookAsync
    // ---------------------------------------------------------

    @Test
    void addBookSync_WritesCorrectRecordToBooksFile() throws Exception {
        Book book = new Book("Clean Code", "Robert Martin", "111", false);

        FileControler.addBookSync(book);

        assertTrue(Files.exists(booksPath));

        List<String> lines = Files.readAllLines(booksPath);
        assertEquals(1, lines.size());

        // الشكل الجديد للـ record
        assertEquals(
                "Clean Code,Robert Martin,111,false,BOOK,Other",
                lines.get(0)
        );
    }


    @Test
    void addBookAsync_WritesRecord_Asynchronously() throws Exception {
        // arrange
        Book book = new Book("Refactoring", "Martin Fowler", "222", true);

        // act
        FileControler.addBookAsync(book);

        // نخلي الـ thread يخلص كتابة الملف
        Thread.sleep(200);

        // assert
        assertTrue(Files.exists(booksPath));
        List<String> lines = Files.readAllLines(booksPath);
        assertEquals(1, lines.size());

        // الشكل الجديد: name,author,ISBN,borrowed,mediaType,category
        assertEquals(
                "Refactoring,Martin Fowler,222,true,BOOK,Other",
                lines.get(0)
        );
    }

    // ---------------------------------------------------------
    // addUserSync (via addUserAsync) + rewriteUsersFile + searchUser
    // ---------------------------------------------------------

    @Test
    void addUserAsync_And_searchUser_WorksCorrectly() throws Exception {
        User u = new User("Ali", "Ahmad", "ali1", "ali@mail.com", "1234", new String[]{"B1", "B2"});
        u.setAdmin(true);

        FileControler.addUserAsync(u);
        Thread.sleep(200);

        assertTrue(Files.exists(usersPath));
        List<String> lines = Files.readAllLines(usersPath);
        assertEquals(1, lines.size());
        assertEquals("Ali,Ahmad,ali1,ali@mail.com,1234,B1;B2,true", lines.get(0));

        // searchUser should find it
        assertTrue(FileControler.searchUser("ali1"));
        assertFalse(FileControler.searchUser("unknown"));
    }

    @Test
    void searchUserAsync_CallsCallbackWithResult() throws Exception {
        // جهّز ملف users
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
    void rewriteUsersFile_OverwritesUsersFileWithUserList() throws Exception {
        // عبّي UserList
        User u1 = new User("A", "One", "u1", "u1@mail.com", "p1", new String[]{"B1"});
        u1.setAdmin(false);
        User u2 = new User("B", "Two", "u2", "u2@mail.com", "p2", new String[0]);
        u2.setAdmin(true);

        FileControler.UserList.add(u1);
        FileControler.UserList.add(u2);

        FileControler.rewriteUsersFile();

        assertTrue(Files.exists(usersPath));
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
        // no file
        assertNull(FileControler.getEmailForUser("nope"));

        // create file but username not found
        Files.writeString(usersPath, "F,L,xx,xx@mail.com,p\n");
        assertNull(FileControler.getEmailForUser("other"));
    }

    // ---------------------------------------------------------
    // addBorrowedBook + updateBookBorrowFlag + unBorrowIfNoFine
    // ---------------------------------------------------------

    @Test
    void addBorrowedBook_AppendsLineAndSetsBookFlagTrue() throws Exception {
        // Books file فيه كتاب متاح
        Files.writeString(booksPath, "Clean Code,Robert,111,false\n");

        FileControler.addBorrowedBook("111", "Clean Code", "user1");

        assertTrue(Files.exists(borrowedPath));
        List<String> borrowedLines = Files.readAllLines(borrowedPath);
        assertEquals(1, borrowedLines.size());
        // نتحقق من بداية السطر (التاريخ متغيّر)
        assertTrue(borrowedLines.get(0).startsWith("111,Clean Code,"));

        // تحديث Books
        List<String> booksLines = Files.readAllLines(booksPath);
        assertEquals("Clean Code,Robert,111,true", booksLines.get(0));
    }

    @Test
    void unBorrowIfNoFine_RemovesLineAndSetsBookFlagFalse_WhenWithin28Days() throws Exception {
        LocalDate borrowDate = LocalDate.now().minusDays(5);
        Files.writeString(borrowedPath,
                "111,Clean Code," + borrowDate + ",user1\n");

        Files.writeString(booksPath, "Clean Code,Robert,111,true\n");

        boolean result = FileControler.unBorrowIfNoFine("111", "user1");
        assertTrue(result);

        // الملف Borrowed مفروض يكون فاضي
        List<String> borrowedLines = Files.readAllLines(borrowedPath);
        assertTrue(borrowedLines.isEmpty());

        // Books flag لازم يكون false
        List<String> booksLines = Files.readAllLines(booksPath);
        assertEquals("Clean Code,Robert,111,false", booksLines.get(0));
    }

    @Test
    void unBorrowIfNoFine_ReturnsFalse_WhenOverdueOrNotFound() throws Exception {
        // متأخر
        LocalDate lateDate = LocalDate.now().minusDays(40);
        Files.writeString(borrowedPath,
                "111,Clean Code," + lateDate + ",user1\n");

        Files.writeString(booksPath, "Clean Code,Robert,111,true\n");

        boolean resultLate = FileControler.unBorrowIfNoFine("111", "user1");
        assertFalse(resultLate);

        // سطر غير مطابق
        Files.writeString(borrowedPath,
                "222,Other Book," + LocalDate.now() + ",otherUser\n");

        boolean resultNoMatch = FileControler.unBorrowIfNoFine("111", "user1");
        assertFalse(resultNoMatch);
    }

    // ---------------------------------------------------------
    // renewLoan
    // ---------------------------------------------------------

    @Test
    void renewLoan_UpdatesBorrowDate_WhenMatchFound() throws Exception {
        LocalDate oldDate = LocalDate.now().minusDays(10);
        Files.writeString(borrowedPath,
                "111,Clean Code," + oldDate + ",user1\n");

        boolean result = FileControler.renewLoan("111", "user1");
        assertTrue(result);

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
        String content =
                "111,B1," + within + ",user1\n";
        Files.writeString(borrowedPath, content);

        assertFalse(FileControler.hasOverdueBooks("otherUser"));
        assertFalse(FileControler.hasOverdueBooks("user1")); // مش متأخر
    }

    // ---------------------------------------------------------
    // loadLoansForUser
    // ---------------------------------------------------------

    @Test
    void loadLoansForUser_LoadsLoansAndUsesBooksListIfAvailable() throws Exception {
        // BooksList فيه كتاب بنفس الـ ISBN
        Book b = new Book("Clean Code", "Robert", "111", true);
        FileControler.BooksList.add(b);

        LocalDate d1 = LocalDate.now().minusDays(3);
        LocalDate d2 = LocalDate.now().minusDays(10);

        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.createDirectories(loansPath.getParent());

        // format: username,isbn,title,startDate,dueDate,status,fee
        String content =
                "user1,111,Clean Code," + d1 + "," + d1.plusDays(28) + ",BORROWED,0.0\n" +
                        "user1,333,Missing Book," + d2 + "," + d2.plusDays(28) + ",BORROWED,0.0\n" +
                        "otherUser,111,Clean Code," + d1 + "," + d1.plusDays(28) + ",BORROWED,0.0\n";

        Files.writeString(
                loansPath,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        User user = new User("F", "L", "user1", "u@mail.com", "p");

        // act
        List<Loan> loans = FileControler.loadLoansForUser(user);

        // عندنا فقط سطرين لـ user1
        assertEquals(2, loans.size());

        // الأول: من BooksList (نكتفي بالتأكد من العنوان والتاريخ)
        Loan l1 = loans.get(0);
        assertEquals("Clean Code", l1.getItem().getTitle());
        assertEquals(d1, l1.getBorrowDate());

        // الثاني: كتاب مش موجود في BooksList، ينشأ من الملف
        Loan l2 = loans.get(1);
        assertEquals("Missing Book", l2.getItem().getTitle());
        assertEquals(d2, l2.getBorrowDate());
    }


    // ---------------------------------------------------------
    // Librarian: getLibrarian + fillLibrariansDataAsync
    // ---------------------------------------------------------

    @Test
    void getLibrarian_ReturnsNull_WhenFileMissingOrNoMatch() {
        // لا يوجد ملف
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
    void fillLibrariansDataAsync_PopulatesLibrarianList() throws Exception {
        String content =
                "F1,L1,lib1,l1@mail.com,p1\n" +
                        "invalid line\n";
        Files.writeString(librarianPath, content);

        FileControler.fillLibrariansDataAsync();
        Thread.sleep(200);

        assertEquals(1, FileControler.LibrarianList.size());
        User u = FileControler.LibrarianList.get(0);
        assertEquals("lib1", u.getUsername());
    }

    // ---------------------------------------------------------
    // updateBookBorrowFlag + syncBorrowedStatusOnce
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
        assertEquals("B1,A1,111,true",  lines.get(0)); // أول واحد اتغير
        assertEquals("B2,A2,111,false", lines.get(1)); // التاني زي ما هو
        assertEquals("B3,A3,222,true",  lines.get(2));
    }

    @Test
    void syncBorrowedStatusOnce_SetsBorrowedFlagsBasedOnBorrowedBooksFile() throws Exception {
        // Borrowed: 2 نسخ من نفس الـ ISBN و 1 من آخر
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
        // فيه 2 مستعارة من 111 و 1 من 222
        assertEquals("B1,A1,111,true",  lines.get(0));
        assertEquals("B1,A1,111,true",  lines.get(1));
        assertEquals("B2,A2,222,true",  lines.get(2));
        assertEquals("B3,A3,333,false", lines.get(3)); // مفيش استعارة → false
    }

    // ---------------------------------------------------------
    // fillBorrowedBookAsync
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
        ArrayList<Book> list = new ArrayList<>();
        ArrayList<Book> result = FileControler.fillBorrowedBookAsync(list);
        assertTrue(result.isEmpty());
    }

    // ---------------------------------------------------------
    // fillBooksDataAsync + fillUsersDataAsync
    // ---------------------------------------------------------

    @Test
    void fillBooksDataAsync_PopulatesBooksListFromFile() throws Exception {
        String content =
                "B1,A1,111,true\n" +
                        "B2,A2,222,false\n" +
                        "invalid line\n";
        Files.writeString(booksPath, content);

        FileControler.fillBooksDataAsync();
        Thread.sleep(200);

        assertEquals(2, FileControler.BooksList.size());
        assertEquals("B1", FileControler.BooksList.get(0).getName());
        assertTrue(FileControler.BooksList.get(0).isBorrowed());
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
        // file missing
        assertTrue(FileControler.searchBooksContains("any").isEmpty());
    }

    @Test
    void searchBooksContains_SearchesNameAuthorAndIsbn_CaseInsensitive() throws Exception {
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
    // markLoanReturned + findBookByIsbn + userHasActiveLoansOrFines
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
    void markLoanReturned_RemovesLineAndUpdatesBookAndCallsSync() throws Exception {
        // Borrowed file فيه سطرين
        String content =
                "111,B1," + LocalDate.now() + ",user1\n" +
                        "222,B2," + LocalDate.now() + ",user2\n";
        Files.writeString(borrowedPath, content);

        // BooksList فيه كتاب مستعار 111
        Book b1 = new Book("B1", "A1", "111", true);
        FileControler.BooksList.add(b1);

        // Books.txt لازم يكون موجود عشان syncBorrowedStatusOnce
        Files.writeString(booksPath, "B1,A1,111,true\n");

        boolean result = FileControler.markLoanReturned("user1", "111");
        assertTrue(result);

        List<String> newLines = Files.readAllLines(borrowedPath);
        assertEquals(1, newLines.size());
        assertTrue(newLines.get(0).startsWith("222,")); // بقى بس الثاني

        assertFalse(b1.isBorrowed());
    }

    @Test
    void markLoanReturned_ReturnsFalse_WhenNoMatch() throws Exception {
        Files.writeString(borrowedPath,
                "111,B1," + LocalDate.now() + ",user1\n");
        boolean result = FileControler.markLoanReturned("user2", "222");
        assertFalse(result);
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsFalse_WhenUserNullOrFileMissing() {
        assertFalse(FileControler.userHasActiveLoansOrFines(null));
        User u = new User("F", "L", "user1", "e", "p");
        assertFalse(FileControler.userHasActiveLoansOrFines(u)); // no file
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsTrue_WhenActiveLoanOrOverdueOrBadDate() throws Exception {
        User u = new User("F", "L", "user1", "e", "p");

        // 1) قرض نشِط (<=28 يوم)
        LocalDate d1 = LocalDate.now().minusDays(5);
        Files.writeString(borrowedPath,
                "111,B1," + d1 + ",user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));

        // 2) تاريخ خربان → true
        Files.writeString(borrowedPath,
                "111,B1,not-a-date,user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));

        // 3) متأخر >28 يوم → true
        LocalDate dLate = LocalDate.now().minusDays(40);
        Files.writeString(borrowedPath,
                "111,B1," + dLate + ",user1\n");
        assertTrue(FileControler.userHasActiveLoansOrFines(u));
    }

    @Test
    void userHasActiveLoansOrFines_ReturnsFalse_WhenNoRowForUser() throws Exception {
        User u = new User("F", "L", "user1", "e", "p");
        Files.writeString(borrowedPath,
                "111,B1," + LocalDate.now() + ",otherUser\n");
        assertFalse(FileControler.userHasActiveLoansOrFines(u));
    }

    @Test
    void addFine_And_getTotalFineForUser_WorksCorrectly() throws Exception {
        Path pricesPath = Paths.get(FileControler.PRICES_PATH);
        Files.deleteIfExists(pricesPath);

        // نضيف غرامتين لنفس اليوزر وواحدة ليوزر آخر
        FileControler.addFine("user1", "111", 10.0);
        FileControler.addFine("user1", "222", 5.5);
        FileControler.addFine("other", "333", 7.0);

        double totalUser1 = FileControler.getTotalFineForUser("user1");
        double totalOther = FileControler.getTotalFineForUser("other");
        double totalNone  = FileControler.getTotalFineForUser("nope");

        assertEquals(15.5, totalUser1, 0.0001);
        assertEquals(7.0,  totalOther, 0.0001);
        assertEquals(0.0,  totalNone,  0.0001);

        assertTrue(FileControler.hasOutstandingFine("user1"));
        assertFalse(FileControler.hasOutstandingFine("nope"));
    }

    @Test
    void clearFineForUserAndBook_RemovesOnlyMatchingFine() throws Exception {
        Path pricesPath = Paths.get(FileControler.PRICES_PATH);
        Files.deleteIfExists(pricesPath);

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

    @Test
    void logMail_AppendsMailRecordToFile() throws Exception {
        Path mailsPath = Paths.get(FileControler.MAILS_PATH);
        Files.deleteIfExists(mailsPath);

        FileControler.logMail("user1", "u1@mail.com", "Test Subject");

        assertTrue(Files.exists(mailsPath));
        List<String> lines = Files.readAllLines(mailsPath);
        assertEquals(1, lines.size());

        String[] p = lines.get(0).split(",");
        assertEquals("user1",      p[0].trim());
        assertEquals("u1@mail.com",p[1].trim());
        assertEquals("Test Subject", p[2].trim());
        // p[3] = timestamp → بس نتأكد إنه موجود
        assertTrue(p[3].trim().length() > 0);
    }

    @Test
    void appendLoanRecord_And_loadLoansFromFile_CreateAndReadLoanRecords() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now();
        LocalDate due   = start.plusDays(28);

        FileControler.appendLoanRecord("user1", "111", "Clean Code", start, due);

        List<FileControler.LoanRecord> records = FileControler.loadLoansFromFile();
        assertEquals(1, records.size());

        FileControler.LoanRecord r = records.get(0);
        assertEquals("user1",      r.username);
        assertEquals("111",        r.isbn);
        assertEquals("Clean Code", r.title);
        assertEquals(start,        r.startDate);
        assertEquals(due,          r.dueDate);
        assertEquals("BORROWED",   r.status);
        assertEquals(0.0,          r.fee, 0.0001);
    }

    @Test
    void updateLoanStatus_ChangesStatusAndFee_WhenLoanMatches() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate due   = start.plusDays(28);

        // نضيف ريكورد واحد
        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        List<FileControler.LoanRecord> list = new ArrayList<>();
        list.add(rec);
        FileControler.saveLoansToFile(list);

        boolean updated = FileControler.updateLoanStatus("user1", "111", start, "OVERDUE", 15.0);
        assertTrue(updated);

        List<FileControler.LoanRecord> after = FileControler.loadLoansFromFile();
        assertEquals(1, after.size());
        FileControler.LoanRecord r = after.get(0);
        assertEquals("OVERDUE", r.status);
        assertEquals(15.0,      r.fee, 0.0001);
    }

    @Test
    void updateLoanStatus_ReturnsFalse_WhenNoMatchingLoan() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(5);
        LocalDate due   = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        boolean updated = FileControler.updateLoanStatus("user1", "222", start, "RETURNED", 0.0);
        assertFalse(updated);
    }

    @Test
    void markLoanReturnedInFile_UpdatesStatusToReturned() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate due   = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        boolean ok = FileControler.markLoanReturnedInFile("user1", "111", start.toString(), due.toString());
        assertTrue(ok);

        List<FileControler.LoanRecord> list = FileControler.loadLoansFromFile();
        assertEquals(1, list.size());
        assertEquals("RETURNED", list.get(0).status);
    }

    @Test
    void markLoanOverdueInFile_UpdatesStatusToOverdueAndFee() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(40);
        LocalDate due   = start.plusDays(28);

        FileControler.LoanRecord rec =
                new FileControler.LoanRecord("user1", "111", "Clean Code", start, due, "BORROWED", 0.0);
        FileControler.saveLoansToFile(List.of(rec));

        boolean ok = FileControler.markLoanOverdueInFile("user1", "Clean Code", start.toString(), 20.0);
        assertTrue(ok);

        List<FileControler.LoanRecord> list = FileControler.loadLoansFromFile();
        assertEquals(1, list.size());
        FileControler.LoanRecord r = list.get(0);
        assertEquals("OVERDUE", r.status);
        assertEquals(20.0,      r.fee, 0.0001);
    }

    @Test
    void loadAllLoansRows_ReadsRowsFromLoanFile() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

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
    void searchLoansRows_FiltersByUserBookOrStatus() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

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
    void autoUpdateOverdueLoans_SetsStatusOverdueOrBorrowedCorrectly() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate today = LocalDate.now();

        FileControler.LoanRecord r1 =
                new FileControler.LoanRecord(
                        "user1","111","Old Book",
                        today.minusDays(40),
                        today.minusDays(5),   // due قبل 5 أيام → متأخر
                        "BORROWED",0.0);

        FileControler.LoanRecord r2 =
                new FileControler.LoanRecord(
                        "user2","222","New Book",
                        today.minusDays(3),
                        today.plusDays(10),   // لسه بدري → BORROWED
                        "BORROWED",0.0);

        FileControler.saveLoansToFile(List.of(r1, r2));

        FileControler.autoUpdateOverdueLoans();

        List<FileControler.LoanRecord> after = FileControler.loadLoansFromFile();
        assertEquals(2, after.size());

        FileControler.LoanRecord a1 = after.get(0);
        FileControler.LoanRecord a2 = after.get(1);

        assertEquals("OVERDUE", a1.status);
        assertTrue(a1.fee > 0.0);

        assertEquals("BORROWED", a2.status);
    }

    @Test
    void hasOverdueCDs_ReturnsTrue_WhenUserHasOverdueCDLoan() throws Exception {
        // نحضّر Loan.txt مباشرة
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(10);
        LocalDate due   = start.plusDays(7); // انتهى قبل 3 أيام

        String line = String.join(",",
                "user1",
                "CD-001",
                "Some CD",
                start.toString(),
                due.toString(),
                "BORROWED",
                "0.0"
        );
        Files.writeString(loansPath, line + System.lineSeparator(), StandardCharsets.UTF_8);

        // BooksList فيها Book يمثل CD
        Book cd = new Book("Some CD", "Artist", "CD-001", true);
        cd.setMediaType("CD");
        FileControler.BooksList.add(cd);

        User u = new User("F","L","user1","e","p");

        assertTrue(FileControler.hasOverdueCDs(u));
    }

    @Test
    void hasOverdueCDs_ReturnsFalse_WhenNoCDOrNotOverdue() throws Exception {
        Path loansPath = Paths.get(FileControler.LOANS_PATH);
        Files.deleteIfExists(loansPath);

        LocalDate start = LocalDate.now().minusDays(2);
        LocalDate due   = start.plusDays(7); // لسه مش متأخر

        String line = String.join(",",
                "user1",
                "111",
                "Clean Code",
                start.toString(),
                due.toString(),
                "BORROWED",
                "0.0"
        );
        Files.writeString(loansPath, line + System.lineSeparator(), StandardCharsets.UTF_8);

        // Book مش CD
        Book b = new Book("Clean Code", "Robert", "111", true);
        b.setMediaType("BOOK");
        FileControler.BooksList.add(b);

        User u = new User("F","L","user1","e","p");

        assertFalse(FileControler.hasOverdueCDs(u));
    }

    @Test
    void addRenewRequest_Then_HasAndClearRenewRequest_Works() throws Exception {
        Path renewPath = Paths.get(FileControler.RENEW_REQUESTS_PATH);
        Files.deleteIfExists(renewPath);

        User u = new User("F","L","user1","e","p");
        Book b = new Book("Clean Code","Robert","111", true);
        LocalDate borrowDate = LocalDate.now().minusDays(10);

        // نفترض إن Loan عندك فيه constructor (Media, User, LocalDate, int periodDays)
        Loan loan = new Loan(b, u, borrowDate, 28);

        FileControler.addRenewRequest(u, loan);

        assertTrue(Files.exists(renewPath));
        assertTrue(FileControler.hasRenewRequest("user1","111", borrowDate));

        FileControler.clearRenewRequest("user1","111", borrowDate);

        assertFalse(FileControler.hasRenewRequest("user1","111", borrowDate));
    }

    @Test
    void updateUserInFile_UpdatesMatchingUserLine() throws Exception {
        Path usersPathLocal = Paths.get(FileControler.USERS_PATH);
        Files.deleteIfExists(usersPathLocal);

        String content =
                "F1,L1,u1,u1@mail.com,p1,B1;B2,false\n" +
                        "F2,L2,u2,u2@mail.com,p2,,true\n";
        Files.writeString(usersPathLocal, content, StandardCharsets.UTF_8);

        User updated = new User("NewF","NewL","u1","new@mail.com","newPass", new String[]{"X"});
        updated.setAdmin(true);

        boolean ok = FileControler.updateUserInFile(updated);
        assertTrue(ok);

        List<String> lines = Files.readAllLines(usersPathLocal);
        assertEquals(2, lines.size());
        assertEquals("NewF,NewL,u1,new@mail.com,newPass,X,true", lines.get(0));
    }

    @Test
    void updateLibrarianInFile_UpdatesMatchingLibrarianLine() throws Exception {
        Path libsPath = Paths.get(FileControler.LIBRARIANS_PATH);
        Files.deleteIfExists(libsPath);

        String content =
                "F1,L1,lib1,l1@mail.com,p1\n" +
                        "F2,L2,lib2,l2@mail.com,p2\n";
        Files.writeString(libsPath, content, StandardCharsets.UTF_8);

        User upd = new User("NF","NL","lib2","new@mail.com","newPass", new String[0]);

        boolean ok = FileControler.updateLibrarianInFile(upd);
        assertTrue(ok);

        List<String> lines = Files.readAllLines(libsPath);
        assertEquals(2, lines.size());
        assertEquals("NF,NL,lib2,new@mail.com,newPass", lines.get(1));
    }


}
