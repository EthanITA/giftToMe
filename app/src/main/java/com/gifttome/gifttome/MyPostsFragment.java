package com.gifttome.gifttome;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.StrictMode;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;


public class MyPostsFragment extends Fragment implements ItemClickListener{
    final private Integer POST_ON_TWITTER = 0;
    final private Integer MODIFY_POST_ON_TWITTER = 1;
    private Button mButtton;
    private Button mButttonCoordinates;
    private TextView mTextView;
    private TextView mTextViewLON;
    private TextView mTextViewLAT;
    private EditText category;
    private EditText description;
    private EditText name;
    private EditText lat;
    private EditText lon;
    private double latitude;
    private double longitude;
    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
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
    //private ArrayList<AvailableObjectsData> availableObjectsDataList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationClient;
    //private ListView mListView;


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
        //availableObjectsDataList = new ArrayList<>();
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
        mAdapter = new MyAdapter(myPostsList, getActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        //farea richiesta del gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(Objects.requireNonNull(getActivity()));
        //mFusedLocationClient.requestLocationUpdates(mLocationRequest,            mLocationCallback,            null /* Looper */);  /* can use Looper.getMainLooper(); */
        mButtton = thisFragment.findViewById(R.id.test_button_my_post_twitter);
        mTextView = thisFragment.findViewById(R.id.my_posts_text);

        mButtton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                longitudineLatitudine(true);
                //dialogDetailsPost();
            }
        });
