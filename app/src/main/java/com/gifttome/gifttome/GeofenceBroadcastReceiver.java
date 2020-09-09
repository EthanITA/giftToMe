package com.gifttome.gifttome;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    //nofification
    NotificationManagerCompat notificationManagerCompat;
    int notificationId = 1000;
    Intent notificationIntent;

    ArrayList<AvailableObjectsData> postsList = new ArrayList<>();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("intentreceived!", "onReceive: received ");
        createNotificationChannel(context);
        Bundle intentBundle = intent.getExtras();
        if (intentBundle != null) {
            Log.i("insdintntbundle", "onReceive: " + intentBundle.get("jsonlist"));
            Log.i("insdintntbundle", "onReceive2: "+ intentBundle.get("oblist"));
            Log.i("insdintntbundle", "onReceive3: "+ (ArrayList<AvailableObjectsData>)intentBundle.get("oblist"));
            Gson gson = new Gson();
            String json = intentBundle.getString("jsonlist");
            if(json != null) {
                Log.i("arraylistdabrodrec", json);
                Type type = new TypeToken<ArrayList<AvailableObjectsData>>() {
                }.getType();
                postsList = gson.fromJson(json, type);
            }
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e("geofencingEventError", errorMessage);
            return;
        }
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Transizione di entrata nell'area del geofence
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            // Geofences attivati, invio notifica
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            for (Geofence geofence : triggeringGeofences) {
                Log.i("trigggeofence", geofence.getRequestId());
                sendNotification(context, Integer.parseInt(geofence.getRequestId()));
            }
            Log.i("geofencinglocation", String.valueOf(geofencingEvent.getTriggeringLocation()));
        }
        else {
            // Log the error.
            Log.e("Geofencetransition","error");
        }
    }


    public void sendNotification(Context context, int id){
        //tap intent todo
        notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        //create notification
        notificationManagerCompat = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(R.string.CHANNEL_ID));
        mBuilder.setContentTitle("Sei vicino ad un oggetto!: " + postsList.get(id).getName())
                .setContentText(postsList.get(id).getLat()+ ": " + postsList.get(id).getLon())
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //send notification
        notificationManagerCompat.notify(notificationId, mBuilder.build());
        notificationId++;
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = String.valueOf(R.string.channel_name);
            String description = String.valueOf(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(String.valueOf(R.string.CHANNEL_ID), name, importance);
            Log.i("Channel_ID in broad rec", String.valueOf(R.string.CHANNEL_ID));
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}