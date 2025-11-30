package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AdminControlTest {

    private AdminControl admin;
    private Path booksPath;

    @BeforeEach
    void setUp() throws Exception {
        admin = new AdminControl();

        // نظف الـ in-memory list
        FileControler.BooksList.clear();

        // جهّز ملف Books.txt المستخدم في FileControler
        booksPath = Paths.get(FileControler.BOOKS_PATH).toAbsolutePath();

        if (!Files.exists(booksPath.getParent())) {
            Files.createDirectories(booksPath.getParent());
        }

        Files.writeString(
                booksPath,
                "",
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // ---------------------------------------------------------
    // searchBook()
    // ---------------------------------------------------------

    @Test
    void testSearchBook_ByName() {
        Book b1 = new Book("Harry Potter", "J.K. Rowling", "123", false);
        FileControler.BooksList.add(b1);

        Book result = admin.searchBook("Harry");

        assertNotNull(result);
        assertEquals(b1, result);   // نفس الريفرنس
    }

    @Test
    void testSearchBook_ByAuthor() {
        Book b1 = new Book("The Hobbit", "Tolkien", "456", false);
        FileControler.BooksList.add(b1);

        Book result = admin.searchBook("Tolkien");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    @Test
    void testSearchBook_ByISBN() {
        Book b1 = new Book("Dune", "Frank Herbert", "999-888", false);
        FileControler.BooksList.add(b1);

        Book result = admin.searchBook("999-888");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    @Test
    void testSearchBook_NoMatch_ReturnsNull() {
        FileControler.BooksList.add(new Book("Book A", "Author A", "111", false));

        Book result = admin.searchBook("XYZ");

        assertNull(result);
    }

    @Test
    void testSearchBook_EmptyBooksList_ReturnsNull() {
        Book result = admin.searchBook("Anything");
        assertNull(result);
    }

    @Test
    void testSearchBook_PartialMatch_ReturnsFirstMatchingBook() {
        Book b1 = new Book("Java Programming", "Someone", "111", false);
        Book b2 = new Book("Advanced Java", "Another Author", "222", false);

        FileControler.BooksList.add(b1);
        FileControler.BooksList.add(b2);

        Book result = admin.searchBook("Java");

        assertNotNull(result);
        assertEquals(b1, result);   // أول واحد في الليست
    }

    @Test
    void testSearchBook_CaseSensitiveBehavior() {
        Book b1 = new Book("harry potter", "j.k. rowling", "123", false);
        FileControler.BooksList.add(b1);

        // contains بدون toLowerCase → حساس لحالة الأحرف
        Book byName   = admin.searchBook("Harry");
        Book byAuthor = admin.searchBook("Rowling");

        assertNull(byName);
        assertNull(byAuthor);
    }

    @Test
    void testSearchBook_EmptySearchString_ReturnsFirstBook() {
        Book b1 = new Book("Book A", "Author A", "111", false);
        Book b2 = new Book("Book B", "Author B", "222", false);

        FileControler.BooksList.add(b1);
        FileControler.BooksList.add(b2);

        // "".contains("") = true → أول كتاب هيتطابق
        Book result = admin.searchBook("");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    // ---------------------------------------------------------
    // addBook()
    // ---------------------------------------------------------

    @Test
    void testAddBook_WritesCorrectlyToFile() throws Exception {
        // تأكد إن الملف فاضي
        Files.writeString(
                booksPath,
                "",
                java.nio.charset.StandardCharsets.UTF_8,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING
        );

        // استدعاء addBook (تستخدم addBookAsync داخليًا)
        admin.addBook("Harry Potter", "J.K. Rowling", "123", false);

        // استنى شوية عشان الـ thread يخلص
        Thread.sleep(200);

        String content = Files.readString(booksPath).trim();
        assertEquals("Harry Potter,J.K. Rowling,123,false", content);
    }
}
