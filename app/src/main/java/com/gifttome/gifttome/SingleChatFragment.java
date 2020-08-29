package com.gifttome.gifttome;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
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
import java.util.UUID;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class SingleChatFragment extends Fragment {

    Twitter twitter = TwitterFactory.getSingleton();
    private String currentUser = "CurrentUser";

    private ArrayList<Reply> messageList = new ArrayList<>();
    private AvailableObjectsData mainArticle;
    private UserTimeline userTimeline;
    private boolean mainArticleExists;

    private Button sendMesssageButton;
    private EditText textToSend;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ChatAdapter chatAdapter;


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
                    sendMessageOnTwitter(textToSend.getText().toString(), messageList.size()-1);
                } catch (TwitterException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });


        recyclerView = thisFragment.findViewById(R.id.chat_recycler_view);
        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        chatAdapter = new ChatAdapter(messageList, getActivity());
        //mAdapter.setClickListener(this::onClick);
        //Log.i("clicklistener", );
        recyclerView.setAdapter(chatAdapter);
        chatAdapter.notifyDataSetChanged();

        return thisFragment;
    }

    private void getRepliesTweets() {
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
                    UUID targetId = UUID.fromString(jsonObject.get("target").toString());

                    if (targetId.equals(mainArticle.getId())){
                        String sender = jsonObject.get("sender").toString();
                        Log.i("cred", sender);

                        String receiver = jsonObject.get("receiver").toString();
                        Log.i("cred","kem" );
                        UUID id = UUID.fromString(jsonObject.get("id").toString());
                        String message = jsonObject.get("message").toString();


                        Reply reply = new Reply(id, sender,targetId, receiver, message);
                        reply.setTwitterId(tweet.getId());
                        reply.setReplyToId(tweet.inReplyToStatusId);

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

    public Status sendMessageOnTwitter(String text, int position) throws TwitterException, JSONException {


        Reply newReply = new Reply(UUID.randomUUID(), "this.sender", mainArticle.getId(), mainArticle.getIssuer(), text);
        String textInJsonFormat = newReply.formatToString();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        StatusUpdate newStat= new StatusUpdate(textInJsonFormat);

        newStat.setInReplyToStatusId(messageList.get(position).getTwitterId());
        //Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(newStat);
        newReply.setTwitterId(status.getId());
        messageList.add(newReply);

        System.out.println("Successfully updated the status to [" + status.getText() + "].");

        return status;
    }


    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.SentViewHolder>{
        Integer mExpandedPosition = -1;
        private static final int VIEW_TYPE_MESSAGE_SENT = 0;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;

        private ArrayList<Reply> messageList = new ArrayList<>();
        private Context context;
        private static ItemClickListener itemClickListener;
        private  View.OnClickListener buttonClickListener;
        private AvailableObjectsData mainObject;

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder
        public class SentViewHolder extends RecyclerView.ViewHolder {

            private View view;
            private TextView message;
            private Button chatButton;


            public SentViewHolder(View view) {
                super(view);
                this.view = view;
                message = view.findViewById(R.id.object_name);
            }
        }

        @Override
        public int getItemViewType(int position) {
            Reply message = messageList.get(position);

            if (message.getSender().equals("CurrentUser")) {
                // If the current user is the sender of the message
                return VIEW_TYPE_MESSAGE_SENT;
            } else {
                // If some other user sent the message
                return VIEW_TYPE_MESSAGE_RECEIVED;
            }
        }

        // Provide a suitable constructor (depends on the kind of dataset)
        public ChatAdapter(ArrayList<Reply> myDataset, Context context) {

            messageList = myDataset;
            this.context = context;
            //this.buttonClickListener = onClickListener;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public SentViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {


            if (viewType == VIEW_TYPE_MESSAGE_SENT) {
                View v = (View) LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.sent_message_in_chat, parent, false);
                return new SentViewHolder(v);
            }
            else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.received_message_in_chat, parent, false);
                return new SentViewHolder(v);
            }
            return null;
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(SentViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Reply message = messageList.get(position);
            //ho mandato io il messaggio
            holder.message.setText(messageList.get(position).getMessage());


            /*holder.itemView.setOnClickListener(new View.OnClickListener() {
             */

            //holder.chatButton.setText("Manda un messaggio");
            //holder.chatButton.setId(position);
            //holder.chatButton.setOnClickListener(buttonClickListener);

            final boolean isExpanded = position == mExpandedPosition;

            //holder.chatButton.setVisibility(isExpanded ? View.VISIBLE:View.GONE);

            holder.itemView.setActivated(isExpanded);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mExpandedPosition = isExpanded ? -1:position;
                    notifyItemChanged(position);
                }
            });
        }


        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {

            return messageList.size();

        }


        public void setClickListener(ItemClickListener itemClickListener){
            Log.v("tag", "tag");
            //this.itemClickListener = itemClickListener;
        }
    }
}
