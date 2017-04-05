package com.example.test.googlemapdemo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;

import java.util.List;

import static android.content.ContentValues.TAG;



public class GeoFenceService extends IntentService {
    public String mNotificationResult;
    public static int myNotification_ID = 0;
    public GeoFenceService(String name) {
        super(name);
    }

    //Intent resultIntent = new Intent(this,MyLocationService.class);
    public GeoFenceService()
    {
        this(GeoFenceService.class.getName());

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent event = GeofencingEvent.fromIntent(intent);
        if(event.hasError())
        {
            //Handle Error
        }
        else {
            int transition = event.getGeofenceTransition();
            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);
            String requestId = geofence.getRequestId();

            NotificationManager notificationManager = null;
            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                //Display Notification
                Intent notificationIntent = new Intent();
                PendingIntent notificationPendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);
                Notification notification = new Notification.Builder(this).
                        setTicker("Ticker Title")
                        .setContentTitle("Notification From RemindMe")
                        .setContentText("Location reached")
                        .setSmallIcon(R.drawable.ic_stat_name)
                        .setContentIntent(notificationPendingIntent).getNotification();

                notification.flags = Notification.FLAG_AUTO_CANCEL;
                notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                //notification.defaults |= Notification.DEFAULT_SOUND;
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                if(alarmSound == null){
                    alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                    if(alarmSound == null){
                        alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    }
                }
                notification.sound = alarmSound;
                notificationManager.notify(myNotification_ID, notification);

                Log.d(TAG, "Entering Goefence region" + requestId);
                mNotificationResult = "Inside";
                // createIntent(mNotificationResult);
            } else if (transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                //Display exit notification
                Log.d(TAG, "Exiting Geofence Region" + requestId);
                mNotificationResult = "Outside";
                notificationManager.cancel(myNotification_ID);

                // createIntent(mNotificationResult);
            }
//            resultIntent.putExtra("result",mNotificationResult);
//            startService(resultIntent);

        }

    }

//    public void createIntent(String locationResult)
//
//    {
//        Intent intent = new Intent("com.example.test.googlemapdemo");
//        if(locationResult == "Inside")
//        {
//            intent.putExtra("result",locationResult);
//            this.sendBroadcast(intent);
//        }
//        else
//        {
//            intent.putExtra("result",locationResult);
//            this.sendBroadcast(intent);
//        }
//    }
}
