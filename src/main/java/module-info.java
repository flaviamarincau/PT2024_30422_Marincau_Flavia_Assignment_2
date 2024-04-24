module java{
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens gui to javafx.fxml;
    exports gui;
    opens businesslogic to javafx.fxml;
    exports businesslogic;
    exports model;
}