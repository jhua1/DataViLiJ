package dataprocessors;

import java.io.File;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

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
        // TODO: NOT A PART OF HW 1
        try {
            String readtxt = new String(Files.readAllBytes(dataFilePath));
            processor.clear();
            processor.processString(readtxt);
            String[] tmp = readtxt.split("\n");
            ArrayList<String> ret = new ArrayList();
            for(String s :tmp){
                ret.add(s+"\n");
            }
            ((AppUI)applicationTemplate.getUIComponent()).getTextBox().clear();
            ((AppUI)applicationTemplate.getUIComponent()).setEntireText(ret);
            ((AppUI)applicationTemplate.getUIComponent()).getTextBox().appendText(ret.remove(0));
            ((AppUI)applicationTemplate.getUIComponent()).getTextBox().setEditable(false);
            ((AppData)applicationTemplate.getDataComponent()).MetaData(dataFilePath.toString());
            ((AppUI)applicationTemplate.getUIComponent()).getAlgos().setVisible(true);
            Classification();
        }
        catch (Exception e){
        ErrorDialog err = (ErrorDialog)applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        err.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),e.getMessage());
        }
    }

    public void loadData(String dataString) throws Exception {
        try {
            processor.clear();
            processor.processString(dataString);
            MetaData("");
            ((AppUI)applicationTemplate.getUIComponent()).getAlgos().setVisible(true);
            Classification();
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE: completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            String s = ((AppUI) applicationTemplate.getUIComponent()).getCurrentText();
            ArrayList<String> t = ((AppUI)applicationTemplate.getUIComponent()).getEntireText();
            if (t != null)
                for (String a : t){
                    s+=a;
            }
            writer.write(s);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void MetaData(String path){
        Set<String> key = processor.getDataLabels().keySet();
        String[] keyset = processor.getDataLabels().keySet().toArray(new String[key.size()]);
        Collection<String> value = processor.getDataLabels().values();
        String[] values = processor.getDataLabels().values().toArray(new String[value.size()]);
        ArrayList<String> Uniques = new ArrayList<String>();
        for ( String s : values){
            if (Uniques.indexOf(s) == -1)
                Uniques.add(s);
        }
        String UniquesMeta = "";
        for ( String t : Uniques){
            UniquesMeta += t + "\n";
        }
        String MetaData = "";
        MetaData = "Number of Instances: " + keyset.length + "\n" + "Number of Labels: " + Uniques.size() + "\n" + UniquesMeta;
        ((AppUI)applicationTemplate.getUIComponent()).getMetaData().setText(MetaData + "\n" + path);
        ((AppUI)applicationTemplate.getUIComponent()).getMetaData().setVisible(true);
    }
    
    public void Classification(){
        ArrayList<String> clas = new ArrayList();
        Collection<String> value = processor.getDataLabels().values();
        String[] values = processor.getDataLabels().values().toArray(new String[value.size()]);
        for (String s : values){
            if (clas.size() > 2)
                break;
            else if (!clas.contains(s))
                clas.add(s);
        }
        if (clas.size() == 2)
            ((AppUI)applicationTemplate.getUIComponent()).getAlgos().getPanes().get(0).setDisable(false);
        else
            ((AppUI)applicationTemplate.getUIComponent()).getAlgos().getPanes().get(0).setDisable(true);
    }
    
    @Override
    public void clear() {
        processor.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
