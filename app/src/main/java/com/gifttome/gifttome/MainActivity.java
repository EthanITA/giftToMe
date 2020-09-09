package com.gifttome.gifttome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

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
import android.widget.Toast;

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
import com.google.gson.reflect.TypeToken;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class MainActivity extends AppCompatActivity {
    boolean notified = false;

    private AppBarConfiguration mAppBarConfiguration;
    private int notificationId = 1000;
    ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);
    private String username;
    private ArrayList<AvailableObjectsData> availablePosts = new ArrayList<>();
    private ArrayList<AvailableObjectsData> myPosts = new ArrayList<>();
    private ArrayList<Reply> repliesToMyPosts = new ArrayList<>();
    private ArrayList<String> myRepliesUUID = new ArrayList<>();
    private Intent checkRepliesIntent;


    public void addMyRepliesUUID(String myRepliesUUID) {
        this.myRepliesUUID.add(myRepliesUUID);
        Log.i("jhdon", "dentro add replies: ");
        stopService(checkRepliesIntent);
        startCheckingForReplies();
    }

    public ArrayList<AvailableObjectsData> getMyPosts() {
        return myPosts;
    }

    public void addToMyPosts(ArrayList<AvailableObjectsData> actualMyPosts){
        myPosts.clear();
        Log.i("jhdon", "dentro add to my posts: ");
        Log.i("jhdon", "addToMyPosts: " + actualMyPosts.size());
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
        TwitterAuthConfig authConfig = new TwitterAuthConfig(getResources().getString(R.string.Consumer_key), getResources().getString(R.string.CONSUMER_SECRET));
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(authConfig)
                .debug(true)
                .build();
        com.twitter.sdk.android.core.Twitter.initialize(config);
        //Twitter twitter = TwitterFactory.getSingleton();

        //setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main_nav_dra);

        SharedPreferences sharedPreferences = getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
/*
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            LogInFragment logInFragment = new LogInFragment();

            fragmentTransaction.add(R.id.fragment_container, logInFragment);
            fragmentTransaction.commit();

 */
        // Intent loginIntent = new Intent(this, LoginActivity.class);
        //startActivity(loginIntent);

            /*
            SharedPreferences shaPreferences = this.getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("username", null);
            editor.apply();

             */
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

        System.setProperty("twitter4j.oauth.consumerKey", "fud09hdnKuTT7PtYNuCZn2tRV");
        System.setProperty("twitter4j.oauth.consumerSecret", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp");
        System.setProperty("twitter4j.oauth.accessToken", "1271353616847245315-Ru2rzisv9JsFyYglrOjdwN6zBTmlFC");
        System.setProperty("twitter4j.oauth.accessTokenSecret", "AYbNR5QC1pSOxXZHIDLnuiio0X3car8tdSZHVS8dZVvQe");
        System.setProperty("twitter4j.debug", "true");

        geofencingClient = LocationServices.getGeofencingClient(this);

        fusedLocationClientMain = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.v("lastlocationisnull", "F");
                    return;
                }
                lastLocationMain = locationResult.getLastLocation();
            }
        };


        //WorkRequest checkRepliesWorkRequest = new PeriodicWorkRequest.Builder(checkRepliesWorker.class, 15, TimeUnit.MINUTES).build();
        //WorkManager.getInstance(getApplication()).enqueue(checkRepliesWorkRequest);

        makeLocationRequest();
        startLocationUpdates();

        getMyPostsTweets();

        Log.i("mdifua", "onCreate:reptomyposts "+ myRepliesUUID.size());
        Log.i("mdifua", "onCreate:mposts "+ myPosts.size());

        //Gson gson = new Gson();
        //String  = gson.toJson(availablePosts);

