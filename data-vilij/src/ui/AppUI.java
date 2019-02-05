package ui;

import actions.AppActions;
import algorithms.Algorithm;
import algorithms.KMeansClusterer;
import algorithms.RandomClassifier;
import algorithms.RandomClusterer;
import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import java.lang.reflect.Constructor;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;
    Stage primaryStage;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private Text                         description;

    private CheckBox                     toggle;

    private ToggleGroup                  group;
    private ToggleGroup                  subGroup;

    private RadioButton                  classification;
    private RadioButton                  clustering;


    private ExperimentRadioButton[]                classAlgo;
    private Button[]                     classList;

    private ExperimentRadioButton[]                clusteringAlgo;
    private Button[]                     clusterList;

    private ExperimentRadioButton        randomCluster;
    private String                       type;

    private Thread thread;
    private Algorithm algorithm;
    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate){
        super(primaryStage, applicationTemplate);
        this.primaryStage = primaryStage;
        this.applicationTemplate = applicationTemplate;
        String cssPath = SEPARATOR + "hw4.css";
        getPrimaryScene().getStylesheets().add(getClass().getResource(cssPath).toExternalForm());

        boolean test = true;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                                                   manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                                   manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        scrnshotButton.setDisable(true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());

        newButton.setDisable(false);
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public String getCurrentText() { return textArea.getText(); }

    private void layout(){
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        xAxis.setForceZeroInRange(false);
        NumberAxis      yAxis   = new NumberAxis();
        yAxis.setForceZeroInRange(false);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        chart.setCreateSymbols(true);
        chart.setAnimated(false);

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.3, windowHeight * 0.35);
        leftPanel.setMinSize(windowWidth * 0.3, windowHeight * 0.35);

        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();
        textArea.setVisible(false);
        textArea.setMinHeight(leftPanel.getMinHeight() * .5);

        toggle = new CheckBox(manager.getPropertyValue(AppPropertyTypes.DONE.name()));
        toggle.setVisible(false);

        description = new Text();
        description.wrappingWidthProperty().bind(leftPanel.widthProperty());

        group = new ToggleGroup();
        subGroup = new ToggleGroup();

        classification = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLASS.name()));
        clustering = new RadioButton(manager.getPropertyValue(AppPropertyTypes.CLUSTER.name()));

        ConfigButton configClass1 = new ConfigButton(manager.getPropertyValue(AppPropertyTypes.CONFIGCLASS1.name()),
                primaryStage,false, applicationTemplate);
        ConfigButton configCluster1 = new ConfigButton(manager.getPropertyValue(AppPropertyTypes.CONFIGCLUSTER1.name()),
                primaryStage,true, applicationTemplate);
        ConfigButton configCluster2 = new ConfigButton("RandomCluster", primaryStage, true, applicationTemplate);

        ExperimentRadioButton class1 = new ExperimentRadioButton(manager.getPropertyValue(AppPropertyTypes.CLASS1.name())
        ,configClass1);
        ExperimentRadioButton cluster1 = new ExperimentRadioButton(manager.getPropertyValue(AppPropertyTypes.CLUSTER1.name())
        ,configCluster1);
        randomCluster = new ExperimentRadioButton("RandomClusterer",configCluster2);

        classAlgo = new ExperimentRadioButton[1];
        clusteringAlgo = new ExperimentRadioButton[2];

        classList = new Button[1];
        clusterList = new Button[2];

        classAlgo[0] = class1;
        clusteringAlgo[0] = cluster1;
        clusteringAlgo[1] = randomCluster;

        classification.setVisible(false);
        clustering.setVisible(false);

        class1.setVisible(false);
        cluster1.setVisible(false);
        randomCluster.setVisible(false);

        classification.setToggleGroup(group);
        clustering.setToggleGroup(group);

        class1.setToggleGroup(subGroup);
        cluster1.setToggleGroup(subGroup);
        randomCluster.setToggleGroup(subGroup);
        type = "";

        //Experiment
        HBox classTypes = new HBox();
        classTypes.getChildren().addAll(class1, configClass1);
        HBox clusterTypes = new HBox();
        clusterTypes.setPadding(new Insets(10));
        clusterTypes.getChildren().addAll(cluster1, configCluster1);
        HBox clusterRandom = new HBox();
        clusterRandom.setPadding(new Insets(10));
        clusterRandom.getChildren().addAll(randomCluster, configCluster2);

        VBox types = new VBox();
        types.getChildren().addAll(classTypes, clusterTypes, clusterRandom);
        VBox general = new VBox();
        general.getChildren().addAll(classification, clustering, types);
        //End
        configClass1.setOnAction(event -> {
            configClass1.getConfig().setInitial();
            configClass1.getConfig().showAndWait();
            if(subGroup.getSelectedToggle() != null) {
                displayButton.setVisible(true);
            }
        });
        configCluster1.setOnAction(event -> {
            configCluster1.getConfig().setInitial();
            configCluster1.getConfig().showAndWait();
            if(subGroup.getSelectedToggle() != null) {
                displayButton.setVisible(true);
            }
        });
        configCluster2.setOnAction(event -> {
            configCluster2.getConfig().setInitial();
            configCluster2.getConfig().showAndWait();
            if(subGroup.getSelectedToggle() != null) {
                displayButton.setVisible(true);
            }
        });

        configClass1.setVisible(false);
        configCluster1.setVisible(false);
        configCluster2.setVisible(false);

        classList[0] = configClass1;
        clusterList[0] = configCluster1;
        clusterList[1] = configCluster2;

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);
        displayButton.setVisible(false);

        leftPanel.getChildren().addAll(leftPanelTitle, textArea, description, toggle, general, processButtonsBox);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);

