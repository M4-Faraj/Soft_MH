package org.Code;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class GLibrarianControl extends Application {

    // ========= FXML BINDINGS ===========
    @FXML private TableView<OverdueEntry> tblOverdue;
    @FXML private TableColumn<OverdueEntry,String> colIsbn;
    @FXML private TableColumn<OverdueEntry,String> colTitle;
    @FXML private TableColumn<OverdueEntry,String> colUser;
    @FXML private TableColumn<OverdueEntry,String> colBorrowDate;
    @FXML private TableColumn<OverdueEntry,String> colDueDate;
    @FXML private TableColumn<OverdueEntry,String> colDaysOverdue;
    @FXML private TableColumn<OverdueEntry,String> colFine;

    @FXML private Label lblSelIsbn;
    @FXML private Label lblSelTitle;
    @FXML private Label lblSelUser;
    @FXML private Label lblSelBorrowDate;
    @FXML private Label lblSelDueDate;
    @FXML private Label lblSelDaysOverdue;
    @FXML private Label lblSelFine;

    @FXML private Label lblOverdueCount;
    @FXML private Label lblLibrarianName;
    @FXML private Label lblCurrentDate;

    @FXML private TextField txtFilterUser;
    @FXML private TextField txtFilterIsbn;

    @FXML private Button btnRefreshOverdue;
    @FXML private Button btnFilterOverdue;
    @FXML private Button btnClearFilter;
    @FXML private Button btnLogout;

    @FXML private Button btnSendMailSelected;
    @FXML private Button btnSendMailThread;

    private final ObservableList<OverdueEntry> overdueList = FXCollections.observableArrayList();
    private User currentLibrarian;


    // ========== STRUCT ==========
    public static class OverdueEntry {
        public String isbn, title, username;
        public LocalDate borrowDate, dueDate;
        public long overdueDays;
        public double fine;

        public OverdueEntry(String isbn, String title, String username, LocalDate borrowDate) {
            this.isbn = isbn;
            this.title = title;
            this.username = username;
            this.borrowDate = borrowDate;
            this.dueDate = borrowDate.plusDays(28);

            long totalDays = ChronoUnit.DAYS.between(borrowDate, LocalDate.now());
            overdueDays = Math.max(0, totalDays - 28);
            fine = overdueDays > 0 ? 10.0 : 0.0;
        }
    }


    // ========== LOAD FXML ==========
    @Override
    public void start(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/Code/librarian.fxml"));
            Parent root = loader.load();
            stage.setScene(new Scene(root));
            stage.setTitle("Librarian Panel");
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ========= SET LIBRARIAN INFO =========
    public void setLibrarian(User librarian) {
        this.currentLibrarian = librarian;
        if (lblLibrarianName != null && librarian != null) {
            lblLibrarianName.setText("Welcome, " + librarian.getFirstName());
        }
    }


    // ========== INITIALIZE ==========
    @FXML
    private void initialize() {
        setupTable();
        lblCurrentDate.setText("Today: " + LocalDate.now());
        loadOverdueData();

        tblOverdue.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) showDetails(newSel);
        });

        btnRefreshOverdue.setOnAction(e -> loadOverdueData());
        btnFilterOverdue.setOnAction(e -> applyFilter());

        btnClearFilter.setOnAction(e -> {
            txtFilterUser.clear();
            txtFilterIsbn.clear();
            tblOverdue.setItems(overdueList);
            lblOverdueCount.setText(String.valueOf(overdueList.size()));
        });

        btnLogout.setOnAction(e -> {
            try { handleLogout(); }
            catch (IOException ex) { throw new RuntimeException(ex); }
        });

        btnSendMailSelected.setOnAction(this::onSendMailSelected);
        btnSendMailThread.setOnAction(this::onSendReminderThread);
    }


    // ========== TABLE SETUP ==========
    private void setupTable() {
        colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isbn));
        colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title));
        colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username));
        colBorrowDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().borrowDate.toString()));
        colDueDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dueDate.toString()));
        colDaysOverdue.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().overdueDays)));
        colFine.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().fine)));

        tblOverdue.setItems(overdueList);
    }


    // ========== LOAD BORROWED FILE ==========
    private void loadOverdueData() {
        overdueList.clear();

        try {
            List<String> lines = Files.readAllLines(Paths.get(FileControler.BORROWED_PATH));

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                String[] p = line.split(",");
                if (p.length < 4) continue;

                String isbn = p[0].trim();
                String title = p[1].trim();
                LocalDate borrow = LocalDate.parse(p[2].trim());
                String user = p[3].trim();

                OverdueEntry entry = new OverdueEntry(isbn, title, user, borrow);

                if (entry.overdueDays > 0) {  // Only overdue
                    overdueList.add(entry);
                }
            }

            tblOverdue.setItems(overdueList);
            lblOverdueCount.setText(String.valueOf(overdueList.size()));

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error", "Failed to load overdue data.");
        }
    }


    // ========== DETAILS PANEL ==========
    private void showDetails(OverdueEntry e) {
        lblSelIsbn.setText(e.isbn);
        lblSelTitle.setText(e.title);
        lblSelUser.setText(e.username);
        lblSelBorrowDate.setText(e.borrowDate.toString());
        lblSelDueDate.setText(e.dueDate.toString());
        lblSelDaysOverdue.setText(String.valueOf(e.overdueDays));
        lblSelFine.setText(String.valueOf(e.fine));
    }


    // ========== FILTER ==========
    private void applyFilter() {
        String u = txtFilterUser.getText().trim().toLowerCase();
        String i = txtFilterIsbn.getText().trim().toLowerCase();

        ObservableList<OverdueEntry> filtered = overdueList.filtered(e ->
                (u.isEmpty() || e.username.toLowerCase().contains(u)) &&
                        (i.isEmpty() || e.isbn.toLowerCase().contains(i))
        );

        tblOverdue.setItems(filtered);
        lblOverdueCount.setText(String.valueOf(filtered.size()));
    }


    // ========== LOGOUT ==========
    private void handleLogout() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/loginGui.fxml"));
        Parent root = loader.load();

        Stage stage = (Stage) tblOverdue.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle("MH Library - Login");
        stage.centerOnScreen();
    }


    // =====================================================================
    // 🔥 SEND MAIL TO SELECTED USER
    // =====================================================================
    @FXML
    private void onSendMailSelected(ActionEvent event) {

        OverdueEntry selected = tblOverdue.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showError("No Selection", "Choose a user from the table first.");
            return;
        }

        String email = FileControler.getEmailForUser(selected.username);
        if (email == null) {
            showError("Missing Email", "This user has no email registered.");
            return;
        }

        try {
            Dotenv dotenv = Dotenv.load();
            String mailUser = dotenv.get("EMAIL_USERNAME");
            String mailPass = dotenv.get("EMAIL_PASSWORD");

            EmailService mailService = new EmailService(mailUser, mailPass);

            String subject = "Library Reminder";
            String body =
                    "Dear " + selected.username + ",\n\n" +
                            "This is a reminder regarding your borrowed book:\n" +
                            "\"" + selected.title + "\" (ISBN: " + selected.isbn + ").\n\n" +
                            "Best regards,\nMH Library";

            mailService.sendEmail(email, subject, body);

            showInfo("Email Sent", "Reminder sent to: " + email);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Email Error", "Failed to send reminder.");
        }
    }


    // =====================================================================
    // 🔥 THREAD: SEND REMINDER WHEN 3 DAYS LEFT
    // =====================================================================
    @FXML
    private void onSendReminderThread(ActionEvent event) {

        Thread t = new Thread(() -> {
            int sentCount = 0;

            try {
                // 1) حمّل كل الـ loans من ملف الاستعارة
                List<String> lines = Files.readAllLines(Paths.get(FileControler.BORROWED_PATH));

                // 2) جهّز إعدادات الإيميل مرّة وحدة
                Dotenv dotenv = Dotenv.load();
                String mailUser = dotenv.get("EMAIL_USERNAME");
                String mailPass = dotenv.get("EMAIL_PASSWORD");
                EmailService mailService = new EmailService(mailUser, mailPass);

                LocalDate today = LocalDate.now();

                for (String line : lines) {
                    if (line.trim().isEmpty()) continue;

                    // format: ISBN,Name,Date,User
                    String[] p = line.split(",");
                    if (p.length < 4) continue;

                    String isbn   = p[0].trim();
                    String title  = p[1].trim();
                    LocalDate borrowDate;
                    try {
                        borrowDate = LocalDate.parse(p[2].trim());
                    } catch (Exception ex) {
                        continue; // تاريخ خربان
                    }
                    String username = p[3].trim();

                    // due بعد 28 يوم
                    LocalDate dueDate = borrowDate.plusDays(28);
                    long daysLeft = ChronoUnit.DAYS.between(today, dueDate);

                    // 👈 هون الشرط تبعك: قبل الـ overdue بثلاث أيام
                    if (daysLeft != 3) continue;

                    // جيب الإيميل من Users.txt
                    String email = FileControler.getEmailForUser(username);
                    if (email == null) {
                        System.err.println("No email for user " + username);
                        continue;
                    }

                    String subject = "Library Reminder: 3 Days Left";
                    String body =
                            "Dear " + username + ",\n\n" +
                                    "Your borrowed book \"" + title + "\" (ISBN: " + isbn + ") " +
                                    "will be due in 3 days (" + dueDate + ").\n" +
                                    "Please return or renew it on time.\n\n" +
                                    "Best regards,\nMH Library";

                    try {
                        mailService.sendEmail(email, subject, body);
                        sentCount++;
                        Thread.sleep(800); // عشان ما نفقع السبام للسيرفر
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

            int finalCount = sentCount;
            Platform.runLater(() ->
                    showInfo("Thread Complete", "Reminders sent: " + finalCount)
            );
        });

        t.setDaemon(true);
        t.start();
    }

    // ========== ALERT HELPERS ==========
    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
