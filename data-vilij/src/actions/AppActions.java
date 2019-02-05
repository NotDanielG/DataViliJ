package actions;

import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import ui.AppUI;
import ui.Warning;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static com.sun.deploy.util.SessionState.save;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.SEPARATOR;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;
    TextArea area;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        try {
            applicationTemplate.getDataComponent().clear();
            applicationTemplate.getUIComponent().clear();
            AppUI appUI = ((AppUI)applicationTemplate.getUIComponent());
            TextArea area = appUI.getArea();
            CheckBox toggle = appUI.getToggle();

            appUI.setScreenShotDisable(true);
            appUI.setDisplay(false);
            appUI.getText().setText("");

            toggle.setVisible(true);
            toggle.setSelected(false);

            area.setDisable(false);
            area.setVisible(true);
            appUI.changeRadio(false);
            appUI.setConfigButtonsVisible(false);


            isUnsaved.set(false);
        } catch (Exception e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        String s = ((AppUI) applicationTemplate.getUIComponent()).getCurrentText();
        TSDProcessor test = ((AppData) applicationTemplate.getDataComponent()).getProcessor();
        try {
            test.processString(s);
            PropertyManager manager = applicationTemplate.manager;
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL dataDirURL = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                        String.format("*%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                }
            } else {
                save();
            }
            ((AppData) applicationTemplate.getDataComponent()).clear();

        }
        catch(Exception e){
            ((AppData) applicationTemplate.getDataComponent()).error();
            ((AppData) applicationTemplate.getDataComponent()).clear();
        }
    }

    @Override
    public void handleLoadRequest() {
        PropertyManager manager = applicationTemplate.manager;

        FileChooser fileChooser = new FileChooser();
        String dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(manager.getPropertyValue(AppPropertyTypes.LOAD_TITLE.name()));

        String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                String.format("*%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        if(selected != null){
            AppUI appUI = ((AppUI)applicationTemplate.getUIComponent());
            appUI.clear();
            ((AppData)applicationTemplate.getDataComponent()).loadData(selected.toPath());

            TextArea area = ((AppUI)applicationTemplate.getUIComponent()).getArea();
            CheckBox toggle = ((AppUI)applicationTemplate.getUIComponent()).getToggle();

            appUI.setScreenShotDisable(true);
            appUI.setConfigButtonsVisible(false);

            toggle.setVisible(false);
            area.setDisable(true);
            area.setVisible(true);
        }
    }

    @Override
    public void handleExitRequest() {
        try {
            if (!isUnsaved.get() || promptToSave())
                if(((AppUI) applicationTemplate.getUIComponent()).getClassifier().isRunning() && warning()){
                    System.exit(0);
                }
                else{
                    if(!((AppUI) applicationTemplate.getUIComponent()).getClassifier().isRunning()){
                        System.exit(0);
                    }
                }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        WritableImage x = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        PropertyManager manager = applicationTemplate.manager;

        FileChooser fileChooser = new FileChooser();
        String dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

        String description = manager.getPropertyValue(AppPropertyTypes.PNG_DESC.name());
        String extension = manager.getPropertyValue(AppPropertyTypes.PNG.name());

        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                String.format("*%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());

        if(selected !=null){
            ImageIO.write(SwingFXUtils.fromFXImage(x,
                    null), "png", selected);
        }
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (.*%s)", description, extension),
                                                                String.format("*.%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }
        else{
            if(dialog.getSelectedOption().equals(ConfirmationDialog.Option.NO)){
                return true;
            }
            else{
                if(dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL)){
                    return false;
                }
            }
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }
    private boolean classRunning(){
        AppUI ui = ((AppUI)applicationTemplate.getUIComponent());
        return ui.getClassifier().isRunning();
    }
    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
        ((AppUI)applicationTemplate.getUIComponent()).setSaveButton(true);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
    public void connect(){
        try{
            handleScreenshotRequest();
        }catch(Exception e){

        }
    }
    public boolean warning(){
        Warning warn = new Warning(applicationTemplate.getUIComponent().getPrimaryWindow());
        warn.init(applicationTemplate.getUIComponent().getPrimaryWindow());
        warn.showAndWait();
        if(warn.getOption() == "yes"){
            return true;
        }
        else{
            return false;
        }
    }
}
