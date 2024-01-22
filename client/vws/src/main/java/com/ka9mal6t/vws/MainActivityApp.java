package com.ka9mal6t.vws;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainActivityApp extends Application {
    private Button logOutButton, encryptButton, decryptButton, changeUsernameButton, changePasswordButton;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        primaryStage.setTitle("VWS");

        logOutButton = new Button("Log Out");
        encryptButton = new Button("Encrypt");
        decryptButton = new Button("Decrypt");
        changeUsernameButton = new Button("Change Username");
        changePasswordButton = new Button("Change Password");

        // Apply styles to buttons
        applyButtonStyles(encryptButton, "-fx-background-color: #2196f3; -fx-text-fill: #ffffff;");
        applyButtonStyles(decryptButton, "-fx-background-color: #2196f3; -fx-text-fill: #ffffff;");
        applyButtonStyles(changeUsernameButton, "-fx-background-color: #bdb002; -fx-text-fill: #ffffff;");
        applyButtonStyles(changePasswordButton, "-fx-background-color: #bdb002; -fx-text-fill: #ffffff;");
        applyButtonStyles(logOutButton, "-fx-background-color: #f44336; -fx-text-fill: #ffffff;");

        changeUsernameButton.setOnAction(event -> {
            ChangeUsernameApp app2 = new ChangeUsernameApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, Code, SessionKey, AesKey);
            primaryStage.close();
        });

        changePasswordButton.setOnAction(event -> {
            ChangePasswordApp app2 = new ChangePasswordApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, Code, SessionKey, AesKey);
            primaryStage.close();
        });

        encryptButton.setOnAction(event -> {
            EncryptApp app2 = new EncryptApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, Code, SessionKey, AesKey);
            primaryStage.close();
        });

        decryptButton.setOnAction(event -> {
            DecryptApp app2 = new DecryptApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, Code, SessionKey, AesKey);
            primaryStage.close();
        });

        logOutButton.setOnAction(event -> {
            LoginApp app2 = new LoginApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });


        VBox vBox = new VBox(10,
                encryptButton, decryptButton, changeUsernameButton, changePasswordButton, logOutButton);

        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox);

        Scene scene = new Scene(stackPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void applyButtonStyles(Button button, String style) {
        button.setStyle(style);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {

    }
}

