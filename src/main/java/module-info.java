module org.stepaniuk.laboratorywork {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens org.stepaniuk.laboratorywork to javafx.fxml;
    exports org.stepaniuk.laboratorywork;
    exports org.stepaniuk.laboratorywork.controllers;
    opens org.stepaniuk.laboratorywork.controllers to javafx.fxml;
}