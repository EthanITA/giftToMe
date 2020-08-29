package com.gifttome.gifttome;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class RepliesFragment extends Fragment {

    public RepliesFragment() {
        // Required empty public constructor
    }

    ArrayList<Reply> repliesList = new ArrayList<>();

    private RecyclerView repliesRecyclerView;
    private RepliesAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    UserTimeline userTimeline;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View thisFragment = inflater.inflate(R.layout.fragment_chats, container, false);


        MainActivity mainActivity = (MainActivity) getActivity();

        getRepliesTweets();
        repliesRecyclerView = thisFragment.findViewById(R.id.replies_recycler_view);
        repliesRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new RepliesAdapter(repliesList);
        //mAdapter.setClickListener(this::onClick);
        repliesRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

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
                    MainActivity mainActivity = (MainActivity) getActivity();
                    assert mainActivity != null;

                    JSONObject jsonObject = new JSONObject(jsonString);
                    UUID targetId = UUID.fromString(jsonObject.get("target").toString());

                    for (AvailableObjectsData myObject :
                            mainActivity.getMyPosts()) {

                        if (myObject.getId().equals(targetId)) {
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

                            Log.v("TagSuccc", "str");

                            //DA MODIFICARE CONDIZIONI CON CUI FILTRARE I POST USANDO ID
                            if (!sender.equals("") && !receiver.equals(""))
                                repliesList.add(reply);
                            Log.v("credd1", String.valueOf(repliesList.size()));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                maxId = tweet.id;

            }

            //mAdapter.notifyDataSetChanged();
            Log.v("credd2", String.valueOf(repliesList.size()));

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

    private static class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.RepliesViewHolder>{
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
        public class RepliesViewHolder extends RecyclerView.ViewHolder {

            private View view;
            private TextView objectRepliedTo;
            private TextView replierName;
            private TextView replyMessage;
            private Button replyButton;


            public RepliesViewHolder(View view) {
                super(view);
                this.view = view;
                objectRepliedTo = view.findViewById(R.id.object_replied_to);
                replierName = view.findViewById(R.id.replier_name);
                replyMessage = view.findViewById(R.id.reply_message);
            }
        }


        // Provide a suitable constructor (depends on the kind of dataset)
        public RepliesAdapter(ArrayList<Reply> myDataset) {

            messageList = myDataset;
            //this.buttonClickListener = onClickListener;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public RepliesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.reply_on_my_post, parent, false);
                return new RepliesViewHolder(v);        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(RepliesViewHolder holder, final int position) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            Reply message = messageList.get(position);

            holder.replyMessage.setText(message.getMessage());
            holder.replierName.setText(message.getSender());
            holder.objectRepliedTo.setText(message.getObjectRepliedTo().getName());


            /*holder.itemView.setOnClickListener(new View.OnClickListener() {
             */


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
