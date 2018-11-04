package actions;

import dataprocessors.AppData;
import dataprocessors.TSDProcessor;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import settings.AppPropertyTypes;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static java.io.File.separator;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javax.imageio.ImageIO;
import ui.AppUI;
import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;

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

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;
    SimpleBooleanProperty AlgoRun;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
        this.AlgoRun = new SimpleBooleanProperty(false);
    }

    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }
    public void setAlgoRunProperty(boolean property) {AlgoRun.set(property); }
    public boolean noDataPath() { return dataFilePath == null;}
    public Path getDataFilePath() { return dataFilePath;}
    
    @Override
    public void handleNewRequest() {
        applicationTemplate.getDataComponent().clear();
        applicationTemplate.getUIComponent().clear();
        isUnsaved.set(false);
        dataFilePath = null;
        ((AppUI)applicationTemplate.getUIComponent()).getTextBox().setVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getDisplayToggle().setVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getDisplayToggle().setSelected(false);
        ((AppUI)applicationTemplate.getUIComponent()).getTextBox().setEditable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getAlgos().setVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getAlgos().getPanes().get(0).setDisable(true);
    }

    @Override
    public void handleSaveRequest() {
        try {
            TSDProcessor check = new TSDProcessor();
            check.processString(((AppUI)applicationTemplate.getUIComponent()).getCurrentText());
        }
        catch (Exception e) { 
        ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        err.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),e.getMessage());
        return;
        }
        try{
            if (dataFilePath == null) {
                promptToSave();
            }
            else
                save();
            
            setIsUnsavedProperty(false);
            ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
        }
        catch (IOException e) {errorHandlingHelper();}
        
    }
        // TODO: NOT A PART OF  HW 1
    

    @Override
    public void handleLoadRequest() {
        applicationTemplate.getUIComponent().clear();
        // TODO: NOT A PART OF HW 1
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = "/" + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));

        String description = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
                String.format("*.%s", extension));

        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showOpenDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        Path holder;
        if (selected != null) {
            holder = selected.toPath();
        }
        else {
            return;
        }
        applicationTemplate.getDataComponent().loadData(holder);
        dataFilePath = holder;
        ((AppUI)applicationTemplate.getUIComponent()).getTextBox().setVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getDisplayToggle().setVisible(false);
    }

    @Override
    public void handleExitRequest() {
        if (AlgoRun.get()) {
            ConfirmationDialog dialog = ConfirmationDialog.getDialog();
            dialog.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.EXIT_WHILE_RUNNING_WARNING.name()));
            if (dialog.getSelectedOption() == null || 
                dialog.getSelectedOption().equals(ConfirmationDialog.Option.NO) ||
                dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL))
                return;
            if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES))
                System.exit(0);
        }
        else {
            try {
                if (!isUnsaved.get() || promptToSave()) {
                    System.exit(0);
                }
            } catch (IOException e) {
                errorHandlingHelper();
            }
        }
    }

    @Override
    public void handlePrintRequest() {
        // TODO: NOT A PART OF HW 1
    }

    public void handleScreenshotRequest() throws IOException {
        FileChooser fileChooser = new FileChooser();
        String dataDirPath = "/" + applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
        URL dataDirURL = getClass().getResource(dataDirPath);

        fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
        fileChooser.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));

        String description = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT_DESC.name());
        String extension = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
                String.format("*.%s", extension));
        fileChooser.getExtensionFilters().add(extFilter);
        File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
        
        if (selected == null) return;
        
        WritableImage snapShot = ((AppUI)applicationTemplate.getUIComponent()).getChart().snapshot(new SnapshotParameters(), null);
        ImageIO.write(SwingFXUtils.fromFXImage(snapShot, null),applicationTemplate.manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT.name()),selected);
        // TODO: NOT A PART OF HW 1
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
                String      dataDirPath = "/" + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*.%s)", description, extension),
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

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
}
