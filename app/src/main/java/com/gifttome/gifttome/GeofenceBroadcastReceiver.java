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
import android.widget.FrameLayout;

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
    int notificationId = 0;
    FrameLayout fragmentContainer;
    Intent notificatioIntent;

    ArrayList<AvailableObjectsData> postsList = new ArrayList<>();
    @Override
    public void onReceive(Context context, Intent intent) {
        createNotificationChannel(context);
        Bundle intentBundle = intent.getExtras();
        if (intentBundle != null) {
            intentBundle.get("jsonlist");
            Gson gson = new Gson();
            String json = intentBundle.getString("jsonlist");
            Log.i("arraylist da brod rec", json);
            Type type = new TypeToken<ArrayList<AvailableObjectsData>>() {}.getType();
            postsList = gson.fromJson(json, type);
        }
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
            if (geofencingEvent.hasError()) {
                String errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.getErrorCode());
                Log.e("geofencingEvent Error", errorMessage);
                return;
            }

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

                // Get the geofences that were triggered. A single event can trigger
                // multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
                // Get the transition details as a String.
                /*String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        this,
                        geofenceTransition,
                        triggeringGeofences
                );
                 */
                for (Geofence geofence :
                        triggeringGeofences) {

                    Log.i("geofence", geofence.getRequestId().toString());

                    sendNotification(context, Integer.parseInt(geofence.getRequestId()));
                }
                // Send notification and log the transition details.
                //sendNotification(geofenceTransitionDetails);
                Log.i("geofencing location", String.valueOf(geofencingEvent.getTriggeringLocation()));
            } else {
                // Log the error.
                Log.e("Geofence transition","error");
            }
        }


    public void sendNotification(Context context, int id){
        //tap intent
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        //create notification
        notificationManagerCompat = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(R.string.CHANNEL_ID));
        mBuilder.setContentTitle("Sei vicino ad un oggetto!")
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
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = String.valueOf(R.string.channel_name);
            String description = String.valueOf(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(String.valueOf(R.string.CHANNEL_ID), name, importance);
            Log.i("Channel_ID in broad rec", String.valueOf(R.string.CHANNEL_ID));
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    }
