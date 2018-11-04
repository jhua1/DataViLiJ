/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Algorithm.Algorithms;

import Algorithm.Clusterer;
import Algorithm.DataSet;
import actions.AppActions;
import javafx.geometry.Point2D;


import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet       dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private final AtomicBoolean continuous;
    
    public ApplicationTemplate appTemp;


    public KMeansClusterer(ApplicationTemplate app, DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters,boolean continuous) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
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
        initializeCentroids();
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
            recomputeCentroids();
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

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                i=(++i%instanceNames.size());
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
        tocontinue.set(true);
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        tocontinue.set(false);
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                                 .entrySet()
                                 .stream()
                                 .filter(entry -> i == Integer.parseInt(entry.getValue()))
                                 .map(entry -> dataset.getLocations().get(entry.getKey()))
                                 .reduce(new Point2D(0, 0), (p, q) -> {
                                     clusterSize.incrementAndGet();
                                     return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                                 });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }
    
}
