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

public class ChangePasswordApp extends Application {
    private Button backButton, confirmButton;

    private PasswordField passwordField, newPasswordField, confirmNewPasswordField;
    private ProgressBar progressBar;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Change Password");

        backButton = new Button("Back");
        confirmButton = new Button("Confirm");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label passwordLabel = new Label("Password:");

        newPasswordField = new PasswordField();
        passwordField.setPromptText("New Password");
        Label newPasswordLabel = new Label("New Password:");

        confirmNewPasswordField = new PasswordField();
        passwordField.setPromptText("Confirm New Password");
        Label confirmNewPasswordLabel = new Label("Confirm New Password:");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        applyButtonStyles(confirmButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");


        backButton.setOnAction(event -> {
            MainActivityApp app2 = new MainActivityApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, sa.getCodeAnswer(), sa.getSessionKey(), sa.getAesKey());
            primaryStage.close();
        });
        confirmButton.setOnAction(event -> {
            confirmButton.setDisable(true);
            backButton.setDisable(true);
            progressBar.setVisible(true);
            CPThread thread = new CPThread(primaryStage);
            thread.start();

        });

        VBox vBox = new VBox(10,
                createLabeledPasswordField(passwordLabel, passwordField),
                createLabeledPasswordField(newPasswordLabel, newPasswordField),
                createLabeledPasswordField(confirmNewPasswordLabel, confirmNewPasswordField),
                confirmButton, backButton);

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
    public class CPThread extends Thread {
        private Stage primaryStage;
        public CPThread(Stage primaryStage){
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            Platform.runLater(() -> {
                if (PasswordCheck(newPasswordField.getText(), confirmNewPasswordField.getText())) {
                    ServerAnswer saNew = RequestToServer.change_password(newPasswordField.getText(), passwordField.getText(), sa);
                    sa = saNew;
                    if (saNew.getCodeAnswer() == 200) {
                        MainActivityApp app2 = new MainActivityApp();
                        Stage app2Stage = new Stage();
                        app2.start(app2Stage, saNew.getCodeAnswer(), saNew.getSessionKey(), saNew.getAesKey());
                        primaryStage.close();

                    } else if (saNew.getCodeAnswer() == 201) {
                        showToast(saNew.getError());
                    }else if (saNew.getCodeAnswer() == 202) {
                        showToast(saNew.getError());
                    }else if (saNew.getCodeAnswer() == 401) {
                        showToast(saNew.getError());
                    }else {
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
            backButton.setDisable(false);
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
