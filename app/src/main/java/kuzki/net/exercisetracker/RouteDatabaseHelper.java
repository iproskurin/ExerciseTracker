package kuzki.net.exercisetracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class RouteDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = RouteDatabaseHelper.class.getCanonicalName();


    public final class RoutesContract {
        // To prevent someone from accidentally instantiating the contract class,
        // give it an empty constructor.
        public RoutesContract() {}

        /* Inner class that defines the table contents */
        public abstract class RouteEntry implements BaseColumns {
            public static final String TABLE_NAME = "routes";
            public static final String COLUMN_NAME_ROUTE_NAME = "routeName";
            public static final String COLUMN_NAME_TIME = "time";
            public static final String COLUMN_NAME_LAT = "latitude";
            public static final String COLUMN_NAME_LNG = "longitude";
        }

        private static final String TEXT_TYPE = " TEXT";
        private static final String INT_TYPE = " INTEGER";
        private static final String FLOAT_TYPE = " REAL";
        private static final String COMMA_SEP = ",";


        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                        RouteEntry._ID + " INTEGER PRIMARY KEY," +
                        RouteEntry.COLUMN_NAME_ROUTE_NAME + TEXT_TYPE + COMMA_SEP +
                        RouteEntry.COLUMN_NAME_LAT + TEXT_TYPE + COMMA_SEP +
                        RouteEntry.COLUMN_NAME_LNG + TEXT_TYPE +
                        RouteEntry.COLUMN_NAME_TIME + TEXT_TYPE + COMMA_SEP +
                        " )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME;
    }

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "ExerciseTracker.db";

    public RouteDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(RoutesContract.SQL_CREATE_ENTRIES);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(RoutesContract.SQL_DELETE_ENTRIES);

        // Create tables again
        onCreate(db);
    }

    // Adding new route
    public void addRoute(Route route) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (Route.RoutePoint point : route.getPoints()) {
            ContentValues values = new ContentValues();
            values.put(RoutesContract.RouteEntry.COLUMN_NAME_ROUTE_NAME, route.name);
            values.put(RoutesContract.RouteEntry.COLUMN_NAME_LAT, point.lat);
            Log.d(TAG, "Storing lat: " + point.lat + " in db: " + values.get(RoutesContract.RouteEntry.COLUMN_NAME_LAT));
            values.put(RoutesContract.RouteEntry.COLUMN_NAME_LNG, point.lng);
            values.put(RoutesContract.RouteEntry.COLUMN_NAME_TIME, point.time);

            // Inserting Row
            db.insert(RoutesContract.RouteEntry.TABLE_NAME, null, values);
        }
        db.close(); // Closing database connection
        Log.d(TAG, "New Route is added to DB with name: " + route.name);
    }

    public Route getRoute(String routeName) {
        List<Route> routeList = getRoutes();
        for (Route r : routeList) {
            if (r.name.equals(routeName)) {
                return r;
            }
        }
        Log.d(TAG, "Did not find the route: " + routeName);
        return null;
    }

    // Getting all routes
    public List<Route> getRoutes() {
        List<Route> routeList = new ArrayList<Route>();
        // Select All Query
        Log.d(TAG, "Reading all qeries from db..");
        String selectQuery = "SELECT  * FROM " + RoutesContract.RouteEntry.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        Log.d(TAG, "Got cursor: " + cursor.getCount());
        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Log.d(TAG, "Parsed cursor with lat: ");
                String routeName = cursor.getString(1);
                Log.d(TAG, "name: " + routeName);
                Double lat = Double.parseDouble(cursor.getString(2));
                Log.d(TAG, "lat: " + lat);
                Double lng = Double.parseDouble(cursor.getString(3));
                Log.d(TAG, "lng: " + lng);
                Long time = 0L;//Long.parseLong(cursor.getString(4));
                Log.d(TAG, "time: " + time);

                Log.d(TAG, "Current route list size: " + routeList.size());
                if (routeList.isEmpty() || !routeList.get(routeList.size()-1).name.equals(routeName)) {
                    Log.d(TAG, "Creating new route with name: " + routeName);
                    routeList.add(new Route(routeName));
                } else {
                    Log.d(TAG, "Adding point to the last route with name: " + routeName);
                    Route lastRoute = routeList.get(routeList.size()-1);
                    lastRoute.addPoint(lat, lng, time);
                }
            } while (cursor.moveToNext());
        }

        Log.d(TAG, "Route list is retrieved from the db: " + routeList.size());
        // return contact list
        return routeList;
    }


    public void removeAllRoutes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(RoutesContract.RouteEntry.TABLE_NAME, null, null);
    }
}