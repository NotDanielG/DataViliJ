package dataprocessors;

import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        StringWriter writer = new StringWriter();
        try {
            processor.clear();
            FileReader f = new FileReader(dataFilePath.toFile());
            BufferedReader bf = new BufferedReader(f);
            String text = bf.readLine();

            while(text != null) {
                writer.append(text + "\n");
                text = bf.readLine();
            }

            processor.processString(writer.toString());
            TextArea area = ((AppUI)applicationTemplate.getUIComponent()).getArea();
            Text desc = ((AppUI)applicationTemplate.getUIComponent()).getText();

            area.setText(writer.toString());
            area.setVisible(true);
            area.setDisable(false);

            ((AppUI)applicationTemplate.getUIComponent()).changeRadio(true);
            ((AppUI)applicationTemplate.getUIComponent()).setSaveButton(true);

            PropertyManager manager = PropertyManager.getManager();
            String instances = manager.getPropertyValue(AppPropertyTypes.INSTANCES_LOADED.name());
            String labels = manager.getPropertyValue(AppPropertyTypes.LABELS_LOADED.name());
            String labelsAre = manager.getPropertyValue(AppPropertyTypes.LABELS_ARE.name());
            String pathLocation = manager.getPropertyValue(AppPropertyTypes.PATH_SOURCE.name());
            desc.setText(processor.getCurrent()-1 + instances + "\n" + processor.getLabelAmount() +
                    labels + "\n" + pathLocation + dataFilePath.toString() + "\n \n" +
                    labelsAre + "\n" + processor.getLabelNames());
            //processor.getLabelNames()
            AppUI appUI = ((AppUI)applicationTemplate.getUIComponent());

            if(appUI.getGroup().getSelectedToggle() != null){
                appUI.getGroup().getSelectedToggle().setSelected(false);
            }
        }catch(Exception e){

            error();
        }
    }

    public void loadData(String dataString) {
        try {
            processor.clear();
            processor.processString(dataString);
        } catch (Exception e) {
            ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            PropertyManager manager  = applicationTemplate.manager;
            String          errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
            String          errMsg   = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name());
            String          errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
            dialog.show(errTitle, errMsg + errInput);
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            error();
        }
    }

    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
    public void error(){
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
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

    public TSDProcessor getProcessor() {
        return processor;
    }
}
