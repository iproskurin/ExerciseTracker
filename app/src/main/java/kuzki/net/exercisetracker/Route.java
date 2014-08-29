package kuzki.net.exercisetracker;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iproskurin on 8/28/14.
 */
public class Route {
    List<LatLng> points;

    public Route() {
        points = new ArrayList<LatLng>();
    }

    public void addPoint(LatLng latLng) {
        points.add(latLng);
    }

    public void addPoint(double lat, double lng) {
        addPoint(new LatLng(lat, lng));
    }

    public List<LatLng> getPoints() {
        return points;
    }
}
