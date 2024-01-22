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

public class FinalForgotPasswordApp extends Application {
    private PasswordField passwordField, confirmPasswordField;
    private Button confirmButton;
    private ProgressBar progressBar;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Success!");

        passwordField = new PasswordField();
        passwordField.setPromptText("New Password");
        Label passwordLabel = new Label("New Password:");

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        Label confirmPasswordLabel = new Label("Confirm New Password:");

        confirmButton = new Button("Confirm");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        applyButtonStyles(confirmButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");

        confirmButton.setOnAction(event -> {
            confirmButton.setDisable(true);
            progressBar.setVisible(true);
            FFPThread thread = new FFPThread(primaryStage);
            thread.start();

        });

        VBox vBox = new VBox(10,
                createLabeledPasswordField(passwordLabel, passwordField),
                createLabeledPasswordField(confirmPasswordLabel, confirmPasswordField),
                confirmButton);
        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox, progressBar);

        Scene scene = new Scene(stackPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private HBox createLabeledPasswordField(Label label, PasswordField passwordField) {
        HBox hbox = new HBox(5, label, passwordField);
        hbox.setAlignment(Pos.CENTER);
        return hbox;
    }
    private void applyButtonStyles(Button button, String style) {
        button.setStyle(style);
    }

    public boolean PasswordCheck(String password, String confirmPassword){
        if (password.trim().isEmpty()){
            showToast("Enter the Password");
            return false;
        }
        if (password.length() < 8){
            showToast("Password must be more than 8 characters long");
            return false;
        }
        if (!password.matches("(.*)[a-z](.*)")){
            showToast("Password must contain an lowercase character");
            return false;
        }
        if (!password.matches("(.*)[A-Z](.*)")){
            showToast("Password must contain an uppercase character");
            return false;
        }
        if (!password.matches("(.*)[@$!%*?&_](.*)")){
            showToast("Password must contain one of symbols @$!%*?&_");
            return false;
        }
        if (!password.equals(confirmPassword)){
            showToast("Password mismatch");
            return false;
        }
        return true;
    }
    public class FFPThread extends Thread {
        private Stage primaryStage;
        public FFPThread(Stage primaryStage){
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            Platform.runLater(() -> {
                if (PasswordCheck(passwordField.getText(), confirmPasswordField.getText())) {
                    ServerAnswer saNew = RequestToServer.final_forgot_password(passwordField.getText(), sa);
                    sa = saNew;
                    if (saNew.getCodeAnswer() == 200) {
                        LoginApp app2 = new LoginApp();
                        Stage app2Stage = new Stage();
                        app2.start(app2Stage);
                        primaryStage.close();

                    } else if (saNew.getCodeAnswer() == 201) {
                        showToast(saNew.getError());
                    } else {
                        showToast(saNew.getError());
                        LoginApp app2 = new LoginApp();
                        Stage app2Stage = new Stage();
                        app2.start(app2Stage);
                        primaryStage.close();
                    }
                }
            });
        }

    }
    private void showToast(final String text) {
        Platform.runLater(() -> {
            progressBar.setVisible(false);
            confirmButton.setDisable(false);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
        });
    }

    @Override
    public void start(Stage primaryStage) {
    }
}

