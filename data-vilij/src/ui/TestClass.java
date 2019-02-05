package ui;

import javafx.application.Application;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;

public class TestClass {
    Thread t = new Thread("JavaFX Init Thread") {
        public void run() {
            Application.launch(DataVisualizer.class, new String[0]);
        }
    };
}
