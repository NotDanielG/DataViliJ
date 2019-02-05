package ui;

import dataprocessors.TSDProcessor;
import javafx.geometry.Point2D;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AppUITest {
    @Test
    public void textAreaSave() throws TSDProcessor.InvalidDataNameException{
        String textAreaString = "@A" + "\t" + "1" + "\t" + "2,2";
        TSDProcessor processor = new TSDProcessor();
        processor.processString(textAreaString);
        Map<String, String> labels = new HashMap<>();
        Map<String, Point2D> points = new HashMap<>();

        Map<String, String> TSDLabels = processor.getDataLabels();
        Map<String, Point2D> TSDPoints = processor.getDataPoints();

        labels.put("@A","1");
        points.put("@A",new Point2D(2,2));
        Point2D test = new Point2D(2,2);
        if(TSDLabels.containsKey("@A") && TSDLabels.containsValue("1") && TSDPoints.containsKey("@A") &&
                TSDPoints.containsValue(test)) {
//            System.out.println(TSDLabels.containsKey("@A"));
//            System.out.println(TSDLabels.containsValue("1"));
//
//            System.out.println(TSDPoints.containsKey("@A"));
//            System.out.println(TSDPoints.containsValue(test));
        }
        else{
            throw new TSDProcessor.InvalidDataNameException("Test");
        }

    }

    @Test
    public void correctKMeansClusterer() throws InterruptedException {
        ConfigValues config = new ConfigValues("10","1",true,"4",true);
        //For correct values, values must be
        // Max Iterations = 10, Update Intervals = 1, Continuous = true, ClusterAmount = 4, isCluster = false.
        if(config.getMax() == 10 && config.getUpdate() == 1 && config.isContinuous() && config.getClusterAmount() == 4 && config.isCluster()){

        }
        else{
            throw new InterruptedException();
        }
    }
    @Test
    public void incorrectKMeansClusterer() throws InterruptedException {
        ConfigValues config = new ConfigValues("ad10","ad1",true,"asda5",true);
        //For correct values, values must be
        // Max Iterations = 10, Update Intervals = 1, Continuous = true, ClusterAmount = 0(Because isCluster = false) isCluster = false.
        // Incorrect Values will reset the values to
        // Max = 1, UpdateIntervals = 1, Continuous = false, ClusterAmount = 0
        if(config.getMax() == 1 && config.getUpdate() == 1 && !config.isContinuous() && config.getClusterAmount() == 0){

        }
        else{
            throw new InterruptedException();
        }
    }
    @Test
    public void correctRandomClassifier() throws InterruptedException {
        ConfigValues config = new ConfigValues("20","2",true,null,false);
        //Cluster Amount = false since the algorithm is not a cluster algorithm.
        if(config.getMax()==20 && config.getUpdate() == 2 && config.isContinuous() && config.getClusterAmount() == 0 &&
                !config.isCluster()){

        }
        else{
            throw new InterruptedException();
        }
    }
    @Test
    public void incorrectRandomClassifier() throws InterruptedException {
        ConfigValues config = new ConfigValues("2sad0","wdq2",true,null,false);
        //Values reset to
        //Max = 1, update = 1, continuous = false, cluster = 0;
        if(config.getMax() == 1 && config.getUpdate() == 1 && !config.isContinuous() && config.getClusterAmount() == 0){

        }
        else{
            throw new InterruptedException();
        }
    }
    @Test
    public void correctRandomClusterer() throws InterruptedException {
        ConfigValues config = new ConfigValues("15","3",true,"3",true);
        if(config.getMax() == 15 && config.getUpdate() == 3 && config.isContinuous() && config.getClusterAmount() == 3){

        }else{
            throw new InterruptedException();
        }
    }
    @Test
    public void incorrectRandomClusterer() throws InterruptedException {
        ConfigValues config = new ConfigValues("1asd5","3asd",true,"asda3",true);
        if(config.getMax() == 1 && config.getUpdate() == 1 && !config.isContinuous() && config.getClusterAmount() == 0){

        }else{
            throw new InterruptedException();
        }
    }


}