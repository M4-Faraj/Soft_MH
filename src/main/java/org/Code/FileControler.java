package org.Code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FileControler {

    // مسارات الملفات (نفس المشروع اللي عندك)
    public static final String BOOKS_PATH = "src/main/InfoBase/Books.txt";
    public static final String USERS_PATH = "src/main/InfoBase/Users.txt";
    public static final String BORROWED_PATH = "src/main/InfoBase/Borrowed_Books.txt";
    public static final String LIBRARIANS_PATH = "src/main/InfoBase/Librarian.txt";

    public static final ArrayList<User> LibrarianList = new ArrayList<>();
    public static final ArrayList<Book> BooksList = new ArrayList<>();
    public static final ArrayList<User> UserList  = new ArrayList<>();
    public static final ArrayList<User> BorrowedList  = new ArrayList<>();
    public static final ArrayList<User> LoanedList  = new ArrayList<>();

    public FileControler() {
        // لو بدك تحميل تلقائي بالخلفية
        fillBooksDataAsync();
        fillUsersDataAsync();
    }

    // ===================== ASYNC LOADERS (Threads) =====================
    public static void addBorrowedBook(String isbn, String name, String user) {
        // 1) Append to Borrowed_Books.txt
        try (FileWriter writer = new FileWriter(BORROWED_PATH, true)) {
            String line = isbn + "," + name + "," + LocalDate.now() + "," + user + "\n";
            writer.write(line);
        } catch (IOException e) {
            System.out.println("Error writing to Borrowed_Books.txt: " + e.getMessage());
        }

        // 2) عدّل Books.txt وخلي الفلاغ = true (مستعار) لأول نسخة من هذا الـ ISBN
        updateBookBorrowFlag(isbn, true);
    }



    // يحاول "إلغاء الاستعارة" (إرجاع الكتاب) فقط إذا ما في غرامة
// يرجع true لو تم الإرجاع (انمسح من Borrowed_Books وتعدل Books)
// ويرجع false لو في غرامة أو ما لقى سطر مناسب
    public static boolean unBorrowIfNoFine(String isbn, String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(BORROWED_PATH));
            List<String> updated = new ArrayList<>();

            boolean removedOne = false;
            boolean hasFine = false;

            LocalDate today = LocalDate.now();

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    updated.add(line);
                    continue;
                }

                // شكل السطر: ISBN,Name,Date,User
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    updated.add(line);
                    continue;
                }

                String lineIsbn  = parts[0].trim();
                String lineUser  = parts[3].trim();
                String dateStr   = parts[2].trim();

                if (!removedOne && lineIsbn.equals(isbn) && lineUser.equals(username)) {
                    // هذا السطر تبع هذا اليوزر وهذا الكتاب
                    LocalDate borrowDate;
                    try {
                        borrowDate = LocalDate.parse(dateStr);
                    } catch (Exception e) {
                        // تاريخ خربان → نعتبر أنه عليه مشكلة، ما نسمح برجوع بدون مراجعة
                        hasFine = true;
                        updated.add(line); // خليه زي ما هو
                        continue;
                    }

                    long days = ChronoUnit.DAYS.between(borrowDate, today);

                    // إذا أكثر من 28 يوم → عليه غرامة 10 شيكل → ما نرجع الكتاب
                    if (days > 28) {
                        hasFine = true;
                        updated.add(line); // خليه
                    } else {
                        // مافي غرامة → نحذف هذا السطر (ما نضيفه للـ updated)
                        removedOne = true;
                    }

                    continue;
                }

                // باقي الأسطر
                updated.add(line);
            }

            if (hasFine || !removedOne) {
                // يا إما عليه غرامة، أو ما لقينا سطر مطابق
                return false;
            }

            // كتبنا الملف بدون هذا السطر
            Files.write(Paths.get(BORROWED_PATH), updated);

            // وعدلنا Books.txt نخلي الكتاب متاح (borrowed = false)
            updateBookBorrowFlag(isbn, false);

            return true;

        } catch (IOException e) {
            System.out.println("Error in unBorrowIfNoFine: " + e.getMessage());
            return false;
        }
    }
    public static final String LIBRARIAN_PATH = "src/main/InfoBase/Librarian.txt";

    public static User getLibrarian(String username, String password) {
        Path path = Paths.get(LIBRARIAN_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("❌ Librarian file not found: " + path);
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(path);

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                // format: username,password
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String fileUser = parts[0].trim();
                String filePass = parts[1].trim();

                if (fileUser.equals(username) && filePass.equals(password)) {
                    // librarian has no firstname/lastname/email/books
                    User lib = new User(fileUser, "", fileUser, "", filePass, new String[0]);
                    lib.setAdmin(false); // مش أدمِن
                    lib.setLibrarian(true); // لازم تضيف flag بالمستخدم إنو Librarian
                    return lib;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    // يحسب عدد أيام التأخير فوق 28 يوم لهذا اليوزر وهذا الكتاب
// لو رجع 0 → مافي تأخير (أو ≤ 28)
// لو رجع رقم > 0 → هذا عدد الأيام اللي فوق 28

    // يرجع الكتاب فعلياً: يحذف أول سطر مطابق في Borrowed_Books ويخلي borrowed=false
// يرجع true لو فعلاً شال سطر وحدث Books.txt
    public static void fillLibrariansDataAsync() {
        Thread t = new Thread(FileControler::fillLibrariansDataSync);
        t.setDaemon(true);
        t.start();
    }

    private static void fillLibrariansDataSync() {
        LibrarianList.clear();
        Path path = Paths.get(LIBRARIANS_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("Librarian file not found at: " + path);
            return;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {

                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                // نتوقع: firstName,lastName,username,email,password
                if (parts.length >= 5) {
                    String firstName = parts[0].trim();
                    String lastName  = parts[1].trim();
                    String username  = parts[2].trim();
                    String email     = parts[3].trim();
                    String password  = parts[4].trim();

                    // ما في كتب ولا isAdmin هنا
                    String[] books = new String[0];

                    User u = new User(firstName, lastName, username, email, password, books);
                    // ممكن لو حابب تعتبرهم role خاص، لكن ما نعطيهم admin هنا
                    u.setAdmin(false);

                    LibrarianList.add(u);
                } else {
                    System.out.println("Invalid librarian line: " + line);
                }
            }

            System.out.println("Loaded " + LibrarianList.size() + " librarians from " + path);

        } catch (IOException e) {
            System.out.println("Error reading librarians file: " + e.getMessage());
        }
    }

    // دالة عامة لتعديل الفلاغ لأي ISBN
    public static void updateBookBorrowFlag(String isbn, boolean flag) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(BOOKS_PATH));
            List<String> updated = new ArrayList<>();
            boolean updatedOne = false; // حتى نعدل أول نسخة فقط

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    updated.add(line);
                    continue;
                }

                String[] parts = line.split(",");

                if (parts.length >= 4) {
                    String lineIsbn = parts[2].trim();

                    // لو لسه ما عدلنا و الـ ISBN مطابق
                    if (!updatedOne && lineIsbn.equals(isbn)) {
                        parts[3] = String.valueOf(flag); // عدّل الفلاغ

                        String rebuilt = String.join(",", parts);
                        updated.add(rebuilt);

                        updatedOne = true; // ما نعدل أي نسخة ثانية بعد هيك
                        continue;
                    }
                }

                // باقي الأسطر تظل زي ما هي
                updated.add(line);
            }

            Files.write(Paths.get(BOOKS_PATH), updated);

        } catch (IOException e) {
            System.out.println("Error updating Books.txt: " + e.getMessage());
        }
    }

    public static void syncBorrowedStatusOnce() {
        try {
            // 1) اقرأ كل الـ ISBNs المستعارة من Borrowed_Books.txt مع عدد مرات الاستعارة
            List<String> borrowedLines = Files.readAllLines(Paths.get(BORROWED_PATH));
            java.util.HashMap<String, Integer> borrowedCount = new java.util.HashMap<>();

            for (String line : borrowedLines) {
                if (line.trim().isEmpty()) continue;

                // شكل السطر: ISBN,Name,Date,User
                String[] parts = line.split(",");
                if (parts.length >= 1) {
                    String isbn = parts[0].trim();
                    borrowedCount.put(isbn, borrowedCount.getOrDefault(isbn, 0) + 1);
                }
            }

            // 2) عدّل Books.txt بناءً على عدد المرات في borrowedCount
            List<String> bookLines = Files.readAllLines(Paths.get(BOOKS_PATH));
            List<String> updated = new ArrayList<>();

            for (String line : bookLines) {
                if (line.trim().isEmpty()) {
                    updated.add(line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    String name   = parts[0].trim();
                    String author = parts[1].trim();
                    String isbn   = parts[2].trim();

                    int count = borrowedCount.getOrDefault(isbn, 0);

                    if (count > 0) {
                        // في نسخة من هذا الـ ISBN مستعارة → خلي هاي النسخة borrowed = true
                        parts[3] = String.valueOf(true);
                        borrowedCount.put(isbn, count - 1); // استهلكنا نسخة
                    } else {
                        // ما ظل نسخ مستعارة لهذا الـ ISBN → هاي النسخة متاحة
                        parts[3] = String.valueOf(false);
                    }

                    String rebuilt = String.join(",", parts);
                    updated.add(rebuilt);
                } else {
                    updated.add(line);
                }
            }

            Files.write(Paths.get(BOOKS_PATH), updated);

            System.out.println("✅ syncBorrowedStatusOnce: synced Books.txt with Borrowed_Books.txt (using counts)");

        } catch (IOException e) {
            System.out.println("Error syncing borrowed status: " + e.getMessage());
        }
    }


// لازم يكون عندك هالإيمبورتس فوق
// import java.nio.file.Files;
// import java.nio.file.Paths;
// import java.util.List;
// import java.util.ArrayList;

    public static ArrayList<Book> fillBorrowedBookAsync(ArrayList<Book> books) {

        String path = BORROWED_PATH;

        try {
            List<String> lines = Files.readAllLines(Paths.get(path));

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;   // سطر فاضي

                String[] parts = line.split(",");

                // شكل السطر: ISBN,Name,LocalDate
                if (parts.length >= 2) {
                    String isbn  = parts[0].trim();
                    String name  = parts[1].trim();
                    // String date  = parts[2].trim(); // لو حبيت تستعمله لاحقاً

                    // author مش موجود في الملف → نخليه فاضي أو "Unknown"
                    String author = "";

                    // هذا الكتاب مستعار → borrowed = true
                    Book b = new Book(name, author, isbn, true);
                    books.add(b);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading Borrowed_Books.txt: " + e.getMessage());
        }

        return books;
    }


    public static void fillLoanedAsync(ArrayList<Book> books,String Name){

    }
    public static void fillLoanedAsync(ArrayList<Book> books){

    }
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

    public static void addBookSync(Book book) {
        Path path = Paths.get(BOOKS_PATH).toAbsolutePath();

        try {
            // لو الفولدر مش موجود أنشئه
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            // name,author,ISBN,borrowed
            String record = book.getName() + "," +
                    book.getAuthor() + "," +
                    book.getISBN() + "," +
                    book.isBorrowed();

            Files.writeString(
                    path,
                    record + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

            System.out.println("✅ Book saved to: " + path);
        } catch (IOException e) {
            System.out.println("❌ Failed to save book!");
            e.printStackTrace();
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
    //---------------------------------search book--------------------//
    public static ArrayList<Book> searchBooksContains(String keyword) {
        ArrayList<Book> result = new ArrayList<>();

        if (keyword == null || keyword.trim().isEmpty()) {
            return result; // رجّع فاضي لو مفيش keyword
        }

        String search = keyword.toLowerCase().trim();
        Path path = Paths.get(BOOKS_PATH).toAbsolutePath();

        if (!Files.exists(path)) {
            System.out.println("❌ Books file not found at: " + path);
            return result;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 4) continue; // name,author,ISBN,borrowed

                String name   = parts[0].trim();
                String author = parts[1].trim();
                String isbn   = parts[2].trim();
                boolean borrowed = Boolean.parseBoolean(parts[3].trim());

                // make them lowercase for case-insensitive search
                String nameLow   = name.toLowerCase();
                String authorLow = author.toLowerCase();
                String isbnLow   = isbn.toLowerCase();

                // if keyword appears in any field
                if (nameLow.contains(search) || authorLow.contains(search) || isbnLow.contains(search)) {
                    result.add(new Book(name, author, isbn, borrowed));
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading books file");
            e.printStackTrace();

        }

        return result;
    }


}
