package ui;

import actions.AppActions;
import Algorithm.*;
import dataprocessors.AppData;
import java.io.File;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import static java.io.File.separator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import static settings.AppPropertyTypes.APP_CSS_FILE;
import static settings.AppPropertyTypes.APP_CSS_FILE_DIR;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.settings.PropertyTypes;
import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;
import java.lang.reflect.*;
import javafx.scene.Node;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;

    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox                     displayToggle;
    private ArrayList<String>            entireText;
    private Text                         metaData;
    private Accordion                    algorithms;
    private ToggleGroup                  algo;
    private HashMap<String,Algorithm>    algoParams;
    
    public Accordion getAlgos() { return algorithms;}
    public Text getMetaData(){ return metaData;}
    public CheckBox getDisplayToggle(){ return displayToggle;}
    public void setEntireText(ArrayList<String> l){ entireText = l; }
    public ArrayList<String> getEntireText() { return entireText;}
    public Button getSaveButton() {return saveButton;}
    public LineChart<Number, Number> getChart() { return chart; }

    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(separator,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        newButton.setDisable(false);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        //printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
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
        metaData.setText("");
        algorithms.setVisible(false);
        displayButton.setVisible(false);
        entireText = new ArrayList();
        scrnshotButton.setDisable(true);
        algoParams.clear();
    }

    public String getCurrentText() { return textArea.getText(); }
    public Button getDisplayBut() { return displayButton;}
    public TextArea getTextBox() {return textArea;}
    public Button getScreenS() { return scrnshotButton;}
    

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis      xAxis   = new NumberAxis();
        NumberAxis      yAxis   = new NumberAxis();
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        xAxis.setForceZeroInRange(false);
        yAxis.setForceZeroInRange(false);
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));

        VBox leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.3);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.3);
