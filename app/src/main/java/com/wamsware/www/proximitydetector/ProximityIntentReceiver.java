//package com.wamsware.www.proximitydetector;
//
//import android.app.IntentService;
//import android.app.Notification;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.graphics.Color;
//import android.location.LocationManager;
//import android.support.v4.app.NotificationCompat;
//import android.util.Log;
//
//public class ProximityIntentReceiver extends BroadcastReceiver {
//
//    private static final int NOTIFICATION_ID = 1000;
//
//    static int counter = 0;
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//
//        Boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
//
//        String message = "";
//        if (entering) {
//            message = "You are moving nearer to your point of interest.";
//        }
//        else {
//            message = "You are away from your point of interest.";
//        }
//
//        counter++;
//
//        NotificationCompat.Builder notifBldr =
//                new NotificationCompat.Builder(context)
//                        .setSmallIcon(R.drawable.ic_apply_event)
//                        .setContentTitle("Proximity Detector")
//                        .setContentText(message + " " + String.valueOf(counter));
//
//        NotificationManager notificationManager =
//                (NotificationManager) context.getSystemService(IntentService.NOTIFICATION_SERVICE);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_ONE_SHOT);
//
//        notifBldr.setContentIntent(pendingIntent);
//
//        notificationManager.notify(NOTIFICATION_ID, notifBldr.build());
//
//    }
//
//
//}
