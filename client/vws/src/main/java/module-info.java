module com.ka9mal6t.vws {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.bouncycastle.provider;
    requires java.desktop;
    requires org.bytedeco.ffmpeg;
    requires org.bytedeco.javacv;
    requires org.bytedeco.opencv;


    opens com.ka9mal6t.vws to javafx.fxml;
    exports com.ka9mal6t.vws;
}