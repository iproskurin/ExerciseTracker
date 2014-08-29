package kuzki.net.exercisetracker;

import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapUtil {

    private static final String TAG = MapUtil.class.getCanonicalName();
    private static final int DEFAULT_ZOOM = 19;

    public static void setupMapFragment(GoogleMap map) {
        if (map != null) {
          map.setMyLocationEnabled(true);
        }
    }

    public static void clearMap(GoogleMap map) {
        if (map != null) {
            map.clear();
        }
    }

    public static void drawRoute(GoogleMap map, Route route) {
        if (map == null) {
            return;
        }
        map.clear();
        PolylineOptions polyLine = new PolylineOptions().geodesic(true);
        for (LatLng point : route.getPoints()) {
            polyLine.add(point);
        }
        map.addPolyline(polyLine);
    }

    public static void moveToMyLocation(GoogleMap map, Location newLoc) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(newLoc.getLatitude(), newLoc.getLongitude()), DEFAULT_ZOOM));
        }
    }
}
