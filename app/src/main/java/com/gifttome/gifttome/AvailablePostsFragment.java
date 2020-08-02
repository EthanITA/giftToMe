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
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    private RequestQueue queueBearer;
    private RequestQueue queueAvailableTweets;
    private String bearerToken;
    private JSONArray jsonArray;
    private StringRequest stringRequest;


    private RecyclerView recyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    //String [] myDataset = {"ciao", "hi", "bella"};
    List<AvailableObjectsData> avObData = new ArrayList<AvailableObjectsData>();
    String [] name = {"uno", "due", "tre", "wuattro", "cinque", "sei", "sette", "otto", "nove", "deeix"};
    String [] username = {"1", "2", "3", "w4", "5", "6", "7","8", "9" , "10"};

    public AvailablePostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        thisFragment = inflater.inflate(R.layout.fragment_available_posts, container, false);
        inizializzazione();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        for (int i = 0; i < name.length; i++){
            AvailableObjectsData y = new AvailableObjectsData(name[i], username[i]);
            avObData.add(y);
        }
        mAdapter = new MyAdapter(avObData, getActivity());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setClickListener(this);

        Toast.makeText(getActivity(), "aftef setclicklistener", Toast.LENGTH_SHORT).show();
        bearTokenTwitter();

        getTweetsAvailablePosts();

        return thisFragment;
    }


    private void getTweetsAvailablePosts() {
        // Instantiate the RequestQueue.
        queueAvailableTweets = Volley.newRequestQueue(getActivity());
        String url = "https://api.twitter.com/1.1/statuses/user_timeline.json?screen_name=GiftToMe5&include_rts=false&exclude_replies=true";

        // Request a string response from the provided URL
        stringRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getActivity(),
                                "success",
                                Toast.LENGTH_SHORT).show();
                        try {
                            jsonArray = new JSONArray(response);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String testTotal = "";
                        for (int i = 0; i < jsonArray.length(); i++) {
                            try {
                                testTotal = testTotal + jsonArray.getJSONObject(i).get("text");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        testTwitterText.setText("Texts: " + testTotal);
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Toast.makeText(getActivity(),
                                "This is a message displayed in a fail",
                                Toast.LENGTH_SHORT).show();
                        testTwitterText.setText("Response: " + "fuck error"+ error.toString());
                        Log.e("onErrorResponse", error.toString());

                    }

                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                // Basic Authentication
                String auth = "Bearer " + bearerToken;
                //String auth = "";
                //headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                headers.put("Authorization", auth);
                return headers;
            }
        };
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
                            testTwitterText.setText("Response: " + response.toString() + " "+ response.getString("access_token"));
                            bearerToken = response.getString("access_token");
                        } catch (JSONException e) {
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
                        testTwitterText.setText("Response: " + "fuck error"+ error.toString());

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

        recyclerView = (RecyclerView) thisFragment.findViewById(R.id.my_recycler_view);



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
        goToChatFragment();
    }

    private void goToChatFragment() {
            // Create fragment and give it an argument specifying the article it should show
            ChatsFragment newFragment = new ChatsFragment();
            FragmentTransaction transaction;
            transaction = getActivity().getSupportFragmentManager().beginTransaction();

// Replace whatever is in the fragment_container view with this fragment,
// and add the transaction to the back stack so the user can navigate back
            transaction.add(R.id.activity_main_layout, newFragment);
            transaction.addToBackStack(null);
        Toast.makeText(getActivity(), "dentro a gotochat", Toast.LENGTH_SHORT).show();
// Commit the transaction
            transaction.commit();

        }



}
