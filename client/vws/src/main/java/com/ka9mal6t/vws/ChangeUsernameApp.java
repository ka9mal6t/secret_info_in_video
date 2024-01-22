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

public class ChangeUsernameApp extends Application {
    private Button backButton, confirmButton;
    private TextField newUsernameField;
    private PasswordField passwordField;
    private ProgressBar progressBar;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Change Username");

        backButton = new Button("Back");
        confirmButton = new Button("Confirm");

        newUsernameField = new TextField();
        newUsernameField.setPromptText("New Username");
        Label newUsernameLabel = new Label("New Username:");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        Label passwordLabel = new Label("Password:");

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
            CUThread thread = new CUThread(primaryStage);
            thread.start();

        });

        VBox vBox = new VBox(10,
                createLabeledTextField(newUsernameLabel, newUsernameField),
                createLabeledPasswordField(passwordLabel, passwordField),
                confirmButton, backButton);

        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox, progressBar);

        Scene scene = new Scene(stackPane, 400, 500);
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
    public class CUThread extends Thread {
        private Stage primaryStage;
        public CUThread(Stage primaryStage){
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            Platform.runLater(() -> {
            ServerAnswer saNew = RequestToServer.change_username(newUsernameField.getText(), passwordField.getText(), sa);
                sa = saNew;
                if (saNew.getCodeAnswer() == 200) {
                    MainActivityApp app2 = new MainActivityApp();
                    Stage app2Stage = new Stage();
                    app2.start(app2Stage, saNew.getCodeAnswer(), saNew.getSessionKey(), saNew.getAesKey());
                    primaryStage.close();

                } else if (saNew.getCodeAnswer() == 201) {
                    showToast(saNew.getError());
                } else if (saNew.getCodeAnswer() == 202) {
                    showToast(saNew.getError());
                }else {
                    showToast(saNew.getError());
                    LoginApp app2 = new LoginApp();
                    Stage app2Stage = new Stage();
                    app2.start(app2Stage);
                    primaryStage.close();
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

