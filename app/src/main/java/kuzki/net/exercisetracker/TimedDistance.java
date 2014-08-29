package kuzki.net.exercisetracker;


public class TimedDistance {
    public Long getTime() {
        return time;
    }

    public double getDistance() {
        return distance;
    }

    private Long time;
    private double distance;
    public TimedDistance(Long time, double distance) {
        this.time = time;
        this.distance  = distance;
    }
}