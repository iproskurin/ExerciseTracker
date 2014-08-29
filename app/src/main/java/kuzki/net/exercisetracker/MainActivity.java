/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kuzki.net.exercisetracker;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This the app's main Activity. It provides buttons for requesting the various features of the
 * app, displays the current location, the current address, and the status of the location client
 * and updating services.
 *
 * {@link #getLocation} gets the current location using the Location Services getLastLocation()
 * function. {@link #getAddress} calls geocoding to get a street address for the current location.
 * {@link #startUpdates} sends a request to Location Services to send periodic location updates to
 * the Activity.
 * {@link #stopUpdates} cancels previous periodic update requests.
 *
 * The update interval is hard-coded to be 5 seconds.
 */
public class MainActivity extends FragmentActivity implements
        LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final boolean DEBUG = true;
    private static final boolean FAKE_LOC = true;
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    // Handles to UI widgets
    private TextView mLatLng;
    private TextView mConnectionState;
    private TextView mConnectionStatus;
    private Button mStartStopRecording;

    // Handle to SharedPreferences for this app
    SharedPreferences mPrefs;

    // Handle to a SharedPreferences editor
    SharedPreferences.Editor mEditor;

    /*
     * Note if updates have been turned on. Starts out as "false"; is set to "true" in the
     * method handleRequestSuccess of LocationUpdateReceiver.
     *
     */
    boolean mUpdatesRequested = false;

    GoogleMap mMap;
    private boolean mStartRecording = false;
    private Route mRecordRoute = null;
    private LatLng mPrevLatLng;
    private RouteDatabaseHelper mDbHelper;

    private List<TimedDistance>  tDistance = null;
    private List<TimedLocation>  tLocation = null;
    private List<TimedDistance> base = null;

    /*
     * Initialize the Activity
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get handles to the UI view objects
        mLatLng = (TextView) findViewById(R.id.lat_lng);
        mConnectionState = (TextView) findViewById(R.id.text_connection_state);
        mConnectionStatus = (TextView) findViewById(R.id.text_connection_status);
        if (!DEBUG) {
            mLatLng.setVisibility(View.GONE);
            mConnectionState.setVisibility(View.GONE);
            mConnectionStatus.setVisibility(View.GONE);
        }
        mStartStopRecording = (Button) findViewById(R.id.start_stop_button);

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();

        /*
         * Set the update interval
         */
        mLocationRequest.setInterval(kuzki.net.exercisetracker.LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(kuzki.net.exercisetracker.LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Note that location updates are off until the user turns them on
        mUpdatesRequested = false;

        // Open Shared Preferences
        mPrefs = getSharedPreferences(kuzki.net.exercisetracker.LocationUtils.SHARED_PREFERENCES, Context.MODE_PRIVATE);

        // Get an editor
        mEditor = mPrefs.edit();

        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
        mLocationClient = new LocationClient(this, this, this);

        mDbHelper = new RouteDatabaseHelper(this);

        setUpMapIfNeeded();

        // Draw a dummy blank diagram.
        tDistance.add(new TimedDistance(0L, 0.0));
        base = tDistance;
        DrawUtil.drawChart(tDistance, base, (LinearLayout) findViewById(R.id.diagram_layout),
                MainActivity.this);

    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                MapUtil.setupMapFragment(mMap);
            }
        }
    }

    /*
     * Called when the Activity is no longer visible at all.
     * Stop updates and disconnect.
     */
    @Override
    public void onStop() {

        // If the client is connected
        if (mLocationClient.isConnected()) {
            stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        mLocationClient.disconnect();

        super.onStop();
    }
    /*
     * Called when the Activity is going into the background.
     * Parts of the UI may be visible, but the Activity is inactive.
     */
    @Override
    public void onPause() {

        // Save the current setting for updates
        mEditor.putBoolean(kuzki.net.exercisetracker.LocationUtils.KEY_UPDATES_REQUESTED, mUpdatesRequested);
        mEditor.commit();

        super.onPause();

        stopUpdates();
    }

    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */
    @Override
    public void onStart() {

        super.onStart();

        /*
         * Connect the client. Don't re-start any requests here;
         * instead, wait for onResume()
         */
        mLocationClient.connect();

    }
    /*
     * Called when the system detects that this Activity is now visible.
     */
    @Override
    public void onResume() {
        super.onResume();

        // If the app already has a setting for getting location updates, get it
        if (mPrefs.contains(kuzki.net.exercisetracker.LocationUtils.KEY_UPDATES_REQUESTED)) {
            mUpdatesRequested = mPrefs.getBoolean(kuzki.net.exercisetracker.LocationUtils.KEY_UPDATES_REQUESTED, false);

        // Otherwise, turn off location updates until requested
        } else {
            mEditor.putBoolean(kuzki.net.exercisetracker.LocationUtils.KEY_UPDATES_REQUESTED, false);
            mEditor.commit();
        }
    }

    /*
     * Handle results returned to this Activity by other Activities started with
     * startActivityForResult(). In particular, the method onConnectionFailed() in
     * LocationUpdateRemover and LocationUpdateRequester may call startResolutionForResult() to
     * start an Activity that handles Google Play services problems. The result of this
     * call returns here, to onActivityResult.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Choose what to do based on the request code
        switch (requestCode) {

            // If the request code matches the code sent in onConnectionFailed
            case kuzki.net.exercisetracker.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST :

                switch (resultCode) {
                    // If Google Play services resolved the problem
                    case Activity.RESULT_OK:

                        // Log the result
                        Log.d(kuzki.net.exercisetracker.LocationUtils.APPTAG, getString(R.string.resolved));

                        // Display the result
                        mConnectionState.setText(R.string.connected);
                        mConnectionStatus.setText(R.string.resolved);
                    break;

                    // If any other result was returned by Google Play services
                    default:
                        // Log the result
                        Log.d(kuzki.net.exercisetracker.LocationUtils.APPTAG, getString(R.string.no_resolution));

                        // Display the result
                        mConnectionState.setText(R.string.disconnected);
                        mConnectionStatus.setText(R.string.no_resolution);

                    break;
                }

            // If any other request code was received
            default:
               // Report that this Activity received an unknown requestCode
               Log.d(kuzki.net.exercisetracker.LocationUtils.APPTAG,
                       getString(R.string.unknown_activity_request_code, requestCode));

               break;
        }
    }

    /**
     * Verify that Google Play services is available before making a request.
     *
     * @return true if Google Play services is available, otherwise false
     */
    private boolean servicesConnected() {

        // Check that Google Play services is available
        int resultCode =
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            // In debug mode, log the status
            Log.d(kuzki.net.exercisetracker.LocationUtils.APPTAG, getString(R.string.play_services_available));

            // Continue
            return true;
        // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), kuzki.net.exercisetracker.LocationUtils.APPTAG);
            }
            return false;
        }
    }

    /**
     * Invoked by the "Get Location" button.
     *
     * Calls getLastLocation() to get the current location
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    public void getLocation(View v) {

        // If Google Play Services is available
        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();

            // Display the current location in the UI
            mLatLng.setText(kuzki.net.exercisetracker.LocationUtils.getLatLng(this, currentLocation));
        }
    }

    /**
     * Invoked by the "Get Address" button.
     * Get the address of the current location, using reverse geocoding. This only works if
     * a geocoding service is available.
     *
     * @param v The view object associated with this method, in this case a Button.
     */
    // For Eclipse with ADT, suppress warnings about Geocoder.isPresent()
    @SuppressLint("NewApi")
    public void getAddress(View v) {

        // In Gingerbread and later, use Geocoder.isPresent() to see if a geocoder is available.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && !Geocoder.isPresent()) {
            // No geocoder is present. Issue an error message
            Toast.makeText(this, R.string.no_geocoder_available, Toast.LENGTH_LONG).show();
            return;
        }

        if (servicesConnected()) {

            // Get the current location
            Location currentLocation = mLocationClient.getLastLocation();
        }
    }

    /**
     * Invoked by the "Start Updates" button
     * Sends a request to start location updates
     */
    private void startUpdates() {
        mUpdatesRequested = true;

        if (servicesConnected()) {
            startPeriodicUpdates();
        }
    }

    /**
     * Invoked by the "Stop Updates" button
     * Sends a request to remove location updates
     * request them.
     */
    private void stopUpdates() {
        mUpdatesRequested = false;

        if (servicesConnected()) {
            stopPeriodicUpdates();
        }
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        mConnectionStatus.setText(R.string.connected);

        if (mUpdatesRequested) {
            startPeriodicUpdates();
        }
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        mConnectionStatus.setText(R.string.disconnected);
    }


    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        /*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(
                        this,
                        kuzki.net.exercisetracker.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

                /*
                * Thrown if Google Play services canceled the original
                * PendingIntent
                */

            } catch (IntentSender.SendIntentException e) {

                // Log the error
                e.printStackTrace();
            }
        } else {

            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }

    /**
     * Report location updates to the UI.
     *
     * @param location The updated location.
     */
    @Override
    public void onLocationChanged(Location location) {

        // Report to the UI that the location was updated
        mConnectionStatus.setText(R.string.location_updated);

        if (FAKE_LOC) {
            updateFakeLocation(location);
        }

        // In the UI, set the latitude and longitude to the value received
        mLatLng.setText(kuzki.net.exercisetracker.LocationUtils.getLatLng(this, location));

        if (mStartRecording && mRecordRoute != null) {
            mRecordRoute.addPoint(location.getLatitude(), location.getLongitude());
            MapUtil.drawRoute(mMap, mRecordRoute);
            MapUtil.moveToMyLocation(mMap, location);

            tLocation.add(new TimedLocation(
                    location.getTime()/1000,
                    location.getLatitude(),
                    location.getLongitude()));
            tDistance = TimedLocation.convertToTimedDistance(tLocation);
            base = tDistance;

            DrawUtil.drawChart(tDistance, base, (LinearLayout) findViewById(R.id.diagram_layout),
                    MainActivity.this);

        }

    }

    private void updateFakeLocation(Location location) {
        if (mPrevLatLng == null) {
            mPrevLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        }
        Random r = new Random();
        double side1 = 2.5 * r.nextDouble() - 1 > 0 ? -1 : 1;
        double side2 = 2.5 * r.nextDouble() - 1 > 0 ? -1 : 1;
        location.setLatitude(mPrevLatLng.latitude + .00005d * side1);
        location.setLongitude(mPrevLatLng.longitude + .00005d * side2);
        mPrevLatLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    /**
     * In response to a request to start updates, send a request
     * to Location Services
     */
    private void startPeriodicUpdates() {

        mLocationClient.requestLocationUpdates(mLocationRequest, this);
        mConnectionState.setText(R.string.location_requested);
    }

    /**
     * In response to a request to stop updates, send a request to
     * Location Services
     */
    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
        mConnectionState.setText(R.string.location_updates_stopped);
    }

    /**
     * Show a dialog returned by Google Play services for the
     * connection error code
     *
     * @param errorCode An error code returned from onConnectionFailed
     */
    private void showErrorDialog(int errorCode) {

        // Get the error dialog from Google Play services
        Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
            errorCode,
            this,
                kuzki.net.exercisetracker.LocationUtils.CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), kuzki.net.exercisetracker.LocationUtils.APPTAG);
        }
    }

    public void onStartStopRecordingClicked(View view) {
        if (mStartRecording) {
            // Stop clicked.
            stopUpdates();
            mStartRecording = false;
            mStartStopRecording.setText(R.string.start);
            mStartStopRecording.setBackgroundColor(Color.GREEN);
            this.getRouteName();
//            mRecordRoute.name = "testRouteName";
        } else {
            // Start clicked.
            startUpdates();
            mStartRecording = true;
            mRecordRoute = new Route();
            mStartStopRecording.setText(R.string.stop);
            mStartStopRecording.setBackgroundColor(Color.RED);
            tDistance = new ArrayList<TimedDistance>();
            tLocation = new ArrayList<TimedLocation>();
            base = new ArrayList<TimedDistance>();


        }
    }

    private void addRecordedRouteToDb() {
        mDbHelper.addRoute(mRecordRoute);
    }

    private void getRouteName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(R.string.name_your_route_prompt);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mRecordRoute.name = input.getText().toString();
                addRecordedRouteToDb();
                finish();
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mRecordRoute = null;
                mMap.clear();
            }
        });

        alert.show();
    }

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        return intent;
    }

    /**
     * Define a DialogFragment to display the error dialog generated in
     * showErrorDialog.
     */
    public static class ErrorDialogFragment extends DialogFragment {

        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /**
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
