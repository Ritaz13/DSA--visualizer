
module com.example.dsavisualizer {
    requires javafx.controls;
    requires javafx.fxml;
    //requires com.example.dsavisualizer;

    opens com.example.dsavisualizer.controller to javafx.fxml;
    opens com.example.dsavisualizer.controller.modules to javafx.fxml;

    exports com.example.dsavisualizer;
}
