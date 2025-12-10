package org.Code;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public static final String PRICES_PATH = "src/main/InfoBase/Prices.txt";
    public static final String MAILS_PATH = "src/main/InfoBase/Mails.txt";
    public static final String LOANS_PATH = "src/main/InfoBase/Loan.txt"; // أو Loans.txt حسب اسم الملف عندك


    public static final ArrayList<User> LibrarianList = new ArrayList<>();
    public static final ArrayList<Book> BooksList = new ArrayList<>();
    public static final ArrayList<User> UserList  = new ArrayList<>();
    public static final ArrayList<User> BorrowedList  = new ArrayList<>();
    public static final ArrayList<User> LoanedList  = new ArrayList<>();

    public static String RETURNED="RETURNED";
    public static String LOADED="LOADED";
    public FileControler() {
        // لو بدك تحميل تلقائي بالخلفية
        fillBooksDataAsync();
        fillUsersDataAsync();
    }

    // ===================== ASYNC LOADERS (Threads) =====================
    // الأساس: يكتب Book أو CD حسب الـ type
    private static void addBorrowedMediaCore(String id,
                                             String title,
                                             String username,
                                             String type,
                                             int durationDays,
                                             double finePerDay) {

        LocalDate today = LocalDate.now();
        LocalDate due   = today.plusDays(durationDays);

        try (FileWriter writer = new FileWriter(BORROWED_PATH, true)) {
            // لو BOOK: 4 أعمدة (توافق للخلف)
            // لو CD:   5 أعمدة (مع نوع)
            String line = id + "," + title + "," + today + "," + username;
            if (!"BOOK".equalsIgnoreCase(type)) {
                line += "," + type;
            }
            writer.write(line + System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error writing to Borrowed_Books.txt: " + e.getMessage());
        }

        // book/CD flag في Books.txt (لسه يعتبره borrowed)
        updateBookBorrowFlag(id, true);

        // history في Loan.txt
        appendLoanRecord(username, id, title, today, due);
    }

    // 📚 للكتب (28 يوم, غرامة 10)
    public static void addBorrowedBook(String isbn, String name, String user) {
        addBorrowedMediaCore(isbn, name, user, "BOOK", 28, 10.0);
    }

    // 💿 للـ CD (7 أيام, غرامة 20)
    public static void addBorrowedCD(String code, String title, String user) {
        addBorrowedMediaCore(code, title, user, "CD", 7, 20.0);
    }

    public static boolean unBorrowIfNoFine(String isbn, String username) {
        try {
            List<String> lines   = Files.readAllLines(Paths.get(BORROWED_PATH));
            List<String> updated = new ArrayList<>();

            boolean removedOne = false;
            boolean hasFine    = false;
            LocalDate today    = LocalDate.now();

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    updated.add(line);
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length < 4) {
                    updated.add(line);
                    continue;
                }

                if (!removedOne && isTargetBorrowLine(parts, isbn, username)) {
                    if (hasLateFine(parts[2].trim(), today)) {
                        hasFine = true;
                        updated.add(line);
                    } else {
                        removedOne = true;
                    }
                    continue;
                }

                updated.add(line);
            }

            if (hasFine || !removedOne) {
                return false;
            }

            // نكتب الملف الجديد بدون السطر المحذوف
            Files.write(Paths.get(BORROWED_PATH), updated);

            // نعدّل حالة الكتاب في Books.txt
            updateBookBorrowFlag(isbn, false);

            return true;

        } catch (IOException e) {
            System.out.println("Error in unBorrowIfNoFine: " + e.getMessage());
            return false;
        }
    }

    private static boolean isTargetBorrowLine(String[] parts, String isbn, String username) {
        String lineIsbn = parts[0].trim();
        String lineUser = parts[3].trim();
        return lineIsbn.equals(isbn) && lineUser.equals(username);
    }

    private static boolean hasLateFine(String dateStr, LocalDate today) {
        try {
            LocalDate borrowDate = LocalDate.parse(dateStr);
            long days = ChronoUnit.DAYS.between(borrowDate, today);
            return days > 28;
        } catch (Exception e) {
            // تاريخ خربان → نعتبر أن عليه مشكلة ويحتاج مراجعة
            return true;
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



    public static boolean renewLoan(String isbn, String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(BORROWED_PATH));
            List<String> updated = new ArrayList<>();

            boolean renewedOne = false;

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    updated.add(line);
                    continue;
                }

                String[] p = line.split(",");
                if (p.length < 4) {
                    updated.add(line);
                    continue;
                }

                String lineIsbn = p[0].trim();
                String lineUser = p[3].trim();

                if (!renewedOne && lineIsbn.equals(isbn) && lineUser.equals(username)) {
                    // نعدّل تاريخ الاستعارة لليوم
                    p[2] = LocalDate.now().toString();
                    String rebuilt = String.join(",", p);
                    updated.add(rebuilt);
                    renewedOne = true;
                } else {
                    updated.add(line);
                }
            }

            if (!renewedOne) {
                return false; // ما لقينا سطر مطابق
            }

            Files.write(Paths.get(BORROWED_PATH), updated);
            System.out.println("✅ renewLoan: updated borrow date for " + isbn + " / " + username);
            return true;

        } catch (IOException e) {
            System.out.println("Error in renewLoan: " + e.getMessage());
            return false;
        }
    }
    public static String getEmailForUser(String username) {
        Path path = Paths.get(USERS_PATH);

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 4) continue;

                String fileUser = p[2].trim();
                String email    = p[3].trim();

                if (fileUser.equals(username))
                    return email;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // not found
    }
    public static boolean hasOverdueBooks(String username) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(BORROWED_PATH));
            LocalDate today = LocalDate.now();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 4) continue;

                String isbn = p[0].trim();
                String title = p[1].trim();
                LocalDate borrowDate = LocalDate.parse(p[2].trim());
                String user = p[3].trim();

                if (!user.equals(username)) continue;

                long days = java.time.temporal.ChronoUnit.DAYS.between(borrowDate, today);

                if (days > 28) {
                    return true;  // عنده متأخر
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false; // لا يوجد متأخر
    }public static List<Loan> loadLoansForUser(User user) {

        List<Loan> result = new ArrayList<>();
        if (user == null) return result;

        String username = user.getUsername();
        if (username == null) return result;

        // نقرأ من Loan.txt عن طريق LoanRecord
        List<LoanRecord> records = loadLoansFromFile();

        for (LoanRecord r : records) {
            // فلترة على هذا اليوزر فقط
            if (!r.username.equals(username)) continue;

            // جيب الـ Book من BooksList
            Book book = findBookByIsbn(r.isbn);
            if (book == null) {
                // لو مش موجود، أنشئ واحد بسيط من البيانات
                // نعتبر borrowed = true لو status مش RETURNED
                boolean borrowed = !RETURNED.equalsIgnoreCase(r.status);
                book = new Book(r.title, "", r.isbn, borrowed);

                // لو حابب تحفظه في BooksList:
                // BooksList.add(book);
            }

            // عدد أيام فترة الإعارة = الفرق بين startDate و dueDate
            int periodDays = (int) ChronoUnit.DAYS.between(r.startDate, r.dueDate);

            Loan loan = new Loan(book, user, r.startDate, periodDays);

            // علم إذا كان راجع
            if (RETURNED.equalsIgnoreCase(r.status)) {
                loan.setReturned(true);
            }

            // (isOverdue() جوه Loan بيحسب من dueDate, فمش محتاج نخزن status)

            result.add(loan);
        }

        return result;
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

            System.out.println(LOADED + LibrarianList.size() + " librarians from " + path);

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

                if (parts.length >= 4) {
                    String name     = parts[0].trim();
                    String author   = parts[1].trim();
                    String isbn     = parts[2].trim();
                    boolean borrowed = Boolean.parseBoolean(parts[3].trim());

                    String type = "BOOK";
                    if (parts.length >= 5) {
                        type = parts[4].trim();
                        if (type.isEmpty()) type = "BOOK";
                    }

                    Book b = new Book(name, author, isbn, borrowed);
                    b.setMediaType(type);

                    BooksList.add(b);
                } else {
                    System.out.println("Invalid book line: " + line);
                }
            }
            System.out.println(LOADED + BooksList.size() + " books from " + path);
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
            System.out.println(LOADED + UserList.size() + " users from " + path);
        } catch (IOException e) {
            System.out.println("Error reading users file: " + e.getMessage());
        }
    }

    // ===================== ADD BOOK / USER (SYNC) =====================

    public static void addBookSync(Book book) {
        Path path = Paths.get(BOOKS_PATH).toAbsolutePath();

        try {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }

            String mediaType = (book.getMediaType() == null || book.getMediaType().isEmpty())
                    ? "BOOK"
                    : book.getMediaType().toUpperCase();

            String category = (book.getCategory() == null || book.getCategory().isEmpty())
                    ? "Other"
                    : book.getCategory();

            // name,author,ISBN,borrowed,mediaType,category
            String record = book.getName() + "," +
                    book.getAuthor() + "," +
                    book.getISBN() + "," +
                    book.isBorrowed() + "," +
                    mediaType + "," +
                    category;

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
// imports المطلوبة في أعلى الملف:
// import java.nio.file.*;
// import java.nio.charset.StandardCharsets;

    public static void rewriteUsersFile() throws IOException {
        Path path = Paths.get(USERS_PATH);

        List<String> lines = new ArrayList<>();

        for (User u : UserList) {
            StringBuilder sb = new StringBuilder();
            sb.append(u.getFirstName()).append(',')
                    .append(u.getLastName()).append(',')
                    .append(u.getUsername()).append(',')
                    .append(u.getEmail()).append(',')
                    .append(u.getPassword());

            // الكتب (لو عندك مصفوفة أو list في User)
            String[] books = u.getBooks();   // ⚠️ غيّر الاسم حسب دالةك الحقيقية
            if (books != null && books.length > 0) {
                sb.append(',');
                sb.append(String.join(";", books));
            } else {
                sb.append(','); // فاضي للـ books
            }

            // isAdmin في آخر العمود
            sb.append(',').append(u.isAdmin());

            lines.add(sb.toString());
        }

        Files.write(
                path,
                lines,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
        );
    }
    public static boolean markLoanReturned(String username, String bookTitleOrIsbn) {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(BORROWED_PATH);
            if (!java.nio.file.Files.exists(path)) return false;

            java.util.List<String> lines = java.nio.file.Files.readAllLines(path);
            java.util.List<String> newLines = new java.util.ArrayList<>();

            boolean removed = false;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 4) {
                    newLines.add(line);
                    continue;
                }

                String isbn  = p[0].trim();
                String title = p[1].trim();
                String date  = p[2].trim();
                String user  = p[3].trim();

                boolean matchUser = user.equals(username);
                boolean matchBook = isbn.equals(bookTitleOrIsbn) || title.equalsIgnoreCase(bookTitleOrIsbn);

                if (matchUser && matchBook && !removed) {
                    // ما نضيف السطر → هيك بنعتبره رجع الكتاب
                    removed = true;

                    // حدّث BooksList بالحالة
                    Book b = findBookByIsbn(isbn);
                    if (b != null) {
                        b.updateBorrowed(false);
                    }
                } else {
                    newLines.add(line);
                }
            }

            if (!removed) return false;

            java.nio.file.Files.write(
                    path,
                    newLines,
                    java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                    java.nio.file.StandardOpenOption.CREATE
            );

            // بعد التعديل، ممكن تعيد مزامنة
            syncBorrowedStatusOnce();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // helper بسيط في نفس FileControler
    public static Book findBookByIsbn(String isbn) {
        for (Book b : BooksList) {
            if (b.getISBN().equals(isbn)) return b;
        }
        return null;
    }

    public static boolean userHasActiveLoansOrFines(User user) {
        if (user == null) return false;

        String username = user.getUsername();
        Path path = Paths.get(BORROWED_PATH);

        if (!Files.exists(path)) {
            return false; // ما في ملف → ما في قروض
        }

        try {
            List<String> lines = Files.readAllLines(path);

            LocalDate today = LocalDate.now();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                // Format: ISBN,Title,BorrowDate,User
                String[] p = line.split(",");
                if (p.length < 4) continue;

                String fileUser = p[3].trim();

                // هذا السطر تبع نفس اليوزر؟
                if (!fileUser.equals(username)) continue;

                // ---- ACTIVE LOAN CHECK ----
                // وجود السطر يعني الكتاب غير مُعاد → ACTIVE
                // ما دام السطر موجود → المستخدم لم يرجع الكتاب
                String dateStr = p[2].trim();
                LocalDate borrowDate;

                try {
                    borrowDate = LocalDate.parse(dateStr);
                } catch (Exception e) {
                    // تاريخ خربان = اعتبره خطر → ارجع true
                    return true;
                }

                long days = ChronoUnit.DAYS.between(borrowDate, today);

                // ---- OVERDUE CHECK ----
                if (days > 28) {
                    return true; // متأخر → عليه غرامة → لا تمسحه
                }

                // إذا واصل لهون → عنده قرض غير مسدد
                return true;
            }

        } catch (Exception e) {
            System.out.println("Error checking user loans: " + e.getMessage());
            return true; // safety: نرجع true لتجنب حذف شخص فيه مشكلة
        }

        return false; // ما عنده ولا قرض ولا غرامة
    }
