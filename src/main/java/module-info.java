module com.example.jfxcustomcomponents {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.ikonli.javafx;
    requires java.desktop;

    opens app.customControls to javafx.fxml;
    exports app.customControls;
}