package com.gifttome.gifttome;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
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
import java.util.List;
import java.util.Objects;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MyPostsFragment extends Fragment implements View.OnClickListener{

    private Button mButtton;
    private TextView mTextView;
    private EditText category;
    private EditText description;
    private EditText name;
    private EditText lat;
    private EditText lon;
    private double latitude;
    private double longitude;
    private RecyclerView recyclerView;//t4j

    Twitter twitter = TwitterFactory.getSingleton();



    public MyPostsAdapter getmAdapter() {
        return mAdapter;
    }

    private MyPostsAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<AvailableObjectsData> myPostsList = new ArrayList<>();
    UserTimeline userTimeline;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                // Update UI with location data
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } };
    private FusedLocationProviderClient mFusedLocationClient;

    public MyPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onPause() {
        super.onPause();
        saveData();
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myPostsList);
        editor.putString("objects data list", json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("objects data list", null);
        Type type = new TypeToken<ArrayList<AvailableObjectsData>>() {}.getType();
        myPostsList = gson.fromJson(json, type);
        if(myPostsList == null){
            myPostsList = new ArrayList<>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisFragment = inflater.inflate(R.layout.fragment_my_posts, container, false);
        recyclerView = thisFragment.findViewById(R.id.my_posts_recyclerview);
        //in initialize()
        //twitter e display di tweets
        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("fud09hdnKuTT7PtYNuCZn2tRV", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp"))
                .debug(true)
                .build();
        com.twitter.sdk.android.core.Twitter.initialize(config);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyPostsAdapter(myPostsList, this, this);
        //mAdapter.setClickListener(this::onClick);
        //Log.i("clicklistener", );
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //farea richiesta del gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        //mFusedLocationClient.requestLocationUpdates(mLocationRequest,            mLocationCallback,            null /* Looper */);  /* can use Looper.getMainLooper(); */
        mButtton = thisFragment.findViewById(R.id.test_button_my_post_twitter);
        mButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longitudineLatitudine(true);
            }
        });

        getMyPostsTweets();

        return thisFragment;
    }

    public void modifyPost(final int position) {
        AvailableObjectsData object = myPostsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Modifica Post");      // set the custom layout
        View thisDialog = getLayoutInflater().inflate(R.layout.post_object_on_twitter, null);

        TextInputEditText thisCategory = thisDialog.findViewById(R.id.post_category);
        thisCategory.setText(object.getCategory());

        TextInputEditText thisDescr = thisDialog.findViewById(R.id.post_description);
        thisDescr.setText(object.getDescription());

        TextInputEditText thisName = thisDialog.findViewById(R.id.post_name);
        thisName.setText(object.getName());

        TextInputEditText thislat = thisDialog.findViewById(R.id.post_lat);
        thislat.setText(String.valueOf(object.getLat()));

        TextInputEditText thisLon = thisDialog.findViewById(R.id.post_lon);
        thisLon.setText(String.valueOf(object.getLon()));

        builder.setView(thisDialog);
        builder.setPositiveButton("Modifica Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                //Twitter twitter = TwitterFactory.getSingleton();
                //rimuovo l'oggetto vecchio dall'arraylist
                myPostsList.remove(position);
                mAdapter.notifyDataSetChanged();
                Status tweetStatus;
                try {
                    //cancello il tweet dell'oggetto vecchio
                    twitter.tweets().destroyStatus(object.getTwitterId());
                    //posto il tweet dell'oggetto nuovo
                    MainActivity mainActivity = (MainActivity) getActivity();
                    AvailableObjectsData newObject = new AvailableObjectsData(thisName.getText().toString(), "username", mainActivity.getUserid().toString(), thisCategory.getText().toString(), Double.valueOf(thislat.getText().toString()), Double.valueOf(thisLon.getText().toString()), thisDescr.getText().toString());
                    tweetStatus = postOnTwitter(setFormatTweetArticle(newObject));
                    //aggiungo l'oggetto nuovo all'arraylist
                    newObject.setTwitterId(tweetStatus.getId());
                    myPostsList.add(newObject);

                    mAdapter.notifyDataSetChanged();
                    Toast.makeText(getActivity(), "Post modificato", Toast.LENGTH_SHORT).show();

                } catch (TwitterException | JSONException e) {
                    e.printStackTrace();
                    tweetStatus = null;
                }
            }
        });
        builder.setNeutralButton("Cancella post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                removeObject(position);
            }

        });
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void removeObject(int position) {
        AvailableObjectsData object = myPostsList.get(position);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Log.i("removeobj", "removeObject: "+ position);
        try {
            twitter.tweets().destroyStatus(object.getTwitterId());
            myPostsList.remove(position);
            mAdapter.notifyDataSetChanged();
            Log.i("removeobj", "removeObject: "+ position + "inside");

        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

    public void longitudineLatitudine(final Boolean dialog) {
        //ask permission to access location if not already granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Toast.makeText(getContext(), String.valueOf(latitude), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getContext(), String.valueOf(longitude), Toast.LENGTH_SHORT).show();
                    if(dialog){
                        dialogDetailsPost();
                    }
                }
            }
        });
    }

    private void dialogDetailsPost() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Seleziona i dettagli dell'oggetto che vuoi regalare");      // set the custom layout
        final View thisDialog = getLayoutInflater().inflate(R.layout.post_object_on_twitter, null);
        //prendo i valori longitudine e latitudine
        lat = thisDialog.findViewById(R.id.post_lat);
        lon = thisDialog.findViewById(R.id.post_lon);
        lat.setText(String.valueOf(latitude));
        lon.setText(String.valueOf(longitude));
        Toast.makeText(getContext(), String.valueOf(latitude), Toast.LENGTH_SHORT).show();

        builder.setView(thisDialog);
        builder.setPositiveButton("POST", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                category = (thisDialog.findViewById(R.id.post_category));
                String categoryString = category.getText().toString();

                description = thisDialog.findViewById(R.id.post_description);

                name = thisDialog.findViewById(R.id.post_name);

                String latString;
                if(!lat.getText().toString().equals("")){
                    latString = lat.getText().toString();
                }
                else{
                    latString = String.valueOf(latitude);}

                String lonString;
                if(!lon.getText().toString().equals("")){
                    lonString = lon.getText().toString();
                }
                else{
                    lonString = String.valueOf(longitude);
                }
                Status tweetStat = null;
                MainActivity mainActivity = (MainActivity) getActivity();
                AvailableObjectsData myNewObject= new AvailableObjectsData(name.getText().toString(), "me",mainActivity.getUserid().toString() , category.getText().toString(), Double.valueOf(latString), Double.valueOf(lonString), description.getText().toString());
                try {
                    //should make it so that postOnTwitter takes an Available object data
                    tweetStat = postOnTwitter(setFormatTweetArticle(myNewObject));
                    myPostsList.add(myNewObject);
                    myNewObject.setTwitterId(tweetStat.getId());
                    mAdapter.notifyDataSetChanged();
                } catch (TwitterException | JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public String setFormatTweetArticle(String name, String category, String description, String lat, String lon) throws JSONException {
        String tweet = "#LAM_giftToMe_2020-article \n";
        JSONObject json =  new JSONObject();
        json.put("id", "id");
        json.put("issuer", "issuer");
        json.put("category", category);
        json.put("name", name);
        json.put("lat", lat);
        json.put("lon", lon);
        json.put("description", description);
        tweet = tweet + json.toString(1);
        return tweet;
    }
    public String setFormatTweetArticle(AvailableObjectsData object) throws JSONException {
        String tweet = "#LAM_giftToMe_2020-article \n";
        JSONObject json =  new JSONObject();
        json.put("id", object.getId());
        json.put("issuer", object.getIssuer());
        json.put("category", object.getCategory());
        json.put("name", object.getName());
        json.put("lat",object.getLat());
        json.put("lon", object.getLon());
        json.put("description",object.getDescription());
        tweet = tweet + json.toString(1);
        return tweet;
    }

    public Status postOnTwitter(String text) throws TwitterException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(text);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
        mTextView.setText(status.getText());

        return status;
    }

    public void getMyPostsTweets() {
        userTimeline = new UserTimeline.Builder()
                .screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callback);
    }
    Callback<TimelineResult<Tweet>> callback = new Callback<TimelineResult<Tweet>>()
    {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            long maxId = 0;
            myPostsList.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //this is the body

                try {
                    //MANCA ID TRA LE COSE
                    MainActivity mainActivity = (MainActivity) getActivity();
                    assert mainActivity != null;

                    JSONObject jsonObject = new JSONObject(jsonString);
                    Log.i("jsonid", jsonObject.get("id").toString());
                    Log.i("jsonid", (String) jsonObject.get("id"));
                    if (String.valueOf(mainActivity.getUserid()).equals(String.valueOf(jsonObject.get("id")))){
                        String name1 = jsonObject.get("name").toString();
                        Log.i("cred", name1);

                        String username1 = jsonObject.get("issuer").toString();
                        Log.i("cred", username1);

                        String category = jsonObject.get("category").toString();
                        double lat = Double.parseDouble(jsonObject.get("lat").toString());
                        double lon = Double.parseDouble(jsonObject.get("lon").toString());
                        String description = jsonObject.get("description").toString();

                        AvailableObjectsData myPost = new AvailableObjectsData(name1, username1, String.valueOf(mainActivity.getUserid()), category, lat, lon, description);
                        myPost.setTwitterId(tweet.getId());
                        Log.v("TagSuccc", "str");


                        //DA MODIFICARE COMDIZIONI CON CUI FILTRARE I POST USANDO ID
                        if (!name1.equals("") && !username1.equals(""))
                            myPostsList.add(myPost);
                        Log.v("credd1", String.valueOf(myPostsList.size()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;

            }

            mAdapter.notifyDataSetChanged();
            Log.v("credd2", String.valueOf(myPostsList.size()));

            //da ricontrollare
            if (searchResult.data.items.size() == 100) {
                userTimeline.previous(maxId, callback);
            }
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error)
        {
            Log.e("TAG","Error");
        }
    };

    @Override
    public void onClick(View v) {
        Log.i("onclickposition", "onClick: "+ v.getId());
        modifyPost(v.getId());
    }
}