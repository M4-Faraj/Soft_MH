package org.Code;

/**
 * Handles all file-based operations related to user management.
 * <p>
 * This controller reads from and writes to the {@code Users.txt} file,
 * supporting CRUD operations (Create, Read, Update, Delete) for users.
 * The file format per line is:
 * <pre>
 * firstName,lastName,username,email,password,book1|book2|...,fine,isAdmin
 * </pre>
 *
 * <p>All methods are static, meaning the class functions as a utility
 * component for user persistence.</p>
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class UserControl {

    // مسار ملف المستخدمين
    private static final Path USERS_FILE = Paths.get("InfoBase", "Users.txt");

    // ================== عمليات أساسية ==================

    /* إضافة مستخدم جديد للملف */
    /**
     * Adds a new user to the Users.txt file by appending a serialized entry.
     *
     * @param user the user to add
     * @throws IOException if file writing fails
     */
    public static void addUser(User user) throws IOException {
        ensureFileExists();

        try (BufferedWriter bw = Files.newBufferedWriter(
                USERS_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        )) {
            bw.write(serialize(user));
            bw.newLine();
        }
    }

    /*البحث عن مستخدم حسب username، يرجّع User أو null لو مش موجود */
    /**
     * Searches for a user by their username.
     *
     * @param username the username to search for
     * @return a matching {@link User}, or {@code null} if not found
     * @throws IOException if reading the file fails
     */
    public static User findUserByUsername(String username) throws IOException {
        if (!Files.exists(USERS_FILE)) {
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(USERS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = deserialize(line);
                if (u != null && u.getUsername().equals(username)) {
                    return u;
                }
            }
        }

        return null;
    }

    /* حذف مستخدم حسب username – يرجع true لو انحذف فعلاً */
    /**
     * Removes a user from the file based on the username.
     * The method rewrites the file excluding that user.
     *
     * @param username the username to remove
     * @return true if the user was successfully removed, false otherwise
     * @throws IOException if file operations fail
     */
    public static boolean removeUser(String username) throws IOException {
        if (!Files.exists(USERS_FILE)) {
            return false;
        }

        List<String> allLines = Files.readAllLines(USERS_FILE);
        List<String> updated = new ArrayList<>();
        boolean removed = false;

        for (String line : allLines) {
            User u = deserialize(line);
            if (u == null) {
                continue; // سطر خربان، طنشه
            }

            if (u.getUsername().equals(username)) {
                // ما نضيفه → يعني انحذف
                removed = true;
            } else {
                updated.add(line);
            }
        }

        if (removed) {
            try (BufferedWriter bw = Files.newBufferedWriter(
                    USERS_FILE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            )) {
                for (String l : updated) {
                    bw.write(l);
                    bw.newLine();
                }
            }
        }

        return removed;
    }

    /* تحديث بيانات مستخدم: يمسح القديم بنفس username ويضيف الجديد */
    /**
     * Updates an existing user by replacing the old record with the new one.
     *
     * @param updatedUser the new version of the user
     * @return true if the user existed and was updated, false otherwise
     * @throws IOException if file writing fails
     */
    public static boolean updateUser(User updatedUser) throws IOException {
        if (!Files.exists(USERS_FILE)) {
            return false;
        }

        List<User> users = getAllUsers();
        boolean found = false;

        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getUsername().equals(updatedUser.getUsername())) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) return false;

        // اكتب الملف من جديد
        try (BufferedWriter bw = Files.newBufferedWriter(
                USERS_FILE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE
        )) {
            for (User u : users) {
                bw.write(serialize(u));
                bw.newLine();
            }
        }

        return true;
    }

    /*رجّع كل المستخدمين من الملف */
    /**
     * Reads all users stored in {@code Users.txt}.
     *
     * @return a list of {@link User} objects
     * @throws IOException if reading fails
     */
    public static List<User> getAllUsers() throws IOException {
        List<User> users = new ArrayList<>();

        if (!Files.exists(USERS_FILE)) {
            return users;
        }

        try (BufferedReader br = Files.newBufferedReader(USERS_FILE)) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = deserialize(line);
                if (u != null) {
                    users.add(u);
                }
            }
        }

        return users;
    }

    // ================== Helpers داخليّة ==================

    private static void ensureFileExists() throws IOException {
        if (!Files.exists(USERS_FILE.getParent())) {
            Files.createDirectories(USERS_FILE.getParent());
        }
        if (!Files.exists(USERS_FILE)) {
            Files.createFile(USERS_FILE);
        }
    }

    /** User -> String (سطر واحد) */
    private static String serialize(User u) {
        String booksJoined = "";
        if (u.getBooks() != null && u.getBooks().length > 0) {
            booksJoined = String.join("|", u.getBooks());
        }

        return u.getFirstName() + "," +
                u.getLastName() + "," +
                u.getUsername() + "," +
                u.getEmail() + "," +
                u.getPassword() + "," +
                booksJoined + "," +
                u.getFine() + "," +
                u.isAdmin();
    }

    /** String -> User */
    private static User deserialize(String line) {
        if (line == null || line.isBlank()) return null;

        String[] parts = line.split(",");
        if (parts.length < 8) return null;  // سطر ناقص

        String firstName = parts[0].trim();
        String lastName  = parts[1].trim();
        String username  = parts[2].trim();
        String email     = parts[3].trim();
        String password  = parts[4].trim();

        String booksPart = parts[5].trim();
        String[] books = new String[0];
        if (!booksPart.isEmpty()) {
            books = booksPart.split("\\|");
        }

        double fine = 0;
        try {
            fine = Double.parseDouble(parts[6].trim());
        } catch (NumberFormatException ignored) { }

        boolean isAdmin = Boolean.parseBoolean(parts[7].trim());

        User u = new User(firstName, lastName, username, email, password, books);
        u.addFine(fine);          // set fine indirectly
        u.setAdmin(isAdmin);      // set admin flag

        return u;
    }
}
