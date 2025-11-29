package org.Code;

import org.junit.jupiter.api.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

public class FileControlerTest {

    Path booksPath = Paths.get(FileControler.BOOKS_PATH);
    Path usersPath = Paths.get(FileControler.USERS_PATH);

    @BeforeEach
    void setup() throws Exception {
        // Ensure files exist and clear them
        Files.createDirectories(booksPath.getParent());
        Files.writeString(booksPath, "");
        Files.writeString(usersPath, "");

        FileControler.BooksList.clear();
        FileControler.UserList.clear();
    }

    // ============================================================
    // 1) TEST LOADING BOOKS FILE
    // ============================================================
    @Test
    void testFillBooksDataAsync_LoadsBooksCorrectly() throws Exception {
        // Write mock file
        Files.writeString(booksPath,
                "HP,Rowling,123,false\n" +
                        "LOTR,Tolkien,456,true\n"
        );

        FileControler.fillBooksDataAsync();
        Thread.sleep(100); // Wait for async thread

        assertEquals(2, FileControler.BooksList.size());

        assertEquals("HP", FileControler.BooksList.get(0).getName());
        assertEquals("Rowling", FileControler.BooksList.get(0).getAuthor());
        assertEquals("123", FileControler.BooksList.get(0).getISBN());
        assertFalse(FileControler.BooksList.get(0).isBorrowed());
    }

    // ============================================================
    // 2) TEST LOADING USERS FILE
    // ============================================================
    @Test
    void testFillUsersDataAsync_LoadsUsersCorrectly() throws Exception {

        Files.writeString(usersPath,
                "John,Doe,john,mail@gmail.com,pass,HP;LOTR,true\n" +
                        "Jane,Lee,jane,jane@gmail.com,word,,false\n"
        );

        FileControler.fillUsersDataAsync();
        Thread.sleep(100);

        assertEquals(2, FileControler.UserList.size());

        User u = FileControler.UserList.get(0);
        assertEquals("John", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals("john", u.getUsername());
        assertEquals("mail@gmail.com", u.getEmail());
        assertEquals("pass", u.getPassword());
        assertTrue(u.isAdmin());
        assertArrayEquals(new String[]{"HP", "LOTR"}, u.getBooks());
    }

    // ============================================================
    // 3) TEST addBookAsync (WRITES TO FILE)
    // ============================================================
    @Test
    void testAddBookAsync_WritesToFile() throws Exception {
        Book b = new Book("Harry Potter", "J.K. Rowling", "123", false);

        FileControler.addBookAsync(b);
        Thread.sleep(150);

        String content = Files.readString(booksPath).trim();

        assertEquals("Harry Potter,J.K. Rowling,123,false", content);
    }

    // ============================================================
    // 4) TEST addUserAsync (WRITES TO FILE)
    // ============================================================
    @Test
    void testAddUserAsync_WritesToFile() throws Exception {
        User u = new User("John", "Doe", "john", "j@e.com", "pass", new String[]{"HP"});
        u.setAdmin(true);

        FileControler.addUserAsync(u);
        Thread.sleep(150);

        String content = Files.readString(usersPath).trim();

        assertEquals("John,Doe,john,j@e.com,pass,HP,true", content);
    }

    // ============================================================
    // 5) TEST searchUser (SYNC)
    // ============================================================
    @Test
    void testSearchUser_ReturnsCorrectBoolean() throws Exception {
        Files.writeString(usersPath,
                "A,B,admin,aa@mail.com,123,,true\n" +
                        "User,Test,user,user@mail.com,pass,,false\n"
        );

        assertTrue(FileControler.searchUser("admin"));
        assertTrue(FileControler.searchUser("user"));
        assertFalse(FileControler.searchUser("notFound"));
    }

    // ============================================================
    // 6) TEST searchUserAsync (ASYNC)
    // ============================================================
    @Test
    void testSearchUserAsync_CallbackReceivesCorrectValue() throws Exception {
        Files.writeString(usersPath,
                "A,B,admin,aa@mail.com,123,,true\n"
        );

        final boolean[] result = {false};

        FileControler.searchUserAsync("admin", found -> result[0] = found);

        Thread.sleep(150);

        assertTrue(result[0]);
    }
}
