package org.Code;

import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoginControlTest {

    private static final String TEST_ADMIN_PATH = "src/test/InfoBase/Admin.txt";
    private static final String TEST_USERS_PATH = "src/test/InfoBase/Users.txt";

    @BeforeEach
    void setup() throws IOException {
        // Ensure directory exists
        Files.createDirectories(Paths.get("src/test/InfoBase"));

        // Create empty admin & user files
        Files.writeString(Paths.get(TEST_ADMIN_PATH), "");
        Files.writeString(Paths.get(TEST_USERS_PATH), "");
    }

    // Helper: overwrite file contents for a test
    private void writeToFile(String path, String content) throws IOException {
        Files.writeString(Paths.get(path), content);
    }

    // Test Admin Login Success
//    @Test
//    void testIsAdmin_Success() throws IOException {
//        // Write a test admin user
//        writeToFile(TEST_ADMIN_PATH, "admin,1234");
//
//        // Make sure LoginControl uses this test file (assume setter exists)
//        LoginControl.setAdminFilePath(TEST_ADMIN_PATH);
//
//        // Run the method
//        boolean result = LoginControl.isAdmin("admin", "1234");
//
//        // Assert
//        assertTrue(result, "Expected admin login to succeed");
//    }

    // Test Admin Wrong Password
    @Test
    void testIsAdmin_WrongPassword() throws IOException {
        writeToFile(TEST_ADMIN_PATH, "admin,1234");

        boolean result = LoginControl.isAdmin("admin", "wrong");
        assertFalse(result, "Expected admin login to fail with wrong password");
    }

    // Test Admin File Empty
    @Test
    void testIsAdmin_EmptyFile() {
        boolean result = LoginControl.isAdmin("admin", "1234");
        assertFalse(result, "Empty admin file should return false");
    }

    // Test Admin Invalid Lines
    @Test
    void testIsAdmin_InvalidLine() throws IOException {
        writeToFile(TEST_ADMIN_PATH, "invalid_line_without_comma");

        boolean result = LoginControl.isAdmin("admin", "1234");
        assertFalse(result, "Invalid lines should not crash and should return false");
    }

    // Test Registered User Success
//    @Test
//    void testIsRegisteredUser_Success() throws IOException {
//        // Write a test user to the file
//        writeToFile(TEST_USERS_PATH,
//                "John,Doe,john123,john@gmail.com,pass123,book1;book2,false");
//
//        // Make sure LoginControl uses this test file (assume setter exists)
//        LoginControl.setUsersFilePath(TEST_USERS_PATH);
//
//        // Run the method
//        boolean result = LoginControl.isRegisteredUser("john123", "pass123");
//
//        // Assert
//        assertTrue(result, "Expected registered user login to succeed");
//    }

    // Test Registered User Wrong Password
    @Test
    void testIsRegisteredUser_WrongPassword() throws IOException {
        writeToFile(TEST_USERS_PATH,
                "John,Doe,john123,john@gmail.com,pass123,book1,false");

        boolean result = LoginControl.isRegisteredUser("john123", "wrong");
        assertFalse(result, "Expected login failure with wrong password");
    }

    // Test User File Empty
    @Test
    void testIsRegisteredUser_EmptyFile() {
        boolean result = LoginControl.isRegisteredUser("user", "pass");
        assertFalse(result, "Empty users file should return false");
    }

    // Test User Invalid Lines
    @Test
    void testIsRegisteredUser_InvalidLines() throws IOException {
        writeToFile(TEST_USERS_PATH, "invalid_line");

        boolean result = LoginControl.isRegisteredUser("user", "pass");
        assertFalse(result, "Invalid lines should not crash and should return false");
    }
}
