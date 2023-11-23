module com.example.mediaplayerlab2 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens com.example.mediaplayerlab2 to javafx.fxml;
    exports com.example.mediaplayerlab2;
}

