package org.Code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CDTest {

    @Test
    void testConstructorInitializesFieldsCorrectly() {
        CD cd = new CD("Best Hits", "Artist A", false);

        assertEquals("Best Hits", cd.getTitle());
        assertEquals("Artist A", cd.getArtist());
        assertFalse(cd.isBorrowed());
    }

    @Test
    void testBorrowedFlagTrue() {
        CD cd = new CD("Album X", "Singer Z", true);

        assertTrue(cd.isBorrowed());
    }

    @Test
    void testGetLoanDuration_Is7Days() {
        CD cd = new CD("Rock Music", "Band X", false);

        assertEquals(7, cd.getLoanDuration());
        assertEquals(7, cd.getBorrowDurationDays());
    }

    @Test
    void testGetFinePerDay_Is20NIS() {
        CD cd = new CD("Album", "Artist", false);

        assertEquals(20, cd.getFinePerDay());
        assertEquals(20, cd.getOverdueFine());
    }

    @Test
    void testChangeBorrowStatus() {
        CD cd = new CD("Album", "Artist", false);

        assertFalse(cd.isBorrowed());

        cd.updateBorrowed(true);

        assertTrue(cd.isBorrowed());
    }

    @Test
    void testArtistGetter() {
        CD cd = new CD("Soundtrack", "Hans Zimmer", false);

        assertEquals("Hans Zimmer", cd.getArtist());
    }

    @Test
    void testNullArtistAllowed() {
        CD cd = new CD("Unknown Album", null, false);

        assertNull(cd.getArtist());
    }

    @Test
    void testEmptyTitleAllowed() {
        CD cd = new CD("", "Artist", false);

        assertEquals("", cd.getTitle());
    }

    @Test
    void testBorrowedStateUpdateViaSetter() {
        CD cd = new CD("Song Collection", "DJ", false);

        assertFalse(cd.isBorrowed());

        cd.setBorrowed(true);

        assertTrue(cd.isBorrowed());
    }

    @Test
    void testCDDoesNotModifyTitleInternally() {
        String title = "My Album";
        CD cd = new CD(title, "Artist", false);

        // ensure no accidental trim/modify
        assertEquals(title, cd.getTitle());
    }
}
