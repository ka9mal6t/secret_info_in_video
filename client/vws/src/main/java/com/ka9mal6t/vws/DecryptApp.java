package com.ka9mal6t.vws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DecryptApp extends Application {

    private Button chooseVideoButton, backButton, decryptButton;
    private static final String DIRECTORY_TO_CLEAR = "C:\\Users\\Public\\Documents\\VWS\\";
    private TextArea messageTextArea;
    private ProgressBar progressBar;
    private File selectedVideoFile;
    private ServerAnswer sa;
    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Decrypt Video");

        chooseVideoButton = new Button("Choose Video");
        chooseVideoButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");

        backButton = new Button("Back");

        decryptButton = new Button("Decrypt");
        decryptButton.setDisable(true);
        decryptButton.setStyle("-fx-background-color: #a9a9a9");

        messageTextArea = new TextArea();

        progressBar = new ProgressBar();
        progressBar.setVisible(false);

        chooseVideoButton.setOnAction(event -> pickVideo(primaryStage));

        backButton.setOnAction(event -> {

            MainActivityApp app2 = new MainActivityApp();
            Stage app2Stage = new Stage();
            app2.start(app2Stage, sa.getCodeAnswer(), sa.getSessionKey(), sa.getAesKey());
            primaryStage.close();
        });

        decryptButton.setOnAction(event -> {
            try {
                decrypt(primaryStage, Code, SessionKey, AesKey);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        VBox vBox = new VBox(10,
                chooseVideoButton, messageTextArea, decryptButton, backButton);

        vBox.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(vBox, progressBar);

        Scene scene = new Scene(stackPane, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void pickVideo(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4"));

        selectedVideoFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedVideoFile != null) {
            chooseVideoButton.setDisable(true);
            chooseVideoButton.setStyle("-fx-background-color: #a9a9a9");
            decryptButton.setDisable(false);
            decryptButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");
        }
    }
    private void clearDirectory(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        // Рекурсивная очистка поддиректории
                        clearDirectory(file);
                    } else {
                        // Удаление файла
                        file.delete();
                    }
                }
            }
        }
    }
    private void clearDirectory() throws IOException {
        File directoryToClear = new File(DIRECTORY_TO_CLEAR);
        if (directoryToClear.exists() && directoryToClear.isDirectory()) {
            // Выполнение очистки директории
            clearDirectory(directoryToClear);
        }
        else {
            Files.createDirectories(Paths.get(DIRECTORY_TO_CLEAR));
        }
    }
    private void decrypt(Stage primaryStage, int codeAnswer, String session, String key) throws IOException {
        progressBar.setVisible(true);
        DeThread thread = new DeThread(primaryStage);
        thread.start();
    }
    private void showToast(final String text) {
        Platform.runLater(() -> {
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
    public class DeThread extends Thread {
        private Stage primaryStage;
        public DeThread(Stage primaryStage) {
            this.primaryStage = primaryStage;
        }
        @Override
        public void run() {
            try {
                clearDirectory();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            VideoToFrames.run(selectedVideoFile.getPath(), DIRECTORY_TO_CLEAR);
            String message = "";
            try {
                message = ImageDecryption.allDecrypt(DIRECTORY_TO_CLEAR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (sa.getCodeAnswer() == 200) {
                ServerAnswer saNew = RequestToServer.decrypt(message, sa);
                sa = saNew;
                Platform.runLater(() -> {
                    if (saNew.getCodeAnswer() == 200) {
                        String result = saNew.getMessage();
                        messageTextArea.setText(result);
                        decryptButton.setDisable(true);
                        decryptButton.setStyle("-fx-background-color: #a9a9a9");
                        progressBar.setVisible(false);

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
            } else {
                Platform.runLater(() -> showToast("Bad connection"));
            }
        }
        private void showToast(final String text) {
            Platform.runLater(() -> {
                progressBar.setVisible(false);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText(null);
                alert.setContentText(text);
                alert.showAndWait();
            });
        }
    }

}

