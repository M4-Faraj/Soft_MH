package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BooksTest {

    private Books books;

    @BeforeEach
    void setup() {
        books = new Books();
        books.availableBooks.add(new Book("Harry Potter", "J.K. Rowling", "123", false));
        books.availableBooks.add(new Book("The Hobbit", "Tolkien", "456", false));
        books.availableBooks.add(new Book("Hamlet", "Shakespeare", "789", false));
    }

    @Test
    void testSearchBook_ByName() {
        Book result = books.searchBook("Harry Potter");
        assertNotNull(result);
        assertEquals("Harry Potter", result.getName());
    }

    @Test
    void testSearchBook_ByAuthor() {
        Book result = books.searchBook("Tolkien");
        assertNotNull(result);
        assertEquals("The Hobbit", result.getName());
    }

    @Test
    void testSearchBook_ByISBN() {
        Book result = books.searchBook("789");
        assertNotNull(result);
        assertEquals("Hamlet", result.getName());
    }

    @Test
    void testSearchBook_NoMatch() {
        Book result = books.searchBook("Nonexistent");
        assertNull(result);
    }

    @Test
    void testSearchBook_PartialMatch() {
        Book result = books.searchBook("Harry");
        assertNotNull(result);
        assertEquals("Harry Potter", result.getName());
    }

    @Test
    void testSearchBook_EmptyString() {
        Book result = books.searchBook("");
        assertNotNull(result); // returns first book in the list
        assertEquals("Harry Potter", result.getName());
    }
}
