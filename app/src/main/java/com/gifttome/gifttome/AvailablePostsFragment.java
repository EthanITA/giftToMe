package com.gifttome.gifttome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import static android.util.Base64.NO_WRAP;


public class AvailablePostsFragment extends Fragment implements ItemClickListener, View.OnClickListener{
    private View thisFragment;
    private TextView testTwitterText;
    private JsonObjectRequest jsonObjectRequest;
    private JsonArrayRequest jsonArrayRequest;
    private RequestQueue queueBearer;
    public String bearerToken100 = "AAAAAAAAAAAAAAAAAAAAACdfFAEAAAAAoVHrmvDLmFC5OKGmmoxJ1sGTVH8%3DhCzOtao5shMMNcyBmjZ3q267cXtOOaSoUH0tzn9r1QkcA6ekCI";
    private RequestQueue queueAvailableTweets;
    private String bearerToken;
    private JSONArray jsonArray;
    private StringRequest stringRequest;
    private UserTimeline userTimeline;

    private String username;

    private RecyclerView recyclerView;
    private MyAdapter nAdapter;
    private RecyclerView.LayoutManager layoutManager;
    ArrayList<AvailableObjectsData> avObData = new ArrayList<>();
    private MainActivity mainActivity;
    private Button searchButton;


    public AvailablePostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        thisFragment = inflater.inflate(R.layout.fragment_available_posts, container, false);
        mainActivity = (MainActivity) getActivity();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        Log.i("usernamefromshared", "onCreateView: " + username);
        inizializzazione();
        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("fud09hdnKuTT7PtYNuCZn2tRV", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        nAdapter = new MyAdapter(avObData, getActivity(), this);
        nAdapter.setClickListener(this);
        recyclerView.setAdapter(nAdapter);

        searchButton = thisFragment.findViewById(R.id.search_button1);


        //bearTokenTwitter();
        makeTwitterRequest("true");
        //getAvailablePostsTweets();
        nAdapter.notifyDataSetChanged();

        return thisFragment;
    }


    private void getTweetsAvailablePosts(final String bear) throws URISyntaxException, MalformedURLException {
        Toast.makeText(getActivity(), "dentro getTweetsAvailablePosts", Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), bear, Toast.LENGTH_SHORT).show();

