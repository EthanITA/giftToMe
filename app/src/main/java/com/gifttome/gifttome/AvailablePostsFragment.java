package com.gifttome.gifttome;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import com.google.android.gms.common.util.IOUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.util.Base64.NO_WRAP;


public class AvailablePostsFragment extends Fragment implements ItemClickListener{
    private Button testTwitterButton;
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

    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    //String [] myDataset = {"ciao", "hi", "bella"};
    ArrayList<AvailableObjectsData> avObData = new ArrayList<>();
    String [] name = {"uno", "due", "tre", "wuattro", "cinque", "sei", "sette", "otto", "nove", "deeix"};
    String [] username = {"1", "2", "3", "w4", "5", "6", "7","8", "9" , "10"};
    /*for (int i = 0; i < name.length; i++){
         avObData.add(new AvailableObjectsData(name[i], username[i]));
     }
     Adapter
     */
    public AvailablePostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        thisFragment = inflater.inflate(R.layout.fragment_available_posts, container, false);
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
        mAdapter = new MyAdapter(avObData, getActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        //bearTokenTwitter();
        //getAvailablePostsTweets();
        mAdapter.notifyDataSetChanged();

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
        testTwitterButton = thisFragment.findViewById(R.id.button_twitter_test_button);
        testTwitterText = thisFragment.findViewById(R.id.test_twitter_text);
        testTwitterText.setMovementMethod(new ScrollingMovementMethod());

        recyclerView = thisFragment.findViewById(R.id.my_recycler_view);



        testTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //queueAvailableTweets.add(stringRequest);

            }
        });
    }

    @Override
    public void onClick(View view, int position) {
        Toast.makeText(getActivity(), "before gototchat"+String.valueOf(position), Toast.LENGTH_SHORT).show();
        //in teoria se clicchi ti manda al chat fragment
        goToChatFragment();
    }

    public void goToChatFragment(){
        ChatsFragment newChatFragment = new ChatsFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, newChatFragment)
                .addToBackStack(null)
                .commit();
    }


    public void getAvailablePostsTweets() {
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
                    Log.v("cred", name1);

                    String username1 =jsonObject.get("issuer").toString();
                    Log.v("cred", username1);

                    if(name1 != null && !name1.equals("") && username1!= null && !username1.equals(""))
                        avObData.add(new AvailableObjectsData(name1, username1));

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;
                Log.v("TagSuccc","str");

            }

            mAdapter.notifyDataSetChanged();
            Log.v("credd", String.valueOf(avObData.size()));

            //da ricontrollare
            if (searchResult.data.items.size() == 100) {
                userTimeline.previous(maxId, callback);
            }
            else {


            }

        }
        @Override
        public void failure(TwitterException error)
        {
            Log.e("TAG","Error");
        }
    };
}