/*
        Text   leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname       = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize       = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));
*/
        textArea = new TextArea();
        textArea.setVisible(false);
        textArea.setMinHeight(200);

        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        displayButton.setVisible(false);
        displayToggle = new CheckBox(manager.getPropertyValue(AppPropertyTypes.TOGGLE_DISPLAY.name()));
        displayToggle.setVisible(false);
        metaData = new Text();
        metaData.setVisible(false);
        metaData.setWrappingWidth(leftPanel.getMaxWidth());
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        
        processButtonsBox.getChildren().add(displayButton);
        processButtonsBox.getChildren().add(displayToggle);
        
        algoParams = new HashMap<>();
        algo = new ToggleGroup();
        VBox clu = new VBox();
        VBox cla = new VBox();
        File dir = new File(manager.getPropertyValue(AppPropertyTypes.ALGORITHM_RESOURCE_PATH.name()));
        File[] listAlgo = dir.listFiles();
        String iconPath = "/" + String.join(separator,
                                             manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                             manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String iconsPath = String.join(separator, iconPath, manager.getPropertyValue(AppPropertyTypes.EDIT_ICON_PATH.name()));
        for ( File f : listAlgo){
            HBox tmp = new HBox();
            Button s = new Button(null, new ImageView(new Image(getClass().getResourceAsStream(iconsPath))));
            s.setTooltip(new Tooltip(manager.getPropertyValue(AppPropertyTypes.EDIT_CONFIGURATION.name())));
            RadioButton r = new RadioButton(f.getName().substring(0, f.getName().length()-5));
            r.setToggleGroup(algo);
            tmp.getChildren().addAll(r,s);
            if (f.getName().contains("Classifier")){
                s.setOnAction( e-> ConfigAlgo(f.getName().substring(0,f.getName().length()-5)));
                cla.getChildren().add(tmp);
            }
            else if (f.getName().contains("Clusterer")){
                s.setOnAction( e-> ConfigAlgo(f.getName().substring(0,f.getName().length()-5)));
                clu.getChildren().add(tmp);
            }
            
        }

        displayButton.setDisable(true);
        
        TitledPane clusters = new TitledPane(manager.getPropertyValue(AppPropertyTypes.Algorithm_Cluster.name()),clu);
        TitledPane classi = new TitledPane(manager.getPropertyValue(AppPropertyTypes.Algorithm_Class.name()), cla);
        classi.setDisable(true);
        algorithms = new Accordion();
        algorithms.getPanes().addAll(classi,clusters);
        algorithms.setVisible(false);
        
        leftPanel.getChildren().addAll(textArea, processButtonsBox,metaData,algorithms,displayButton);

        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);

        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        
        chartCSS();
    }
    
    private void chartCSS(){
        String css = "/" + String.join(separator,
                applicationTemplate.manager.getPropertyValue(APP_CSS_FILE_DIR.name()),
                applicationTemplate.manager.getPropertyValue(APP_CSS_FILE.name()));
        primaryScene.getStylesheets().add(getClass().getResource(css).toExternalForm());
    }

    private int strCount(String s){
        String[] tmp = s.split("\n");
        return tmp.length;
    }
    
    public void disableEdit(boolean b){
        for(TitledPane t : algorithms.getPanes()){
            for ( Node u : (((VBox)t.getContent()).getChildren())){
                u.setDisable(b);
            }
        }
    }
    
    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setToggleButtonActions();
        setScreenshotButtonActions();
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (newValue.equals(""))
                    saveButton.setDisable(true);
                else if(!newValue.equals(oldValue)) {
                    ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                    if (newValue.charAt(newValue.length() - 1) == '\n') {
                        hasNewText = true;
                    }
                    newButton.setDisable(false);
                    saveButton.setDisable(false);
                    if (entireText != null && strCount(newValue) < 10 && entireText.size() > 0) {
                        textArea.appendText(entireText.remove(0));
                        saveButton.setDisable(true);
                    }

                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    public void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
            //if (hasNewText) {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    String s = textArea.getText();
                    if(entireText!=null)
                        for( String x : entireText){
                            s+=x;
                    }
                    dataComponent.loadData(s);
                    dataComponent.displayData();
                    setHoverPoints();
                    displayButton.setDisable(true);
                    scrnshotButton.setDisable(true);
                    disableEdit(true);
                    ((AppActions) applicationTemplate.getActionComponent()).setAlgoRunProperty(true);
                    Thread d = new Thread(algoParams.get(((RadioButton)algo.getSelectedToggle()).getText()));
                    d.start();
                    scrnshotButton.setDisable(false);
                } catch (Exception e) {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    PropertyManager manager = applicationTemplate.manager;
                    String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String errMsg = e.getMessage();
                    String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    dialog.show(errTitle, errMsg + errInput);
                }

            });
    }
     
    private void setToggleButtonActions(){
        displayToggle.setOnAction(e-> {
            if (displayToggle.isSelected()) {
                try {
                    ((AppData) applicationTemplate.getDataComponent()).loadData(textArea.getText());
                    textArea.setEditable(false);
                } catch (Exception ex) {
                    ErrorDialog dialog = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    PropertyManager manager = applicationTemplate.manager;
                    String errTitle = manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name());
                    String errMsg = ex.getMessage();
                    String errInput = manager.getPropertyValue(AppPropertyTypes.TEXT_AREA.name());
                    dialog.show(errTitle, errMsg + errInput);
                    displayToggle.setSelected(false);
                }
            }
            else{
                textArea.setEditable(true);
                algorithms.setVisible(false);
                metaData.setVisible(false);
            }
        });
    }
    
    private void setScreenshotButtonActions(){
        scrnshotButton.setOnAction(e-> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } 
            catch (IOException ex) {
                
            }
        });
    }
    
    public ArrayList<Double> minmax(){
        double xl,xh,yl,yh;
        xl = (double)chart.getData().get(0).getData().get(0).getXValue();
        xh = (double)chart.getData().get(0).getData().get(0).getXValue();
        yl = (double)chart.getData().get(0).getData().get(0).getYValue();
        yh = (double)chart.getData().get(0).getData().get(0).getYValue();
        for(XYChart.Series<Number,Number>s : chart.getData()){
            for(XYChart.Data<Number,Number> d : s.getData()){
                double dx = (double)d.getXValue();
                if ( dx > xh)
                    xh = dx;
                else if ( dx < xl)
                    xl = dx;
                double dy = (double)d.getYValue();
                if ( dy > yh)
                    yh = dy;
                else if ( dy < yl)
                    yl = dy;
            }
        }
        ArrayList<Double> ret = new ArrayList();
        ret.add(xl);
        ret.add(yl);
        ret.add(xh);
        ret.add(yh);
        return ret;
    }
    
    private void setHoverPoints(){
        for(XYChart.Series<Number,Number> s : chart.getData()){
            for(XYChart.Data<Number,Number> d : s.getData()){ 
                Tooltip.install(d.getNode(), new Tooltip(s.getName()));
                d.getNode().setOnMouseEntered( e -> {
                    primaryScene.setCursor(Cursor.CROSSHAIR);
                });
                d.getNode().setOnMouseExited( e -> {
                    primaryScene.setCursor(Cursor.DEFAULT);
                }); 
            }
        }
    }
    

    private void ConfigAlgo(String z){
        Stage configur = new Stage();
        configur.setTitle(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION_EDIT_TITLE.name()));
        VBox lay = new VBox();
        Label title = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONFIGURATION_EDIT_TITLE.name()));
        TextArea maxIter = new TextArea();
        TextArea updateInt = new TextArea();
        maxIter.setMaxHeight(10);
        maxIter.setMaxWidth(80);
        updateInt.setMaxHeight(10);
        updateInt.setMaxWidth(80);
        Label maxI = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MAX_ITERATIONS.name()));
        Label updateI = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.UPDATE_INTERVAL.name()));
        HBox hbox1 = new HBox();
        HBox hbox2 = new HBox();
        hbox1.getChildren().addAll(maxI,maxIter);
        hbox2.getChildren().addAll(updateI,updateInt);
        lay.getChildren().addAll(title,hbox1,hbox2);
        HBox hbox3;
        Label clusterNum;
        TextArea clusterCt = new TextArea();
        clusterCt.setMaxHeight(10);
        clusterCt.setMaxWidth(80);
        String algoCluster = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Algorithm_Cluster.name());
        String algoClassi = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.Algorithm_Class.name());
        if ( z.contains(algoCluster)){
            hbox3 = new HBox();
            clusterNum = new Label(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CLUSTER_NUMBER.name()));
            hbox3.getChildren().addAll(clusterNum,clusterCt);
            lay.getChildren().add(hbox3);
        }
        CheckBox cont = new CheckBox(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.CONTINUOUS.name()));
        Button confirm = new Button("OK");
        lay.getChildren().addAll(cont,confirm);
        if ( algoParams.containsKey(z)) {
            maxIter.setText(String.valueOf(algoParams.get(z).getMaxIterations()));
            updateInt.setText(String.valueOf(algoParams.get(z).getUpdateInterval()));
            cont.setSelected(algoParams.get(z).tocontinue());
            if ( z.contains(algoCluster))
                clusterCt.setText(String.valueOf(((Clusterer)algoParams.get(z)).getNumberOfClusters()));           
        }
        if (z.contains(algoClassi)) {
            confirm.setOnAction(e -> {
                try {
                    saveClassifier(maxIter.getText(), updateInt.getText(), cont.isSelected(), configur,z);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                }
            });
        } else {
            confirm.setOnAction(e -> {
                try {
                    saveCluster(maxIter.getText(), updateInt.getText(), cont.isSelected(), clusterCt.getText(), configur, z);
                } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | DataSet.InvalidDataNameException | IOException ex) {
                }
            });
        }
        Scene sc = new Scene(lay);
        configur.setScene(sc);
        configur.show();
        
    }
    
    private void saveClassifier(String a, String b, boolean c, Stage s,String z) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        int x, y;
        try {
            x = Integer.parseInt(a);
            if (x <= 0) {
                x = 100;
            }
        } catch (Exception e) {
            x = 100;
        }
        try {
            y = Integer.parseInt(b);
            if (y <= 0) {
                y = 2;
            }
        } catch (Exception e) {
            y = 2;
        }
        String classP = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM_CLASS_PATH.name())+z;
        Class tmp = Class.forName(classP);
        Constructor<?> clas = tmp.getConstructor(ApplicationTemplate.class, int.class, int.class, boolean.class);
        Algorithm instance = (Algorithm) clas.newInstance(applicationTemplate,x,y,c);
        
        if (algoParams.containsKey(z))
            algoParams.replace(z, instance);
        else
            algoParams.put(z,instance);
        
        s.hide();
        displayButton.setVisible(true);
        displayButton.setDisable(false);
    }

    private void saveCluster(String a, String b, boolean c, String d, Stage s, String algoN) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, DataSet.InvalidDataNameException, IOException {
        int x, y, v;
        try {
            x = Integer.parseInt(a);
            if (x <= 0) {
                x = 100;
            }
        } catch (Exception e) {
            x = 100;
        }
        try {
            y = Integer.parseInt(b);
            if (y <= 0) {
                y = 5;
            }
        } catch (Exception e) {
            y = 5;
        }
        try {
            v = Integer.parseInt(d);
            if (v <= 0) {
                v = 2;
            }
        } catch (Exception e) {
            v = 2;
        }
        
        DataSet hold;
        if (((AppActions)applicationTemplate.getActionComponent()).noDataPath()){
            hold = new DataSet();
            String[] lines = textArea.getText().split("\n");
            for (String str : lines){
                hold.addInstance(str);
            }
        }
        else {
            hold = DataSet.fromTSDFile(((AppActions)applicationTemplate.getActionComponent()).getDataFilePath());
        }
        String classP = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.ALGORITHM_CLASS_PATH.name())+algoN;
        Class tmp = Class.forName(classP);
        Constructor<?> clas = tmp.getConstructor(ApplicationTemplate.class, DataSet.class, int.class, int.class,int.class,boolean.class);
        Algorithm instance = (Algorithm) clas.newInstance(applicationTemplate,hold,x,y,v,c);
        
        if (algoParams.containsKey(algoN))
            algoParams.replace(algoN, instance);
        else
            algoParams.put(algoN,instance);    
        
        s.hide();
        displayButton.setVisible(true);
        displayButton.setDisable(false);
    }

}
