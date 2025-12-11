package org.Code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookTest {

    // ---------------------------------------------------------
    // Constructor & basic getters
    // ---------------------------------------------------------

    @Test
    void testConstructor_SetsAllFieldsCorrectly() {
        Book book = new Book("Clean Code", "Robert C. Martin", "111-222", true);

        // Book-specific fields
        assertEquals("Clean Code", book.getName());
        assertEquals("Robert C. Martin", book.getAuthor());
        assertEquals("111-222", book.getISBN());

        // Media fields
        assertEquals("Clean Code", book.getTitle());
        assertTrue(book.isBorrowed());
        assertTrue(book.isBorrowed());  // legacy method
    }

    @Test
    void testConstructor_AllowsEmptyStrings() {
        Book book = new Book("", "", "", false);

        assertEquals("", book.getName());
        assertEquals("", book.getAuthor());
        assertEquals("", book.getISBN());
        assertEquals("", book.getTitle());
        assertFalse(book.isBorrowed());
        assertFalse(book.isBorrowed());
    }

    // ---------------------------------------------------------
    // Update methods (name, author, ISBN)
    // ---------------------------------------------------------

    @Test
    void testUpdateName_ChangesNameOnly() {
        Book book = new Book("Old Title", "Author", "123", false);

        book.updateName("New Title");

        assertEquals("New Title", book.getName());
        // NOTE: current implementation does NOT update Media.title
        assertEquals("Old Title", book.getTitle());
    }

    @Test
    void testUpdateAuthor_ChangesAuthor() {
        Book book = new Book("Title", "Old Author", "123", false);

        book.updateAuthor("New Author");

        assertEquals("New Author", book.getAuthor());
    }

    @Test
    void testUpdateISBN_ChangesISBN() {
        Book book = new Book("Title", "Author", "123", false);

        book.updateISBN("999-888");

        assertEquals("999-888", book.getISBN());
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    @Test
    void testSetters_WorkAsExpected() {
        Book book = new Book("T1", "A1", "111", false);

        book.setName("T2");
        book.setAuthor("A2");
        book.setISBN("222");

        assertEquals("T2", book.getName());
        assertEquals("A2", book.getAuthor());
        assertEquals("222", book.getISBN());
    }

    // ---------------------------------------------------------
    // Borrowed / availability state
    // ---------------------------------------------------------

    @Test
    void testUpdateBorrowed_ChangesBorrowedFlag() {
        Book book = new Book("Title", "Author", "123", false);

        assertFalse(book.isBorrowed());


        book.updateBorrowed(true);

        assertTrue(book.isBorrowed());

        book.updateBorrowed(false);

        assertFalse(book.isBorrowed());

    }

    @Test
    void testBorrowedFlagFromConstructor() {
        Book available = new Book("Title1", "Author1", "111", false);
        Book borrowed  = new Book("Title2", "Author2", "222", true);

        assertFalse(available.isBorrowed());


        assertTrue(borrowed.isBorrowed());

    }

    // ---------------------------------------------------------
    // Loan / fine rules (business rules)
    // ---------------------------------------------------------

    @Test
    void testBorrowDuration_Is28Days() {
        Book book = new Book("Title", "Author", "123", false);

        assertEquals(28, book.getBorrowDurationDays());
        assertEquals(28, book.getLoanDuration());
    }

    @Test
    void testFineRules_Are10NISPerDay() {
        Book book = new Book("Title", "Author", "123", false);

        assertEquals(10, book.getOverdueFine());
        assertEquals(10.0, book.getFinePerDay(), 0.0001);
    }

    @Test
    void testConstructorWithCategoryOnly_DefaultsAndCustomCategory() {
        // category = null  => "Book"
        Book b1 = new Book("N1", "A1", "I1", false, (String) null);

        assertNull(b1.getName());
        assertEquals("N1", b1.getTitle());
        assertEquals("A1", b1.getAuthor());
        assertEquals("I1", b1.getISBN());
        assertEquals("Book", b1.getCategory());
        assertFalse(b1.isBorrowed());

        // category = blank => "Book"
        Book b2 = new Book("N2", "A2", "I2", true, "   ");

        assertNull(b2.getName());
        assertEquals("N2", b2.getTitle());
        assertEquals("Book", b2.getCategory());
        assertTrue(b2.isBorrowed());

        // custom category stays as-is
        Book b3 = new Book("N3", "A3", "I3", false, "Science");

        assertEquals("Science", b3.getCategory());
    }

}
