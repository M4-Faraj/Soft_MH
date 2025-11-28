package org.Code;   // <-- عدّل اسم الباكج حسب مجلدك (شرحته تحت)

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginControlGUI extends Application {

    public static void main(String[] args) {
        // يستدعي Application.launch الحقيقية من JavaFX
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // تأكد أن الملف اسمه LoginGui.fxml في resources
        FXMLLoader loader = new FXMLLoader(
                LoginControlGUI.class.getResource("/LoginGui.fxml")
        );

        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);
        primaryStage.setTitle("MH Library Login");
        primaryStage.show();
    }
}
