package com.ka9mal6t.vws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class SignupApp extends Application {

    private Button registerButton, backButton;
    private ProgressBar progressBar;
    private TextField usernameField, emailField;
    private PasswordField passwordField, confirmPasswordField;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sign Up");

        backButton = new Button("Back");
        registerButton = new Button("Sign Up");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        Label usernameLabel = new Label("Username:");

        emailField = new TextField();
        emailField.setPromptText("Email");
        Label emailLabel = new Label("Email:");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label passwordLabel = new Label("Password:");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm Password");
        Label confirmPasswordLabel = new Label("Confirm Password:");

        applyButtonStyles(registerButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");

        backButton.setOnAction(event -> {
            LoginApp app2 = new LoginApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });

        registerButton.setOnAction(event -> {
            if (registrationCheck()) {
                progressBar.setVisible(true);
                backButton.setDisable(true);
                registerButton.setDisable(true);

                RegistrationThread registrationThread = new RegistrationThread(
                        usernameField.getText(),
                        emailField.getText(),
                        passwordField.getText(),
                        primaryStage
                );
                registrationThread.start();
            }
        });

        VBox vBox = new VBox(10,
                createLabeledTextField(usernameLabel, usernameField),
                createLabeledTextField(emailLabel, emailField),
                createLabeledPasswordField(passwordLabel, passwordField),
                createLabeledPasswordField(confirmPasswordLabel, confirmPasswordField),
                registerButton,
                backButton);

        vBox.setAlignment(Pos.CENTER);
        StackPane stackPane = new StackPane(vBox, progressBar);
        Scene scene = new Scene(stackPane, 400, 300);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean registrationCheck() {
        if (Objects.equals(passwordField.getText(), confirmPasswordField.getText())){
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                backButton.setDisable(false);
                registerButton.setDisable(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText("Passwords are different");
                alert.showAndWait();
            });
            return false;
        }

        return true;
    }
    private HBox createLabeledTextField(Label label, TextField textField) {
        HBox hbox = new HBox(5, label, textField);
        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }

    private HBox createLabeledPasswordField(Label label, PasswordField passwordField) {
        HBox hbox = new HBox(5, label, passwordField);
        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }
    private void applyButtonStyles(Button button, String style) {
        button.setStyle(style);
    }

    public class RegistrationThread extends Thread {
        private String username;
        private String email;
        private String password;
        Stage primaryStage;

        public RegistrationThread(String username, String email, String password, Stage primaryStage) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.primaryStage = primaryStage;
        }

        @Override
        public void run() {
            // Simulate network request
            ServerAnswer sa = RequestToServer.start();
            if (sa.getCodeAnswer() == 200) {
                ServerAnswer saNew = RequestToServer.register(username, email, password, sa);
                Platform.runLater(() -> {
                    if (saNew.getCodeAnswer() == 200) {
                        openOtherApp(saNew);
                    } else {
                        showToast(saNew.getError());
                    }
                });
            } else {
                Platform.runLater(() -> showToast("Bad connection"));
            }
        }

        private void openOtherApp(ServerAnswer sa) {
            progressBar.setVisible(false);
            backButton.setDisable(false);
            registerButton.setDisable(false);

            LoginApp app2 = new LoginApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        }

        private void showToast(final String text) {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                backButton.setDisable(false);
                registerButton.setDisable(false);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(text);
                alert.showAndWait();
            });
        }
    }
}