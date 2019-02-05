package algorithms;

import dataprocessors.TSDProcessor;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import ui.DataSet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer{

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private boolean isRunning;

    private int inter;
    private LineChart<Number, Number> chart;
    private Button display;
    private Button screenshot;
    private TSDProcessor tsd;
    private boolean continueOn;


    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean continued,
                           LineChart<Number, Number> chart, Button displayButton, Button screenshot, TSDProcessor tsd) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(false);
        this.chart = chart;
        display = displayButton;
        this.screenshot = screenshot;
        this.tsd = tsd;
        this.continueOn = continued;
        isRunning = true;

        (new Thread(this)).start();
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public synchronized void run() {
        try {
            while(isRunning) {
                initializeCentroids();
                int iteration = 0;
                while (iteration++ < maxIterations & tocontinue.get()) {
                    assignLabels();
                    recomputeCentroids();
                    if(iteration % updateInterval == 0){
                        Platform.runLater(()->chart.getData().clear());
                        Platform.runLater(()-> tsd.setData(dataset.getLabels(),dataset.getLocations()));
                        Platform.runLater(()-> tsd.toChartData(chart));
                    }
                    if (!continueOn && iteration < maxIterations) {
                        Platform.runLater(() -> screenshot.setDisable(false));
                        this.wait();
                    }
                    Thread.sleep(500);
                }
                isRunning = false;
            }
            Platform.runLater(() ->screenshot.setDisable(false));
            Platform.runLater(()-> display.setText("Display"));
            Platform.runLater(()-> display.setDisable(false));
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i))) {
               i++;
            }
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
            Point2D newCentroid = new Point2D((sum.getX() / clusterSize.get()), (sum.getY() / clusterSize.get()));
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
                tocontinue.set(true);
            }
        });
    }
    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

    public synchronized void setNotify(){
        this.notify();
    }
    public boolean isRunning(){
        return isRunning;
    }
}