/*
        Intent checkRepliesIntent = new Intent(this, CheckForReplies.class);

        checkRepliesIntent.putExtra("myPosts", myPosts);
        checkRepliesIntent.putExtra("repliesUUID", repliesToMyPostsUUID);
        CheckForReplies.enqueueWork(this, checkRepliesIntent);

 */


    }

    protected void makeLocationRequest() {

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(createLocationRequest(1500));

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, locationSettingsResponse -> {
            // All location settings are satisfied. The client can initialize location requests here.
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                // Location settings are not satisfied
                try {
                    // Show the dialog by calling startResolutionForResult() to fix them,
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

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //createNotificationChannel();
    }

    public PendingIntent getGeofencePendingIntent(ArrayList<AvailableObjectsData> arr) {
        if (geofencePendingIntent != null) {
            Log.i("availablePostssize", String.valueOf(arr.size()));
            Log.i("availablePostssize", String.valueOf(availablePosts.size()));

            return geofencePendingIntent;
        }
        Log.i("availablePostssize1", String.valueOf(arr.size()));
        Log.i("availablePostssize1", String.valueOf(availablePosts.size()));

        Intent geoIntent = new Intent(this, GeofenceBroadcastReceiver.class);
        Gson gson = new Gson();
        String jsonlist = gson.toJson(arr);
        Log.i("availablePostssize3", String.valueOf(availablePosts.size()));
        Log.i("availablePostssize3", String.valueOf(arr.size()));

        geoIntent.putExtra("jsonlist", jsonlist);
        //geoIntent.putExtra("oblist", arr);
        Log.i("availablePostssize4", String.valueOf(availablePosts.size()));
        Log.i("availablePostssize4", String.valueOf(arr.size()));

        geofencePendingIntent = PendingIntent.getBroadcast(this, 0, geoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    public void generateGeofencesForAvailableObjects() {
        geofenceList.clear();
        //genero geofence per ogni oggetto
        Log.i("creddqua", "generateGeofencesForAvailableObjects: "+  availablePosts.size());
        for (int j = 0; j < availablePosts.size(); j++) {
            buildGeofence(j);
            Log.i("fpqufn", "generateGeofencesForAvailableObjects: " + j);
        }
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
        Log.i("startgeofencing", "addNewAvailablePost: "+ availablePosts.size());

        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent(availablePosts))
                .addOnSuccessListener(this, aVoid -> Log.i("addOnSuccessListener", "geofence added successfully"))
                .addOnFailureListener(this, e -> Log.e("addOnFailureListener", "Failure to add geofence"));
    }
    //todo
    //to stop geofencing
    //geofencingClient.removeGeofences(getGeofencePendingIntent())

    public void addNewAvailablePost(ArrayList<AvailableObjectsData> avObjects){
        geofencePendingIntent = null;
        geofencingClient.removeGeofences(getGeofencePendingIntent(avObjects));
        geofenceList.clear();
        availablePosts.clear();
        Log.i("MainANAP", "addNewAvailablePost: "+availablePosts.addAll(avObjects));

        Log.i("creddMainANAP", "addNewAvailablePost: "+ availablePosts.size());
        generateGeofencesForAvailableObjects();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public static void sendNotification(Context context, AvailableObjectsData myNotifiedPost,
                                        String replyText){
        //tap intent todo
        int notificationId = 1;
        Log.i("insdhn", "dentro sendNotification");

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, 0);

        //create notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(R.string.CHANNEL_ID));
        mBuilder.setContentTitle("Hai ricevuto un nuovo messaggio per l'oggetto " + myNotifiedPost.getName() + "!")
                .setContentText(replyText)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        //send notification
        notificationManagerCompat.notify(notificationId, mBuilder.build());
        notificationId++;
    }

   /* public void responseNotification(){
        executorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                // do stuff
                Log.i("insdthrpllsch", "tictoc: ");
                checkForRepliesTwitter(getApplicationContext(), availablePosts, repliesToMyPostsUUID);

                runOnUiThread(new Runnable() {
                    public void run() {
                        // update your UI component here.

                    }
                });
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
    public static class checkRepliesWorker extends Worker {
        public checkRepliesWorker(
                @NonNull Context context,
                @NonNull WorkerParameters params) {
            super(context, params);
        }

        @NotNull
        @Override
        public Result doWork() {

            // Do the work here--
            Log.i("insd", "dentro checkrepliesworker dowork: ");
            //checkForRepliesTwitter();

            // Indicate whether the work finished successfully with the Result
            return Result.success();
        }
    }*/


    public void startCheckingForReplies(){
        checkRepliesIntent = new Intent(this, BackgroundRepliesService.class);
        Gson gson = new Gson();
        String jsonmyPosts = gson.toJson(myPosts);
        String jsonRepliesuuid = gson.toJson(myRepliesUUID);
        checkRepliesIntent.putExtra("myPosts", jsonmyPosts);
        checkRepliesIntent.putExtra("repliesUUID", jsonRepliesuuid);
        checkRepliesIntent.putExtra("username", username);

        //CheckForReplies.enqueueWork(this, checkRepliesIntent);
        startService(checkRepliesIntent);
    }

    public static class CheckForReplies extends JobIntentService {

        private static final int id = 20;
        private boolean found = false;
        public CheckForReplies() {
            super();
        }


        @Override
        protected void onHandleWork(@NonNull Intent intent) {
            ArrayList<AvailableObjectsData>  myPosts = new ArrayList<>();
            ArrayList<String>  replies = new ArrayList<>();
            String username = intent.getStringExtra("username");

            Gson gson = new Gson();
            String jsonMyPosts = intent.getStringExtra("myPosts");
            if(jsonMyPosts != null) {
                Log.i("myPosts", jsonMyPosts);
                Type type = new TypeToken<ArrayList<AvailableObjectsData>>() {
                }.getType();
                myPosts = gson.fromJson(jsonMyPosts, type);
            }

            String jsonReplies = intent.getStringExtra("repliesUUID");
            if(jsonReplies != null) {
                Log.i("repliesUUID", jsonReplies);
                Type type = new TypeToken<ArrayList<String>>() {
                }.getType();
                replies = gson.fromJson(jsonReplies, type);
            }


            Log.i("hndlwek", "onHandleWork: myposts" + myPosts.size());
            Log.i("hndlwek", "onHandleWork: myposts" + replies.size());
            while(!found) {
                checkForRepliesTwitter(getApplicationContext(), myPosts, replies, username);
                try {
                    Thread.sleep(8000);
                    stopSelf();

                } catch (InterruptedException e) {
                    Log.e("rerror", "onHandleWork: ");

                    e.printStackTrace();

                }
            }
        }

        public static void enqueueWork(Context context, Intent intent) {
            enqueueWork(context, MainActivity.CheckForReplies.class, id, intent);
        }

        private void checkForRepliesTwitter(Context context, ArrayList<AvailableObjectsData> postlist, ArrayList<String> alreadycheckedreply, String username) {
            Twitter twitter = TwitterFactory.getSingleton();
            Log.i("insdthrpllsch", "dentro checkforreplies: ");
            UserTimeline userTimeline = new UserTimeline.Builder().screenName("GiftToME5")
                    .includeRetweets(false)
                    .maxItemsPerRequest(200)
                    .build();
            Log.i("insdthrpllsch", "dentro checkforreplies2: ");

            userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
                @Override
                public void success(Result<TimelineResult<Tweet>> searchResult)
                {
                    Log.i("insdthrpllsch", "dentro callback");
                    List<Tweet> tweets = searchResult.data.items;
                    long maxId = 0;
                    UUID repliedToId;
                    Log.i("insdthrpllsch", "success postlist: " + postlist.size());
                    Log.i("12334", "successalreadychcked: " + alreadycheckedreply.size());
                    for (Tweet tweet : tweets){
                        String jsonString = tweet.text;
                        if(jsonString.contains("#LAM_giftToMe_2020-article"))
                            continue;

                        for (AvailableObjectsData myPost: postlist ){
                            try {
                                JSONObject jsonObject = new JSONObject(jsonString);
                                String replyId = jsonObject.get("id").toString();
                                UUID targetId = UUID.fromString(jsonObject.get("target").toString());
                                String sender = jsonObject.get("sender").toString();

                                //se ho una risposta nuova
                                if(myPost.getId().equals(targetId) && !alreadycheckedreply.contains(replyId) && !sender.equals(username)){
                                    Log.i("insdthrpllsch", "success: risposta trovata" + replyId);
                                    String textReply = String.valueOf(jsonObject.get("message"));
                                    sendNotification(context, myPost, textReply);
                                    found = true;
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.i("fdlna", "error: "+ e.getMessage());
                            }
                        }}
                    Log.i("srchdcl", "successful search: ");
                }

                @Override
                public void failure(com.twitter.sdk.android.core.TwitterException error) {
                    Log.e("TAG","Errorer");
                    Toast.makeText(context, "problema di connessione", Toast.LENGTH_SHORT).show();
                }
            });
        }

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
            long maxId = 0;
            myPosts.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text;
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String username1 = jsonObject.get("issuer").toString();

                    if (username.equals(username1)){
                        String name1 = jsonObject.get("name").toString();
                        Log.i("gtmpoststwiitmain1", name1);

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
                maxId = tweet.id;
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
            long maxId = 0;
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
                        Log.i("credd1myreplies", String.valueOf(myRepliesUUID.size()));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;
            }


            //da ricontrollare
            if (searchResult.data.items.size() == 100) {
                userTimeline.previous(maxId, callbackreplies);
            }

            startCheckingForReplies();
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error) {
            Log.e("TAG","Error");
            Toast.makeText(getApplication(), "problema di connessione", Toast.LENGTH_SHORT).show();
        }


    };
}