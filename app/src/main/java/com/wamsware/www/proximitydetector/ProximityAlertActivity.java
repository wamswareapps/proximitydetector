package com.wamsware.www.proximitydetector;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS;


public class ProximityAlertActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks,
        com.google.android.gms.location.LocationListener,
        ResultCallback {

    public static final int REQUEST_GOOGLE_SERVICES_CONNECT_RESOLVE_ERROR = 1001;
    public static final int REQUEST_CHECK_SETTINGS = 1002; // Check permissions from location request
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int GEOFENCE_RADIUS_IN_METERS = 100;
//    private static final int GEOFENCE_EXPIRATION_IN_MILLISECONDS = 3000000; // 5 minutes
    private static final int GEOFENCE_LOITERING_DELAY_MILLISECONDS = 500;
    private static final int GEOFENCE_NOTFICATION_RESPONSE_MILLISECONDS = 500;

    protected LocationRequest mLocationRequest = null;
    protected GoogleApiClient mGoogleApiClient = null;

    private TextView mLattitudeTextView;
    private TextView mLongitudeTextView;

    private double mLattitude;
    private double mLongitude;

    private Button mEnableGeofenceButton;
    private Button mDisableGeofenceButton;

    private List<Geofence> mGeofenceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mGeofenceList = new ArrayList<Geofence>();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLattitudeTextView = (TextView) findViewById(R.id.point_latitude);
        mLongitudeTextView = (TextView )findViewById(R.id.point_longitude);

        mEnableGeofenceButton = (Button)findViewById(R.id.button_enable_geofence_monitoring);
        mDisableGeofenceButton = (Button)findViewById(R.id.button_disable_geofence_monitoring);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!mEnableGeofenceButton.hasOnClickListeners()) {
            mEnableGeofenceButton.setOnClickListener(new View.OnClickListener() {
                @SuppressWarnings("MissingPermission")
                @Override
                public void onClick(View v) {

                    // TODO:  Manage a list of geofences
                    String GEOFENCE_ID_1 = "GEOFENCE_ID_1";

                    mGeofenceList.add(new Geofence.Builder()
                            // Set the request ID of the geofence. This is a string to identify this
                            // geofence.
                            .setRequestId(GEOFENCE_ID_1)
                            .setExpirationDuration(NEVER_EXPIRE)
                            .setCircularRegion(mLattitude, mLongitude, GEOFENCE_RADIUS_IN_METERS)
                            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                                                    Geofence.GEOFENCE_TRANSITION_EXIT)
                            .build());

                    LocationServices.GeofencingApi.addGeofences(
                            mGoogleApiClient,
                            getGeofencingRequest(),
                            getGeofencePendingIntent()
                    ).setResultCallback(ProximityAlertActivity.this);
                }
            });
        }

        if (!mDisableGeofenceButton.hasOnClickListeners()) {
            mDisableGeofenceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LocationServices.GeofencingApi.removeGeofences(
                            mGoogleApiClient,
                            // This is the same pending intent that was used in addGeofences().
                            getGeofencePendingIntent()
                    ).setResultCallback(ProximityAlertActivity.this); // Result processed in onResult().
                }
            });
        }

        if (mGoogleApiClient != null) {

            if (!mGoogleApiClient.isConnecting()) {

                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                } else {
                    setupLocationManagement();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onConnected(Bundle dataBundle) {

        // Leads to onRequestPermissionsResult() with PERMISSIONS_REQUEST_FINE_LOCAIONT
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {

            // Arrives here after ACCESS_FINE_LOCATION permission is granted or rejected
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupLocationManagement();
                } else {

                    // Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(
                        this, ProximityAlertActivity.REQUEST_GOOGLE_SERVICES_CONNECT_RESOLVE_ERROR);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mLattitude = location.getLatitude();
        mLongitude = location.getLongitude();

        mLongitudeTextView.setText(String.valueOf(mLongitude));
        mLattitudeTextView.setText(String.valueOf(mLattitude));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode) {

            case REQUEST_CHECK_SETTINGS:
                if (resultCode == AppCompatActivity.RESULT_OK) {

                }
                break;

            case REQUEST_GOOGLE_SERVICES_CONNECT_RESOLVE_ERROR:
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;

            default:
                break;
        }
    }

    @SuppressWarnings("MissingPermission")
    private void setupLocationManagement() {

        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
        }

        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, ProximityAlertActivity.this);
    }

    private PendingIntent getGeofencePendingIntent() {
//        // Reuse the PendingIntent if we already have it.
//        if (mGeofencePendingIntent != null) {
//            return mGeofencePendingIntent;
//        }

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    //
    // Handle results from geofences API
    //
    @Override
    public void onResult(@NonNull Result result) {

        if (!result.getStatus().isSuccess()) {


            String msg = "Error: " + result.getStatus().getStatusMessage();
            Toast.makeText(this, msg, Toast.LENGTH_SHORT);

            // TODO: Add more detail to error notifications
            switch (result.getStatus().getStatusCode()) {
                case GEOFENCE_NOT_AVAILABLE:
                    break;
                case GEOFENCE_TOO_MANY_GEOFENCES:
                    break;
                case GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    break;
            }
        }
        else {
            Toast.makeText(this, "Geofence successfully added", Toast.LENGTH_SHORT);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}
