package com.gifttome.gifttome;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class RepliesToMeFragment extends Fragment implements View.OnClickListener{

    public RepliesToMeFragment() {
        // Required empty public constructor
    }

    ArrayList<Reply> repliesList = new ArrayList<>();

    private RepliesAdapter mAdapter;
    private MainActivity mainActivity;
    private UserTimeline userTimeline;
    private String username;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View thisFragment = inflater.inflate(R.layout.fragment_responses_to_me, container, false);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("shared preferences", Context.MODE_PRIVATE);
        username = sharedPreferences.getString("username", null);

        mainActivity = (MainActivity) getActivity();

        RecyclerView repliesRecyclerView = thisFragment.findViewById(R.id.replies_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        repliesRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RepliesAdapter(repliesList, this);
        repliesRecyclerView.setAdapter(mAdapter);

        getRepliesTweets(true);
        mAdapter.notifyDataSetChanged();

        return thisFragment;
    }

    private void getRepliesTweets(boolean toObjects) {
        userTimeline = new UserTimeline.Builder()
                .screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        if (toObjects)
            userTimeline.next(null, callbackToObjects);
        else
            userTimeline.next(null, callbackToReplies );
    }
    Callback<TimelineResult<Tweet>> callbackToObjects = new Callback<TimelineResult<Tweet>>()
    {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult)
        {
            List<Tweet> tweets = searchResult.data.items;
            long maxId = 0;
            repliesList.clear();
            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //this is the body

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;

                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    UUID targetId = UUID.fromString(jsonObject.get("target").toString());
                    String receiver= jsonObject.get("receiver").toString();
                    String sender = jsonObject.get("sender").toString();
                    for (AvailableObjectsData myObject :
                         mainActivity.getMyPosts()) {
                        if (myObject.getId().equals(targetId) && !username.equals(sender)) {
                            UUID id = UUID.fromString(jsonObject.get("id").toString());
                            String message = jsonObject.get("message").toString();

                            Reply reply = new Reply(id, sender, targetId, receiver, message);
                            reply.setTwitterId(tweet.getId());
                            reply.setReplyToId(tweet.inReplyToStatusId);
                            reply.setObjectRepliedTo(myObject);

                            repliesList.add(reply);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
            getRepliesTweets(false);
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error)
        {
            Log.e("TAG","Error");
            Toast.makeText(getActivity(), "problema di connessione", Toast.LENGTH_SHORT).show();
        }
    };

Callback<TimelineResult<Tweet>> callbackToReplies = new Callback<TimelineResult<Tweet>>()
    {
        @Override
        public void success(Result<TimelineResult<Tweet>> searchResult) {
            List<Tweet> tweets = searchResult.data.items;
            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //this is the body

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;

                try {
                    JSONObject jsonObject = new JSONObject(jsonString);
                    UUID targetId = UUID.fromString(jsonObject.get("target").toString());
                    String receiver= jsonObject.get("receiver").toString();

                    for (AvailableObjectsData availPosts :
                            mainActivity.getAvailablePosts()) {
                        if (availPosts.getId().equals(targetId) && username.equals(receiver)) {
                            String sender = jsonObject.get("sender").toString();
                            UUID id = UUID.fromString(jsonObject.get("id").toString());
                            String message = jsonObject.get("message").toString();

                            Reply reply = new Reply(id, sender, targetId, receiver, message);
                            reply.setTwitterId(tweet.getId());
                            reply.setReplyToId(tweet.inReplyToStatusId);
                            reply.setObjectRepliedTo(availPosts);

                            repliesList.add(reply);
                            Log.v("credd1RepliesToMe", String.valueOf(repliesList.size()));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mAdapter.notifyDataSetChanged();
        }
        @Override
        public void failure(com.twitter.sdk.android.core.TwitterException error)
        {
            Log.e("TAG","Error");
            Toast.makeText(getActivity(), "problema di connessione", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onClick(View v) {
        int i = v.getId();
        dialogCreateReplyToReply(repliesList.get(i));

    }

    private void dialogCreateReplyToReply(Reply replyToReplyTo) {
        //costruisco un dialog per la risposta
        final EditText edittext = new EditText(getContext());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle("Scrivi il testo della risposta");
        builder.setMessage("Enter Your Message");
        builder.setView(edittext);
        builder.setPositiveButton("INVIA", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Reply reply =  new Reply(UUID.randomUUID(), username, replyToReplyTo.getTargetid(),replyToReplyTo.getSender(), edittext.getText().toString());

                try {
                    Status tweetStat = Utils.postOnTwitter(reply.formatToString());
                    reply.setTwitterId(tweetStat.getId());
                    Toast.makeText(getActivity(), "Risposta avvenuta con successo", Toast.LENGTH_SHORT).show();
                    mainActivity.addMyRepliesUUID(reply.getId().toString());

                } catch (twitter4j.TwitterException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("annulla", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private static class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder>{
        Integer mExpandedPosition = -1;

        private ArrayList<Reply> messageList;
        private  View.OnClickListener buttonClickListener;
        private AvailableObjectsData mainObject;

        public static class RepliesViewHolder extends RecyclerView.ViewHolder {

            private TextView objectRepliedTo;
            private TextView replierName;
            private TextView replyMessage;
            private Button replyButton;

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
            return new RepliesViewHolder(v);        }

        @Override
        public void onBindViewHolder(RepliesViewHolder holder, final int position) {
            Reply message = messageList.get(position);
            String messageText = "\""+message.getMessage()+"\"";
            holder.replyMessage.setText(messageText);
            String sentFrom = "Inviato da: "+ message.getSender();
            holder.replierName.setText(sentFrom);
            if(message.getObjectRepliedTo() != null){
                String interestedObj = "riguardante "+message.getObjectRepliedTo().getName();
                holder.objectRepliedTo.setText(interestedObj);
            }

            holder.replyButton.setId(position);
            holder.replyButton.setOnClickListener(buttonClickListener);

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