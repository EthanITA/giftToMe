package com.gifttome.gifttome;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Process;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.UserTimeline;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

public class BackgroundRepliesService extends Service {
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private ArrayList<AvailableObjectsData> myPosts = new ArrayList<>();
    private ArrayList<String>  replies = new ArrayList<>();
    private String username;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            while(myPosts.size() > 0) {
                checkForRepliesTwitter(getApplicationContext(), myPosts, replies, username);
                try {
                    Thread.sleep(5000);
                    stopSelf();

                } catch (InterruptedException e) {
                    Log.e("rerror", "onHandleWork: ");
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        createNotificationChannel();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(), "ServiceStarted", Toast.LENGTH_SHORT).show();

        username = intent.getStringExtra("username");
        Gson gson = new Gson();
        String jsonMyPosts = intent.getStringExtra("myPosts");
        if(jsonMyPosts != null) {
            Type type = new TypeToken<ArrayList<AvailableObjectsData>>() {
            }.getType();
            myPosts = gson.fromJson(jsonMyPosts, type);
        }
        String jsonReplies = intent.getStringExtra("repliesUUID");
        if(jsonReplies != null) {
            Log.i("repliesUUID", jsonReplies);
            Type type = new TypeToken<ArrayList<String>>() {
            }.getType();
            replies = gson.fromJson(jsonReplies, type);
        }
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;

        serviceHandler.sendMessage(msg);

        //Se il processo viene ucciso viene richiamato
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void checkForRepliesTwitter(Context context, ArrayList<AvailableObjectsData> postlist, ArrayList<String> alreadycheckedreply, String username) {
        Twitter twitter = TwitterFactory.getSingleton();
        UserTimeline userTimeline = new UserTimeline.Builder().screenName("GiftToME5")
                .includeRetweets(false)
                .maxItemsPerRequest(200)
                .build();
        Toast.makeText(getApplicationContext(), "ricerca della posizione non supportata", Toast.LENGTH_SHORT).show();

        userTimeline.next(null, new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> searchResult) {
                List<Tweet> tweets = searchResult.data.items;
                long maxId = 0;
                UUID repliedToId;
               for (Tweet tweet : tweets){
                    String jsonString = tweet.text;
                    if(jsonString.contains("#LAM_giftToMe_2020-article"))
                        continue;

                    for (AvailableObjectsData myPost: postlist ){
                        try {
                            JSONObject jsonObject = new JSONObject(jsonString);
                            String replyId = jsonObject.get("id").toString();
                            UUID targetId = UUID.fromString(jsonObject.get("target").toString());
                            String sender = jsonObject.get("sender").toString();

                            //se ho una risposta nuova
                            if(myPost.getId().equals(targetId) && !alreadycheckedreply.contains(replyId) && !sender.equals(username)){
                                Log.i("tag", "success: risposta trovata" + replyId);
                                String textReply = String.valueOf(jsonObject.get("message"));
                                sendNotification(context, myPost, textReply);
                                replies.add(replyId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }}
            }
            @Override
            public void failure(com.twitter.sdk.android.core.TwitterException error) {
            }
        });
    }

    public static void sendNotification(Context context, AvailableObjectsData myNotifiedPost,
                                        String replyText){
        //tap intent todo
        int notificationId = 5213;
        Log.i("dentro notification", "sendNotification: ");
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, notificationIntent, 0);

        //create notification
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, String.valueOf(R.string.CHANNEL_ID));
        mBuilder.setContentTitle("Hai ricevuto un nuovo messaggio per l'oggetto " + myNotifiedPost.getName() + "!")
                .setContentText(replyText)
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false);

        //send notification
        notificationManagerCompat.notify(notificationId, mBuilder.build());
        notificationId++;
    }

    private void createNotificationChannel() {
        // notification channel necessario per api 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(getString(R.string.CHANNEL_ID), name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}