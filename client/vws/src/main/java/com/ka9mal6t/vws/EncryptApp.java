package com.ka9mal6t.vws;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class EncryptApp extends Application {
    private Button chooseVideoButton, backButton, downloadButton, encryptButton;
    private static final String DIRECTORY_TO_CLEAR = "C:\\Users\\Public\\Documents\\VWS\\";
    private TextField usernameField;
    private TextArea messageTextArea;
    private ProgressBar progressBar;
    private File selectedVideoFile;
    private ServerAnswer sa;

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage primaryStage, int Code, String SessionKey, String AesKey) {
        sa = new ServerAnswer(Code, SessionKey, AesKey);
        primaryStage.setTitle("Encrypt Video");

        chooseVideoButton = new Button("Choose Video");
        chooseVideoButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");
        backButton = new Button("Back");

        downloadButton = new Button("Download");
        downloadButton.setDisable(true);
        downloadButton.setStyle("-fx-background-color: #a9a9a9");

        encryptButton = new Button("Encrypt");
        encryptButton.setDisable(true);
        encryptButton.setStyle("-fx-background-color: #a9a9a9");

        usernameField = new TextField();
        usernameField.setPromptText("Username");
        Label usernameLabel = new Label("Username:");

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

        encryptButton.setOnAction(event -> {
            try {
                encrypt(primaryStage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Video (*.mp4)", "*.mp4");
        fileChooser.getExtensionFilters().add(extFilter);

        downloadButton.setOnAction(event -> {
            File selectedFile = fileChooser.showSaveDialog(primaryStage);
            if (selectedFile != null) {

                try {
                    saveVideoToFile(selectedFile);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        VBox vBox = new VBox(10,
                chooseVideoButton,
                createLabeledTextField(usernameLabel, usernameField), messageTextArea, encryptButton, downloadButton, backButton, progressBar);

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
    private void saveVideoToFile(File file) throws IOException {
        Path sourceVideoPath = Paths.get(DIRECTORY_TO_CLEAR, "output.mp4");
        Files.copy(sourceVideoPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

    }


    private void pickVideo(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Video Files", "*.mp4"));

        selectedVideoFile = fileChooser.showOpenDialog(primaryStage);

        if (selectedVideoFile != null) {
            chooseVideoButton.setDisable(true);
            chooseVideoButton.setStyle("-fx-background-color: #a9a9a9");
            encryptButton.setDisable(false);
            encryptButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");
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
            clearDirectory(directoryToClear);
        }
        else {
            Files.createDirectories(Paths.get(DIRECTORY_TO_CLEAR));
        }
    }
    private void encrypt(Stage primaryStage) throws IOException {
        progressBar.setVisible(true);
        encryptButton.setDisable(true);
        chooseVideoButton.setDisable(true);
        downloadButton.setDisable(true);
        EnThread thread = new EnThread(primaryStage);
        thread.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public class EnThread extends Thread {
        private Stage primaryStage;
        public EnThread(Stage primaryStage) {
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

            String username = usernameField.getText();

            String text = messageTextArea.getText();

            if (sa.getCodeAnswer() == 200) {
                ServerAnswer saNew = RequestToServer.encrypt(username, text, sa);
                    sa = saNew;
                    if (saNew.getCodeAnswer() == 200) {

                        ImageEncryption.allEncrypt(DIRECTORY_TO_CLEAR, sa.getMessage());
                        ImageEncryption.allEncrypt(DIRECTORY_TO_CLEAR, sa.getMessage());
                        ImagesToVideo.run(DIRECTORY_TO_CLEAR, DIRECTORY_TO_CLEAR+"output.mp4");

                        encryptButton.setDisable(true);
                        encryptButton.setStyle("-fx-background-color: #a9a9a9");
                        downloadButton.setDisable(false);
                        downloadButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: #ffffff;");
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
            }
            else {
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
