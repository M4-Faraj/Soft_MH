package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControlTest {

    private Path usersPath;

    @BeforeEach
    void setUp() throws Exception {
        usersPath = Paths.get("InfoBase", "Users.txt").toAbsolutePath();

        // تأكد إن الفولدر موجود
        if (!Files.exists(usersPath.getParent())) {
            Files.createDirectories(usersPath.getParent());
        }

        // صفّي الملف أو أنشئه من جديد
        Files.writeString(
                usersPath,
                "",
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }

    // ---------------------------------------------------------
    // addUser() + getAllUsers()
    // ---------------------------------------------------------

    @Test
    void testAddUser_AndGetAllUsers_SingleUser() throws Exception {
        User u = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass",
                new String[]{"Book1", "Book2"});
        u.addFine(15.5);
        u.setAdmin(true);

        UserControl.addUser(u);

        List<User> users = UserControl.getAllUsers();
        assertEquals(1, users.size());

        User loaded = users.get(0);
        assertEquals("John", loaded.getFirstName());
        assertEquals("Doe", loaded.getLastName());
        assertEquals("jdoe", loaded.getUsername());
        assertEquals("jdoe@mail.com", loaded.getEmail());
        assertEquals("pass", loaded.getPassword());
        assertTrue(loaded.isAdmin());
        assertEquals(15.5, loaded.getFine(), 0.0001);

        String[] books = loaded.getBooks();
        assertArrayEquals(new String[]{"Book1", "Book2"}, books);
    }

    @Test
    void testAddUser_MultipleUsers_AndOrder() throws Exception {
        User u1 = new User("User", "One", "user1", "u1@mail.com", "p1",
                new String[0]);
        User u2 = new User("User", "Two", "user2", "u2@mail.com", "p2",
                new String[]{"B1"});

        UserControl.addUser(u1);
        UserControl.addUser(u2);

        List<User> users = UserControl.getAllUsers();
        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUsername());
        assertEquals("user2", users.get(1).getUsername());
    }

    @Test
    void testGetAllUsers_EmptyFile_ReturnsEmptyList() throws Exception {
        List<User> users = UserControl.getAllUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    // ---------------------------------------------------------
    // findUserByUsername()
    // ---------------------------------------------------------

    @Test
    void testFindUserByUsername_FindsCorrectUser() throws Exception {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass1");
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");

        UserControl.addUser(u1);
        UserControl.addUser(u2);

        User found = UserControl.findUserByUsername("jsmith");
        assertNotNull(found);
        assertEquals("Jane", found.getFirstName());
        assertEquals("jsmith@mail.com", found.getEmail());
    }

    @Test
    void testFindUserByUsername_NotFound_ReturnsNull() throws Exception {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass1");
        UserControl.addUser(u1);

        User found = UserControl.findUserByUsername("unknown");
        assertNull(found);
    }

    // ---------------------------------------------------------
    // removeUser()
    // ---------------------------------------------------------

    @Test
    void testRemoveUser_RemovesExistingUserFromFile() throws Exception {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass1");
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");

        UserControl.addUser(u1);
        UserControl.addUser(u2);

        boolean removed = UserControl.removeUser("jdoe");
        assertTrue(removed);

        List<User> remaining = UserControl.getAllUsers();
        assertEquals(1, remaining.size());
        assertEquals("jsmith", remaining.get(0).getUsername());
    }

    @Test
    void testRemoveUser_NonExistingUser_ReturnsFalse_AndDoesNotChangeFile() throws Exception {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass1");
        UserControl.addUser(u1);

        String before = Files.readString(usersPath);

        boolean removed = UserControl.removeUser("ghost");
        assertFalse(removed);

        String after = Files.readString(usersPath);
        assertEquals(before, after);
    }

    @Test
    void testRemoveUser_OnEmptyFile_ReturnsFalse() throws Exception {
        boolean removed = UserControl.removeUser("anyUser");
        assertFalse(removed);

        List<User> users = UserControl.getAllUsers();
        assertTrue(users.isEmpty());
    }

    // ---------------------------------------------------------
    // updateUser()
    // ---------------------------------------------------------

    @Test
    void testUpdateUser_UpdatesExistingUser() throws Exception {
        User original = new User("John", "Doe", "jdoe", "old@mail.com", "oldPass",
                new String[]{"B1"});
        original.addFine(5);
        original.setAdmin(false);

        UserControl.addUser(original);

        // create updated version with same username
        User updated = new User("Johnathan", "Doe", "jdoe", "new@mail.com", "newPass",
                new String[]{"B1", "B2"});
        updated.addFine(20);
        updated.setAdmin(true);

        boolean success = UserControl.updateUser(updated);
        assertTrue(success);

        List<User> users = UserControl.getAllUsers();
        assertEquals(1, users.size());

        User loaded = users.get(0);
        assertEquals("Johnathan", loaded.getFirstName());
        assertEquals("Doe", loaded.getLastName());
        assertEquals("jdoe", loaded.getUsername());
        assertEquals("new@mail.com", loaded.getEmail());
        assertEquals("newPass", loaded.getPassword());
        assertTrue(loaded.isAdmin());
        assertEquals(20.0, loaded.getFine(), 0.0001);

        assertArrayEquals(new String[]{"B1", "B2"}, loaded.getBooks());
    }

    @Test
    void testUpdateUser_UserNotFound_ReturnsFalse_AndFileUnchanged() throws Exception {
        User existing = new User("John", "Doe", "jdoe", "mail@mail.com", "pass",
                new String[0]);
        UserControl.addUser(existing);

        String before = Files.readString(usersPath);

        User nonExisting = new User("X", "Y", "ghost", "ghost@mail.com", "x",
                new String[0]);

        boolean success = UserControl.updateUser(nonExisting);
        assertFalse(success);

        String after = Files.readString(usersPath);
        assertEquals(before, after);
    }

    // ---------------------------------------------------------
    // التعامل مع سطور فاسدة / ناقصة في الملف (deserialize)
    // ---------------------------------------------------------

    @Test
    void testGetAllUsers_SkipsInvalidLines() throws Exception {
        // line 1: valid
        // line 2: invalid (less than 8 parts)
        String content =
                "John,Doe,jdoe,jdoe@mail.com,pass,,10.0,true\n" +
                        "invalid,line,missing,fields\n" +
                        "Jane,Smith,jsmith,jsmith@mail.com,pass2,BookX|BookY,5.0,false\n";

        Files.writeString(
                usersPath,
                content,
                StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING
        );

        List<User> users = UserControl.getAllUsers();
        assertEquals(2, users.size());

        User u1 = users.get(0);
        assertEquals("jdoe", u1.getUsername());
        assertEquals(10.0, u1.getFine(), 0.0001);
        assertTrue(u1.isAdmin());

        User u2 = users.get(1);
        assertEquals("jsmith", u2.getUsername());
        assertEquals(5.0, u2.getFine(), 0.0001);
        assertFalse(u2.isAdmin());
        assertArrayEquals(new String[]{"BookX", "BookY"}, u2.getBooks());
    }
}