/*
        //listview
        mListView = thisFragment.findViewById(R.id.my_posts_listview);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                modifyPost(position);
            }
        });
        //adapter
        final MyPostsAdapter myPostsAdapter = new MyPostsAdapter(availableObjectsDataList);
        mListView.setAdapter(myPostsAdapter);
 */
        mTextViewLAT = thisFragment.findViewById(R.id.my_posts_text_lat);
        mTextViewLON = thisFragment.findViewById(R.id.my_posts_text_lon);

        //longitudineLatitudine();
        mButttonCoordinates = thisFragment.findViewById(R.id.coordinates_button);
        mButttonCoordinates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //longitudineLatitudine(false);
                //availableObjectsDataList.add(new AvailableObjectsData("test3", "test3"));
                //((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.addAvailablePost();
            }
        });
        //getMyPostsTweets();
        return thisFragment;
    }

    private void modifyPost(final int position) {
        final AvailableObjectsData object = myPostsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Modifica Post");      // set the custom layout
        final View thisDialog = getLayoutInflater().inflate(R.layout.post_object_on_twitter, null);

        final TextView thisCategory = thisDialog.findViewById(R.id.post_category);
        thisCategory.setText(object.getCategory());
        final TextView thisDescr = thisDialog.findViewById(R.id.post_description);
        thisDescr.setText(object.getDescription());
        final TextView thisName = thisDialog.findViewById(R.id.post_name);
        thisName.setText(object.getName());
        final TextView thislat = thisDialog.findViewById(R.id.post_lat);
        thislat.setText(String.valueOf(object.getLat()));
        final TextView thisLon = thisDialog.findViewById(R.id.post_lon);
        thisLon.setText(String.valueOf(object.getLon()));

        builder.setView(thisDialog);
        builder.setPositiveButton("Modifica Post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                Twitter twitter = TwitterFactory.getSingleton();
                //rimuovo l'oggetto vecchio dall'arraylist
                myPostsList.remove(position);
                mAdapter.notifyDataSetChanged();
                Status tweetStatus;
                try {
                    //cancello il tweet dell'oggetto vecchio
                    twitter.tweets().destroyStatus(object.getTwitterId());
                    //posto il tweet dell'oggetto nuovo
                    tweetStatus = postOnTwitter(setFormatTweetArticle(thisName.getText().toString(), thisCategory.getText().toString(), thisDescr.getText().toString(), thislat.getText().toString(), thisLon.getText().toString()));
                    myPostsList.add(new AvailableObjectsData(thisName.getText().toString(), "me", "id", thisCategory.getText().toString(), Double.valueOf(thislat.getText().toString()), Double.valueOf(thisLon.getText().toString()), thisDescr.getText().toString()));
                    mAdapter.notifyDataSetChanged();
                } catch (TwitterException | JSONException e) {
                    e.printStackTrace();
                    tweetStatus = null;
                }
                //aggiungo l'oggetto nuovo all'arraylist
                AvailableObjectsData newObject = new AvailableObjectsData(thisName.getText().toString(), "issuer");
                //aggiungo l'id del tweet all'oggetto
                if (tweetStatus != null){
                    newObject.setTwitterId(tweetStatus.getId());
                }
                myPostsList.add(newObject);
                //((BaseAdapter) mListView.getAdapter()).notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("annulla", null);
        builder.setNeutralButton("Cancella post", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                Twitter twitter = TwitterFactory.getSingleton();
                try {
                    twitter.tweets().destroyStatus(object.getTwitterId());
                    myPostsList.remove(position);
                    mAdapter.notifyDataSetChanged();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }

                Toast.makeText(getActivity(), "Post cancellato", Toast.LENGTH_SHORT).show();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void longitudineLatitudine(final Boolean dialog) {
        //ask permission to access location if not already granted
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not grante
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        // da togliere test lines
                        mTextViewLAT.setText(String.valueOf(location.getLatitude()));
                        mTextViewLON.setText(String.valueOf(location.getLongitude()));

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
            //LocationRequest mLocationRequest = LocationRequest.create();
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //mFusedLocationClient.getLastLocation();

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
                String descriptionString = description.getText().toString();

                name = thisDialog.findViewById(R.id.post_name);
                String nameString = name.getText().toString();

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
               try {
                    //should make it so that postOnTwitter takes an Available object data
                    tweetStat = postOnTwitter(setFormatTweetArticle(nameString, categoryString, descriptionString, latString, lonString));
                    myPostsList.add(new AvailableObjectsData(nameString, "me", "id", categoryString, Double.valueOf(latString), Double.valueOf(lonString), descriptionString));
                    mAdapter.notifyDataSetChanged();
               } catch (TwitterException | JSONException e) {
                    e.printStackTrace();
                }

                AvailableObjectsData newObject = new AvailableObjectsData(nameString, "issuer");
                if (tweetStat != null){
                    newObject.setTwitterId(tweetStat.getId());
                }
                myPostsList.add(newObject);
                recyclerView.getAdapter().notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private String setFormatTweetArticle(String name, String category, String description, String lat, String lon) throws JSONException {
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

    private Status postOnTwitter(String text) throws TwitterException {
        System.setProperty("twitter4j.oauth.consumerKey","fud09hdnKuTT7PtYNuCZn2tRV");
        System.setProperty("twitter4j.oauth.consumerSecret","gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp");
        System.setProperty("twitter4j.oauth.accessToken","1271353616847245315-Ru2rzisv9JsFyYglrOjdwN6zBTmlFC");
        System.setProperty("twitter4j.oauth.accessTokenSecret","AYbNR5QC1pSOxXZHIDLnuiio0X3car8tdSZHVS8dZVvQe");
        System.setProperty("twitter4j.debug","true");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(text);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");
        mTextView.setText(status.getText());
        Toast.makeText(getActivity(), status.getText(), Toast.LENGTH_SHORT).show();

        //add object to arraylist
        return status;
    }
/*
    private class MyPostsAdapter extends BaseAdapter {

        ArrayList<AvailableObjectsData> availableObjectsData;

        // override other abstract methods here

        public MyPostsAdapter(ArrayList<AvailableObjectsData> availableObjectsData) {
            this.availableObjectsData = availableObjectsData;
        }

        @Override
        public int getCount() {
            return availableObjectsData.size();
        }

        @Override
        public Object getItem(int position) {
            return availableObjectsData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.available_object_lay, container, false);
            }

            ((TextView) convertView.findViewById(R.id.object_name))
                    .setText(String.valueOf(position) + " name");
            ((TextView) convertView.findViewById(R.id.object_name))
                    .setText(String.valueOf(position) + " username");
            return convertView;
        }
    }

 */

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
            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //Here is the body
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    String name1 =jsonObject.get("name").toString();
                    Log.i("cred", name1);

                    String username1 =jsonObject.get("issuer").toString();
                    Log.i("cred", username1);

                    if(name1 != null && !name1.equals("") && username1!= null && !username1.equals(""))
                        myPostsList.add(new AvailableObjectsData(name1, username1));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;
                Log.v("TagSuccc","str");

            }

            mAdapter.notifyDataSetChanged();
            Log.v("credd", String.valueOf(myPostsList.size()));

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
    public void onClick(View view, int position) {
        Toast.makeText(getActivity(), "in MyPostsFragment onClick"+String.valueOf(position), Toast.LENGTH_SHORT).show();
        //in teoria se clicchi ti manda al chat fragment
        //goToChatFragment();

        //dialog che mostra i dettagli e propone di cancellare/modificare il tweet

    }
}