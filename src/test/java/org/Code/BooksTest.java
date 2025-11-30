package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooksTest {

    private Books books;

    @BeforeEach
    void setUp() {
        books = new Books();
    }

    // ---------------------------------------------------------
    // searchBook() basic behavior
    // ---------------------------------------------------------

    @Test
    void testSearchBook_ByName() {
        Book b1 = new Book("Harry Potter", "J.K. Rowling", "123", false);
        Book b2 = new Book("The Hobbit", "Tolkien", "456", false);

        books.availableBooks.add(b1);
        books.availableBooks.add(b2);

        Book result = books.searchBook("Harry");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    @Test
    void testSearchBook_ByAuthor() {
        Book b1 = new Book("Book A", "Author A", "111", false);
        Book b2 = new Book("Book B", "Author B", "222", false);

        books.availableBooks.add(b1);
        books.availableBooks.add(b2);

        Book result = books.searchBook("Author B");

        assertNotNull(result);
        assertEquals(b2, result);
    }

    @Test
    void testSearchBook_ByISBN() {
        Book b1 = new Book("Book A", "Author A", "111-222", false);
        books.availableBooks.add(b1);

        Book result = books.searchBook("111-222");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    // ---------------------------------------------------------
    // searchBook() – partial matches and order
    // ---------------------------------------------------------

    @Test
    void testSearchBook_PartialMatch_ReturnsFirstMatchingBook() {
        Book b1 = new Book("Java Programming", "Someone", "111", false);
        Book b2 = new Book("Advanced Java", "Someone Else", "222", false);

        books.availableBooks.add(b1);
        books.availableBooks.add(b2);

        // both contain "Java" in name, but b1 is earlier in the list
        Book result = books.searchBook("Java");

        assertNotNull(result);
        assertEquals(b1, result);
    }

    @Test
    void testSearchBook_NoMatch_ReturnsNull() {
        Book b1 = new Book("Book A", "Author A", "111", false);
        books.availableBooks.add(b1);

        Book result = books.searchBook("XYZ");

        assertNull(result);
    }

    @Test
    void testSearchBook_EmptyList_ReturnsNull() {
        // no books at all
        Book result = books.searchBook("Anything");
        assertNull(result);
    }

    // ---------------------------------------------------------
    // Case sensitivity / exact behavior
    // ---------------------------------------------------------

    @Test
    void testSearchBook_CaseSensitiveBehavior() {
        Book b1 = new Book("harry potter", "j.k. rowling", "123", false);
        books.availableBooks.add(b1);

        // current implementation uses String.contains() without toLowerCase()
        // so this is CASE-SENSITIVE → "Harry" != "harry"
        Book resultByName   = books.searchBook("Harry");
        Book resultByAuthor = books.searchBook("Rowling");

        assertNull(resultByName);
        assertNull(resultByAuthor);
    }

    // ---------------------------------------------------------
    // Edge-ish: empty search string
    // ---------------------------------------------------------

    @Test
    void testSearchBook_EmptySearchString_ReturnsFirstBook() {
        Book b1 = new Book("Book A", "Author A", "111", false);
        Book b2 = new Book("Book B", "Author B", "222", false);

        books.availableBooks.add(b1);
        books.availableBooks.add(b2);

        // "".contains("") is true → first book will match on name
        Book result = books.searchBook("");

        assertNotNull(result);
        assertEquals(b1, result);
    }
}
