/*module com.example.dsavisualizer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.example.dsavisualizer to javafx.fxml;
    exports com.example.dsavisualizer;
}*/
module com.example.dsavisualizer {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.example.dsavisualizer.controller to javafx.fxml;
    opens com.example.dsavisualizer.controller.modules to javafx.fxml;

    exports com.example.dsavisualizer;
}
