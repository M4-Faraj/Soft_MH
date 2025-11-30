package org.Code;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminTest {

    // ---------------------------------------------------------
    // Constructor & basic getters
    // ---------------------------------------------------------

    @Test
    void testConstructor_SetsUsernameAndPassword() {
        Admin admin = new Admin("adminUser", "secret123");

        assertEquals("adminUser", admin.getUsername());
        assertEquals("secret123", admin.getPassword());
    }

    @Test
    void testConstructor_AllowsEmptyStrings() {
        Admin admin = new Admin("", "");

        assertEquals("", admin.getUsername());
        assertEquals("", admin.getPassword());
    }

    @Test
    void testConstructor_AllowsNullValues() {
        Admin admin = new Admin(null, null);

        assertNull(admin.getUsername());
        assertNull(admin.getPassword());
    }

    // ---------------------------------------------------------
    // Setters
    // ---------------------------------------------------------

    @Test
    void testSetters_UpdateFieldsCorrectly() {
        Admin admin = new Admin("oldUser", "oldPass");

        admin.setUsername("newUser");
        admin.setPassword("newPass");

        assertEquals("newUser", admin.getUsername());
        assertEquals("newPass", admin.getPassword());
    }

    @Test
    void testSetters_CanSetNullValues() {
        Admin admin = new Admin("adminUser", "secret");

        admin.setUsername(null);
        admin.setPassword(null);

        assertNull(admin.getUsername());
        assertNull(admin.getPassword());
    }
}
