/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithm.Algorithms;

import Algorithm.Clusterer;
import Algorithm.DataSet;
import actions.AppActions;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 *
 * @author James
 */
public class RandomClusterer extends Clusterer{
    
    private static final Random RAND = new Random();
    
    private DataSet dataset;
    
    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean continuous;
    
    public ApplicationTemplate appTemp;
    
    public RandomClusterer(ApplicationTemplate app, DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters,boolean continuous){
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(true);
        this.continuous = new AtomicBoolean(continuous);
        appTemp = app;
    }
    
    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }
    public boolean getCont() { return continuous.get();}
    
        @Override
    public void run() {
        int iteration = 0;
        while (iteration++ < maxIterations & tocontinue.get()) {            
            if (!getCont()) {
                ((AppUI) appTemp.getUIComponent()).getDisplayBut().setOnAction(e -> {
                    ((AppUI) appTemp.getUIComponent()).getDisplayBut().setDisable(true); 
                    synchronized (this) {
                        this.notify();
                    }
                });
            }
            assignLabels();
            if (iteration % updateInterval == 0) {
                Platform.runLater(() -> {
                    LineChart<Number, Number> line = ((AppUI) (appTemp.getUIComponent())).getChart();
                    dataset.toChartData(line);
                });
                if (getCont()){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                    }
                }                
                else {                   
                    try {
                        ((AppUI) appTemp.getUIComponent()).getDisplayBut().setDisable(false);
                        synchronized (this) {
                            this.wait();
                        }
                    } catch (InterruptedException ex) {
                    }
                }

            }
        
        }
        
        ((AppUI) appTemp.getUIComponent()).getDisplayBut().setDisable(false);
        ((AppUI) appTemp.getUIComponent()).getScreenS().setDisable(false);
        ((AppActions) appTemp.getActionComponent()).setAlgoRunProperty(false);
        ((AppUI) appTemp.getUIComponent()).disableEdit(false);
        ((AppUI) appTemp.getUIComponent()).setDisplayButtonActions();
    }
    
    private void assignLabels(){
        dataset.getLabels().forEach((instanceName,labels) -> {
            dataset.getLabels().put(instanceName, Integer.toString(RAND.nextInt(numberOfClusters)+1));
        });
    }
}
