package org.Code;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
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

    @FXML
    private void onSignUp(ActionEvent event) throws IOException {
        System.out.println("SignUp clicked!");
        switchScene("SignUp.fxml","MH Library");
    }
    @FXML
    private void onLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Missing data", "Please enter username and password.");
            return;
        }

        try {
            if (LoginControl.isAdmin(username, password)) {
                // افتح واجهة الـ Admin
                switchScene("Admin.fxml", "Admin Panel");
            } else if (LoginControl.isRegisteredUser(username, password)) {
                // افتح واجهة الـ User
                switchScene("User.fxml", "User Panel");
            } else {
                showError("Login failed", "Wrong username or password.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Could not open next screen.");
        }
    }

    // تغيير الـ Scene لنفس الـ Stage
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