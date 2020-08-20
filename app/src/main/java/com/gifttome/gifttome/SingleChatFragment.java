package com.gifttome.gifttome;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toolbar;

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
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class SingleChatFragment extends Fragment {

    Twitter twitter = TwitterFactory.getSingleton();


    private ArrayList<Reply> messageList = new ArrayList<>();
    private AvailableObjectsData mainArticle;
    private UserTimeline userTimeline;
    private boolean mainArticleExists;

    private Button sendMesssageButton;
    private EditText textToSend;

    public SingleChatFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View thisFragment = inflater.inflate(R.layout.fragment_single_chat, container, false);
        Toolbar myToolbar = (Toolbar) thisFragment.findViewById(R.id.chat_toolbar);
        myToolbar.setTitle("mainArticle.getName()");

        TwitterConfig config = new TwitterConfig.Builder(getContext())
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("fud09hdnKuTT7PtYNuCZn2tRV", "gqzr3e1Rlz4noKtuhIytOBgfzjsJGSPNiMqmQO0quby2ycs1lp"))
                .debug(true)
                .build();
        com.twitter.sdk.android.core.Twitter.initialize(config);

        textToSend = thisFragment.findViewById(R.id.message_to_send);

        sendMesssageButton = thisFragment.findViewById(R.id.send_button);
        sendMesssageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessageOnTwitter(textToSend.getText().toString());
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
            }
        });


        return thisFragment;
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
            messageList.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //this is the body

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;
                try {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    assert mainActivity != null;

                    JSONObject jsonObject = new JSONObject(jsonString);
                    String targetId = jsonObject.get("target").toString();
                    if (targetId.equals(mainArticle.getId())){
                        String sender = jsonObject.get("sender").toString();
                        Log.i("cred", sender);

                        String receiver = jsonObject.get("receiver").toString();
                        Log.i("cred","kem" );

                        String message = jsonObject.get("message").toString();

                        String id = jsonObject.get("id").toString();

                        Reply reply = new Reply(id, sender,targetId, receiver, message);
                        reply.setTwitterId(tweet.getId());

                        Log.v("TagSuccc", "str");

                        //DA MODIFICARE CONDIZIONI CON CUI FILTRARE I POST USANDO ID
                        if (!sender.equals("") && !receiver.equals(""))
                            messageList.add(reply);
                        Log.v("credd1", String.valueOf(messageList.size()));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;

            }

            //mAdapter.notifyDataSetChanged();
            Log.v("credd2", String.valueOf(messageList.size()));

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
/*
    #LAM_giftToMe_2020 -reply{
    "id":  "04367a30 -e487 -439d-8f10 -3 f891f0883cc",
    "sender ": "senderUsername",
    "target ": "9a61bc0c -ce39 -4d3b -b72d -e63af675522a",
    "receiver ": "username",
    "message ": "Ciao , sono  interessato ."
    }


 */
    public class Reply{

        private String id;
        private String sender;
        private String targetid;
        private String receiver;
        private String message;
        private  Long twitterId;

    public Long getTwitterId() {
        return twitterId;
    }

    public void setTwitterId(Long twitterId) {
        this.twitterId = twitterId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTargetid() {
        return targetid;
    }

    public void setTargetid(String targetid) {
        this.targetid = targetid;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Reply(String id, String sender, String targetid, String receiver, String message) {
        this.id = id;
        this.sender = sender;
        this.targetid = targetid;
        this.receiver = receiver;
        this.message = message;
    }

    public String formatToString() throws JSONException {
        String string = "#LAM_giftToMe_2020-article \n";
        JSONObject json =  new JSONObject();
        json.put("id", this.getId());
        json.put("sender", this.getSender());
        json.put("target", this.getTargetid());
        json.put("receiver", this.getReceiver());
        json.put("message", this.getMessage());

        string = string + json.toString(1);

        return string;
    }
}

    public Status sendMessageOnTwitter(String text) throws TwitterException {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StatusUpdate newStat= new StatusUpdate(text);

        newStat.setInReplyToStatusId(mainArticle.getTwitterId());
        //Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(newStat);

        System.out.println("Successfully updated the status to [" + status.getText() + "].");

        return status;
    }

}
