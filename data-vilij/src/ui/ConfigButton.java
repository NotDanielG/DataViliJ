package ui;

import javafx.scene.control.Button;
import javafx.stage.Stage;
import vilij.templates.ApplicationTemplate;

public class ConfigButton extends Button {
    private Configuration config;
    public ConfigButton(String name, Stage stage, boolean isCluster, ApplicationTemplate applicationTemplate){
        super(name);
        config = new Configuration(isCluster, applicationTemplate);
        config.init(stage);
    }
    public int getMax(){
        return config.getMaxIterations();
    }
    public int getIntervals(){
        return config.getUpdateInterval();
    }
    public boolean isContinue(){
        return config.tocontinue();
    }
    public boolean isTouched() {return config.isTouched();}
    public Configuration getConfig(){
        return config;
    }


}
