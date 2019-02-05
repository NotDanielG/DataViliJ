package algorithms;

import dataprocessors.TSDProcessor;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.effect.Light;
import ui.DataSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {
    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private boolean isRunning;
    private int inter;

    private Map<String, Point2D> map;
    private LineChart<Number, Number> chart;
    private Button display;
    private Button screenshot;
    private TSDProcessor tsd;
    public RandomClusterer(DataSet dataset, int maxIterations, int updateIntervals, int numberofClusters, boolean continued,
                           LineChart<Number, Number> chart, Button displayButton, Button screenshot, TSDProcessor tsd){
        super(numberofClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateIntervals;
        this.tocontinue = new AtomicBoolean(continued);
        this.chart = chart;
        this.display = displayButton;
        this.screenshot = screenshot;
        this.tsd = tsd;
        isRunning = true;

        map = new HashMap<>();
        (new Thread(this)).start();
    }
    public boolean isRunning(){
        return isRunning;
    }
    public synchronized void run(){
        try{
            isRunning = true;
            while(isRunning) {
                int iterations = 0;
                while (iterations++ < maxIterations) {
                    assignLabels();
                    System.out.println(iterations);
                    if(iterations % updateInterval == 0){
                        Platform.runLater(()->chart.getData().clear());

                        Platform.runLater(()-> tsd.setData(dataset.getLabels(),dataset.getLocations()));
                        Platform.runLater(()-> tsd.toChartData(chart));
                    }
                    if (!tocontinue() && iterations != maxIterations) {
                        Platform.runLater(() -> screenshot.setDisable(false));
                        this.wait();
                    }
                    Thread.sleep(500);
                }
                isRunning = false;
                Platform.runLater(() -> screenshot.setDisable(false));
                Platform.runLater(() -> display.setText("Display"));
                Platform.runLater(() -> display.setDisable(false));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void assignLabels(){
        ArrayList<String> chosen = new ArrayList<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        while(chosen.size()<numberOfClusters){
            int i = 0;
            while(chosen.contains(instanceNames.get(i))){
                i++;
            }
            chosen.add(Integer.toString(i));
            i++;
        }
        inter = 0;
        dataset.getLocations().forEach((instanceName, location) -> {
            dataset.getLabels().put(instanceName,Integer.toString((int)Math.floor(Math.random()*numberOfClusters) +1));
            inter++;
            if(inter > numberOfClusters){
                inter = 0;
            }
        });

    }
    public Map<String, Point2D> getRandomMap(){
        Set<String> labels = dataset.getLabels().keySet();
        Random r = new Random();
        for(String label: labels){
            Point2D point = new Point2D(r.nextInt(5),r.nextInt(5));
            map.put(label, point);
        }

        dataset.setLocations(map);
        return map;
    }
    @Override
    public int getUpdateInterval() {
        return 0;
    }

    @Override
    public int getMaxIterations() {
        return 0;
    }

    @Override
    public int getNumberOfClusters() {
        return super.getNumberOfClusters();
    }
    public synchronized void setNotify(){
        this.notify();
    }
    public boolean tocontinue(){
        return tocontinue.get();
    }
}
