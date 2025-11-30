package org.Code;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsersTest {

    private Users users;

    @BeforeEach
    void setUp() {
        users = new Users();
    }

    // ---------------------------------------------------------
    // addUser()
    // ---------------------------------------------------------

    @Test
    void testAddUser_AddsUserToList() {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass");
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");

        users.addUser(u1);
        users.addUser(u2);

        List<User> list = users.getAllUsers();
        assertEquals(2, list.size());
        assertTrue(list.contains(u1));
        assertTrue(list.contains(u2));
    }

    @Test
    void testAddUser_AllowsNullUser() {
        // الكود الحالي ما يمنع null → نثبّت السلوك في التست
        users.addUser(null);

        List<User> list = users.getAllUsers();
        assertEquals(1, list.size());
        assertNull(list.get(0));
    }

    // ---------------------------------------------------------
    // removeUser()
    // ---------------------------------------------------------

    @Test
    void testRemoveUser_RemovesExistingUser() {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass");
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");

        users.addUser(u1);
        users.addUser(u2);

        users.removeUser(u1);

        List<User> list = users.getAllUsers();
        assertEquals(1, list.size());
        assertFalse(list.contains(u1));
        assertTrue(list.contains(u2));
    }

    @Test
    void testRemoveUser_NonExistingUser_DoesNothing() {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass");
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");
        User other = new User("Other", "User", "other", "other@mail.com", "x");

        users.addUser(u1);
        users.addUser(u2);

        users.removeUser(other); // مش موجود في الليست

        List<User> list = users.getAllUsers();
        assertEquals(2, list.size());
        assertTrue(list.contains(u1));
        assertTrue(list.contains(u2));
    }

    @Test
    void testRemoveUser_OnEmptyList_DoesNotCrash() {
        User u = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass");

        // ما في يوزرز، بس لازم ما يعمل Exception
        users.removeUser(u);

        assertTrue(users.getAllUsers().isEmpty());
    }

    // ---------------------------------------------------------
    // getAllUsers()
    // ---------------------------------------------------------

    @Test
    void testGetAllUsers_ReturnsInternalListReference() {
        User u1 = new User("John", "Doe", "jdoe", "jdoe@mail.com", "pass");
        users.addUser(u1);

        List<User> list = users.getAllUsers();
        assertEquals(1, list.size());
        assertTrue(list.contains(u1));

        // لو عدلنا الليست نفسها، لازم ينعكس في الكلاس لأنه بيرجع نفس المرجع
        User u2 = new User("Jane", "Smith", "jsmith", "jsmith@mail.com", "pass2");
        list.add(u2);

        assertEquals(2, users.getAllUsers().size());
        assertTrue(users.getAllUsers().contains(u2));
    }

    @Test
    void testGetAllUsers_InitiallyEmpty() {
        List<User> list = users.getAllUsers();
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }
}