//        NumberAxis x1Axis = new NumberAxis(95,100,2);
//        NumberAxis y1Axis = new NumberAxis(40,100,2);
//        chart = new LineChart<>(x1Axis,y1Axis);

    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setScreenShotActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setDisplayButtonActions(){
        displayButton.setOnAction(event -> {
            try {
                ConfigButton button = ((ExperimentRadioButton) subGroup.getSelectedToggle()).getButton();
                scrnshotButton.setDisable(true);
                if(algorithm == null || !algorithm.isRunning()) {
                    displayButton.setText("Display");
                    displayButton.setDisable(true);
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    dataComponent.loadData(textArea.getText());
                    dataComponent.displayData();

                    DataSet x = new DataSet();
                    DataSet test = x.fromTextArea(textArea.getText());
                    if(type == "algorithms.RandomClassifier"){
                        Class<?> klass = Class.forName(type);
                        Constructor konstructor = klass.getConstructors()[0];
                        algorithm = (RandomClassifier) konstructor.newInstance(test,button.getMax(), button.getIntervals(),
                            button.isContinue(), chart, displayButton, scrnshotButton);
                    }else{
                        if(type == "algorithms.RandomClusterer"){
                            Class<?> klass = Class.forName(type);
                            Constructor konstructor = klass.getConstructors()[0];
                            algorithm = (RandomClusterer) konstructor.newInstance(test,button.getMax(), button.getIntervals(),
                                    button.getConfig().getCluster(), button.isContinue(), chart, displayButton, scrnshotButton,
                                    ((AppData) applicationTemplate.getDataComponent()).getProcessor());
                        }
                        else{
                            Class<?> klass = Class.forName(type);
                            Constructor konstructor = klass.getConstructors()[0];
                            algorithm = (KMeansClusterer) konstructor.newInstance(test,button.getMax(), button.getIntervals(),
                                    button.getConfig().getCluster(), button.isContinue(), chart, displayButton, scrnshotButton,
                                    ((AppData) applicationTemplate.getDataComponent()).getProcessor());
                        }
                    }
                    if(!button.isContinue()){
                        displayButton.setDisable(false);
                        displayButton.setText("Click to Continue");
                    }

                }
                else{
                    algorithm.setNotify();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        toggle.setOnAction(event -> {
            try{
                PropertyManager manager = applicationTemplate.manager;
                //If CheckBox is Done
                if(toggle.isSelected()) {
                    if(group.getSelectedToggle() != null){
                        group.getSelectedToggle().setSelected(false);
                    }

                    TSDProcessor processor = ((AppData) applicationTemplate.getDataComponent()).getProcessor();
                    toggle.setText(manager.getPropertyValue(AppPropertyTypes.EDIT.name()));
                    ((AppData)applicationTemplate.getDataComponent()).clear();
                    processor.processString(textArea.getText());

                    String instances = manager.getPropertyValue(AppPropertyTypes.INSTANCES_LOADED.name());
                    String labels = manager.getPropertyValue(AppPropertyTypes.LABELS_LOADED.name());
                    String labelsAre = manager.getPropertyValue(AppPropertyTypes.LABELS_ARE.name());

                    description.setText(processor.getCurrent()-1 + instances + "\n" + processor.getLabelAmount() +
                            labels + "\n" +  labelsAre + processor.getLabelNames());
                    textArea.setDisable(true);

                    classification.setVisible(true);
                    clustering.setVisible(true);
                    if(processor.getLabelAmount() >= 2){
                        int nonNull = 0;
                        for(int i = 0; i < processor.getLabelList().length;i++){
                            if(processor.getLabelList()[i] != null){
                                nonNull++;
                            }
                        }
                        if(nonNull >= 2){
                            classification.setDisable(false);
                        }
                    }
                    else{
                        classification.setDisable(true);
                    }
                }
                else{
                    toggle.setText(manager.getPropertyValue(AppPropertyTypes.DONE.name()));
                    textArea.setDisable(false);

                    classification.setVisible(false);
                    clustering.setVisible(false);

                    group.selectToggle(null);
                    subGroup.selectToggle(null);

                    for(int i = 0; i < classAlgo.length; i++){
                        classAlgo[i].setVisible(false);
                        classList[i].setVisible(false);
                    }
                    for(int i = 0; i < classAlgo.length; i++){
                        clusteringAlgo[i].setVisible(false);
                        clusterList[i].setVisible(false);
                    }
                }
            }catch (Exception e){
                toggle.setSelected(false);
                toggle.setText("Done");
                errorHelper();
            }
        });
        group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(group.getSelectedToggle() != null){
                    displayButton.setVisible(false);
                    algorithm = null;
                    Toggle tog = group.getSelectedToggle();
                    if(tog == classification){
                        for(int i = 0; i < classAlgo.length; i++){
                            classAlgo[i].setVisible(true);
                            classList[i].setVisible(true);
                        }
                        for(int i = 0; i < clusteringAlgo.length; i++){
                            clusteringAlgo[i].setVisible(false);
                            clusterList[i].setVisible(false);
                        }
                    }
                    else {
                        if (tog == clustering) {
                            for(int i = 0; i < classAlgo.length; i++){
                                classAlgo[i].setVisible(false);
                                classList[i].setVisible(false);
                            }
                            for(int i = 0; i < clusteringAlgo.length; i++){
                                clusteringAlgo[i].setVisible(true);
                                clusterList[i].setVisible(true);
                            }
                        }
                    }
                }
            }
        });
        subGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if(subGroup.getSelectedToggle() != null){
                    displayButton.setVisible(false);
                    algorithm = null;
                    if(group.getSelectedToggle() == classification){
                        type = "algorithms.RandomClassifier";
                        if(((ConfigButton)classList[0]).isTouched()){
                            displayButton.setVisible(true);
                        }
                    }
                    else{
                        if(subGroup.getSelectedToggle() == randomCluster){
                            type = "algorithms.RandomClusterer";
                            if(((ConfigButton)clusterList[1]).isTouched()){
                                displayButton.setVisible(true);
                            }
                        }
                        else{
                            type = "algorithms.KMeansClusterer";
                            if(((ConfigButton)clusterList[0]).isTouched()){
                                displayButton.setVisible(true);
                            }
                        }
                    }
                }
            }
        });
    }
    private void setScreenShotActions(){
        try {
            scrnshotButton.setOnAction(e -> ((AppActions) applicationTemplate.getActionComponent()).connect());
        } catch(Exception e){

        }
    }
    public TextArea getArea(){
        return textArea;
    }
    public Text getText(){
        return description;
    }
    public CheckBox getToggle(){
        return toggle;
    }
    public void errorHelper(){
        ErrorDialog dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        TSDProcessor processor = ((AppData)applicationTemplate.getDataComponent()).getProcessor();

        String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
        String          errMsg;
        if(processor.getDuplicate().length() != 0) {
            errMsg = manager.getPropertyValue(AppPropertyTypes.DUPLICATE_ERROR.name()) +
                    processor.getDuplicate() +
                    manager.getPropertyValue(AppPropertyTypes.LINE_NUMBER.name()) +
                    processor.getCurrent();
        }
        else{
            errMsg = manager.getPropertyValue(AppPropertyTypes.DISPLAY_ERROR.name()) +
                    processor.getCurrent();
        }
        String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
        dialog.show(errTitle, errMsg + errInput);
    }
    public void changeRadio(boolean b){
        classification.setVisible(b);
        clustering.setVisible(b);
    }
    public void setConfigButtonsVisible(boolean b){
        for(int i = 0; i< classList.length;i++){
            classList[i].setVisible(b);
            classAlgo[i].setVisible(b);
        }
        for(int i = 0; i< clusterList.length;i++){
            clusterList[i].setVisible(b);
            clusteringAlgo[i].setVisible(b);
        }
    }
    public void setSaveButton(boolean b){
        saveButton.setDisable(b);
    }
    public void setScreenShotDisable(boolean b){
        scrnshotButton.setDisable(b);
    }
    public ToggleGroup getGroup(){
        return group;
    }
    public CheckBox getCheckBox(){
        return toggle;
    }
    public Algorithm getClassifier(){
        return algorithm;
    }
    public Stage getPrimaryStage(){
        return primaryStage;
    }
    public void setDisplay(boolean b){
        displayButton.setVisible(b);
    }
}
