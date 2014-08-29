package kuzki.net.exercisetracker;


import java.util.ArrayList;
import java.util.List;

public class TimedLocation {
    public Long getTime() {
        return time;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    private Long time;
    private double longitude;
    private double latitude;

    public TimedLocation(Long time, double latitude, double longitude) {
        this.time = time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // return distance between this timed location and that timed location
    // measured in statute miles
    public double distanceTo(TimedLocation that) {
        double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
        double lat1 = Math.toRadians(this.latitude);
        double lon1 = Math.toRadians(this.longitude);
        double lat2 = Math.toRadians(that.latitude);
        double lon2 = Math.toRadians(that.longitude);

        // great circle distance in radians, using law of cosines formula
        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        // each degree on a great circle of Earth is 60 nautical miles
        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }


    // in miles
    public static double distanceBetween(TimedLocation start, TimedLocation end) {
        double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
        double lat1 = Math.toRadians(start.latitude);
        double lon1 = Math.toRadians(start.longitude);
        double lat2 = Math.toRadians(end.latitude);
        double lon2 = Math.toRadians(end.longitude);

        // great circle distance in radians, using law of cosines formula
        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        // each degree on a great circle of Earth is 60 nautical miles
        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }


    public static List<TimedDistance> convertToTimedDistance(List<TimedLocation> tLocations) {

        if (tLocations == null || tLocations.size() == 0) {
            return null;
        }

        TimedLocation start = tLocations.get(0);

        ArrayList<TimedDistance> result =  new ArrayList<TimedDistance>();

        for (int i = 0; i < tLocations.size(); i++) {
            TimedLocation current = tLocations.get(i);
            result.add(new TimedDistance(current.getTime()- start.getTime(), distanceBetween(current, start)));
        }
        return result;
    }

    // return string representation of this point
    public String toString() {
        return time + " at: (" + latitude + ", " + longitude + ")";
    }

    // test client
    public static void main(String[] args) {
        TimedLocation loc1 = new TimedLocation(1234567L, 40.366633, 74.640832);
        TimedLocation loc2 = new TimedLocation(123458L,  42.443087, 76.488707);
        double distance = loc1.distanceTo(loc2);
        System.out.printf("%6.3f miles from\n", distance);
        System.out.println(loc1 + " to " + loc2);
    }
}

