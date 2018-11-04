package Algorithm.Algorithms;

import Algorithm.Classifier;
import actions.AppActions;
import Algorithm.DataSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Circle;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

/**
 * @author Ritwik Banerjee
 */
public class RandomClassifier extends Classifier {

    private static final Random RAND = new Random();

    @SuppressWarnings("FieldCanBeLocal")
    // this mock classifier doesn't actually use the data, but a real classifier will
    private DataSet dataset;

    private final int maxIterations;
    private final int updateInterval;
    public ApplicationTemplate apptemp;

    // currently, this value does not change after instantiation
    private final AtomicBoolean tocontinue;

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    public RandomClassifier(ApplicationTemplate app,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue) {
        //this.dataset = dataset;
        this.apptemp = app;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);

    }

    public double calc(int A, int B, double given, int c){
        return (-1*c-given*A)/B;
    }
    
    @Override
    public void run() {
        if (tocontinue()) {
            for (int i = 1; i <= maxIterations && tocontinue(); i++) {
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);

                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
                if (i % updateInterval == 0) {
                    //System.out.printf("Iteration number %d: ", i); //
                    Platform.runLater(() -> {
                        LineChart<Number, Number> line = ((AppUI) (apptemp.getUIComponent())).getChart();
                        int siz = line.getData().size() - 1;
                        try {
                            if (line.getData().get(siz).getNode().getId().equals("classifier")) {
                                line.getData().remove(siz);
                            }
                        } catch (Exception e) {
                        }
                        XYChart.Series<Number, Number> ser = new XYChart.Series();
                        ArrayList<Double> tmp = ((AppUI) (apptemp.getUIComponent())).minmax();
                        XYChart.Data min = new XYChart.Data(tmp.get(0),
                                (calc(xCoefficient, yCoefficient, tmp.get(0), constant)));
                        XYChart.Data max = new XYChart.Data(tmp.get(2),
                                (calc(xCoefficient, yCoefficient, tmp.get(2), constant)));
                        Circle startp = new Circle(0);
                        Circle endp = new Circle(0);
                        max.setNode(startp);
                        min.setNode(endp);
                        ser.getData().addAll(max, min);
                        line.getData().add(ser);
                        ser.getNode().setId("classifier");
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                    }
                }
                /*if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                System.out.printf("Iteration number %d: ", i);
                flush();
                break;
            }
                 */
            }
        } else {
            for (int i = 1; i <= maxIterations; i++) {
                int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                int yCoefficient = 10;
                int constant = RAND.nextInt(11);

                // this is the real output of the classifier
                output = Arrays.asList(xCoefficient, yCoefficient, constant);
                ((AppUI) apptemp.getUIComponent()).getDisplayBut().setOnAction(e -> {
                        synchronized(this){
                            this.notify();
                        }
                    });   
                ((AppUI) apptemp.getUIComponent()).getDisplayBut().setDisable(false);
                // everything below is just for internal viewing of how the output is changing
                // in the final project, such changes will be dynamically visible in the UI
                if (i % updateInterval == 0) {
                    //System.out.printf("Iteration number %d: ", i); //
                    Platform.runLater(() -> {
                        LineChart<Number, Number> line = ((AppUI) (apptemp.getUIComponent())).getChart();
                        int siz = line.getData().size() - 1;
                        try {
                            if (line.getData().get(siz).getNode().getId().equals("classifier")) {
                                line.getData().remove(siz);
                            }
                        } catch (Exception e) {
                        }
                        XYChart.Series<Number, Number> ser = new XYChart.Series();
                        ArrayList<Double> tmp = ((AppUI) (apptemp.getUIComponent())).minmax();
                        XYChart.Data min = new XYChart.Data(tmp.get(0),
                                (calc(xCoefficient, yCoefficient, tmp.get(0), constant)));
                        XYChart.Data max = new XYChart.Data(tmp.get(2),
                                (calc(xCoefficient, yCoefficient, tmp.get(2), constant)));
                        Circle startp = new Circle(0);
                        Circle endp = new Circle(0);
                        max.setNode(startp);
                        min.setNode(endp);
                        ser.getData().addAll(max, min);
                        line.getData().add(ser);
                        ser.getNode().setId("classifier");
                        });
                    try {
                        synchronized(this){
                            this.wait();
                            ((AppUI)apptemp.getUIComponent()).getDisplayBut().setDisable(false);
                        }
                    }
                    catch (InterruptedException ex) {
                    }

                }
            }
        }
        ((AppUI) apptemp.getUIComponent()).getDisplayBut().setDisable(false);
        ((AppUI) apptemp.getUIComponent()).getScreenS().setDisable(false);
        ((AppActions) apptemp.getActionComponent()).setAlgoRunProperty(false);
        ((AppUI) apptemp.getUIComponent()).disableEdit(false);
        ((AppUI) apptemp.getUIComponent()).setDisplayButtonActions();
        
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }

    /** A placeholder main method to just make sure this code runs smoothly */
    /*public static void main(String... args) throws IOException {
        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
        classifier.run(); // no multithreading yet
    }
*/
}