package ui;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Warning extends Stage{
    private Button continueButton;
    private Button exit;
    private Label warningMessage;
    private String option;
    private Stage stage;
    public Warning(Stage stage){
        this.stage = stage;
    }
    public void init(Stage owner){
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);
        continueButton = new Button("No");
        exit = new Button("Yes");
        warningMessage = new Label("Algorithm is currently running. Proceed to exit?");
        VBox buttonBox = new VBox(5);
        buttonBox.getChildren().addAll(warningMessage,continueButton, exit);
        VBox messagePane = new VBox(buttonBox);

        continueButton.setOnAction((ActionEvent event) -> {
            option = "no";
            hide();
        });
        exit.setOnAction((ActionEvent event) -> {
            option = "yes";
            hide();
        });
        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        this.setScene(new Scene(messagePane));
    }
    public String getOption(){
        return option;
    }
}
