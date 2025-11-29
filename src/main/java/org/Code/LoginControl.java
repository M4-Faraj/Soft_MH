package org.Code;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.Code.FileControler.USERS_PATH;
import static org.Code.FileControler.fillBorrowedBookAsync;

public class LoginControl {
    private String username;
    private String password;
    public LoginControl(String username, String password) {
        this.username = username;
        this.password = password;
        getUser(this.username, this.password);
    }

    // ----------------------------------------------------------
    // Return User object if username/password correct
    // null if not found
    // ----------------------------------------------------------
    public static User getUser(String username, String password) {
        Path path = Paths.get(USERS_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("❌ Users file not found at: " + path);
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length < 5) continue;

                String firstName = parts[0].trim();
                String lastName  = parts[1].trim();
                String fileUser  = parts[2].trim();
                String email     = parts[3].trim();
                String pass      = parts[4].trim();

                String[] books = new String[0];
                if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                    books = parts[5].trim().split(";");
                    for (int i = 0; i < books.length; i++)
                        books[i] = books[i].trim();
                }

                boolean isAdmin = false;
                if (parts.length >= 7) {
                    isAdmin = Boolean.parseBoolean(parts[6].trim());
                }

                // 🔥 Actual login check
                if (fileUser.equals(username) && pass.equals(password)) {
                    User u = new User(firstName, lastName, fileUser, email, pass, books);
                    u.setAdmin(isAdmin);
                    return u;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // no match found
    }
    public static boolean isLibrarian(String username, String password) {
        return FileControler.getLibrarian(username, password) != null;
    }

    public static User getLibrarianUser(String username, String password) {
        return FileControler.getLibrarian(username, password);
    }

    public static boolean isAdmin(String username, String password) {
        Path path = Paths.get("src/main/InfoBase/Admin.txt").toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("Admin file not found at: " + path);
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String fileUser = parts[0].trim();
                String filePass = parts[1].trim();

                if (fileUser.equals(username) && filePass.equals(password)) {
                    return true;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
    public static boolean isRegisteredUser(String username, String password) {
        User u = getUser(username, password);
        return u != null && !u.isAdmin();
    }
    public LoginControl(){

    }

/// ////////////////////////////a called class to log in


}
