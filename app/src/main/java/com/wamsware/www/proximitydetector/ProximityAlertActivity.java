package com.wamsware.www.proximitydetector;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
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

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.gms.location.Geofence.NEVER_EXPIRE;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES;
import static com.google.android.gms.location.GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS;


public class ProximityAlertActivity extends AppCompatActivity {

    public static final int REQUEST_CHECK_SETTINGS = 1002; // Check permissions from location request
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    private static final int GEOFENCE_RADIUS_IN_METERS = 10;

    protected LocationRequest mLocationRequest = null;
    private GeofencingClient mGeofencingClient;
    private FusedLocationProviderClient mFusedLocationClient;

    private LocationCallback mLocationCallback;

    private boolean mRequestingLocationUpdates = false;

    private TextView mLattitudeTextView;
    private TextView mLongitudeTextView;

    private double mLattitude;
    private double mLongitude;

    private Button mEnableGeofenceButton;
    private Button mDisableGeofenceButton;

    private List<Geofence> mGeofenceList;

    private ProximityAlertActivity mThis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        updateValuesFromBundle(savedInstanceState);

        // In this prototype, always request location updates
        // ... control the flag as needed in a real implementation to
        // help preserve battery life
        mRequestingLocationUpdates = true;

        mThis = this;

        setContentView(R.layout.activity_main);

        // Leads to onRequestPermissionsResult() with PERMISSIONS_REQUEST_FINE_LOCAIONT
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSIONS_REQUEST_FINE_LOCATION);

        mGeofenceList = new ArrayList<>();

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mFusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    // ...
                    mLattitude = location.getLatitude();
                    mLongitude = location.getLongitude();
                    mLattitudeTextView.setText(String.valueOf(mLattitude));
                    mLongitudeTextView.setText(String.valueOf(mLongitude));
                }
            }
        };

        mLattitudeTextView = findViewById(R.id.point_latitude);
        mLongitudeTextView = findViewById(R.id.point_longitude);

        mEnableGeofenceButton = findViewById(R.id.button_enable_geofence_monitoring);
        mDisableGeofenceButton = findViewById(R.id.button_disable_geofence_monitoring);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }

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

                    mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                            .addOnSuccessListener(mThis, new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Geofences added
                                    // ...
                                }
                            })
                            .addOnFailureListener(mThis, new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Failed to add geofences
                                    // ...
                                }
                            });
                }
            });
        }

        if (!mDisableGeofenceButton.hasOnClickListeners()) {
            mDisableGeofenceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mGeofencingClient.removeGeofences(getGeofencePendingIntent())
                        .addOnSuccessListener(mThis, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                // Geofences removed
                                // ...
                            }
                        })
                        .addOnFailureListener(mThis, new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Failed to remove geofences
                                // ...
                            }
                        });
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(getString(
                R.string.instanceStateRequestingLocationUpdates),
                mRequestingLocationUpdates);

        super.onSaveInstanceState(outState);
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }

        String requestingLocationUpdatesKey =
                getString(R.string.instanceStateRequestingLocationUpdates);

        // Update the value of mRequestingLocationUpdates from the Bundle.
        if (savedInstanceState.keySet().contains(requestingLocationUpdatesKey)) {
            mRequestingLocationUpdates = savedInstanceState.getBoolean(
                    requestingLocationUpdatesKey);
        }

        // Update UI to match restored state
        updateUI();
    }

    private void updateUI() {

    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
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
                } else {
                    // Disable the functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case REQUEST_CHECK_SETTINGS:
                if (resultCode == AppCompatActivity.RESULT_OK) {

                }
                break;

            default:
                break;
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (mLocationRequest == null) {
            mLocationRequest = new LocationRequest();
        }

        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private PendingIntent getGeofencePendingIntent() {

        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }
}
