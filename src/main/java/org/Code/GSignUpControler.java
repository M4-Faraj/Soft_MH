package org.Code;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class GSignUpControler {

    // ----------- FXML FIELDS -----------
    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private TextField txtPhone;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button btnCreateAccount;
    @FXML private Button btnBackToLogin;
    @FXML private Label lblError;

    // ----------- FILE PATH -----------
    private static final Path USERS_FILE = Paths.get("src", "main", "infoBase", "Admin.txt").toAbsolutePath();

    // ----------- EVENTS -----------
    @FXML
    private void onCreate(ActionEvent event) {

        // تشغيل signup داخل Thread
        Thread signUpThread = new Thread(() -> {
            System.out.println("🚀 Thread started for Sign Up");

            String fullName = txtFullName.getText().trim();
            String username = txtUsername.getText().trim();
            String email    = txtEmail.getText().trim();
            String phone    = txtPhone.getText().trim();
            String password = txtPassword.getText().trim();
            String confirm  = txtConfirmPassword.getText().trim();

            if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                updateUILabel("Missing data!");
                return;
            }

            if (!password.equals(confirm)) {
                updateUILabel("Passwords don't match!");
                return;
            }

            if (FileControler.searchUser(username)) {
                updateUILabel("User already exists!");
                return;
            }

            String[] names = fullName.split(" ");
            if (names.length < 2) {
                updateUILabel("Write First and Last name correctly!");
                return;
            }

            String first = names[0];
            String last  = names[1];

            // إنشاء الـ User object
            User newUser;
            newUser = new User(first, last, username, email, password, new String[0]);

            // كتابة المستخدم للملف
            FileControler.addUserAsync(newUser);

            updateUILabel("✅ Account Created Successfully!");
            showThreadAlert("Success","Account Created!");

            // رجوع ل login بعد التسجيل
            Platform.runLater(() -> {
                try {
                    switchScene("loginGui.fxml", "MH Login");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        });

        signUpThread.setDaemon(true);
        signUpThread.start();
    }

// ========== HELPERS داخل نفس الكلاس ==========

    // تحديث label من داخل Thread (بدون كراش)
    private void updateUILabel(String text) {
        Platform.runLater(() -> lblError.setText(text));
    }

    // Alert safe وغير مجمّد للـ UI
    private void showThreadAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }

    @FXML
    private void onBackToLogin(ActionEvent event) throws IOException {
        System.out.println("DEBUG: Back pressed!");
        switchScene("loginGui.fxml", "MH Library Login");
    }

    // ----------- FILE MANIPULATION -----------
    public static boolean addUser(User u, String phone) throws IOException {
        if (!Files.exists(USERS_FILE)) {
            Files.createDirectories(USERS_FILE.getParent());
        }
        String line = u.getFirstName() + "," + u.getLastName() + "," + u.getUsername() + ","
                + u.getEmail() + "," + u.getPassword() + ",," + phone;
        Files.writeString(USERS_FILE, line + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        return true;
    }

    public static boolean removeUser(String username) throws IOException {
        if (!Files.exists(USERS_FILE)) return false;
        Path tmp = Paths.get("InfoBase","tmp.txt");
        try(BufferedReader br = Files.newBufferedReader(USERS_FILE);
            BufferedWriter bw = Files.newBufferedWriter(tmp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)){
            String l;
            boolean removed=false;
            while((l=br.readLine())!=null){
                if(l.split(",").length>2 && l.split(",")[2].trim().equals(username)){
                    removed=true; removed=true; removed=false;
                } else {
                    bw.write(l); bw.newLine();
                }
            }
        }
        Files.delete(USERS_FILE);
        Files.move(tmp, USERS_FILE);
        return true;
    }

    public static boolean searchUser(String username) throws IOException {
        if (!Files.exists(USERS_FILE)) return false;
        try(BufferedReader br = Files.newBufferedReader(USERS_FILE)){
            String l;
            while((l=br.readLine())!=null){
                if(l.split(",").length>2 && l.split(",")[2].trim().equals(username)){
                    System.out.println("USER FOUND: " + l);
                    return true;
                }
            }
        }
        return false;
    }

    // ----------- HELPERS -----------
    private boolean usernameExists(String username) {
        try {
            if (!Files.exists(USERS_FILE)) return false;
            try(BufferedReader br = Files.newBufferedReader(USERS_FILE)){
                String line;
                while((line=br.readLine())!=null){
                    String[] p = line.split(",");
                    if(p.length>2 && p[2].trim().equals(username)) return true;
                }
            }
        }catch(IOException ignore){}
        return false;
    }

    private int nextUserId() {
        return Integer.parseInt(txtPhone.getText().replaceAll("[^0-9]", ""));
    }

    private void appendUserRecord(String record) throws IOException {
        if (!Files.exists(USERS_FILE.getParent())) {
            Files.createDirectories(USERS_FILE.getParent());
        }
        try(BufferedWriter bw = Files.newBufferedWriter(USERS_FILE, StandardOpenOption.CREATE, StandardOpenOption.APPEND)){
            bw.write(record); bw.newLine();
        }
    }

    private void setError(String msg) {
        lblError.setText(msg);
    }

    private void clearError() {
        lblError.setText("");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void switchScene(String fxmlName, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlName));
        Parent root = loader.load();
        Stage stage = (Stage) txtFullName.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
    }
}
