package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {

    private Book book;

    @BeforeEach
    void setup() {
        book = new Book("Harry Potter", "J.K. Rowling", "123", false);
    }

    // === Test getters ===
    @Test
    void testGetName() {
        assertEquals("Harry Potter", book.getName());
    }

    @Test
    void testGetAuthor() {
        assertEquals("J.K. Rowling", book.getAuthor());
    }

    @Test
    void testGetISBN() {
        assertEquals("123", book.getISBN());
    }

    @Test
    void testIsBorrowed() {
        // Initially the book is not borrowed
        Book book = new Book("Harry Potter", "J.K. Rowling", "123", false);
        assertFalse(book.isBorrowed(), "Book should not be borrowed initially");

        // Mark the book as borrowed
        book.updateBorrowed(true);
        assertTrue(book.isBorrowed(), "Book should be borrowed after updating");

        // Mark it as returned
        book.updateBorrowed(false);
        assertFalse(book.isBorrowed(), "Book should not be borrowed after returning");
    }


    // === Test setters / update methods ===
    @Test
    void testUpdateName() {
        book.updateName("The Hobbit");
        assertEquals("The Hobbit", book.getName());
    }

    @Test
    void testUpdateAuthor() {
        book.updateAuthor("Tolkien");
        assertEquals("Tolkien", book.getAuthor());
    }

    @Test
    void testUpdateISBN() {
        book.updateISBN("456");
        assertEquals("456", book.getISBN());
    }

    @Test
    void testUpdateBorrowed() {
        book.updateBorrowed(true);
        assertTrue(book.isborrowed());
    }

    @Test
    void testSetName() {
        book.setName("New Name");
        assertEquals("New Name", book.getName());
    }

    @Test
    void testSetAuthor() {
        book.setAuthor("New Author");
        assertEquals("New Author", book.getAuthor());
    }

    @Test
    void testSetISBN() {
        book.setISBN("999");
        assertEquals("999", book.getISBN());
    }

    @Test
    void testSetBorrowed() {
        book.setborrowed(true);
        assertTrue(book.isborrowed());
    }

    // === Test combined usage ===
    @Test
    void testBookLifecycle() {
        book.updateName("Updated Book");
        book.updateAuthor("Updated Author");
        book.updateISBN("321");
        book.updateBorrowed(true);

        assertEquals("Updated Book", book.getName());
        assertEquals("Updated Author", book.getAuthor());
        assertEquals("321", book.getISBN());
        assertTrue(book.isborrowed());
    }

}
