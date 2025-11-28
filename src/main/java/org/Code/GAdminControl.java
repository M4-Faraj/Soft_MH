package org.Code;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;

public class GAdminControl {

    // ========= TOP BAR =========
    @FXML private Label lblAdminName;
    @FXML private Button btnLogoutAdmin;

    // ========= RIGHT SIDE =========
    @FXML private ImageView imgSideAdmin;

    // ========= TABS ROOT =========
    @FXML private TabPane tabPaneAdmin;

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
    @FXML private ComboBox<String> cmbBookCategory;
    @FXML private TextField txtBookYear;
    @FXML private TextField txtBookQuantity;
    @FXML private TextArea txtBookDescription;

    @FXML private Button btnClearBookForm;
    @FXML private Button btnAddBook;
    @FXML private Button btnUpdateBook;
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

    // ========= REPORTS TAB =========
    @FXML private DatePicker dpReportFrom;
    @FXML private DatePicker dpReportTo;
    @FXML private ComboBox<String> cmbReportType;
    @FXML private Button btnGenerateReport;
    @FXML private Button btnExportReport;
    @FXML private TableView<ReportRow> tblReportPreview;
    @FXML private TableColumn<ReportRow, String> colReportCol1;
    @FXML private TableColumn<ReportRow, String> colReportCol2;
    @FXML private TableColumn<ReportRow, String> colReportCol3;

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
    }

    // ========= SETUP METHODS =========

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

        // لسه ما عندنا overdue logic -> نخليها 0
        lblOverdueCountAdmin.setText("0");
    }

    private void setupCombos() {
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
        txtBookYear.clear();
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

        Book b = new Book(title, author, id, false);
        FileControler.BooksList.add(b);
        FileControler.addBook(b);

        tblAdminBooks.getItems().add(b);
        lblTotalBooks.setText(String.valueOf(FileControler.BooksList.size()));

        onClearBookForm(null);
    }

    @FXML
    private void onUpdateBook(ActionEvent event) {
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
        Book selected = tblAdminBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a book to delete.");
            return;
        }

        FileControler.BooksList.remove(selected);
        tblAdminBooks.getItems().remove(selected);
        lblTotalBooks.setText(String.valueOf(FileControler.BooksList.size()));

        showAlert("Info", "Book removed from list. (File rewrite not implemented yet)");
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

        FileControler.UserList.remove(selected);
        tblAdminUsers.getItems().remove(selected);
        lblTotalUsers.setText(String.valueOf(FileControler.UserList.size()));

        showAlert("Info", "User removed from list. (File rewrite not implemented yet)");
    }

    // ----- Loans (stubs حالياً) -----

    @FXML
    private void onSearchLoans(ActionEvent event) {
        String key = txtSearchLoans.getText();
        System.out.println("Search loans for: " + key);
        showAlert("Info", "Search loans not implemented yet.");
    }

    @FXML
    private void onMarkReturned(ActionEvent event) {
        showAlert("Info", "Mark as returned not implemented yet.");
    }

    @FXML
    private void onMarkOverdue(ActionEvent event) {
        showAlert("Info", "Mark as overdue not implemented yet.");
    }

    // ----- Reports (stubs) -----

    @FXML
    private void onGenerateReport(ActionEvent event) {
        showAlert("Info", "Report generation not implemented yet.");
    }

    @FXML
    private void onExportReport(ActionEvent event) {
        showAlert("Info", "Report export not implemented yet.");
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
        private final String id;
        private final String user;
        private final String book;
        private final String start;
        private final String due;
        private final String status;

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
