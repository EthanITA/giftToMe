package com.gifttome.gifttome;

import android.os.StrictMode;

import twitter4j.Status;
import twitter4j.TwitterFactory;

public class Utils {

    //posta il testo passatogli su twitter
    public static Status postOnTwitter(String text) throws twitter4j.TwitterException {

        twitter4j.Twitter twitter = TwitterFactory.getSingleton();

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //Twitter twitter = TwitterFactory.getSingleton();
        Status status = twitter.updateStatus(text);
        System.out.println("Successfully updated the status to [" + status.getText() + "].");

        return status;
    }
}
