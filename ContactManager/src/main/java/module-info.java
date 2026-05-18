module com.contactmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.xml;

    opens com.contactmanager to javafx.fxml;
    opens com.contactmanager.model to javafx.base;
    opens com.contactmanager.ui to javafx.fxml;

    exports com.contactmanager;
    exports com.contactmanager.model;
    exports com.contactmanager.ui;
    exports com.contactmanager.service;
    exports com.contactmanager.util;
}
