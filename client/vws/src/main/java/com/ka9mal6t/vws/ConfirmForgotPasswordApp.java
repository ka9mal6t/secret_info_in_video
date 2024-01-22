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

public class ConfirmForgotPasswordApp extends Application {
    private TextField codeField;
    private Button confirmButton;
    private Button backButton;
    private ProgressBar progressBar;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Confirm Forgot Password");

        codeField = new TextField();
        codeField.setPromptText("Code");
        Label codeLabel = new Label("Code:");

        confirmButton = new Button("Confirm");
        backButton = new Button("Back");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        applyButtonStyles(confirmButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");

        confirmButton.setOnAction(event -> {
            backButton.setDisable(true);
            confirmButton.setDisable(true);
            progressBar.setVisible(true);
            CFPThread thread = new CFPThread(primaryStage);
            thread.start();

        });

        backButton.setOnAction(event -> {
            ForgotPasswordApp app2 = new ForgotPasswordApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });

        VBox vBox = new VBox(10,
                createLabeledTextField(codeLabel, codeField),
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
    private void applyButtonStyles(Button button, String style) {
        button.setStyle(style);
    }

    public class CFPThread extends Thread {
        private Stage primaryStage;
        public CFPThread(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            Platform.runLater(() -> {
                ServerAnswer saNew = RequestToServer.confirm_forgot_password(codeField.getText(), sa);
                sa = saNew;
                if (saNew.getCodeAnswer() == 200) {
                    FinalForgotPasswordApp app2 = new FinalForgotPasswordApp();
                    Stage app2Stage = new Stage();
                    app2.start(app2Stage, saNew.getCodeAnswer(), saNew.getSessionKey(), saNew.getAesKey());
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
            });

        }

    }
    private void showToast(final String text) {
        Platform.runLater(() -> {
            backButton.setDisable(false);
            confirmButton.setDisable(false);
            progressBar.setVisible(false);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(text);
            alert.showAndWait();
        });
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
    }
}

