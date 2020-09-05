package com.gifttome.gifttome;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import java.util.UUID;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

public class MyPostsFragment extends Fragment implements View.OnClickListener{
    private String username;
    private Button mButtton;
    private EditText category;
    private EditText description;
    private EditText name;
    private EditText lat;
    private EditText lon;
    private double latitude;
    private double longitude;

    //t4j
    private Twitter twitter = TwitterFactory.getSingleton();

    private MyPostsAdapter mAdapter;
    private ArrayList<AvailableObjectsData> myPostsList = new ArrayList<>();
    private UserTimeline userTimeline;
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
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(myPostsList);
        editor.putString("objects data list", json);
        editor.apply();
    }

    private void loadData(){
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
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
        View thisFragment = inflater.inflate(R.layout.fragment_my_posts, container, false);
        RecyclerView recyclerView = thisFragment.findViewById(R.id.my_posts_recyclerview);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        //in initialize()
        //twitter e display di tweets
        TwitterConfig config = new TwitterConfig.Builder(requireContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("fud09hdnKuTT7PtYNuCZn2tRV", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp"))
                .debug(true)
                .build();
        com.twitter.sdk.android.core.Twitter.initialize(config);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyPostsAdapter(myPostsList, this);
        recyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        //richiesta del gps
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        mButtton = thisFragment.findViewById(R.id.test_button_my_post_twitter);
        mButtton.setOnClickListener(v -> longitudineLatitudine(true));

        getMyPostsTweets();

        return thisFragment;
    }

    public void modifyPost(final int position) {
        AvailableObjectsData object = myPostsList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
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
        builder.setPositiveButton("Modifica Post", (dialog, which) -> {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            //Twitter twitter = TwitterFactory.getSingleton();
            //rimuovo l'oggetto vecchio dall'arraylist
            UUID oldUUID = myPostsList.get(position).getId();
            myPostsList.remove(position);
            mAdapter.notifyDataSetChanged();
            Status tweetStatus;
            try {
                //cancello il tweet dell'oggetto vecchio
                twitter.tweets().destroyStatus(object.getTwitterId());
                //posto il tweet dell'oggetto nuovo
                String nameString = thisName.getText() == null? "" : thisName.getText().toString();
                String categoryString = thisCategory.getText() == null? "" : thisCategory.getText().toString();
                String lonString = thisLon.getText() == null? "" : thisLon.getText().toString();
                String latString = thislat.getText() == null? "" : thislat.getText().toString();
                String descrString = thisDescr.getText() == null? "" : thisDescr.getText().toString();

                AvailableObjectsData newObject = new AvailableObjectsData(nameString, username , oldUUID, categoryString,
                        Double.parseDouble(latString), Double.parseDouble(lonString),descrString);

                tweetStatus = postOnTwitter(setFormatTweetArticle(newObject));
                //aggiungo l'oggetto nuovo all'arraylist
                newObject.setTwitterId(tweetStatus.getId());
                myPostsList.add(newObject);

                mAdapter.notifyDataSetChanged();
                Toast.makeText(getActivity(), "Post modificato", Toast.LENGTH_SHORT).show();

            } catch (TwitterException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "errore nella modifica del post", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "possibile assenza di connessione", Toast.LENGTH_SHORT).show();

            }
        });
        builder.setNeutralButton("Cancella post", (dialog, which) -> removeObject(position));
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
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(requireActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},123);
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                if(dialog){
                    newObjectDialog();
                }
            }
        });
    }

    private void newObjectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Seleziona i dettagli dell'oggetto che vuoi regalare");      // set the custom layout
        final View thisDialog = getLayoutInflater().inflate(R.layout.post_object_on_twitter, null);
        //prendo i valori longitudine e latitudine
        lat = thisDialog.findViewById(R.id.post_lat);
        lon = thisDialog.findViewById(R.id.post_lon);
        lat.setText(String.valueOf(latitude));
        lon.setText(String.valueOf(longitude));

        builder.setView(thisDialog);
        builder.setPositiveButton("POST", (dialog, which) -> {
            category = (thisDialog.findViewById(R.id.post_category));
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
            Status tweetStat;
            AvailableObjectsData myNewObject= new AvailableObjectsData(name.getText().toString(), username, UUID.randomUUID() , category.getText().toString(), Double.parseDouble(latString), Double.parseDouble(lonString), description.getText().toString());
            try {
                tweetStat = postOnTwitter(setFormatTweetArticle(myNewObject));
                myPostsList.add(myNewObject);
                myNewObject.setTwitterId(tweetStat.getId());
                mAdapter.notifyDataSetChanged();
            } catch (TwitterException | JSONException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "errore nell'upload del post", Toast.LENGTH_SHORT).show();
                Toast.makeText(getActivity(), "possibile assenza di connessione", Toast.LENGTH_SHORT).show();
            }

        });
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    public String setFormatTweetArticle(AvailableObjectsData object) throws JSONException {
        String string = "#LAM_giftToMe_2020-article \n";
        JSONObject json =  new JSONObject();
        json.put("id", object.getId());
        json.put("issuer", object.getIssuer());
        json.put("category", object.getCategory());
        json.put("name", object.getName());
        json.put("lat",object.getLat());
        json.put("lon", object.getLon());
        json.put("description",object.getDescription());
        string = string + json.toString(1);
        return string;
    }

    public Status postOnTwitter(String text) throws TwitterException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(text);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");

        return status;
    }

    private void getMyPostsTweets() {
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

                    JSONObject jsonObject = new JSONObject(jsonString);
                    String username1 = jsonObject.get("issuer").toString();
                    Log.i("cred", username1);

                    Log.i("jsonid", jsonObject.get("id").toString());
                    Log.i("jsonid", (String) jsonObject.get("id"));

                    if (username.equals(username1)){
                        String name1 = jsonObject.get("name").toString();
                        Log.i("cred", name1);

                        UUID id1 = UUID.fromString(jsonObject.get("id").toString());

                        String category = jsonObject.get("category").toString();
                        double lat = Double.parseDouble(jsonObject.get("lat").toString());
                        double lon = Double.parseDouble(jsonObject.get("lon").toString());
                        String description = jsonObject.get("description").toString();

                        AvailableObjectsData myPost = new AvailableObjectsData(name1, username1, id1 , category, lat, lon, description);
                        myPost.setTwitterId(tweet.getId());
                        Log.v("TagSuccc", "str");

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
            Log.v("credd2Myposts", String.valueOf(myPostsList.size()));

            //da ricontrollare
            if (searchResult.data.items.size() == 100) {
                userTimeline.previous(maxId, callback);
            }
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error)
        {
            Log.e("TAG","Error");
            Toast.makeText(getActivity(), "errore, controllare la connessione ad internet", Toast.LENGTH_SHORT).show();

        }
    };

    @Override
    public void onClick(View v) {
        Log.i("onclickposition", "onClick: "+ v.getId());
        modifyPost(v.getId());
    }
}