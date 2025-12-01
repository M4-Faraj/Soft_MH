package org.Code;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class GLoginController {

    @FXML
    private Button signInButton;

    @FXML
    private Button btnSignUp;

    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;

    private Books bookss = new Books();

    // ================== SIGN UP ==================
    @FXML
    private void onSignUp(ActionEvent event) throws IOException {
        switchScene("SignUp.fxml", "MH Library");
    }

    // ================== LOGIN ==================
    @FXML
    private void onLogin(ActionEvent event) {

        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Missing data", "Please enter username and password.");
            return;
        }

        try {
            // ✅ مرة واحدة: شغّل الـ background sync + حمّل الملفات
            FileControler.startBackgroundSync();
            FileControler.fillBooksDataAsync();
            FileControler.fillUsersDataAsync();

            // 1) Admin؟
            if (LoginControl.isAdmin(username, password)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Admin.fxml"));
                Parent root = loader.load();

                GAdminControl adminController = loader.getController();
                // لو حابب تعطيه اسم الأدمن مثلاً:
                // adminController.setAdminName(username);

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Admin Control");
                stage.centerOnScreen();
                return;
            }

            // 2) Librarian؟
            if (LoginControl.isLibrarian(username, password)) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Librarian.fxml"));
                Parent root = loader.load();

                User librarian = LoginControl.getLibrarian(username, password);

                GLibrarianControl controller = loader.getController();
                controller.setLibrarian(librarian); // جوه initialize تبع الليبريريان الجداول تشتغل على FileControler

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Librarian Panel");
                stage.centerOnScreen();
                return;
            }

            // 3) User؟
            if (LoginControl.isRegisteredUser(username, password)) {

                User loggedUser = LoginControl.getUser(username, password);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/User.fxml"));
                Parent root = loader.load();

                GUserControl userController = loader.getController();
                userController.setContext(new BookControl(bookss), loggedUser);

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("User Panel");
                stage.centerOnScreen();
                return;
            }

            showError("Login failed", "Wrong username or password.");

        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not open next screen.");
        }
    }

    // ================== HELPERS ==================
    private void switchScene(String fxmlName, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/" + fxmlName));
        Parent root = loader.load();

        Stage stage = (Stage) txtUsername.getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.centerOnScreen();
    }

    private void showError(String title, String message) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(message);
        a.showAndWait();
    }
}
