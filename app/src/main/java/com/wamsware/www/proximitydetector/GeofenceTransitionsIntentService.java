package com.wamsware.www.proximitydetector;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import static android.content.ContentValues.TAG;

/**
 * Listens for TimeLog geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService  {

    private static final int NOTIFICATION_ID = 1000;
    
    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            String errorMsg = "Location Services error: " + String.valueOf(geoFenceEvent.getErrorCode());
            Log.e(TAG, errorMsg);
        } else {

            String fenceList = "";
            for( Geofence fence : geoFenceEvent.getTriggeringGeofences()) {
                if (!fenceList.isEmpty()) {
                    fenceList += ": ";
                }
                fenceList += fence.toString();
            }

            String msg = "";
            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                msg += "Entering geofence: ";
                showToast(this,  msg);
            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                msg += "Exiting geofence: ";
            }
            msg += fenceList;
            showToast(this, msg);

            NotificationCompat.Builder notifBldr =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_apply_event)
                            .setContentTitle("Proximity Detector")
                            .setContentText(msg);

            NotificationManager notificationManager =
                    (NotificationManager) this.getSystemService(IntentService.NOTIFICATION_SERVICE);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

            notifBldr.setContentIntent(pendingIntent);

            notificationManager.notify(NOTIFICATION_ID, notifBldr.build());
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final String msg) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
