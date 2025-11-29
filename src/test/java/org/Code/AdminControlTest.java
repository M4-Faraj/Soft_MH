package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class AdminControlTest {

    private AdminControl admin;

    @BeforeEach
    void setup() {
        admin = new AdminControl();
        FileControler.BooksList.clear(); // important
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
        assertEquals(b1, result);
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
    void testSearchBook_NotFound() {
        FileControler.BooksList.add(new Book("Book A", "Author A", "111", false));

        Book result = admin.searchBook("XYZ");

        assertNull(result);
    }

    // ---------------------------------------------------------
    // addBook()
    // ---------------------------------------------------------

    @Test
    void testAddBook_WritesCorrectlyToFile() throws Exception {
        // Clean Books.txt before test
        Path booksPath = Paths.get(FileControler.BOOKS_PATH);
        Files.createDirectories(booksPath.getParent());
        Files.writeString(booksPath, "");

        // Create AdminControl
        AdminControl admin = new AdminControl();

        // Add book asynchronously
        admin.addBook("Harry Potter", "J.K. Rowling", "123", false);

        // Wait for async write to finish
        Thread.sleep(150);

        // Read file content
        String content = Files.readString(booksPath).trim();

        assertEquals("Harry Potter,J.K. Rowling,123,false", content);
    }

}

