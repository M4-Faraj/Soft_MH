package org.Code;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    // ========= FIELDS FOR LIBRARIAN ===========
    private User currentLibrarian;

    @FXML
    private Label lblLibrarianName;   // لازم يكون موجود في librarian.fxml بنفس fx:id

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

    @FXML private TextField txtFilterUser;
    @FXML private TextField txtFilterIsbn;

    @FXML private Button btnRefreshOverdue;
    @FXML private Button btnFilterOverdue;
    @FXML private Button btnClearFilter;

    private ObservableList<OverdueEntry> overdueList = FXCollections.observableArrayList();

    // ========= Struct-like class ===========
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

            fine = overdueDays > 0 ? 10 : 0;
        }
    }

    // ========= Load FXML ===========
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

    // ========= Called from GLoginController after login ===========
    public void setLibrarian(User librarian) {
        this.currentLibrarian = librarian;
        if (lblLibrarianName != null && librarian != null) {
            lblLibrarianName.setText("Welcome, " + librarian.getFirstName());
        }
    }

    // ========= Initialize ===========
    @FXML
    private void initialize() {
        setupTable();
        loadOverdueData();

        if (tblOverdue != null) {
            tblOverdue.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
                if (newSel != null) showDetails(newSel);
            });
        }

        if (btnRefreshOverdue != null)
            btnRefreshOverdue.setOnAction(e -> loadOverdueData());

        if (btnFilterOverdue != null)
            btnFilterOverdue.setOnAction(e -> filter());

        if (btnClearFilter != null)
            btnClearFilter.setOnAction(e -> {
                if (txtFilterUser != null) txtFilterUser.clear();
                if (txtFilterIsbn != null) txtFilterIsbn.clear();
                loadOverdueData();
            });
    }

    // ========= Create table bindings ===========
    private void setupTable() {
        if (colIsbn != null)
            colIsbn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isbn));
        if (colTitle != null)
            colTitle.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().title));
        if (colUser != null)
            colUser.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().username));
        if (colBorrowDate != null)
            colBorrowDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().borrowDate.toString()));
        if (colDueDate != null)
            colDueDate.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().dueDate.toString()));
        if (colDaysOverdue != null)
            colDaysOverdue.setCellValueFactory(d ->
                    new SimpleStringProperty(String.valueOf(d.getValue().overdueDays)));
        if (colFine != null)
            colFine.setCellValueFactory(d ->
                    new SimpleStringProperty(String.valueOf(d.getValue().fine)));

        if (tblOverdue != null)
            tblOverdue.setItems(overdueList);
    }

    // ========= Load overdue data ===========
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

                OverdueEntry e = new OverdueEntry(isbn, title, user, borrow);

                if (e.overdueDays > 0) {
                    overdueList.add(e);
                }
            }

            if (lblOverdueCount != null)
                lblOverdueCount.setText(String.valueOf(overdueList.size()));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ========= show details panel ===========
    private void showDetails(OverdueEntry e) {
        if (lblSelIsbn != null)        lblSelIsbn.setText(e.isbn);
        if (lblSelTitle != null)       lblSelTitle.setText(e.title);
        if (lblSelUser != null)        lblSelUser.setText(e.username);
        if (lblSelBorrowDate != null)  lblSelBorrowDate.setText(e.borrowDate.toString());
        if (lblSelDueDate != null)     lblSelDueDate.setText(e.dueDate.toString());
        if (lblSelDaysOverdue != null) lblSelDaysOverdue.setText(String.valueOf(e.overdueDays));
        if (lblSelFine != null)        lblSelFine.setText(String.valueOf(e.fine));
    }

    // ========= Filter logic ===========
    private void filter() {
        if (tblOverdue == null) return;

        String u = (txtFilterUser != null ? txtFilterUser.getText().trim().toLowerCase() : "");
        String i = (txtFilterIsbn != null ? txtFilterIsbn.getText().trim().toLowerCase() : "");

        ObservableList<OverdueEntry> filtered = overdueList.filtered(e ->
                (u.isEmpty() || e.username.toLowerCase().contains(u)) &&
                        (i.isEmpty() || e.isbn.toLowerCase().contains(i))
        );

        tblOverdue.setItems(filtered);

        if (lblOverdueCount != null)
            lblOverdueCount.setText(String.valueOf(filtered.size()));
    }
}
