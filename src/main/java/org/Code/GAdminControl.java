package org.Code;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GAdminControl {

    // ========= CONSTANTS =========
    private static final String LOAN_FILE = "src/main/InfoBase/Loan.txt";

    // ========= TOP BAR =========
    @FXML private Label lblAdminName;
    @FXML private Button btnLogoutAdmin;

    // ========= RIGHT SIDE =========
    @FXML private ImageView imgSideAdmin;

    // ========= TABS ROOT =========
    @FXML private TabPane tabPaneAdmin;
    @FXML private ComboBox<String> cmbBookCategory;
    @FXML private ComboBox<String> cmbMediaType;   // جديد

    // ========= DASHBOARD TAB =========
    @FXML private Label lblTotalBooks;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblBorrowedCount;
    @FXML private Label lblOverdueCountAdmin;

    @FXML private TableView<ActivityRow> tblRecentActivity;
    @FXML private TableColumn<ActivityRow, String> colActTime;
    @FXML private TableColumn<ActivityRow, String> colActUser;
    @FXML private TableColumn<ActivityRow, String> colActAction;

    // ========= MANAGE BOOKS TAB =========
    @FXML private TextField txtBookId;
    @FXML private TextField txtBookTitle;
    @FXML private TextField txtBookAuthor;
    @FXML private TextField txtISBN;
    @FXML private TextField txtBookQuantity;
    @FXML private TextArea txtBookDescription;

    @FXML private Button btnClearBookForm;
    @FXML private Button btnAddBook;
    @FXML private Button btnUpdateBook;
    @FXML private Button btnSearchBook;
    @FXML private Button btnDeleteBook;

    @FXML private TableView<Book> tblAdminBooks;
    @FXML private TableColumn<Book, String> colAdminBookId;
    @FXML private TableColumn<Book, String> colAdminBookTitle;
    @FXML private TableColumn<Book, String> colAdminBookAuthor;
    @FXML private TableColumn<Book, String> colAdminBookCategory;
    @FXML private TableColumn<Book, String> colAdminBookYear;
    @FXML private TableColumn<Book, String> colAdminBookQuantity;
    @FXML private TableColumn<Book, String> colAdminBookStatus;

    // ========= MANAGE USERS TAB =========
    @FXML private TextField txtUserId;
    @FXML private TextField txtUserFullName;
    @FXML private TextField txtUserUsername;
    @FXML private ComboBox<String> cmbUserRole;
    @FXML private TextField txtUserEmail;
    @FXML private TextField txtUserPhone;
    @FXML private ComboBox<String> cmbUserStatus;

    @FXML private Button btnClearUserForm;
    @FXML private Button btnAddUser;
    @FXML private Button btnUpdateUser;
    @FXML private Button btnDeleteUser;

    @FXML private TableView<User> tblAdminUsers;
    @FXML private TableColumn<User, String> colAdminUserId;
    @FXML private TableColumn<User, String> colAdminUserName;
    @FXML private TableColumn<User, String> colAdminUserUsername;
    @FXML private TableColumn<User, String> colAdminUserRole;
    @FXML private TableColumn<User, String> colAdminUserEmail;
    @FXML private TableColumn<User, String> colAdminUserPhone;
    @FXML private TableColumn<User, String> colAdminUserStatus;

    // ========= BORROWING TAB =========
    @FXML private TextField txtSearchLoans;
    @FXML private Button btnSearchLoans;
    @FXML private TableView<LoanRow> tblAdminLoans;
    @FXML private TableColumn<LoanRow, String> colLoanIdAdmin;
    @FXML private TableColumn<LoanRow, String> colLoanUserAdmin;
    @FXML private TableColumn<LoanRow, String> colLoanBookAdmin;
    @FXML private TableColumn<LoanRow, String> colLoanStartAdmin;
    @FXML private TableColumn<LoanRow, String> colLoanDueAdmin;
    @FXML private TableColumn<LoanRow, String> colLoanStatusAdmin;
    @FXML private Button btnMarkReturned;
    @FXML private Button btnMarkOverdue;
    @FXML private Button btnSendReminder;   // نستعمله لإرسال الإيميل

    // ========= REPORTS TAB =========
    @FXML private DatePicker dpReportFrom;
    @FXML private DatePicker dpReportTo;
    @FXML private ComboBox<String> cmbReportType;
    private final ObservableList<LoanRow> loansData = FXCollections.observableArrayList();
    @FXML private Button btnGenerateReport;
    @FXML private Button btnExportReport;
    @FXML private TableView<ReportRow> tblReportPreview;
    @FXML private TableColumn<ReportRow, String> colReportCol1;
    @FXML private TableColumn<ReportRow, String> colReportCol2;
    @FXML private TableColumn<ReportRow, String> colReportCol3;

    // ========= DATA HOLDERS =========
    private final ObservableList<LoanRow> adminLoans = FXCollections.observableArrayList();

    // ========= INITIALIZE =========
    @FXML
    private void initialize() {
        System.out.println("Admin controller initialized");

        // تأكد إن الملفات اتقرأت بس مرة
        if (FileControler.BooksList.isEmpty()) {
            FileControler.fillBooksDataAsync();
        }
        if (FileControler.UserList.isEmpty()) {
            FileControler.fillUsersDataAsync();
        }

        setupBooksTable();
        setupUsersTable();
        setupDashboard();
        setupCombos();

        // 🔹 إعداد جدول القروض + تحميل كل القروض
        setupLoansTable();
        try {
            loadLoans(null);   // null أو "" = حمّل الكل
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load loans list.");
        }

        // زر البحث عن القروض
        if (btnSearchLoans != null) {
            btnSearchLoans.setOnAction(this::onSearchLoans);
        }
    }


    // ========= SETUP METHODS =========
    private void setupLoansTable() {
        if (colLoanIdAdmin != null) {
            colLoanIdAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getId())
            );
        }
        if (colLoanUserAdmin != null) {
            colLoanUserAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getUser())
            );
        }
        if (colLoanBookAdmin != null) {
            colLoanBookAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getBook())
            );
        }
        if (colLoanStartAdmin != null) {
            colLoanStartAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getStart())
            );
        }
        if (colLoanDueAdmin != null) {
            colLoanDueAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getDue())
            );
        }
        if (colLoanStatusAdmin != null) {
            colLoanStatusAdmin.setCellValueFactory(
                    data -> new SimpleStringProperty(data.getValue().getStatus())
            );
        }

        if (tblAdminLoans != null) {
            tblAdminLoans.setItems(loansData);
        }
    }

    private void setupBooksTable() {
        colAdminBookId.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getISBN())
        );
        colAdminBookTitle.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getName())
        );
        colAdminBookAuthor.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getAuthor())
        );
        // انت ما عندك category/year/quantity بالـ Book -> نخليهم placeholders
        colAdminBookCategory.setCellValueFactory(
                data -> new SimpleStringProperty("N/A")
        );
        colAdminBookYear.setCellValueFactory(
                data -> new SimpleStringProperty("")
        );
        colAdminBookQuantity.setCellValueFactory(
                data -> new SimpleStringProperty("1")
        );
        colAdminBookStatus.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isBorrowed() ? "Borrowed" : "Available")
        );

        tblAdminBooks.setItems(FXCollections.observableArrayList(FileControler.BooksList));
    }

    private void setupUsersTable() {
        // مبدئياً: نعتمد إن الـ index في UserList هو الـ ID (0,1,2..)
        colAdminUserId.setCellValueFactory(
                data -> new SimpleStringProperty(
                        String.valueOf(FileControler.UserList.indexOf(data.getValue()))
                )
        );
        colAdminUserName.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().getFirstName() + " " + data.getValue().getLastName()
                )
        );
        colAdminUserUsername.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getUsername())
        );
        colAdminUserRole.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().isAdmin() ? "Admin" : "User")
        );
        colAdminUserEmail.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getEmail())
        );
        // ما عندنا phone بالـ User class -> نخليها فاضية
        colAdminUserPhone.setCellValueFactory(
                data -> new SimpleStringProperty("")
        );
        colAdminUserStatus.setCellValueFactory(
                data -> new SimpleStringProperty(
                        data.getValue().hasOutstandingFine() ? "Blocked" : "Active"
                )
        );

        tblAdminUsers.setItems(FXCollections.observableArrayList(FileControler.UserList));
    }

    private void setupDashboard() {
        lblTotalBooks.setText(String.valueOf(FileControler.BooksList.size()));
        lblTotalUsers.setText(String.valueOf(FileControler.UserList.size()));

        long borrowedCount = FileControler.BooksList.stream()
                .filter(Book::isBorrowed)
                .count();

        lblBorrowedCount.setText(String.valueOf(borrowedCount));

        // لسه ما عندنا overdue logic في الـ Admin -> نخليها 0 مؤقتاً
        lblOverdueCountAdmin.setText("0");
    }

    private void setupCombos() {
        if (cmbMediaType != null) {
            cmbMediaType.setItems(FXCollections.observableArrayList(
                    "BOOK",
                    "CD"
            ));
            cmbMediaType.getSelectionModel().selectFirst(); // default BOOK
        }

        if (cmbBookCategory != null) {
            cmbBookCategory.setItems(FXCollections.observableArrayList(
                    "Programming", "Networking", "Electronics", "Math", "Other"
            ));
        }

        if (cmbUserRole != null) {
            cmbUserRole.setItems(FXCollections.observableArrayList(
                    "User", "Admin"
            ));
        }

        if (cmbUserStatus != null) {
            cmbUserStatus.setItems(FXCollections.observableArrayList(
                    "Active", "Blocked"
            ));
        }

        if (cmbReportType != null) {
            cmbReportType.setItems(FXCollections.observableArrayList(
                    "Borrowed Books",
                    "Most Active Users",
                    "Fines Report"
            ));
        }
    }

    // ========= LOAD LOANS FROM Loan.txt =========

    // ========= EVENTS =========

    @FXML
    private void onLogoutAdmin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginGui.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) lblAdminName.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("MH Library - Login");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to go back to login!");
        }
    }

    // ----- Books -----

    @FXML
    private void onClearBookForm(ActionEvent event) {
        txtBookId.clear();
        txtBookTitle.clear();
        txtBookAuthor.clear();
        txtISBN.clear();
        txtBookQuantity.clear();
        txtBookDescription.clear();
        if (cmbBookCategory != null) {
            cmbBookCategory.getSelectionModel().clearSelection();
        }
    }
    @FXML
    private void onAddBook(ActionEvent event) {
        String id = txtBookId.getText().trim();
        String title = txtBookTitle.getText().trim();
        String author = txtBookAuthor.getText().trim();

        if (id.isEmpty() || title.isEmpty() || author.isEmpty()) {
            showAlert("Validation", "Book ID, Title and Author are required.");
            return;
        }

        String mediaType = "BOOK";
        if (cmbMediaType != null && cmbMediaType.getValue() != null) {
            mediaType = cmbMediaType.getValue().toUpperCase();
        }

        String category = "Other";
        if (cmbBookCategory != null && cmbBookCategory.getValue() != null) {
            category = cmbBookCategory.getValue();
        }

        Book b = new Book(title, author, id, false);
        b.setMediaType(mediaType);
        b.setCategory(category);

        FileControler.BooksList.add(b);
        FileControler.addBookAsync(b);

        tblAdminBooks.getItems().add(b);
        lblTotalBooks.setText(String.valueOf(FileControler.BooksList.size()));

        onClearBookForm(null);
    }


    @FXML
    private void onSearchBook() {
        // JOptionPane to ask for the keyword
        String keyword = JOptionPane.showInputDialog(
                null,
                "Enter book name / author / ISBN:",
                "Search Book",
                JOptionPane.QUESTION_MESSAGE
        );

        if (keyword == null || keyword.trim().isEmpty()) {
            // user cancelled or empty input → do nothing
            return;
        }

        // Use FileControler.searchBook
        ArrayList<Book> found = FileControler.searchBooksContains(keyword.trim());

        ObservableList<Book> data = FXCollections.observableArrayList(found);
        tblAdminBooks.setItems(data);

        // optional: if you want a message when nothing found
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No books found for: " + keyword,
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    @FXML
    private void onUpdateBook(ActionEvent event) {
        setupBooksTable();

        Book selected = tblAdminBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a book to update.");
            return;
        }

        selected.setName(txtBookTitle.getText().trim());
        selected.setAuthor(txtBookAuthor.getText().trim());
        selected.setISBN(txtBookId.getText().trim());

        // refresh table
        tblAdminBooks.refresh();
        showAlert("Info", "Book updated in memory. (File rewrite not implemented yet)");
    }

    @FXML
    private void onDeleteBook(ActionEvent event) {
        try {
            Book selected = tblAdminBooks.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Warning", "Select a book to delete.");
                return;
            }

            String isbn = selected.getISBN();

            // 1) ممنوع حذف كتاب مستعار حالياً (Borrowed_Books.txt)
            boolean isBorrowedNow = false;
            java.nio.file.Path bPath = java.nio.file.Paths.get(FileControler.BORROWED_PATH);
            if (java.nio.file.Files.exists(bPath)) {
                java.util.List<String> lines = java.nio.file.Files.readAllLines(bPath);
                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;
                    String[] p = line.split(",");
                    if (p.length < 1) continue;

                    String fileIsbn = p[0].trim();
                    if (fileIsbn.equals(isbn)) {
                        isBorrowedNow = true;
                        break;
                    }
                }
            }

            if (isBorrowedNow) {
                showAlert(
                        "Cannot delete",
                        "This book is currently borrowed. It cannot be deleted."
                        //Alert.AlertType.WARNING
                );
                return;
            }

            // 2) ممنوع حذف كتاب له قروض نشيطة / متأخرة في Loan.txt
            boolean hasActiveLoans = false;
            java.util.List<FileControler.LoanRecord> loanRecords =
                    FileControler.loadLoansFromFile();

            for (FileControler.LoanRecord r : loanRecords) {
                if (r.isbn.equals(isbn)) {
                    // اعتبرنا أن RETURNED هو الوحيد الآمن
                    if (!"RETURNED".equalsIgnoreCase(r.status)) {
                        hasActiveLoans = true;
                        break;
                    }
                }
            }

            if (hasActiveLoans) {
                showAlert(
                        "Cannot delete",
                        "This book has active/overdue loans. It cannot be deleted."
                        //Alert.AlertType.WARNING
                );
                return;
            }

            // 3) تأكيد من الأدمن
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm delete");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    "Are you sure you want to delete this book?\n\n" +
                            "Title: " + selected.getName() + "\n" +
                            "Author: " + selected.getAuthor() + "\n" +
                            "ISBN: " + selected.getISBN()
            );
            java.util.Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK) {
                return;
            }

            // 4) احذف من الذاكرة
            FileControler.BooksList.remove(selected);
            tblAdminBooks.getItems().remove(selected);
            lblTotalBooks.setText(String.valueOf(FileControler.BooksList.size()));

            // 5) اكتب Books.txt من جديد بناءً على BooksList
            rewriteBooksFile();

            showAlert("Info", "Book deleted successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error while deleting book.");
        }
    }
    private void rewriteBooksFile() {
        try {
            java.util.List<String> outLines = new java.util.ArrayList<>();

            for (Book b : FileControler.BooksList) {
                String mediaType = (b.getMediaType() == null || b.getMediaType().isEmpty())
                        ? "BOOK"
                        : b.getMediaType().toUpperCase();

                String category = (b.getCategory() == null || b.getCategory().isEmpty())
                        ? "Other"
                        : b.getCategory();

                String line = String.join(",",
                        b.getName(),
                        b.getAuthor(),
                        b.getISBN(),
                        String.valueOf(b.isBorrowed()),
                        mediaType,
                        category
                );
                outLines.add(line);
            }

            java.nio.file.Path path = java.nio.file.Paths.get(FileControler.BOOKS_PATH);
            java.nio.file.Files.write(path, outLines);
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to rewrite Books.txt file.");
        }
    }

    // ----- Users -----

    @FXML
    private void onClearUserForm(ActionEvent event) {
        txtUserId.clear();
        txtUserFullName.clear();
        txtUserUsername.clear();
        txtUserEmail.clear();
        txtUserPhone.clear();
        if (cmbUserRole != null) cmbUserRole.getSelectionModel().clearSelection();
        if (cmbUserStatus != null) cmbUserStatus.getSelectionModel().clearSelection();
    }

    @FXML
    private void onAddUser(ActionEvent event) {
        String fullName = txtUserFullName.getText().trim();
        String username = txtUserUsername.getText().trim();
        String email    = txtUserEmail.getText().trim();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            showAlert("Validation", "Full name, username and email are required.");
            return;
        }

        String[] parts = fullName.split(" ");
        String first = parts.length > 0 ? parts[0] : fullName;
        String last  = parts.length > 1 ? parts[1] : "";

        User u = new User(first, last, username, email, "1234", new String[0]); // مبدئياً باسورد ثابت
        if (cmbUserRole != null && "Admin".equals(cmbUserRole.getValue())) {
            u.setAdmin(true);
        }

        FileControler.UserList.add(u);
        // تقدر تستخدم FileControler.addUser(u) لو ضبطت فورمات الملف
        tblAdminUsers.getItems().add(u);
        lblTotalUsers.setText(String.valueOf(FileControler.UserList.size()));

        onClearUserForm(null);
    }

    @FXML
    private void onUpdateUser(ActionEvent event) {
        setupUsersTable();
        User selected = tblAdminUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a user to update.");
            return;
        }

        String fullName = txtUserFullName.getText().trim();
        String[] parts = fullName.split(" ");
        selected.setFirstName(parts.length > 0 ? parts[0] : fullName);
        selected.setLastName(parts.length > 1 ? parts[1] : "");

        selected.setUsername(txtUserUsername.getText().trim());
        selected.setEmail(txtUserEmail.getText().trim());

        if (cmbUserRole != null) {
            selected.setAdmin("Admin".equals(cmbUserRole.getValue()));
        }

        tblAdminUsers.refresh();
        showAlert("Info", "User updated in memory. (File rewrite not implemented yet)");
    }

    @FXML
    private void onDeleteUser(ActionEvent event) {
        User selected = tblAdminUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a user to delete.");
            return;
        }

        // منطقك السابق: ما بنحذف لو عنده قروض/غرامات -> مفترض تعمل check هنا
        if (FileControler.userHasActiveLoansOrFines(selected)) {
            showAlert("Cannot delete",
                    "User still has active loans or unpaid fines.");
            return;
        }

        FileControler.UserList.remove(selected);
        tblAdminUsers.getItems().remove(selected);
        lblTotalUsers.setText(String.valueOf(FileControler.UserList.size()));

        showAlert("Info", "User removed from list. (File rewrite not implemented yet)");
    }

    // ----- Loans -----
    private void loadLoans(String filter) throws Exception {
        loansData.clear();

        Path path = Paths.get(FileControler.BORROWED_PATH);

        if (!Files.exists(path)) {
            // ما في Borrowed_Books.txt → ما في قروض
            return;
        }

        List<String> lines = Files.readAllLines(path);
        LocalDate today = LocalDate.now();
        int counter = 1;

        String f = (filter == null) ? "" : filter.trim().toLowerCase();

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;

            // format: ISBN,Title,BorrowDate,User
            String[] p = line.split(",");
            if (p.length < 4) continue;

            String isbn       = p[0].trim();
            String title      = p[1].trim();
            String borrowDate = p[2].trim();
            String username   = p[3].trim();

            LocalDate startDate;
            try {
                startDate = LocalDate.parse(borrowDate);
            } catch (Exception ex) {
                // لو التاريخ معطوب، اعتبره اليوم فقط عشان ما يكسر البرنامج
                startDate = today;
            }

            LocalDate dueDate = startDate.plusDays(28);
            long days = ChronoUnit.DAYS.between(startDate, today);
            String status = (days > 28) ? "Overdue" : "Borrowed";

            // فلترة لو في filter
            boolean matches = true;
            if (!f.isEmpty()) {
                String full = (isbn + " " + title + " " + username).toLowerCase();
                matches = full.contains(f);
            }

            if (!matches) continue;

            LoanRow row = new LoanRow(
                    String.valueOf(counter++),        // id بسيط تسلسلي
                    username,
                    title + " (" + isbn + ")",
                    startDate.toString(),
                    dueDate.toString(),
                    status
            );

            loansData.add(row);
        }
    }

    @FXML
    private void onSearchLoans(ActionEvent event) {
        String key = (txtSearchLoans != null) ? txtSearchLoans.getText() : "";

        try {
            if (key == null || key.trim().isEmpty()) {
                // لو فاضي → رجّع كل القروض
                loadLoans(null);
            } else {
                loadLoans(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to search loans. Please try again.");
        }
    }
    @FXML
    private void onMarkReturned(ActionEvent event) {
        try {
            LoanRow selected = tblAdminLoans.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showAlert("Warning", "Select a loan row first.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm return");
            confirm.setHeaderText(null);
            confirm.setContentText(
                    "Mark this loan as returned?\n\n" +
                            "User: " + selected.getUser() + "\n" +
                            "Book: " + selected.getBook()
            );
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isEmpty() || res.get() != ButtonType.OK)
                return;

            // --- Update file ---
            boolean ok = markLoanReturnedInFile(
                    selected.getUser(),
                    selected.getBook(),
                    selected.getStart(),
                    selected.getDue()
            );

            if (!ok) {
                showAlert("Error", "Failed to mark loan as returned in file.");
                return;
            }

            // --- Update table row ---
            selected.setStatus("Returned");
            tblAdminLoans.refresh();

            // --- Sync ALL book statuses from Borrowed_Books.txt ---
            FileControler.syncBorrowedStatusOnce();

            showAlert("Info", "Loan marked as returned and system synced.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Unexpected error while marking returned.");
        }
    }

    private String extractIsbnFromBookDisplay(String bookDisplay) {
        // متوقّع "Title (ISBN)"
        int open = bookDisplay.lastIndexOf('(');
        int close = bookDisplay.lastIndexOf(')');
        if (open >= 0 && close > open) {
            return bookDisplay.substring(open + 1, close).trim();
        }
        // fallback لو ما كان بالشكل هذا
        return bookDisplay.trim();
    }

    // تعديل Loan.txt داخلياً
    private boolean markLoanReturnedInFile(String username,
                                           String bookDisplay,
                                           String startDateStr,
                                           String dueDateStr) {
        try {
            // نشتغل على Borrowed_Books.txt
            java.nio.file.Path path = java.nio.file.Paths.get(FileControler.BORROWED_PATH);

            if (!java.nio.file.Files.exists(path)) {
                return false;
            }

            java.util.List<String> lines =
                    java.nio.file.Files.readAllLines(path);

            java.util.List<String> updated = new java.util.ArrayList<>();
            boolean removed = false;

            // bookDisplay عندنا بالشكل: "Title (ISBN)"
            String isbn = extractIsbnFromBookDisplay(bookDisplay);

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                // format: ISBN,Title,BorrowDate,User
                String[] p = line.split(",");
                if (p.length < 4) {
                    updated.add(line);
                    continue;
                }

                String fileIsbn   = p[0].trim();
                String fileTitle  = p[1].trim();
                String fileBorrow = p[2].trim();
                String fileUser   = p[3].trim();

                // نطابق على user + isbn + تاريخ الاستعارة
                if (fileUser.equals(username)
                        && fileIsbn.equals(isbn)
                        && fileBorrow.equals(startDateStr)) {
                    // يعني رجّعنا هذا الـ loan → ما نضيفه للـ updated
                    removed = true;
                    continue;
                }

                updated.add(line);
            }

            if (removed) {
                java.nio.file.Files.write(
                        path,
                        updated,
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                        java.nio.file.StandardOpenOption.CREATE
                );
            }

            return removed;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----- Send Reminder (global for many users) -----

    @FXML
    public void onSendReminder(ActionEvent actionEvent) {
        try {
            EmailService emailService = EmailService.fromEnv();

            int sentCount = 0;
            java.time.LocalDate today = java.time.LocalDate.now();

            for (User u : FileControler.UserList) {
                String to = u.getEmail();
                if (to == null || to.trim().isEmpty()) continue;

                List<Loan> loans = FileControler.loadLoansForUser(u);
                if (loans == null || loans.isEmpty()) continue;

                boolean shouldNotify = false;
                StringBuilder body = new StringBuilder();

                for (Loan loan : loans) {
                    if (loan.isReturned()) continue;

                    java.time.LocalDate due = loan.getDueDate();
                    long diff = java.time.temporal.ChronoUnit.DAYS.between(today, due); // due - today

                    // متأخر أو ضايل <= 3 أيام
                    if (diff < 0 || diff <= 3) {
                        shouldNotify = true;
                        body.append("- Book: ")
                                .append(loan.getBook().getName())
                                .append(" (ISBN: ").append(loan.getBook().getISBN()).append(")")
                                .append(" | Due: ").append(due)
                                .append("\n");
                    }
                }

                if (!shouldNotify) continue;

                String subject = "MH Library - Loan Reminder";
                String textBody =
                        "Dear " + u.getFirstName() + ",\n\n" +
                                "This is a reminder about your current library loans:\n\n" +
                                body +
                                "\nPlease visit the library or the system to renew/return.\n" +
                                "Best regards,\nMH Library System";

                try {
                    emailService.sendEmail(to, subject, textBody);
                    sentCount++;
                } catch (RuntimeException ex) {
                    System.out.println("Failed to send email to " + to);
                    ex.printStackTrace();
                }
            }

            showAlert("Info", "Reminder emails attempted for " + sentCount + " users.");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send reminder emails. Check .env and internet.");
        }
    }

    // ----- Reports (stubs) -----

    @FXML
    private void onGenerateReport(ActionEvent event) {

    }

    @FXML
    private void onExportReport(ActionEvent event) {
        showAlert("Info", "Report export not implemented yet.");
    }

    // ======== EXTRA (placeholders from قبل) ========

    public void onSearchUser(ActionEvent actionEvent) {
        // ممكن نربطه لاحقاً مع txtSearchLoans أو search خاص بالمستخدمين
    }

    // ========= HELPER CLASSES =========

    public static class ActivityRow {
        private final String time;
        private final String user;
        private final String action;

        public ActivityRow(String time, String user, String action) {
            this.time = time;
            this.user = user;
            this.action = action;
        }

        public String getTime() { return time; }
        public String getUser() { return user; }
        public String getAction() { return action; }
    }

    public static class LoanRow {
        private String id;
        private String user;
        private String book;
        private String start;
        private String due;
        private String status;

        public LoanRow(String id, String user, String book, String start, String due, String status) {
            this.id = id;
            this.user = user;
            this.book = book;
            this.start = start;
            this.due = due;
            this.status = status;
        }

        public String getId() { return id; }
        public String getUser() { return user; }
        public String getBook() { return book; }
        public String getStart() { return start; }
        public String getDue() { return due; }
        public String getStatus() { return status; }

        // 👈 هذا اللي كان عامل لك error
        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class ReportRow {
        private final String col1;
        private final String col2;
        private final String col3;

        public ReportRow(String c1, String c2, String c3) {
            this.col1 = c1;
            this.col2 = c2;
            this.col3 = c3;
        }

        public String getCol1() { return col1; }
        public String getCol2() { return col2; }
        public String getCol3() { return col3; }
    }

    // ========= UTILS =========

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
