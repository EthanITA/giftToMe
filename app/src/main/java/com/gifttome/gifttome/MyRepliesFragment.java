package com.gifttome.gifttome;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;


public class MyRepliesFragment extends Fragment implements View.OnClickListener{

    public MyRepliesFragment() {
        // Required empty public constructor
    }

    ArrayList<Reply> repliesList = new ArrayList<>();

    private RepliesAdapter mAdapter;
    private Twitter twitter = TwitterFactory.getSingleton();
    private MainActivity mainActivity;
    private String username;

    UserTimeline userTimeline;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View thisFragment = inflater.inflate(R.layout.fragment_my_replies, container, false);
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);
        mainActivity = (MainActivity) getActivity();

        RecyclerView repliesRecyclerView = thisFragment.findViewById(R.id.my_replies_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        repliesRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RepliesAdapter(repliesList, this);
        repliesRecyclerView.setAdapter(mAdapter);

        getRepliesTweets();
        //mAdapter.notifyDataSetChanged();

        return thisFragment;
    }

    private void getRepliesTweets() {
        userTimeline = new UserTimeline.Builder().screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callback);
    }

    Callback<TimelineResult<Tweet>> callback = new Callback<TimelineResult<Tweet>>() {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            long maxId = 0;
            repliesList.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text;

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;

                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    String sender = jsonObject.get("sender").toString();

                    if (sender.equals(username)) {
                        UUID targetId = UUID.fromString(jsonObject.get("target").toString());

                        String receiver = jsonObject.get("receiver").toString();
                        UUID id = UUID.fromString(jsonObject.get("id").toString());
                        String message = jsonObject.get("message").toString();

                        Reply reply = new Reply(id, sender, targetId, receiver, message);
                        reply.setTwitterId(tweet.getId());
                        reply.setReplyToId(tweet.inReplyToStatusId);
                        //reply.setObjectRepliedTo(myObject);
                        repliesList.add(reply);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;
            }
            getObjectsRepliedToTweets();
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error) {
            Log.e("TAG","Error");
            Log.e("TAG", "failure: " + error.getMessage() );
            error.printStackTrace();

            Toast.makeText(getActivity(), "problema di connessione nel cercare risposte", Toast.LENGTH_SHORT).show();
        }
    };

    private void getObjectsRepliedToTweets() {
        userTimeline = new UserTimeline.Builder().screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        userTimeline.next(null, callbackRepliedTo);
    }

    Callback<TimelineResult<Tweet>> callbackRepliedTo = new Callback<TimelineResult<Tweet>>() {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            long maxId = 0;
            UUID repliedToId;
            for (Tweet tweet : tweets){

                String jsonString = tweet.text;
                if(!jsonString.contains("#LAM_giftToMe_2020-article"))
                    continue;
                for (Reply thisReply: repliesList){
                    try {

                        JSONObject jsonObject = new JSONObject(jsonString);
                        repliedToId = UUID.fromString(jsonObject.get("id").toString());

                        if (repliedToId.equals(thisReply.getTargetid())) {
                            String name1 = jsonObject.get("name").toString();
                            String username1 = jsonObject.get("issuer").toString();
                            String category1 = jsonObject.get("category").toString();
                            double lat1 = Double.parseDouble(jsonObject.get("lat").toString());

                            double lon1 = Double.parseDouble(jsonObject.get("lon").toString());
                            String description1 = jsonObject.get("description").toString();
                            AvailableObjectsData targetPost = new AvailableObjectsData(name1, username1, repliedToId, category1, lat1, lon1, description1);
                            targetPost.setTwitterId(tweet.getId());
                            thisReply.setObjectRepliedTo(targetPost);
                            //se non è stata trovato l'oggetto a cui si risponde significa che è stato cancellato
                            //TODO CANCELLARE LA RISPOSTA SE L'OGGETTO A CUI SI RIFERISCE È STATO CANCELLATO
                            boolean postDeleted = true;
                            for (AvailableObjectsData availPost: mainActivity.getAvailablePosts()){
                                if (targetPost.getId().equals(availPost.getId())) {
                                    postDeleted = false;
                                    break;
                                }
                            }
                            if(postDeleted){
                                repliesList.remove(thisReply);
                                deleteReply(-1, thisReply);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.i("ciaoerror", "targetPost.toString()");
                    }
                }
            }

            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error) {
            Log.e("TAG","Error");
            Toast.makeText(getActivity(), "problema di connessione", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View v) {
        int position = v.getId();
        dialogModifyReply(position);
        mAdapter.notifyDataSetChanged();
    }

    private void dialogModifyReply(int position) {
        final EditText edittext = new EditText(getContext());
        Reply replyToEdit = repliesList.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Scrivi il nuovo testo della risposta");
        builder.setMessage("Enter Your New Message");

        builder.setView(edittext);
        builder.setPositiveButton("MODIFICA", (dialog, which) -> {
            if(!deleteReply(position, null))
                return;

            replyToEdit.setMessage(edittext.getText().toString());

            try {
                Status tweetStat = Utils.postOnTwitter(replyToEdit.formatToString());
                replyToEdit.setTwitterId(tweetStat.getId());
                repliesList.add(replyToEdit);
                Toast.makeText(getActivity(), "Risposta modificata con successo", Toast.LENGTH_SHORT).show();

            } catch (TwitterException | JSONException e) {
                e.printStackTrace();
            }
        });
        builder.setNeutralButton("Cancella Risposta", (dialog, which) -> deleteReply(position, null));
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();

        dialog.show();
    }

    private boolean deleteReply(int position, Reply reply) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Status status;
        try {
            if(position != -1){
                status = twitter.tweets().destroyStatus(repliesList.get(position).getTwitterId());
                repliesList.remove(position);
            }
            else{
                status = twitter.tweets().destroyStatus(reply.getTwitterId());}
            mAdapter.notifyDataSetChanged();
            return (status != null);
        }
        catch (TwitterException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private static class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder>{
        Integer mExpandedPosition = -1;

        private ArrayList<Reply> messageList;
        private  View.OnClickListener buttonClickListener;
        private AvailableObjectsData mainObject;

        public static class RepliesViewHolder extends RecyclerView.ViewHolder {

            private TextView replierName;
            private TextView replyMessage;
            private Button replyButton;
            private TextView objectRepliedTo;
            public RepliesViewHolder(View view) {
                super(view);
                objectRepliedTo = view.findViewById(R.id.object_replied_to);
                replierName = view.findViewById(R.id.replier_name);
                replyMessage = view.findViewById(R.id.reply_message);
                replyButton = view.findViewById(R.id.reply_button);
            }
        }

        public RepliesAdapter(ArrayList<Reply> myDataset, View.OnClickListener onClickListener) {
            messageList = myDataset;
            this.buttonClickListener = onClickListener;
        }

        @NotNull
        @Override
        public RepliesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reply, parent, false);
            return new RepliesViewHolder(v);
        }

        @Override
        public void onBindViewHolder(RepliesViewHolder holder, final int position) {
            Reply message = messageList.get(position);
            String messageText = "\"" + message.getMessage()+ "\"";
            holder.replyMessage.setText(messageText);
            String sentTo = "Inviato a " + message.getReceiver();
            holder.replierName.setText(sentTo);
            holder.replyButton.setText("Modifica");
            holder.replyButton.setOnClickListener(buttonClickListener);
            holder.replyButton.setId(position);
            if(message.getObjectRepliedTo() != null) {
                String interestedObj = "riguardante: "+ message.getObjectRepliedTo().getName();
                holder.objectRepliedTo.setText(interestedObj);
            }

            //gestione dell'espansione dell'item
            final boolean isExpanded = position == mExpandedPosition;
            //holder.chatButton.setVisibility(isExpanded ? View.VISIBLE:View.GONE);
            holder.itemView.setActivated(isExpanded);
            holder.itemView.setOnClickListener(v -> {
                mExpandedPosition = isExpanded ? -1:position;
                notifyItemChanged(position);
            });
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }
}