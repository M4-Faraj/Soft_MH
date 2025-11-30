package org.Code;

import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControlTest {

    private static final Path TEST_USERS_FILE = Paths.get("src/test/InfoBase/TestUsers.txt");

    @BeforeAll
    void setupClass() throws IOException {
        // Ensure directory exists
        if (!Files.exists(TEST_USERS_FILE.getParent())) {
            Files.createDirectories(TEST_USERS_FILE.getParent());
        }
        // Empty the test file
        Files.writeString(TEST_USERS_FILE, "");
    }

    @BeforeEach
    void beforeEach() throws IOException {
        // Clear the test file before each test
        Files.writeString(TEST_USERS_FILE, "");
    }

    /** Helper to write test users */
    private void writeUserToFile(User u) throws IOException {
        UserControl.addUser(u);
    }

    @Test
    void testAddUserAndFindUser() throws IOException {
        User user = new User("John", "Doe", "john123", "john@gmail.com", "pass123");
        UserControl.addUser(user);

        User found = UserControl.findUserByUsername("john123");
        assertNotNull(found, "User should be found");
        assertEquals("John", found.getFirstName());
    }

    @Test
    void testRemoveUser() throws IOException {
        User user = new User("Jane", "Doe", "jane123", "jane@gmail.com", "pass123");
        UserControl.addUser(user);

        boolean removed = UserControl.removeUser("jane123");
        assertTrue(removed, "User should be removed");

        User shouldBeNull = UserControl.findUserByUsername("jane123");
        assertNull(shouldBeNull, "User should no longer exist");
    }

    @Test
    void testUpdateUser() throws IOException {
        User user = new User("Mark", "Smith", "mark123", "mark@gmail.com", "pass123");
        UserControl.addUser(user);

        // Update info
        user.setEmail("mark.new@gmail.com");
        boolean updated = UserControl.updateUser(user);
        assertTrue(updated, "Update should succeed");

        User updatedUser = UserControl.findUserByUsername("mark123");
        assertEquals("mark.new@gmail.com", updatedUser.getEmail());
    }

    @Test
    void testGetAllUsers() throws IOException {
        User user1 = new User("Alice", "Blue", "alice123", "alice@gmail.com", "pass1");
        User user2 = new User("Bob", "Green", "bob123", "bob@gmail.com", "pass2");
        UserControl.addUser(user1);
        UserControl.addUser(user2);

        List<User> users = UserControl.getAllUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("alice123")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("bob123")));
    }
}
