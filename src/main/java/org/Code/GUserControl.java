package org.Code;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.Code.FileControler.BORROWED_PATH;

public class GUserControl extends Application {

    // ================== FXML FIELDS ==================

    // Top bar
    @FXML private Label lblCurrentUser;
    @FXML private Button btnLogout;

    // Right image
    @FXML private ImageView imgSide;

    // Tabs
    @FXML private TabPane tabPane;

    // -------- TAB 1: Search & Browse --------
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbFilterType;
    @FXML private Button btnSearch;

    @FXML private TableView<Book> tblBooks;
    @FXML private TableColumn<Book, String> colBookId;
    @FXML private TableColumn<Book, String> colTitle;
    @FXML private TableColumn<Book, String> colAuthor;
    @FXML private TableColumn<Book, String> colCategory;
    @FXML private TableColumn<Book, String> colYear;
    @FXML private TableColumn<Book, String> colStatus;

    @FXML private Label lblSelectedBook;
    @FXML private Button btnBorrow;
    @FXML private Button btnReserve;
    @FXML private Button btnAddFavourite;

    // -------- TAB 2: My Loans --------
    @FXML private TableView<Loan> tblLoans;
    @FXML private TableColumn<Loan, String> colLoanBookTitle;
    @FXML private TableColumn<Loan, String> colLoanAuthor;
    @FXML private TableColumn<Loan, String> colLoanStart;
    @FXML private TableColumn<Loan, String> colLoanDue;
    @FXML private TableColumn<Loan, String> colLoanStatus;
    @FXML private Label lblLoansCount;
    @FXML private Button btnReturn;
    @FXML private Button btnRenew;

    // -------- TAB 3: Reservations --------
    @FXML private TableView<?> tblReservations;
    @FXML private TableColumn<?, ?> colResBookTitle;
    @FXML private TableColumn<?, ?> colResAuthor;
    @FXML private TableColumn<?, ?> colResDate;
    @FXML private TableColumn<?, ?> colResStatus;
    @FXML private Button btnCancelReservation;

    // -------- TAB 4: Favourites --------
    @FXML private TableView<?> tblFavourites;
    @FXML private TableColumn<?, ?> colFavBookTitle;
    @FXML private TableColumn<?, ?> colFavAuthor;
    @FXML private TableColumn<?, ?> colFavCategory;
    @FXML private Button btnRemoveFavourite;

    // -------- TAB 5: Profile --------
    @FXML private TextField txtFullName;
    @FXML private TextField txtProfileUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private TextField txtAddress;
    @FXML private Button btnChangePassword;
    @FXML private Button btnSaveProfile;
    private ObservableList<Loan> loansObservable = FXCollections.observableArrayList();

    // ================== BACKING DATA ==================
    private ObservableList<Book> booksObservable = FXCollections.observableArrayList();

    // منطق الكتب + اليوزر الحالي
    private BookControl bookControl;
    private User currentUser;

    // هادي تستدعيها من الـ Login بعد ما تعرف مين اليوزر والـ BookControl
    public void setContext(BookControl bookControl, User currentUser) {
        this.bookControl = bookControl;
        this.currentUser = currentUser;

        if (currentUser != null && lblCurrentUser != null) {
            lblCurrentUser.setText("Welcome, " + currentUser.getFirstName());
        }

        refreshLoansTable();
    }


