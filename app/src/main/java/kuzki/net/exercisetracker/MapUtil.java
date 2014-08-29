package kuzki.net.exercisetracker;

import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;


public class MapUtil {

    private static final String TAG = MapUtil.class.getCanonicalName();
    private static final int DEFAULT_ZOOM = 15;

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

    public static void drawRoute(GoogleMap map, Route route, Route baseRoute) {
        if (map == null) {
            return;
        }
        map.clear();
        if (route != null) {
            drawPolyline(map, route, false);
        }
        if (baseRoute != null) {
            drawPolyline(map, baseRoute, true);
        }
    }

    private static void drawPolyline(GoogleMap map, Route route, boolean isBase) {
        PolylineOptions polyLine = new PolylineOptions().geodesic(true);
        if (isBase) {
            polyLine.width(10).color(Color.BLUE);
        } else {
            polyLine.width(5).color(Color.BLUE);
        }
        for (Route.RoutePoint point : route.getPoints()) {
            polyLine.add(new LatLng(point.lat, point.lng));
        }
        map.addPolyline(polyLine);
    }

    public static void moveCameraToLocation(GoogleMap map, Double lat, Double lng) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(lat, lng), DEFAULT_ZOOM));
        }

    }

    public static void moveCameraToLocation(GoogleMap map, Location newLoc) {
        moveCameraToLocation(map, newLoc.getLatitude(), newLoc.getLongitude());
    }

    public static void moveCameraToRoute(GoogleMap map, Route route) {
        List<Route.RoutePoint> points = route.getPoints();
        Route.RoutePoint lastPoint = points.get(points.size() - 1);
        moveCameraToLocation(map, lastPoint.lat, lastPoint.lng);
    }
}
