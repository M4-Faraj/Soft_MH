package org.Code;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Handles login and authentication logic for admins, normal users, and librarians.
 * <p>
 * This class reads from simple text files (Admin.txt, Users.txt, Librarian.txt)
 * and checks whether the provided username and password match any stored record.
 * It can also return the corresponding {@link User} object when needed.
 *
 * <p>File formats used:
 * <ul>
 *     <li><b>Admin.txt</b>: {@code username,password}</li>
 *     <li><b>Users.txt</b>: {@code firstName,lastName,username,email,password,books,isAdmin}</li>
 *     <li><b>Librarian.txt</b>:
 *         <ul>
 *             <li>{@code username,password}</li>
 *             <li>{@code username,password,firstName,lastName}</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author HamzaAbdulsalam & Mohammad Dhillieh
 * @version 1.0
 */
public class LoginControl {

    // ================== FILE PATHS ==================
    // خليك ثابت على نفس الكيس تبعت الـ InfoBase
    private static String ADMIN_PATH     = "src/main/InfoBase/Admin.txt";
    private static String USERS_PATH     = "src/main/InfoBase/Users.txt";
    private static String LIBRARIAN_PATH = "src/main/InfoBase/Librarian.txt";

    public LoginControl(String testUserName, String testPassword) {
        // مش محتاج تعمل إشي هون حالياً
    }

    public LoginControl() {
        // default constructor لو بدك تستخدمه
    }

    // ================== SET CUSTOM PATHS (FOR TESTING) ==================
    public static void setAdminPath(String p)     { ADMIN_PATH = p; }
    public static void setUsersPath(String p)     { USERS_PATH = p; }
    public static void setLibrarianPath(String p) { LIBRARIAN_PATH = p; }

    // =======================================================================
    // 1) ADMIN LOGIN (Admin.txt)  → format: username,password
    // =======================================================================
    public static boolean isAdmin(String username, String password) {
        Path path = Paths.get(ADMIN_PATH);

        if (!Files.exists(path)) return false;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 2) continue;

                String fileUser = p[0].trim();
                String filePass = p[1].trim();

                if (fileUser.equals(username) && filePass.equals(password))
                    return true;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // =======================================================================
    // 2) NORMAL USER LOGIN (Users.txt)
    //    Format per line:
    //    firstName,lastName,username,email,password,books,isAdmin
    // =======================================================================
    public static boolean isRegisteredUser(String username, String password) {
        User u = getUser(username, password);
        // مسجل بس مش أدمِن
        return (u != null && !u.isAdmin());
    }

    // يرجع User لو البيانات صح، غير هيك null
    public static User getUser(String username, String password) {
        Path path = Paths.get(USERS_PATH);

        if (!Files.exists(path)) return null;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 5) continue;

                String first    = p[0].trim();
                String last     = p[1].trim();
                String fileUser = p[2].trim();
                String email    = p[3].trim();
                String filePass = p[4].trim();

                if (!fileUser.equals(username) || !filePass.equals(password))
                    continue;

                String[] books = new String[0];
                if (p.length >= 6 && !p[5].trim().isEmpty()) {
                    books = p[5].trim().split(";");
                    for (int i = 0; i < books.length; i++) {
                        books[i] = books[i].trim();
                    }
                }

                boolean isAdmin = false;
                if (p.length >= 7) {
                    isAdmin = Boolean.parseBoolean(p[6].trim());
                }

                User u = new User(first, last, fileUser, email, filePass, books);
                u.setAdmin(isAdmin);
                return u;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // =======================================================================
    // 3) LIBRARIAN LOGIN (Librarian.txt)
    //    Format supported:
    //    1) username,password
    //    2) username,password,firstName,lastName
    // =======================================================================
    public static boolean isLibrarian(String username, String password) {
        return getLibrarian(username, password) != null;
    }

    public static User getLibrarian(String username, String password) {
        Path path = Paths.get(LIBRARIAN_PATH);

        if (!Files.exists(path)) {
            System.out.println("⚠ Librarian file not found: " + path);
            return null;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 2) continue;

                String fileUser = p[0].trim();
                String filePass = p[1].trim();

                if (!fileUser.equals(username) || !filePass.equals(password))
                    continue;

                String first = (p.length >= 3) ? p[2].trim() : "Librarian";
                String last  = (p.length >= 4) ? p[3].trim() : "";

                User u = new User(first, last, fileUser, "", filePass, new String[0]);
                u.setAdmin(false); // librarian مش admin
                return u;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