        // Instantiate the RequestQueue.
        queueAvailableTweets = Volley.newRequestQueue(getActivity());
        //String Myurl = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=GiftToME5&include_rts=false&exclude_replies=true&count=2";
        String myUrl = "https://api.twitter.com/1.1/statuses/user_timeline.json?count=2&screen_name=GiftToME5";
        URL url= new URL(myUrl);
        URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
        myUrl = uri.toASCIIString();

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, myUrl, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        testTwitterText.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        testTwitterText.setText("error getavtwits: " + error.toString());

                    }
                })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                //Bearer Authentication
                String auth = "Bearer " + Base64.encodeToString("AAAAAAAAAAAAAAAAAAAAACdfFAEAAAAAoVHrmvDLmFC5OKGmmoxJ1sGTVH8%3DhCzOtao5shMMNcyBmjZ3q267cXtOOaSoUH0tzn9r1QkcA6ekCI".getBytes(), NO_WRAP);
                //String auth = "Bearer " + Base64.encodeToString(bearerToken.getBytes(), NO_WRAP);
                //headers.put("Accept-Encoding", "gzip");
                headers.put("Authorization", auth);
                //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                //headers.put("Host", "api.twitter.com");
                //headers.put("User-Agent", "GiftToMe2020");
                return headers;
            }

        };

        queueAvailableTweets.add(jsonArrayRequest);

    }

    public void httpconnectionAvailableTweets() throws IOException {
        String url = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=GiftToMe5&include_rts=false&exclude_replies=true&count=2";

        URL myURL = new URL(url);
        HttpURLConnection myURLConnection = (HttpURLConnection)myURL.openConnection();

        String userCredentials = "username:password";
        String auth = "Bearer " + Base64.encodeToString(bearerToken.getBytes(), NO_WRAP);

        myURLConnection.setRequestProperty ("Authorization", auth);
        myURLConnection.setRequestMethod("GET");
        myURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        myURLConnection.setRequestProperty("Content-Length", "");
        myURLConnection.setRequestProperty("Content-Language", "en-US");
        myURLConnection.setUseCaches(false);
        myURLConnection.setDoInput(true);
        myURLConnection.setDoOutput(true);
        int responseCode = myURLConnection.getResponseCode();
        if (responseCode == 200) {
            InputStream inputStr = myURLConnection.getInputStream();
            String encoding = myURLConnection.getContentEncoding() == null ? "UTF-8"
                    : myURLConnection.getContentEncoding();
            //sjon = IOUtils.toString(inputStr, encoding);
            /************** For getting response from HTTP URL end ***************/

        }
    }

    private void bearTokenTwitter() {
        // Instantiate the RequestQueue.
        queueBearer = Volley.newRequestQueue(getActivity());
        String url ="https://api.twitter.com/oauth2/token";

        final String requestBody = "grant_type=client_credentials";

        // Request a string response from the provided URL.
        jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url,null , new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            testTwitterText.setText("Response: " + response.getString("access_token"));
                            bearerToken = response.getString("access_token");
                            getTweetsAvailablePosts(bearerToken);
                            Log.v("myTag", bearerToken);
                        } catch (JSONException | MalformedURLException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getActivity(),
                                "This is a message displayed in a Toast",
                                Toast.LENGTH_SHORT);
                        testTwitterText.setText("Response: " + "bear toker error"+ error.toString());

                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Basic Authentication
                String auth = "Basic " + Base64.encodeToString("fud09hdnKuTT7PtYNuCZn2tRV:gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp".getBytes(), Base64.NO_WRAP);
                headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", auth);
                return headers;
            }
            @Override
            public byte[] getBody() {
                return requestBody.getBytes();
            }
        };
        //in teh  button
        // Add the request to the RequestQueue.
        queueBearer.add(jsonObjectRequest);
    }

    private void inizializzazione() {
        testTwitterText = thisFragment.findViewById(R.id.test_twitter_text);
        testTwitterText.setMovementMethod(new ScrollingMovementMethod());

        recyclerView = thisFragment.findViewById(R.id.my_recycler_view);
    }

    @Override
    public void onClick(View view, int position) {
        Toast.makeText(getActivity(), "before gototchat"+String.valueOf(position), Toast.LENGTH_SHORT).show();
        //in teoria se clicchi ti manda al chat fragment
        Toast.makeText(getActivity(), String.valueOf(view.getRootView().getId()), Toast.LENGTH_SHORT).show();
        goToChatFragment();
    }

    public void goToChatFragment(){
        RepliesFragment newChatFragment = new RepliesFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, newChatFragment)
                .addToBackStack(null)
                .commit();
    }

    public ArrayList<AvailableObjectsData> getAvailablePostsTweets() {
        userTimeline = new UserTimeline.Builder()
                .screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callback);
        return avObData;
    }

    Callback<TimelineResult<Tweet>> callback = new Callback<TimelineResult<Tweet>>()
    {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            long maxId = 0;
            avObData.clear();
            MainActivity mainActivity = (MainActivity) getActivity();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //Here is the body
                try {
                    JSONObject jsonObject = new JSONObject(jsonString);

                    //sceglie i post che non hanno lo stesso username dell'utente
                    String username1 = jsonObject.get("issuer").toString();
                    Log.v("credUsernameAPF", username1);
                    Log.v("credUsernameAPFSP", username);

                    if(!username1.equals(username)) {
                        String name1 = jsonObject.get("name").toString();
                        Log.v("cred", name1);

                        String userId = jsonObject.get("id").toString();
                        String category1 = jsonObject.get("category").toString();
                        double lat1 = Double.parseDouble(jsonObject.get("lat").toString());
                        double lon1 = Double.parseDouble(jsonObject.get("lon").toString());
                        String description1 = jsonObject.get("description").toString();
                        Log.i("idnotvalid", userId);
                        AvailableObjectsData newPost = new AvailableObjectsData(name1, username1, UUID.fromString(userId), category1, lat1, lon1, description1);
                        newPost.setTwitterId(tweet.getId());
                        if (!name1.equals("") && !username1.equals("")) {
                            avObData.add(newPost);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                maxId = tweet.id;
                Log.v("TagSuccc","str");

            }
            assert mainActivity != null;
            mainActivity.addNewAvailablePost(avObData);
            nAdapter.notifyDataSetChanged();
            Log.v("credd", String.valueOf(avObData.size()));

            //da ricontrollare
            if (searchResult.data.items.size() == 100) {
                userTimeline.previous(maxId, callback);
            }
        }
        @Override
        public void failure(TwitterException error)
        {
            Log.e("TAG","Error");
        }
    };

    interface RepositoryCallback {
        void onComplete(ArrayList<AvailableObjectsData> result);
    }

    public void makeTwitterRequest(String text) {
        GetTwitterInBackground gtib = new GetTwitterInBackground(mainActivity.executorService);
        gtib.makeRequest(text, new RepositoryCallback() {
            @Override
            public void onComplete(ArrayList<AvailableObjectsData> result) {
                Log.i("oncompletearray", "onComplete: " + result.size());

/*
                avObData.clear();
                avObData.addAll(result);
                MainActivity mainActivity = (MainActivity) getActivity();
                assert mainActivity != null;
                mainActivity.addNewAvailablePost(avObData);
 */

            }
        });
    }


    @Override
    public void onClick(View v) {
        goToChatFragment();
    }

    public class GetTwitterInBackground {

        private final Executor executor;

        public GetTwitterInBackground(Executor executor) {
            this.executor = executor;
        }

        public void makeRequest(final String text, final RepositoryCallback callback){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ArrayList<AvailableObjectsData> result = makeSynchronousTwitterRequest(text);
                        callback.onComplete(result);
                    } catch (Exception ignored) {

                    }
                }
            });
        }

        public ArrayList<AvailableObjectsData> makeSynchronousTwitterRequest(String text) {
            return getAvailablePostsTweets();
        }


    }

}


