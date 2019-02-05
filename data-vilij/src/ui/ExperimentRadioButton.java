package ui;

import javafx.scene.control.RadioButton;

public class ExperimentRadioButton extends RadioButton {
    private ConfigButton button;

    public ExperimentRadioButton(String name, ConfigButton button){
        super(name);
        this.button = button;
    }
    public ConfigButton getButton(){
        return button;
    }
}
