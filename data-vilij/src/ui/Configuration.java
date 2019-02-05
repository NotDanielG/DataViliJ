package ui;

import algorithms.Algorithm;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;

public class Configuration extends Stage {
    private ApplicationTemplate applicationTemplate;
    private int max;
    private int update;
    private boolean continuous;
    private boolean touched;
    private ConfigValues values;

    private Label textMax;
    private Label intervalMax;
    private Label cont;

    private TextField maxField;
    private TextField intervalField;
    private CheckBox continuousField;


    private int cluster;
    private boolean isCluster;
    private Label clusterText;
    private TextField clusterAmount;


    public Configuration(boolean isCluster, ApplicationTemplate applicationTemplate){
        max = 1;
        update = 1;
        cluster = 1;
        continuous = false;
        this.applicationTemplate = applicationTemplate;
        this.isCluster = isCluster;
        if(isCluster){
            PropertyManager manager = applicationTemplate.manager;

            clusterText = new Label(manager.getPropertyValue(AppPropertyTypes.CLUSTER_AMOUNT.name()));
            clusterAmount = new TextField();
        }
    }
    public Configuration(){
        max = 1;
        update = 1;
        cluster =1;
        continuous = false;
    }

    public void init(Stage owner) {
        initModality(Modality.WINDOW_MODAL); // modal => messages are blocked from reaching other windows
        initOwner(owner);

        PropertyManager manager = applicationTemplate.manager;
        textMax = new Label(manager.getPropertyValue(AppPropertyTypes.MAXITERATIONS.name()));
        intervalMax = new Label(manager.getPropertyValue(AppPropertyTypes.INTERVALS.name()));
        cont = new Label(manager.getPropertyValue(AppPropertyTypes.CONTINUOUS.name()));

        maxField = new TextField();
        intervalField = new TextField();
        continuousField = new CheckBox();

        Button done = new Button(manager.getPropertyValue(AppPropertyTypes.CLICK_WHEN_DONE.name()));

        VBox buttonBox = new VBox(5);
        if(isCluster){
            buttonBox.getChildren().addAll(clusterText, clusterAmount);
        }
        buttonBox.getChildren().addAll(textMax, maxField, intervalMax, intervalField, cont, continuousField, done);

        VBox messagePane = new VBox(buttonBox);

        messagePane.setAlignment(Pos.CENTER);
        messagePane.setPadding(new Insets(10, 20, 20, 20));
        messagePane.setSpacing(10);

        done.setOnAction(event -> {
            try {
                if(!isCluster) {
                    values = new ConfigValues(maxField.getText(), intervalField.getText(),
                            continuousField.isSelected(), "0", isCluster);
                    max = values.getMax();
                    update = values.getUpdate();
                    continuous = values.isContinuous();
                }
                else{
                    values = new ConfigValues(maxField.getText(), intervalField.getText(),
                            continuousField.isSelected(), clusterAmount.getText(), isCluster);
                    max = values.getMax();
                    update = values.getUpdate();
                    continuous = values.isContinuous();
                    cluster = values.getClusterAmount();
                }
//                setMax(maxField.getText());
//                setUpdate(intervalField.getText());
//                continuous = continuousField.isSelected();
//                if (isCluster) {
//                    setClusterAmount(clusterAmount.getText());
//                }
                this.hide();
            }catch(Exception e){
                max = 1;
                update = 1;
                if(isCluster){
                    cluster = 1;
                }
                this.hide();
            }
        });

        this.setScene(new Scene(messagePane));
    }
    public void setInitial(){
        if(touched) {
            maxField.setText(max + "");
            intervalField.setText(update + "");
            continuousField.setSelected(continuous);
            if (isCluster) {
                clusterAmount.setText(cluster + "");
            }
        }
        else{
            touched = true;
        }
    }
    public boolean isTouched(){
        return touched;
    }





    public void setMax(String max){
        this.max = Integer.parseInt(max);
    }
    public void setUpdate(String update){
        this.update = Integer.parseInt(update);
    }
    public void setClusterAmount(String cluster){
        this.cluster = Integer.parseInt(cluster);
    }
    public void setContinue(boolean b){
        continuous = b;
    }
    public int getMaxIterations(){
       return max;
    }
    public int getUpdateInterval() {
        return update;
    }
    public int getCluster() {return cluster;}
    public boolean tocontinue(){
        return continuous;
    }
    public void run(){

    }
}
