package org.Code;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AdminTest {

    @Test
    void testConstructorAndGetters() {
        Admin admin = new Admin("adminUser", "adminPass");

        assertEquals("adminUser", admin.getUsername());
        assertEquals("adminPass", admin.getPassword());
    }

    @Test
    void testSetUsername() {
        Admin admin = new Admin("oldUser", "pass");
        admin.setUsername("newUser");

        assertEquals("newUser", admin.getUsername());
    }

    @Test
    void testSetPassword() {
        Admin admin = new Admin("user", "oldPass");
        admin.setPassword("newPass");

        assertEquals("newPass", admin.getPassword());
    }
}
