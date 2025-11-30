package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

class LoginControlTest {

    private Path adminPath;
    private Path usersPath;
    private Path librarianPath;

    @BeforeEach
    void setUp() throws Exception {
        // Create a temp directory for this test class
        Path tempDir = Files.createTempDirectory("logincontrol-test");

        // Create three temp files for Admin / Users / Librarians
        adminPath = tempDir.resolve("Admin.txt");
        usersPath = tempDir.resolve("Users.txt");
        librarianPath = tempDir.resolve("Librarian.txt");

        Files.createFile(adminPath);
        Files.createFile(usersPath);
        Files.createFile(librarianPath);

        // Point LoginControl to these files instead of src/main/InfoBase/...
        LoginControl.setAdminPath(adminPath.toString());
        LoginControl.setUsersPath(usersPath.toString());
        LoginControl.setLibrarianPath(librarianPath.toString());
    }

    // ---------------------------------------------------------
    // isAdmin()
    // ---------------------------------------------------------

    @Test
    void testIsAdmin_ReturnsTrueForCorrectCredentials() throws Exception {
        // username,password
        String content = "adminUser,secret\nanother,pass\n";
        Files.writeString(adminPath, content, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertTrue(LoginControl.isAdmin("adminUser", "secret"));
        assertFalse(LoginControl.isAdmin("adminUser", "wrong"));
        assertFalse(LoginControl.isAdmin("ghost", "secret"));
    }

    @Test
    void testIsAdmin_ReturnsFalseWhenFileMissing() throws Exception {
        Files.deleteIfExists(adminPath);

        assertFalse(LoginControl.isAdmin("any", "any"));
    }

    // ---------------------------------------------------------
    // getUser() + isRegisteredUser()
    // ---------------------------------------------------------

    @Test
    void testGetUser_ParsesAllFieldsIncludingBooksAndAdminFlag() throws Exception {
        // Format:
        // firstName,lastName,username,email,password,books,isAdmin
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,Book1;Book2,true\n" +
                        "Jane,Smith,jsmith,jsmith@mail.com,pass456,,false\n";
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        User u = LoginControl.getUser("jdoe", "pass123");
        assertNotNull(u);
        assertEquals("John", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals("jdoe", u.getUsername());
        assertEquals("jdoe@mail.com", u.getEmail());
        assertEquals("pass123", u.getPassword());
        assertTrue(u.isAdmin());

        String[] books = u.getBooks();
        assertEquals(2, books.length);
        assertEquals("Book1", books[0]);
        assertEquals("Book2", books[1]);

        // second user
        User u2 = LoginControl.getUser("jsmith", "pass456");
        assertNotNull(u2);
        assertEquals("jsmith", u2.getUsername());
        assertFalse(u2.isAdmin());
        assertEquals(0, u2.getBooks().length);
    }

    @Test
    void testGetUser_ReturnsNullWhenUserNotFound() throws Exception {
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,,false\n";
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        User u = LoginControl.getUser("unknown", "whatever");
        assertNull(u);
    }

    @Test
    void testGetUser_ReturnsNullWhenPasswordIncorrect() throws Exception {
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,,false\n";
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        User u = LoginControl.getUser("jdoe", "wrong");
        assertNull(u);
    }

    @Test
    void testIsRegisteredUser_TrueForNonAdminUser() throws Exception {
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,,false\n"; // isAdmin=false
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertTrue(LoginControl.isRegisteredUser("jdoe", "pass123"));
    }

    @Test
    void testIsRegisteredUser_FalseForAdminUser() throws Exception {
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,,true\n"; // isAdmin=true
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertFalse(LoginControl.isRegisteredUser("jdoe", "pass123"));
    }

    @Test
    void testIsRegisteredUser_FalseWhenUserNotFound() throws Exception {
        String line =
                "John,Doe,jdoe,jdoe@mail.com,pass123,,false\n";
        Files.writeString(usersPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertFalse(LoginControl.isRegisteredUser("ghost", "pass123"));
    }

    @Test
    void testGetUser_ReturnsNullWhenFileMissing() throws Exception {
        Files.deleteIfExists(usersPath);

        User u = LoginControl.getUser("any", "any");
        assertNull(u);
    }

    // ---------------------------------------------------------
    // isLibrarian() + getLibrarian()
    // ---------------------------------------------------------

    @Test
    void testIsLibrarian_TrueWithSimpleFormat_usernamePassword() throws Exception {
        // supported format 1: username,password
        String line =
                "libUser1,libPass\n" +
                        "someone,else\n";
        Files.writeString(librarianPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertTrue(LoginControl.isLibrarian("libUser1", "libPass"));
        assertFalse(LoginControl.isLibrarian("libUser1", "wrong"));
        assertFalse(LoginControl.isLibrarian("unknown", "libPass"));
    }

    @Test
    void testIsLibrarian_TrueWithExtendedFormat_usernamePasswordFirstLast() throws Exception {
        // supported format 2: username,password,firstName,lastName
        String line =
                "libUser1,libPass,Ali,Ahmad\n";
        Files.writeString(librarianPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        assertTrue(LoginControl.isLibrarian("libUser1", "libPass"));

        User lib = LoginControl.getLibrarian("libUser1", "libPass");
        assertNotNull(lib);
        assertEquals("Ali", lib.getFirstName());
        assertEquals("Ahmad", lib.getLastName());
        assertEquals("libUser1", lib.getUsername());
        assertEquals("", lib.getEmail());  // as per implementation
        assertFalse(lib.isAdmin());        // librarians are not admin
    }

    @Test
    void testGetLibrarian_ReturnsNullWhenCredentialsWrong() throws Exception {
        String line =
                "libUser1,libPass,Ali,Ahmad\n";
        Files.writeString(librarianPath, line, StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);

        User libWrongUser = LoginControl.getLibrarian("other", "libPass");
        User libWrongPass = LoginControl.getLibrarian("libUser1", "wrong");

        assertNull(libWrongUser);
        assertNull(libWrongPass);
    }

    @Test
    void testGetLibrarian_ReturnsNullWhenFileMissing() throws Exception {
        Files.deleteIfExists(librarianPath);

        User lib = LoginControl.getLibrarian("any", "any");
        assertNull(lib);
        assertFalse(LoginControl.isLibrarian("any", "any"));
    }
}
