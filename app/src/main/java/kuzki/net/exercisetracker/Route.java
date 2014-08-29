package kuzki.net.exercisetracker;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iproskurin on 8/28/14.
 */
public class Route {
    public String name;
    List<RoutePoint> points;

    public class RoutePoint {
        Double lat;
        Double lng;
        Long time;

        public RoutePoint(double lat, double lng, long time) {
            this.lat = lat;
            this.lng = lng;
            this.time = time;
        }
    }

    public Route(String routeName) {
        points = new ArrayList<RoutePoint>();
        this.name = routeName;
    }

    public Route() {
        this("");
    }

    public void addPoint(LatLng latLng) {
        addPoint(latLng.latitude, latLng.longitude, 0);
    }

    public void addPoint(double lat, double lng, long time) {
        RoutePoint point = new RoutePoint(lat, lng, time);
        points.add(point);
    }

    public List<RoutePoint> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toDebugString() {
        String out = name + ":\n";
        for (RoutePoint p : points) {
            out += "(" + p.lat + ", " + p.lng + ", " + p.time + ")\n";
        }
        return out;
    }
}
