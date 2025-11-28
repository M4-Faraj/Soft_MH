package org.Code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class FileControler {

    // مسارات الملفات (نفس المشروع اللي عندك)
    public static final String BOOKS_PATH = "src/main/InfoBase/Books.txt";
    public static final String USERS_PATH = "src/main/InfoBase/Users.txt";

    public static final ArrayList<Book> BooksList = new ArrayList<>();
    public static final ArrayList<User> UserList  = new ArrayList<>();

    public FileControler() {
        // لو بدك تحميل تلقائي بالخلفية
        fillBooksDataAsync();
        fillUsersDataAsync();
    }

    // ===================== ASYNC LOADERS (Threads) =====================

    public static void fillBooksDataAsync() {
        Thread t = new Thread(FileControler::fillBooksDataSync);
        t.setDaemon(true);
        t.start();
    }

    public static void fillUsersDataAsync() {
        Thread t = new Thread(FileControler::fillUsersDataSync);
        t.setDaemon(true);
        t.start();
    }

    // ===================== SYNC LOADERS (داخل الـ Thread) =====================

    private static void fillBooksDataSync() {
        BooksList.clear();
        Path path = Paths.get(BOOKS_PATH).toAbsolutePath();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String name     = parts[0].trim();
                    String author   = parts[1].trim();
                    String isbn     = parts[2].trim();
                    boolean borrowed = Boolean.parseBoolean(parts[3].trim());

                    BooksList.add(new Book(name, author, isbn, borrowed));
                } else {
                    System.out.println("Invalid book line: " + line);
                }
            }
            System.out.println("Loaded " + BooksList.size() + " books from " + path);
        } catch (IOException e) {
            System.out.println("Error reading books file: " + e.getMessage());
        }
    }

    private static void fillUsersDataSync() {
        UserList.clear();
        Path path = Paths.get(USERS_PATH).toAbsolutePath();

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                // دعم الشكل القديم (بدون isAdmin) والجديد (مع isAdmin)
                if (parts.length >= 5) {
                    String firstName = parts[0].trim();
                    String lastName  = parts[1].trim();
                    String username  = parts[2].trim();
                    String email     = parts[3].trim();
                    String password  = parts[4].trim();

                    String[] books = new String[0];
                    if (parts.length >= 6 && !parts[5].trim().isEmpty()) {
                        books = parts[5].trim().split(";");
                        for (int i = 0; i < books.length; i++) {
                            books[i] = books[i].trim();
                        }
                    }

                    User u = new User(firstName, lastName, username, email, password, books);

                    if (parts.length >= 7) {
                        u.setAdmin(Boolean.parseBoolean(parts[6].trim()));
                    }

                    UserList.add(u);
                } else {
                    System.out.println("Invalid user line: " + line);
                }
            }
            System.out.println("Loaded " + UserList.size() + " users from " + path);
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    // ===================== ADD BOOK / USER (SYNC) =====================

    private static void addBookSync(Book book) {
        Path path = Paths.get(BOOKS_PATH).toAbsolutePath();

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            String record =
                    book.getName() + "," +
                            book.getAuthor() + "," +
                            book.getISBN() + "," +
                            book.isBorrowed();

            Files.writeString(path,
                    record + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("Book added to: " + path);

        } catch (IOException e) {
            System.out.println("Error writing book: " + e.getMessage());
        }
    }

    private static void addUserSync(User user) {
        Path path = Paths.get(USERS_PATH).toAbsolutePath();

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            String booksJoined = "";
            if (user.getBooks() != null && user.getBooks().length > 0) {
                booksJoined = String.join(";", user.getBooks());
            }

            String record =
                    user.getFirstName() + "," +
                            user.getLastName()  + "," +
                            user.getUsername()  + "," +
                            user.getEmail()     + "," +
                            user.getPassword()  + "," +
                            booksJoined         + "," +
                            user.isAdmin();

            Files.writeString(path,
                    record + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println("User added to: " + path);

        } catch (IOException e) {
            System.out.println("Error writing user: " + e.getMessage());
        }
    }

    // ===================== PUBLIC ASYNC WRAPPERS =====================

    public static void addBookAsync(Book book) {
        Thread t = new Thread(() -> addBookSync(book));
        t.setDaemon(true);
        t.start();
    }

    public static void addUserAsync(User user) {
        Thread t = new Thread(() -> addUserSync(user));
        t.setDaemon(true);
        t.start();
    }

    // ===================== SEARCH USER (SYNC + ASYNC) =====================

    // نسخة عادية ترجع boolean (لو احتجتها في LoginControl)
    public static boolean searchUser(String username) {
        Path path = Paths.get(USERS_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String uName = parts[2].trim();
                    if (uName.equals(username)) {
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error searching user: " + e.getMessage());
        }

        return false;
    }

    // نسخة Thread مع callback
    public static void searchUserAsync(String username, Consumer<Boolean> callback) {
        Thread t = new Thread(() -> {
            boolean found = searchUser(username);
            if (callback != null) {
                callback.accept(found);
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public static void addBook(Book e) {
    }
}
