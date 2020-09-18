package com.gifttome.gifttome;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
    private String username;

    public ArrayList<AvailableObjectsData> getAvailablePosts() {
        return availablePosts;
    }

    private ArrayList<AvailableObjectsData> availablePosts = new ArrayList<>();
    private ArrayList<AvailableObjectsData> myPosts = new ArrayList<>();

    //private ArrayList<Reply> repliesToMyPosts = new ArrayList<>();
    private ArrayList<String> myRepliesUUID = new ArrayList<>();
    private Intent checkRepliesIntent;
    public ArrayList<AvailableObjectsData> getMyPosts() {
        return myPosts;
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

    private UserTimeline userTimeline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nav_dra);
        //setContentView(R.layout.activity_main);

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
/*
        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", null);
        editor.apply();
 */

        if(username != null) {
        initialize();

        makeLocationRequest();
        startLocationUpdates();

        StartCheckingForRepliesInBackground();
        }


    }


    private void initialize() {

        initializeTwitter();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_availableposts, R.id.nav_myposts, R.id.nav_replies_to_me, R.id.nav_my_replies)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        geofencingClient = LocationServices.getGeofencingClient(this);

        fusedLocationClientMain = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                lastLocationMain = locationResult.getLastLocation();
            }
        };
    }

    private void initializeTwitter() {
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getResources().getString(R.string.Consumer_key), getResources().getString(R.string.CONSUMER_SECRET));
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(authConfig)
                .debug(true)
                .build();
        com.twitter.sdk.android.core.Twitter.initialize(config);

        System.setProperty("twitter4j.oauth.consumerKey", "fud09hdnKuTT7PtYNuCZn2tRV");
        System.setProperty("twitter4j.oauth.consumerSecret", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp");
        System.setProperty("twitter4j.oauth.accessToken", "1271353616847245315-Ru2rzisv9JsFyYglrOjdwN6zBTmlFC");
        System.setProperty("twitter4j.oauth.accessTokenSecret", "AYbNR5QC1pSOxXZHIDLnuiio0X3car8tdSZHVS8dZVvQe");
        System.setProperty("twitter4j.debug", "true");
    }


    protected void makeLocationRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest(1500));

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MainActivity.this,
                            0x1);
                } catch (IntentSender.SendIntentException sendEx) {
                    Toast.makeText(getApplicationContext(), "ricerca della posizione non supportata", Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    public LocationRequest createLocationRequest(long intervalTime) {
        locationRequestMain = LocationRequest.create();
        locationRequestMain.setInterval(intervalTime);
        locationRequestMain.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequestMain;
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permission = new String[0];
            permission[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this, permission,1);

        }
        fusedLocationClientMain.requestLocationUpdates(locationRequestMain,
                locationCallback,
                Looper.getMainLooper());
    }



    public PendingIntent getGeofencePendingIntent(ArrayList<AvailableObjectsData> arr) {
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent geoIntent = new Intent(this, GeofenceBroadcastReceiver.class);
        Gson gson = new Gson();
        String jsonlist = gson.toJson(arr);

        geoIntent.putExtra("jsonlist", jsonlist);
        //geoIntent.putExtra("oblist", arr);

        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, geoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public void generateGeofencesForAvailableObjects() {
        geofenceList.clear();
        //genero geofence per ogni oggetto
        for (int j = 0; j < availablePosts.size(); j++) {
            buildGeofence(j);
        }
        startGeofencing();
    }

    //crea i singoli geofence
    public void buildGeofence(int position) {
        geofenceList.add(new Geofence.Builder()
                .setRequestId(String.valueOf(position))
                .setCircularRegion(
                        availablePosts.get(position).getLat(),
                        availablePosts.get(position).getLon(),
                        200
                )
                .setExpirationDuration(R.string.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build());
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
            String[] permission = new String[0];
            permission[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            ActivityCompat.requestPermissions(this, permission,1);
        }
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(availablePosts))
                .addOnSuccessListener(this, aVoid -> Log.i("addOnSuccessListener", "geofence added successfully"))
                .addOnFailureListener(this, e -> Log.e("addOnFailureListener", "Failure to add geofence"));
    }

    //to stop geofencing
    //geofencingClient.removeGeofences(getGeofencePendingIntent())

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void addNewAvailablePost(ArrayList<AvailableObjectsData> avObjects){
        geofencePendingIntent = null;
        if(geofencingClient != null)
        geofencingClient.removeGeofences(getGeofencePendingIntent(avObjects));
        geofenceList.clear();
        availablePosts.clear();
        availablePosts.addAll(avObjects);
        generateGeofencesForAvailableObjects();
    }


    private void StartCheckingForRepliesInBackground() {
        //prendo i miei post e le risposte che ho già ricevuto, così da poter filtrare per nuove risposte
        getMyPostsTweets();
    }

    private void getMyPostsTweets() {
        userTimeline = new UserTimeline.Builder()
                .screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callbackMyPosts);
    }

    Callback<TimelineResult<Tweet>> callbackMyPosts = new Callback<TimelineResult<Tweet>>()
    {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            myPosts.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text;
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String username1 = jsonObject.get("issuer").toString();

                    if (username.equals(username1)){
                        String name1 = jsonObject.get("name").toString();

                        UUID id1 = UUID.fromString(jsonObject.get("id").toString());
                        String category = jsonObject.get("category").toString();
                        double lat = Double.parseDouble(jsonObject.get("lat").toString());
                        double lon = Double.parseDouble(jsonObject.get("lon").toString());
                        String description = jsonObject.get("description").toString();

                        AvailableObjectsData myPost = new AvailableObjectsData(name1, username1, id1 , category, lat, lon, description);
                        myPost.setTwitterId(tweet.getId());
                        myPosts.add(myPost);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //prendo anche le risposte
            getRepliesUUiIDS();

        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error)
        {
            Log.e("TAG","Error");
            Toast.makeText(getApplication(), "errore, controllare la connessione ad internet", Toast.LENGTH_SHORT).show();

        }
    };


    private void getRepliesUUiIDS() {
        userTimeline = new UserTimeline.Builder().screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callbackreplies);

    }

    Callback<TimelineResult<Tweet>> callbackreplies = new Callback<TimelineResult<Tweet>>() {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult) {
            List<Tweet> tweets = searchResult.data.items;
            myRepliesUUID.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text;

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;

                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String receiver1 = jsonObject.get("receiver").toString();

                    if (receiver1.equals(username)) {
                        myRepliesUUID.add(jsonObject.get("id").toString());
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            startCheckingForReplies();
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error) {
            Log.e("TAG", "Error");
            Toast.makeText(getApplication(), "problema di connessione", Toast.LENGTH_SHORT).show();
        }
    };

    public void startCheckingForReplies(){
        checkRepliesIntent = new Intent(this, BackgroundRepliesService.class);
        Gson gson = new Gson();
        String jsonmyPosts = gson.toJson(myPosts);
        String jsonRepliesuuid = gson.toJson(myRepliesUUID);
        checkRepliesIntent.putExtra("myPosts", jsonmyPosts);
        checkRepliesIntent.putExtra("repliesUUID", jsonRepliesuuid);
        checkRepliesIntent.putExtra("username", username);

        startService(checkRepliesIntent);
    }


    public void addMyRepliesUUID(String myRepliesUUID) {
        this.myRepliesUUID.add(myRepliesUUID);
        stopService(checkRepliesIntent);
        startCheckingForReplies();
    }

    public void addToMyPosts(ArrayList<AvailableObjectsData> actualMyPosts){
        myPosts.clear();
        myPosts.addAll(actualMyPosts);
        stopService(checkRepliesIntent);
        startCheckingForReplies();

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}