module org.example._version {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.mail;
    requires java.sql;


    opens org.example._version to javafx.fxml;
    exports org.example._version;
}