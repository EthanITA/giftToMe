package com.gifttome.gifttome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    //da settare lo userid
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    private String username;

    private Integer Userid = 1;

    public Integer getUserid() {
        return Userid;
    }

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private TabsAccessAdapter myTabsAccessorAdapter;
    private String bearerToken;
    private ArrayList<AvailableObjectsData> myPosts = new ArrayList<>();

    public ArrayList<AvailableObjectsData> getMyPosts() {
        return myPosts;
    }

    public void addToMyPosts(AvailableObjectsData myPost) {
        this.myPosts.add(myPost);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private ArrayList<AvailableObjectsData> availablePosts = new ArrayList<AvailableObjectsData>();

    //messages i ges
    //private ArrayList<messages>;

    /*public String getBearerToken() {
        return bearerToken;
    }

     */
    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    //location updates
    private LocationRequest locationRequestMain;
    private FusedLocationProviderClient fusedLocationClientMain;
    private LocationCallback locationCallback;
    Location lastLocationMain;

    //geofencing
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    LinkedList<Geofence> geofenceList = new LinkedList<>();


    //nofification
    NotificationManagerCompat notificationManagerCompat;
    int notificationId = 0;
    FrameLayout fragmentContainer;
    Intent notificatioIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        String sharedPrefUsername = sharedPreferences.getString("username", null);
        if(sharedPrefUsername == null){
           /*
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LogInFragment logInFragment = new LogInFragment();

            fragmentTransaction.add(R.id.fragment_container, logInFragment);
            fragmentTransaction.commit();

            */


           Intent loginIntent = new Intent(this, LoginActivity.class);
           startActivity(loginIntent);
        }
        else {
            username = sharedPrefUsername;
            Log.i("sharedPUsernameMainAc", "onCreate: ");
            System.setProperty("twitter4j.oauth.consumerKey", "fud09hdnKuTT7PtYNuCZn2tRV");
            System.setProperty("twitter4j.oauth.consumerSecret", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp");
            System.setProperty("twitter4j.oauth.accessToken", "1271353616847245315-Ru2rzisv9JsFyYglrOjdwN6zBTmlFC");
            System.setProperty("twitter4j.oauth.accessTokenSecret", "AYbNR5QC1pSOxXZHIDLnuiio0X3car8tdSZHVS8dZVvQe");
            System.setProperty("twitter4j.debug", "true");

            //availablePosts.add(new AvailableObjectsData(41.8719, 12.5674));
            //availablePosts.add(new AvailableObjectsData(36.6772, -122.5022));
       /* mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("GiftToMe");
        */
            geofencingClient = LocationServices.getGeofencingClient(this);

            fragmentContainer = findViewById(R.id.fragment_container);

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            MainFragment mainFragment = new MainFragment();

            Bundle bundle = new Bundle();
            bundle.putString("username", username);
            mainFragment.setArguments(bundle);

            fragmentTransaction.add(R.id.fragment_container, mainFragment);
            fragmentTransaction.commit();

            fusedLocationClientMain = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Log.v("last location is null", "F");

                        return;
                    }
                    lastLocationMain = locationResult.getLastLocation();
                }
            };

            //sendNotification();
            makeLocationRequest();
            startLocationUpdates();

            //generateGeofencesForAvailableObjects();
        }

    }

    protected void makeLocationRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize location requests here.
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                0x1);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });

    }


    public LocationRequest createLocationRequest() {
        locationRequestMain = LocationRequest.create();
        locationRequestMain.setInterval(1000);
        //locationRequest.setFastestInterval(5000);
        locationRequestMain.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequestMain;
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
        fusedLocationClientMain.requestLocationUpdates(locationRequestMain,
                locationCallback,
                Looper.getMainLooper());
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stopLocationUpdates();

    }

    private void stopLocationUpdates() {
        if (fusedLocationClientMain != null)
            fusedLocationClientMain.removeLocationUpdates(locationCallback);
    }

    public void sendNotification() {
        //tap intent
        Intent notificatioIntent = new Intent(this, MainActivity.class);

        notificatioIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificatioIntent, 0);

        //create notification
        notificationManagerCompat = NotificationManagerCompat.from(this);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getString(R.string.CHANNEL_ID));
        mBuilder.setContentTitle("Test Notification")
                .setContentText("Test Notification Text")
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        //send notification
        notificationManagerCompat.notify(notificationId, mBuilder.build());
        notificationId++;

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        createNotificationChannel();
    }

    public PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent geoIntent = new Intent(this, GeofenceBroadcastReceiver.class);
        Gson gson = new Gson();
        String jsonlist = gson.toJson(availablePosts);
        geoIntent.putExtra("jsonlist", jsonlist);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, geoIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        return geofencePendingIntent;
    }

    public void generateGeofencesForAvailableObjects() {
        geofenceList.clear();
        //genero geofence per ogni oggetto
        for (int j = 0; j < availablePosts.size(); j++) {
            buildGeofence(j);
        }
        Log.i("gengeof", "generateGeofencesForAvailableObjects: starting geofencing");
        startGeofencing();
    }

    //crea i singoli geofence
    public void buildGeofence(int position) {
        geofenceList.add(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId(String.valueOf(position))

                .setCircularRegion(
                        availablePosts.get(position).getLat(),
                        availablePosts.get(position).getLon(),
                        200
                )
                .setExpirationDuration(R.string.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
        Log.i("lonlat", "buildGeofence: lat " + availablePosts.get(position).getLat() + " lon: " + availablePosts.get(position).getLon());
    }

    //forma la richiesta
    public GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    public void startGeofencing() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("addOnSuccessListener", "geofence added successfully");

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("addOnFailureListener", "Failure to add geofence");
                    }
                });
    }

    //to stop geofencing
    //geofencingClient.removeGeofences(getGeofencePendingIntent())

    public void addNewAvailablePost(ArrayList<AvailableObjectsData> avObjects){
        Log.i("addNewAvailablePost", "avObjects size is "+ avObjects.size());
        availablePosts.clear();
        availablePosts.addAll(avObjects);
        Log.i("addNewAvailablePost", "availablePosts size is "+ availablePosts.size());

        generateGeofencesForAvailableObjects();
    }
}
