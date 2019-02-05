package algorithms;

import javafx.application.Platform;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import ui.DataSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private static LineChart<Number, Number> chart;
    private static Button button;
    private static Button screenshot;
    boolean isRunning;
    boolean almost;
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

    public RandomClassifier(DataSet dataset,
                            int maxIterations,
                            int updateInterval,
                            boolean tocontinue,
                            LineChart<Number, Number> chart,
                            Button button,
                            Button screenButton) {
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(tocontinue);
        this.chart = chart;
        almost = false;
        this.button = button;
        this.screenshot = screenButton;

        (new Thread(this)).start();
    }

    @Override
    public synchronized void run() {
        try {
            isRunning = true;
            while(isRunning) {
                for (int i = 1; i <= maxIterations; i++) {
                    int xCoefficient = new Long(-1 * Math.round((2 * RAND.nextDouble() - 1) * 10)).intValue();
                    int yCoefficient = 10;
                    int constant = RAND.nextInt(11);

                    // this is the real output of the classifier
                    output = Arrays.asList(xCoefficient, yCoefficient, constant);

                    // everything below is just for internal viewing of how the output is changing
                    // in the final project, such changes will be dynamically visible in the UI
                    if (i % updateInterval == 0 || i == maxIterations) {
                        System.out.printf("Iteration number %d: ", i);
                        addLine(output.get(0), output.get(1), output.get(2));
                        flush();
                        if(!tocontinue() && i < maxIterations){
                            Platform.runLater(() -> screenshot.setDisable(false));
                            this.wait();
                        }
                        else {
                            if(tocontinue()) {
                                Thread.sleep(500);
                            }
                        }
                    }
                    if(i == maxIterations){
                        Platform.runLater(() ->screenshot.setDisable(false));
                        Platform.runLater(()-> button.setText("Display"));
                        Platform.runLater(()-> button.setDisable(false));
                    }
                    if (i > maxIterations * .6 && RAND.nextDouble() < 0.05) {
                        System.out.printf("Iteration number %d: ", i);
                        flush();
                        break;
                    }
                    Thread.sleep(250);
                }

                if(tocontinue()) {
                    isRunning = false;
                }
                if(!tocontinue()){
                    isRunning = false;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // for internal viewing only
    protected void flush() {
        System.out.printf("%d\t%d\t%d%n", output.get(0), output.get(1), output.get(2));
    }
    public void setRunning(boolean b){
        isRunning = b;
    }
    public boolean isRunning(){
        return isRunning;
    }
    public synchronized void setNotify(){
        this.notify();
    }
    /** A placeholder main method to just make sure this code runs smoothly */
    public static void main(String... args) throws IOException {
//        DataSet          dataset    = DataSet.fromTSDFile(Paths.get("/path/to/some-data.tsd"));
//        RandomClassifier classifier = new RandomClassifier(dataset, 100, 5, true);
//        classifier.run(); // no multithreading yet
    }
    public static void addLine(int a, int b, int c){
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        int min = chart.getData().get(0).getData().get(0).getXValue().intValue();
        int max = chart.getData().get(0).getData().get(0).getXValue().intValue();
        if(chart.getData().size() > 1){
//            Platform.runLater(() ->chart.getData().remove(1));
            Platform.runLater(() -> chart.getData().remove( chart.getData().size()-1));
        }
        for(int i = 0; i < chart.getData().get(0).getData().size(); i++){
            if(chart.getData().get(0).getData().get(i).getXValue().intValue() > max){
                max = chart.getData().get(0).getData().get(i).getXValue().intValue();
            }
            if(chart.getData().get(0).getData().get(i).getXValue().intValue() < min){
                min = chart.getData().get(0).getData().get(i).getXValue().intValue();
            }
        }
        series.setName("Line");

        double y1 = (-c-a*min)/b;
        double y2 = (-c-a*max)/b;
        if(min == max){
            max = max*a;
        }
        if(y1 == y2){
            y2++;
        }
        series.getData().add(new XYChart.Data<>(min,y1));
        series.getData().add(new XYChart.Data<>(max,y2));

        Platform.runLater(() ->chart.getData().add(series));
    }
    public boolean almostDone(){
        return almost;
    }
}