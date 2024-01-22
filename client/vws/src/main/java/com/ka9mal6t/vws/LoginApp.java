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

public class LoginApp extends Application {
    private TextField emailField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button signUpButton;
    private Button forgotPasswordButton;
    private ProgressBar progressBar;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Login");

        emailField = new TextField();
        emailField.setPromptText("Email");
        Label emailLabel = new Label("Email:");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label passwordLabel = new Label("Password:");

        loginButton = new Button("Log In");
        signUpButton = new Button("Sign Up");
        forgotPasswordButton = new Button("Forgot Password");

        // Apply styles to buttons
        applyButtonStyles(loginButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");
        applyButtonStyles(signUpButton, "-fx-background-color: #2196f3; -fx-text-fill: #ffffff;");
        applyButtonStyles(forgotPasswordButton, "-fx-background-color: #f44336; -fx-text-fill: #ffffff;");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        // Set up event handling
        loginButton.setOnAction(event -> {
            progressBar.setVisible(true);
            disableButtons();

            // Create and start the login thread
            LoginThread loginThread = new LoginThread(emailField.getText(), passwordField.getText(), primaryStage);
            loginThread.start();
        });

        signUpButton.setOnAction(event -> {
            SignupApp app2 = new SignupApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });

        forgotPasswordButton.setOnAction(event -> {
            ForgotPasswordApp app2 = new ForgotPasswordApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });

        VBox vBox = new VBox(10,
                createLabeledTextField(emailLabel, emailField),
                createLabeledPasswordField(passwordLabel, passwordField),
                loginButton, signUpButton, forgotPasswordButton);
        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox, progressBar);

        Scene scene = new Scene(stackPane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
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
    private void disableButtons() {
        loginButton.setDisable(true);
        signUpButton.setDisable(true);
        forgotPasswordButton.setDisable(true);
    }

    public class LoginThread extends Thread {
        private String email;
        private String password;
        private Stage primaryStage;

        public LoginThread(String email, String password, Stage primaryStage) {
            this.email = email;
            this.password = password;
            this.primaryStage = primaryStage;
        }

        @Override
        public void run() {
            // Simulate network request
            ServerAnswer sa = RequestToServer.start();
            if (sa.getCodeAnswer() == 200) {
                ServerAnswer saNew = RequestToServer.login(email, password, sa);
                Platform.runLater(() -> {
                    if (saNew.getCodeAnswer() == 200) {
                        openOtherApp(saNew);
                    } else if (saNew.getCodeAnswer() == 201) {
                        showToast(saNew.getError());
                    } else {
                        showToast(saNew.getError());
                    }
                });
            } else {
                Platform.runLater(() -> showToast("Bad connection"));
            }
        }

        private void openOtherApp(ServerAnswer sa) {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                loginButton.setDisable(false);
                signUpButton.setDisable(false);
                forgotPasswordButton.setDisable(false);

                MainActivityApp app2 = new MainActivityApp();
                Stage app2Stage = new Stage();
                app2.start(app2Stage, sa.getCodeAnswer(), sa.getSessionKey(), sa.getAesKey());
                primaryStage.close();


            });
        }

        private void showToast(final String text) {
            Platform.runLater(() -> {
                progressBar.setVisible(false);
                loginButton.setDisable(false);
                signUpButton.setDisable(false);
                forgotPasswordButton.setDisable(false);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(text);
                alert.showAndWait();
            });
        }
    }


}