    // ================== start(): LOAD FXML ==================
    @Override
    public void start(Stage primaryStage) throws Exception {
        FileControler.startBackgroundSync();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Code/user_view.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("MH Library - User Portal");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    // ================== initialize(): CALLED AFTER FXML LOAD ==================
    @FXML
    private void initialize() {
        // فلتر افتراضي
        if (tblLoans != null) {
            tblLoans.setItems(loansObservable);
        }

        if (btnRenew != null) {
            btnRenew.setOnAction(e -> onRenew());
        }

// (زر return بنشتغل عليه بعدين لو حابب)

        if (btnBorrow != null)  btnBorrow.setOnAction(this::onBorrow);
        if (btnReserve != null) btnReserve.setOnAction(this::onReserve); // 👈 بدال handleReserve

        if (cmbFilterType != null) {
            cmbFilterType.getItems().setAll("Title", "Author", "ISBN");
            cmbFilterType.getSelectionModel().selectFirst();
        }

        // حمّل الكتب من FileControler
        if (FileControler.BooksList.isEmpty()) {
            // لو عندك نسخة async استخدمها، غير هيك استعمل fillBooksData()
            FileControler.fillBooksDataAsync();
        }
        booksObservable.setAll(FileControler.BooksList);

        // ربط أعمدة الكتب
        if (colBookId != null) {
            colBookId.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getISBN()));
        }
        if (colTitle != null) {
            colTitle.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getName()));
        }
        if (colAuthor != null) {
            colAuthor.setCellValueFactory(data ->
                    new SimpleStringProperty(data.getValue().getAuthor()));
        }
        // حالياً ما في category / year في Book
        if (colCategory != null) {
            colCategory.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            (data.getValue().getMediaType() == null || data.getValue().getMediaType().isEmpty())
                                    ? "BOOK"
                                    : data.getValue().getMediaType()
                    )
            );
        }

        if (colYear != null) {
            colYear.setCellValueFactory(data -> new SimpleStringProperty(""));
        }
        if (colStatus != null) {
            colStatus.setCellValueFactory(data ->
                    new SimpleStringProperty(
                            data.getValue().isBorrowed() ? "Borrowed" : "Available"
                    ));
        }

        if (tblBooks != null) {
            tblBooks.setItems(booksObservable);
            // listener على اختيار كتاب
            tblBooks.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) {
                    lblSelectedBook.setText("Selected: " + newSel.getName() + " (" + newSel.getISBN() + ")");
                } else {
                    lblSelectedBook.setText("No book selected");
                }
            });
        }

        // إعداد جدول الـ Loans (لو جاهز في الـ FXML)
        if (tblLoans != null) {
            if (colLoanBookTitle != null) {
                colLoanBookTitle.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getBook().getName()));
            }
            if (colLoanAuthor != null) {
                colLoanAuthor.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getBook().getAuthor()));
            }
            if (colLoanStart != null) {
                colLoanStart.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getStartDate().toString()));
            }
            if (colLoanDue != null) {
                colLoanDue.setCellValueFactory(data ->
                        new SimpleStringProperty(data.getValue().getDueDate().toString()));
            }

            if (colLoanStatus != null) {
                colLoanStatus.setCellValueFactory(data -> {
                    Loan loan = data.getValue();
                    String status;

                    if (loan.isReturned()) {
                        status = "Returned";
                    } else if (loan.isRenewalRequested()) {
                        status = "Waiting";          // 👈 طلب التجديد مبعوث للإدمن
                    } else if (loan.isOverdue()) {
                        status = "Overdue";
                    } else {
                        status = "Borrowed";
                    }

                    return new SimpleStringProperty(status);
                });
            }

            tblLoans.setItems(loansObservable);}

        // أزرار
        if (btnBorrow != null)  btnBorrow.setOnAction(this::onBorrow);
        if (btnAddFavourite != null) btnAddFavourite.setOnAction(e -> handleAddFavourite());
        if (btnSearch != null) btnSearch.setOnAction(e -> handleSearch());
        if (btnLogout != null) {
            btnLogout.setOnAction(e -> {
                try {
                    handleLogout();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }

    // ================== BORROW LOGIC ==================
    @FXML
    private void onBorrow(ActionEvent event) {
        // 0) Check if user has overdue CD loans
        if (currentUser != null && FileControler.hasOverdueCDs(currentUser)) {
            showAlert(
                    "Borrowing blocked",
                    "You cannot borrow new items because you have overdue CD loans.\n" +
                            "Please return your overdue CDs first.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // لو حابب تظل محتفظ بفحص الكتب المتأخرة كمان:
        if (currentUser != null && FileControler.hasOverdueBooks(currentUser.getUsername())) {
            showAlert(
                    "Borrowing blocked",
                    "You cannot borrow a new book because you have overdue books.\n" +
                            "Please return them first.",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 1) خذ الكتاب المختار من الجدول
        Book selected = tblBooks.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showAlert("No book selected", "Please select a book to borrow.", Alert.AlertType.WARNING);
            return;
        }

        // 2) تأكد إن الكتاب/CD مش مستعار أصلاً
        if (selected.isBorrowed()) {
            showAlert("Already borrowed",
                    "This item is already borrowed. Please choose another copy or another item.",
                    Alert.AlertType.INFORMATION);
            return;
        }

        // 3) تأكد إن الـ context جاهز
        if (currentUser == null) {
            showAlert("System error",
                    "Current user is not initialized.",
                    Alert.AlertType.ERROR);
            return;
        }

        // 4) نادِ منطق البورّو في FileControler
        try {
            Book b = selected;

            if ("CD".equalsIgnoreCase(b.getMediaType())) {
                // 💿 استعارة CD
                FileControler.addBorrowedCD(b.getISBN(), b.getName(), currentUser.getUsername());
            } else {
                // 📚 استعارة كتاب عادي
                FileControler.addBorrowedBook(b.getISBN(), b.getName(), currentUser.getUsername());
            }

            // حدّث حالة الكتاب في الذاكرة عشان الجدول ينعكس صح
            selected.updateBorrowed(true);

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Borrow failed",
                    "Could not borrow this item. Please try again.",
                    Alert.AlertType.ERROR);
            return;
        }

        // 5) حدّث جدول الـ loans والـ status في جدول الكتب
        FileControler.syncBorrowedStatusOnce();
        refreshLoansTable();
        tblBooks.refresh();
    }

    // ================== SEARCH ==================
    private void handleSearch() {
        String query = txtSearch.getText();
        if (query == null || query.trim().isEmpty()) {
            return; // no input, do nothing
        }

        String normalizedQuery = query.trim().toLowerCase();

        // Search file for any substring
        ArrayList<Book> found = FileControler.searchBooksContains(normalizedQuery);

        ObservableList<Book> bookList = FXCollections.observableArrayList(found);

        // Fill the table with results
        tblBooks.setItems(bookList);

        // Optional popup when nothing found
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No books found for: " + query,
                    "Search Result",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    // ================== OTHER BUTTONS ==================
    private void handleReserve() {
        Book selected = tblBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No book selected", "Please select a book first.", Alert.AlertType.WARNING);
            return;
        }
        // TODO: logic reserve

        showAlert("Reserve", "Reserved: " + selected.getName(), Alert.AlertType.INFORMATION);
    }

    private void handleAddFavourite() {
        Book selected = tblBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No book selected", "Please select a book first.", Alert.AlertType.WARNING);
            return;
        }
        // TODO: logic add to favourites
        showAlert("Favourites", "Added to favourites: " + selected.getName(), Alert.AlertType.INFORMATION);
    }

    private void handleLogout() throws IOException {
        // غيّر المسار حسب مكان loginGui.fxml
        switchScene("loginGui.fxml","MH Library");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) txtFullName.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
    }

    // ================== MAIN ==================
    public static void main(String[] args) {
        launch(args);
    }
    @FXML
    public void onReserve(ActionEvent actionEvent) {
        // 1) book مختار؟
        Book selected = tblBooks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No book selected", "Please select a book first.", Alert.AlertType.WARNING);
            return;
        }

        // 2) user موجود؟
        if (currentUser == null) {
            showAlert("System error", "Current user is not initialized.", Alert.AlertType.ERROR);
            return;
        }

        // 3) نحاول نعمل "إرجاع بدون غرامة"
        boolean success = FileControler.unBorrowIfNoFine(
                selected.getISBN(),
                currentUser.getUsername()
        );

        if (!success) {
            showAlert(
                    "Cannot unborrow",
                    "You cannot unborrow this book because there is a fine (more than 28 days).",
                    Alert.AlertType.WARNING
            );
            return;
        }

        // 4) لو نجح: حدّث حالة الكتاب في الذاكرة والجدول
        selected.updateBorrowed(false);
        tblBooks.refresh();

        // لو حابب تعيد مزامنة من الملفات:
        // FileControler.syncBorrowedStatusOnce();

        showAlert(
                "Book unborrowed",
                "The book has been unborrowed and your reservation/return is accepted (no fine).",
                Alert.AlertType.INFORMATION
        );
    }
    @FXML
    private void onRenew() {
        Loan selectedLoan = tblLoans.getSelectionModel().getSelectedItem();

        if (selectedLoan == null) {
            showAlert("No loan selected", "Please select a loan to renew.", Alert.AlertType.WARNING);
            return;
        }

        if (currentUser == null) {
            showAlert("System error", "Current user is not initialized.", Alert.AlertType.ERROR);
            return;
        }

        // 1) احسب مجموع الغرامات على كل قروض هذا المستخدم
        double totalFees = 0.0;
        for (Loan loan : tblLoans.getItems()) {
            totalFees += loan.getLoanFee();   // 10 NIS بعد 28 يوم حسب منطقك
        }

        // 2) JOptionPane علشان الدفع + تأكيد الطلب
        int choice = javax.swing.JOptionPane.showConfirmDialog(
                null,
                "Your total current fines: " + totalFees + " NIS.\n"
                        + "Do you want to pay and send a renewal request to the admin?",
                "Renew loans",
                javax.swing.JOptionPane.YES_NO_OPTION
        );

        if (choice != javax.swing.JOptionPane.YES_OPTION) {
            // المستخدم رفض أو سكّر الـ dialog
            return;
        }

        // 3) ما بنعمل تجديد فعلي في الملف الآن
        //    بس نعلّم القرض إن عليه طلب تجديد → Waiting
        selectedLoan.setRenewalRequested(true);

        // 🔥 جديد: نكتب الطلب في RenewRequests.txt
        FileControler.addRenewRequest(currentUser, selectedLoan);

        // 4) ريـفرش الجدول علشان يظهر "Waiting"
        tblLoans.refresh();

        // 5) رسالة نجاح للمستخدم
        showAlert(
                "Request sent",
                "Your renewal request has been sent to the admin.\n"
                        + "Status changed to: Waiting.",
                Alert.AlertType.INFORMATION
        );
    }


    private void refreshLoansTable() {
        if (currentUser == null || tblLoans == null) return;

        List<Loan> userLoans = FileControler.loadLoansForUser(currentUser);
        loansObservable.setAll(userLoans);

        if (lblLoansCount != null) {
            lblLoansCount.setText(String.valueOf(userLoans.size()));
        }
    }


    public void Refresh(ActionEvent actionEvent) {

    }
}


