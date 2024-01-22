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

public class ForgotPasswordApp extends Application {
    private TextField emailField;
    private Button confirmButton;
    private Button backButton;
    private ProgressBar progressBar;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Forgot Password");

        emailField = new TextField();
        emailField.setPromptText("Email");
        Label emailLabel = new Label("Email:");

        confirmButton = new Button("Confirm");
        backButton = new Button("Back");

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        applyButtonStyles(confirmButton, "-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");

        confirmButton.setOnAction(event -> {
            confirmButton.setDisable(true);
            backButton.setDisable(true);
            progressBar.setVisible(true);
            FPThread thread = new FPThread(primaryStage);
            thread.start();

        });

        backButton.setOnAction(event -> {
            LoginApp app2 = new LoginApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage);
            primaryStage.close();
        });

        VBox vBox = new VBox(10,
                createLabeledTextField(emailLabel, emailField),
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

    public class FPThread extends Thread {
        private Stage primaryStage;
        public FPThread(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            sa = RequestToServer.start();
            if (sa.getCodeAnswer() == 200) {
                ServerAnswer saNew = RequestToServer.forgot_password(emailField.getText(), sa);
                Platform.runLater(() -> {
                    sa = saNew;
                    if (saNew.getCodeAnswer() == 200) {
                        ConfirmForgotPasswordApp app2 = new ConfirmForgotPasswordApp();
                        Stage app2Stage = new Stage();
                        app2.start(app2Stage, saNew.getCodeAnswer(), saNew.getSessionKey(), saNew.getAesKey());
                        primaryStage.close();

                    } else if (saNew.getCodeAnswer() == 201) {
                        showToast(saNew.getError());
                    } else {
                        showToast(saNew.getError());
                    }
                });
            }
                else {
                Platform.runLater(() -> showToast("Bad connection"));
                }
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
    }


