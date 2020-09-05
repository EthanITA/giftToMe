package com.gifttome.gifttome;


import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class RepliesToMeFragment extends Fragment implements View.OnClickListener{

    public RepliesToMeFragment() {
        // Required empty public constructor
    }

    ArrayList<Reply> repliesList = new ArrayList<>();

    private RepliesAdapter mAdapter;
    private MainActivity mainActivity;
    private UserTimeline userTimeline;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View thisFragment = inflater.inflate(R.layout.fragment_chats, container, false);

        mainActivity = (MainActivity) getActivity();

        RecyclerView repliesRecyclerView = thisFragment.findViewById(R.id.replies_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        repliesRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RepliesAdapter(repliesList, this);
        repliesRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        getRepliesTweets();

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
            repliesList.clear();

            for (Tweet tweet : tweets){
                String jsonString = tweet.text; //this is the body

                //se il tweet non è una risposta viene ignorato
                if(!jsonString.contains("#LAM_giftToMe_2020 -reply"))
                    continue;

                try {
                    assert mainActivity != null;
                    JSONObject jsonObject = new JSONObject(jsonString);
                    UUID targetId = UUID.fromString(jsonObject.get("target").toString());

                    for (AvailableObjectsData myObject :
                            mainActivity.getMyPosts()) {
                        if (myObject.getId().equals(targetId) ) {
                            String sender = jsonObject.get("sender").toString();
                            Log.i("cred", sender);

                            String receiver = jsonObject.get("receiver").toString();
                            Log.i("cred", "kem");
                            UUID id = UUID.fromString(jsonObject.get("id").toString());
                            String message = jsonObject.get("message").toString();

                            Reply reply = new Reply(id, sender, targetId, receiver, message);
                            reply.setTwitterId(tweet.getId());
                            reply.setReplyToId(tweet.inReplyToStatusId);
                            reply.setObjectRepliedTo(myObject);

                            repliesList.add(reply);

                            if (!sender.equals("") && !receiver.equals(""))
                                repliesList.add(reply);
                            Log.v("credd1RepliesToMe", String.valueOf(repliesList.size()));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;
            }
            mAdapter.notifyDataSetChanged();

            //da ricontrollare
            if (searchResult.data.items.size() == 200) {
                userTimeline.previous(maxId, callback);
            }
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
        //do stuff (todo)
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.nav_single_chat);

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

            holder.replyMessage.setText(message.getMessage());
            holder.replierName.setText(message.getSender());
            holder.objectRepliedTo.setText(message.getObjectRepliedTo().getName());
            holder.replyButton.setText("do stuff (answer)");
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