// ================== PRICES / FINES ==================

    public static boolean hasOutstandingFine(String username) {
        return getTotalFineForUser(username) > 0.0;
    }

    public static double getTotalFineForUser(String username) {
        Path path = Paths.get(PRICES_PATH);
        if (!Files.exists(path)) return 0.0;

        double total = 0.0;
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length < 3) continue;

                String u = p[0].trim();
                if (!u.equalsIgnoreCase(username)) continue;

                double amount = 0.0;
                try { amount = Double.parseDouble(p[2].trim()); }
                catch (NumberFormatException ignore) {}

                total += amount;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total;
    }

    // إضافة غرامة جديدة
    public static void addFine(String username, String isbn, double amount) {
        Path path = Paths.get(PRICES_PATH);
        String line = username + "," + isbn + "," + amount + System.lineSeparator();
        try {
            Files.write(path, line.getBytes(),
                    Files.exists(path)
                            ? java.nio.file.StandardOpenOption.APPEND
                            : java.nio.file.StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // مسح غرامة على كتاب معيّن (بعد الدفع / إعفاء)
    public static void clearFineForUserAndBook(String username, String isbn) {
        Path path = Paths.get(PRICES_PATH);
        if (!Files.exists(path)) return;

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> keep = new java.util.ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length < 3) continue;

                String u = p[0].trim();
                String b = p[1].trim();

                // نحذف فقط الغرامة المطابقة
                if (u.equalsIgnoreCase(username) && b.equalsIgnoreCase(isbn)) {
                    continue;
                }
                keep.add(line);
            }

            Files.write(path, keep);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// ================== MAILS LOG ==================

    public static void logMail(String username, String email, String subject) {
        Path path = Paths.get(MAILS_PATH);
        String now = java.time.LocalDateTime.now().toString();
        String line = username + "," + email + "," + subject + "," + now + System.lineSeparator();
        try {
            Files.write(path, line.getBytes(),
                    Files.exists(path)
                            ? java.nio.file.StandardOpenOption.APPEND
                            : java.nio.file.StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// ================== LOANS (Admin view) ==================

    public static java.util.List<GAdminControl.LoanRow> loadAllLoansRows() {
        java.util.List<GAdminControl.LoanRow> rows = new java.util.ArrayList<>();

        Path path = Paths.get(LOANS_PATH);
        if (!Files.exists(path)) return rows;

        try {
            List<String> lines = Files.readAllLines(path);
            int idCounter = 1;
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length < 7) continue;

                String username = p[0].trim();
                String isbn     = p[1].trim();
                String title    = p[2].trim();
                String start    = p[3].trim();
                String due      = p[4].trim();
                String status   = p[5].trim();
                String fee      = p[6].trim(); // ممكن تستخدمه لاحقاً

                String id = String.valueOf(idCounter++);

                GAdminControl.LoanRow row =
                        new GAdminControl.LoanRow(id, username, title, start, due, status);

                rows.add(row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rows;
    }

    public static java.util.List<GAdminControl.LoanRow> searchLoansRows(String keyword) {
        keyword = keyword.toLowerCase();
        java.util.List<GAdminControl.LoanRow> all = loadAllLoansRows();
        java.util.List<GAdminControl.LoanRow> result = new java.util.ArrayList<>();

        for (GAdminControl.LoanRow row : all) {
            if (row.getUser().toLowerCase().contains(keyword)
                    || row.getBook().toLowerCase().contains(keyword)
                    || row.getStatus().toLowerCase().contains(keyword)) {
                result.add(row);
            }
        }
        return result;
    }
    // ================== LOANS (raw records) ==================

    // قراءة كل السجلات من Loan.txt
    public static List<LoanRecord> loadLoansFromFile() {
        List<LoanRecord> list = new ArrayList<>();
        Path path = Paths.get(LOANS_PATH);
        if (!Files.exists(path)) return list;

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                LoanRecord r = LoanRecord.fromLine(line);
                if (r != null) list.add(r);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // كتابة كل السجلات إلى Loan.txt (rewrite)
    public static void saveLoansToFile(List<LoanRecord> records) {
        Path path = Paths.get(LOANS_PATH);
        List<String> out = new ArrayList<>();
        for (LoanRecord r : records) {
            out.add(r.toLine());
        }
        try {
            Files.write(path, out,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // إضافة قرض جديد (يُستدعى من addBorrowedBook)
    public static void appendLoanRecord(String username,
                                        String isbn,
                                        String title,
                                        LocalDate start,
                                        LocalDate due) {
        LoanRecord r = new LoanRecord(username, isbn, title, start, due, "BORROWED", 0.0);
        Path path = Paths.get(LOANS_PATH);
        String line = r.toLine() + System.lineSeparator();
        try {
            Files.write(path, line.getBytes(StandardCharsets.UTF_8),
                    Files.exists(path)
                            ? StandardOpenOption.APPEND
                            : StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // تحديث حالة قرض معيّن
    public static boolean updateLoanStatus(String username,
                                           String isbn,
                                           LocalDate startDate,
                                           String newStatus,
                                           double fee) {
        List<LoanRecord> all = loadLoansFromFile();
        boolean updated = false;

        for (LoanRecord r : all) {
            if (!r.username.equals(username)) continue;
            if (!r.isbn.equals(isbn)) continue;
            if (!r.startDate.equals(startDate)) continue;

            r.status = newStatus;
            r.fee    = fee;
            updated = true;
            break;
        }

        if (updated) {
            saveLoansToFile(all);
        }

        return updated;
    }

    // يُستخدم من Admin → OnMarkReturned (لو حاب ترجع مباشر بالـ Strings)
    public static boolean markLoanReturnedInFile(String username,
                                                 String isbnOrTitle,
                                                 String startStr,
                                                 String dueStr) {
        LocalDate start;
        try {
            start = LocalDate.parse(startStr);
        } catch (Exception e) {
            return false;
        }

        List<LoanRecord> all = loadLoansFromFile();
        boolean updated = false;

        for (LoanRecord r : all) {
            if (!r.username.equals(username)) continue;
            // نطابق على ISBN أو Title
            if (!r.isbn.equals(isbnOrTitle) && !r.title.equalsIgnoreCase(isbnOrTitle))
                continue;
            if (!r.startDate.equals(start)) continue;

            r.status = RETURNED;
            // لو حابب تلغي أي غرامة:
            // r.fee = 0.0;
            updated = true;
            break;
        }

        if (updated) {
            saveLoansToFile(all);
        }

        return updated;
    }

    // لو حابب مارك كـ OVERDUE من الـ Admin
    public static boolean markLoanOverdueInFile(String username,
                                                String isbnOrTitle,
                                                String startStr,
                                                double fee) {
        LocalDate start;
        try {
            start = LocalDate.parse(startStr);
        } catch (Exception e) {
            return false;
        }

        List<LoanRecord> all = loadLoansFromFile();
        boolean updated = false;

        for (LoanRecord r : all) {
            if (!r.username.equals(username)) continue;
            if (!r.isbn.equals(isbnOrTitle) && !r.title.equalsIgnoreCase(isbnOrTitle))
                continue;
            if (!r.startDate.equals(start)) continue;

            r.status = "OVERDUE";
            r.fee    = fee;
            updated = true;
            break;
        }

        if (updated) {
            saveLoansToFile(all);
        }

        return updated;
    }
    public static class LoanRecord {
        public String username;
        public String isbn;
        public String title;
        public LocalDate startDate;
        public LocalDate dueDate;
        public String status;   // BORROWED / OVERDUE / RETURNED
        public double fee;      // 0, 10, ...

        public LoanRecord(String username,
                          String isbn,
                          String title,
                          LocalDate startDate,
                          LocalDate dueDate,
                          String status,
                          double fee) {
            this.username  = username;
            this.isbn      = isbn;
            this.title     = title;
            this.startDate = startDate;
            this.dueDate   = dueDate;
            this.status    = status;
            this.fee       = fee;
        }

        // يحوّل الريكورد لسطر CSV في Loan.txt
        public String toLine() {
            return String.join(",",
                    username,
                    isbn,
                    title,
                    startDate.toString(),
                    dueDate.toString(),
                    status,
                    String.valueOf(fee)
            );
        }

        // يبني LoanRecord من سطر CSV في Loan.txt
        public static LoanRecord fromLine(String line) {
            if (line == null || line.trim().isEmpty()) return null;

            String[] p = line.split(",");
            if (p.length < 7) return null;

            try {
                String username = p[0].trim();
                String isbn     = p[1].trim();
                String title    = p[2].trim();
                LocalDate start = LocalDate.parse(p[3].trim());
                LocalDate due   = LocalDate.parse(p[4].trim());
                String status   = p[5].trim();
                double fee      = Double.parseDouble(p[6].trim());

                return new LoanRecord(username, isbn, title, start, due, status, fee);
            } catch (Exception e) {
                System.out.println("Invalid loan line: " + line);
                return null;
            }
        }
    }

    public static boolean updateUserInFile(User updated) {
        Path path = Paths.get(USERS_PATH);
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> out   = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) { out.add(line); continue; }

                String[] p = line.split(",");
                if (p.length < 5) { out.add(line); continue; }

                String username = p[2].trim();

                if (username.equals(updated.getUsername())) {
                    // نبني السطر الجديد بنفس الفورمات اللي عندك
                    String booksJoined = "";
                    if (updated.getBooks() != null && updated.getBooks().length > 0) {
                        booksJoined = String.join(";", updated.getBooks());
                    }

                    String newLine =
                            updated.getFirstName() + "," +
                                    updated.getLastName()  + "," +
                                    updated.getUsername()  + "," +
                                    updated.getEmail()     + "," +
                                    updated.getPassword()  + "," +
                                    booksJoined            + "," +
                                    updated.isAdmin();

                    out.add(newLine);
                } else {
                    out.add(line);
                }
            }

            Files.write(path, out);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateLibrarianInFile(User updated) {
        Path path = Paths.get(LIBRARIANS_PATH);
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> out   = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) { out.add(line); continue; }

                String[] p = line.split(",");
                if (p.length < 5) { out.add(line); continue; }

                String username = p[2].trim();

                if (username.equals(updated.getUsername())) {
                    String newLine =
                            updated.getFirstName() + "," +
                                    updated.getLastName()  + "," +
                                    updated.getUsername()  + "," +
                                    updated.getEmail()     + "," +
                                    updated.getPassword();

                    out.add(newLine);
                } else {
                    out.add(line);
                }
            }

            Files.write(path, out);
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean hasOverdueCDs(User user) {
        if (user == null) return false;

        // نستعمل نفس المنطق اللي بيجيب قروض اليوزر
        List<Loan> loans = loadLoansForUser(user);
        if (loans == null) return false;

        for (Loan loan : loans) {
            if (loan == null) continue;

            // لو راجعه خلاص مش محسوب
            if (loan.isReturned()) continue;

            // لازم يكون متأخر
            if (!loan.isOverdue()) continue;

            // نحدد إذا هذا الـ loan هو لــ CD
            Media item = loan.getItem();
            if (item instanceof CD) {
                // loan على CD ومتأخر → بلوك
                return true;
            }

            // لو الـ Media عندك مخزّن كـ Book بس فيه mediaType = "CD"
            if (item instanceof Book) {
                Book b = (Book) item;
                if ("CD".equalsIgnoreCase(b.getMediaType())) {
                    return true;
                }
            }
        }

        return false;
    }
    public static final String RENEW_REQUESTS_PATH = "src/main/InfoBase/RenewRequests.txt";

    public static void addRenewRequest(User user, Loan loan) {
        if (user == null || loan == null || loan.getBook() == null) return;

        String username = user.getUsername();
        String isbn     = loan.getBook().getISBN();
        String title    = loan.getBook().getName();

        String line = String.join(",",
                username,
                isbn,
                title,
                loan.getBorrowDate().toString(),
                loan.getDueDate().toString()
        );

        try {
            java.nio.file.Files.write(
                    java.nio.file.Paths.get(RENEW_REQUESTS_PATH),
                    (line + System.lineSeparator()).getBytes(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Failed to write renew request: " + e.getMessage());
        }
    }

// ================== BACKGROUND SYNC ==================

    private static volatile boolean backgroundSyncStarted = false;

    public static void startBackgroundSync() {
        // تأكد إنها تشتغل مرة وحدة بس
        if (backgroundSyncStarted) return;
        backgroundSyncStarted = true;

        Thread t = new Thread(() -> {
            System.out.println("🔁 Background sync thread started.");
            while (true) {
                try {
                    // 1) مزامنة Books.txt مع Borrowed_Books.txt
                    syncBorrowedStatusOnce();

                    // 2) تحديث حالات القروض في Loan.txt (BORROWED -> OVERDUE لما يتعدى الـ dueDate)
                    autoUpdateOverdueLoans();

                    // 3) (اختياري) ممكن تضيف منطق تاني هنا لو حابب

                    // كل دقيقة مثلاً
                    Thread.sleep(60_000);  // 60,000 ms = 1 minute

                } catch (InterruptedException e) {
                    System.out.println("Background sync thread interrupted, stopping.");
                    break;
                } catch (Exception e) {
                    // ما نخلي ثرود يموت بسبب Exception
                    e.printStackTrace();
                }
            }
        });

        t.setDaemon(true); // يموت مع البرنامج
        t.start();
    }
    // تحديث تلقائي لحالات القروض حسب التاريخ
    public static void autoUpdateOverdueLoans() {
        List<LoanRecord> records = loadLoansFromFile();
        if (records.isEmpty()) return;

        LocalDate today = LocalDate.now();
        boolean changed = false;

        for (LoanRecord r : records) {
            // لو رجع الكتاب خلاص، ما نلعب فيه
            if (RETURNED.equalsIgnoreCase(r.status)) continue;

            // لو اليوم بعد موعد الاستحقاق → خليها OVERDUE
            if (today.isAfter(r.dueDate)) {
                if (!"OVERDUE".equalsIgnoreCase(r.status)) {
                    r.status = "OVERDUE";
                    // لو حابب تحسب غرامة ثابتة أو حسب الأيام:
                    long daysOver = java.time.temporal.ChronoUnit.DAYS.between(r.dueDate, today);
                    // مثال: 10 شيكل ثابت، أو 2 شيكل لليوم، حسب مزاجك
                    r.fee = 10.0; // أو: r.fee = daysOver * 2.0;
                    changed = true;
                }
            } else {
                // لو لسه قبل أو في نفس اليوم:
                // نخليها BORROWED لو مش مرتجعة
                if (!"BORROWED".equalsIgnoreCase(r.status)) {
                    r.status = "BORROWED";
                    // ممكن تصفر الغرامة
                    // r.fee = 0.0;
                    changed = true;
                }
            }
        }

        if (changed) {
            saveLoansToFile(records);
            System.out.println("✅ autoUpdateOverdueLoans: Loan.txt updated.");
        }
    }

    public static boolean hasRenewRequest(String username,
                                          String isbn,
                                          LocalDate borrowDate) {
        Path path = Paths.get(RENEW_REQUESTS_PATH);
        if (!Files.exists(path)) return false;

        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                // format: username,isbn,title,borrowDate,dueDate
                String[] p = line.split(",");
                if (p.length < 5) continue;

                String u    = p[0].trim();
                String fIsbn= p[1].trim();
                String fBorrowStr = p[3].trim();

                LocalDate fBorrow;
                try {
                    fBorrow = LocalDate.parse(fBorrowStr);
                } catch (Exception e) {
                    continue;
                }

                if (u.equals(username)
                        && fIsbn.equals(isbn)
                        && fBorrow.equals(borrowDate)) {
                    return true;    // في طلب تجديد لهذا القرض
                }
            }
        } catch (IOException e) {
            System.out.println("Error in hasRenewRequest: " + e.getMessage());
        }

        return false;
    }

    public static void clearRenewRequest(String username,
                                         String isbn,
                                         LocalDate borrowDate) {
        Path path = Paths.get(RENEW_REQUESTS_PATH);
        if (!Files.exists(path)) return;

        try {
            List<String> lines = Files.readAllLines(path);
            List<String> keep  = new ArrayList<>();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 5) {
                    keep.add(line);
                    continue;
                }

                String u    = p[0].trim();
                String fIsbn= p[1].trim();
                String fBorrowStr = p[3].trim();

                LocalDate fBorrow;
                try {
                    fBorrow = LocalDate.parse(fBorrowStr);
                } catch (Exception e) {
                    keep.add(line);
                    continue;
                }

                // إذا نفس القرض → ما نضيفه (نمسحه)
                if (u.equals(username)
                        && fIsbn.equals(isbn)
                        && fBorrow.equals(borrowDate)) {
                    continue;
                }

                keep.add(line);
            }

            Files.write(path, keep,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            System.out.println("Error in clearRenewRequest: " + e.getMessage());
        }
    }



}
