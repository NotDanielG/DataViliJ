package ui;

public class ConfigValues {
    private int max;
    private int update;
    private boolean continuous;
    private boolean isCluster;
    private int clusterAmount;
    public ConfigValues(String max, String updateIntervals, boolean continuous, String clusterAmount, boolean isCluster){
        try {
            setMax(max);
            setUpdate(updateIntervals);
            setContinuous(continuous);
            this.isCluster = isCluster;
            if(isCluster) {
                setClusterAmount(clusterAmount);
            }else{
                this.clusterAmount = 0;
            }
        }catch(Exception e){
            this.max = 1;
            this.update = 1;
            this.continuous = false;
            this.clusterAmount = 0;
        }
    }
    public void setMax(String max){
        this.max = Integer.parseInt(max);
    }
    public void setUpdate(String update){
        this.update = Integer.parseInt(update);
    }
    public void setClusterAmount(String cluster){
        this.clusterAmount = Integer.parseInt(cluster);
    }
    public void setContinuous(boolean b) {
        this.continuous = b;
    }
    public int getMax(){
        return max;
    }
    public int getUpdate(){
        return update;
    }
    public boolean isContinuous(){
        return continuous;
    }
    public int getClusterAmount(){
        return clusterAmount;
    }
    public boolean isCluster(){
        return isCluster;
    }
}
