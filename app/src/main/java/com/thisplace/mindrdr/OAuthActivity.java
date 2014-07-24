package com.thisplace.mindrdr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.thisplace.mindrdr.model.OAuthData;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class OAuthActivity extends Activity {

    private OAuthData mOAuthData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth);
        mOAuthData = OAuthData.getInstance();

        if(mOAuthData.getInternalData(this, mOAuthData.ACCESS_TOKEN) == null) {

            //no oauth data stored so we need to scan a QRCode
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);

        }else{

            //populate getters:
            mOAuthData.setAccessToken(this, mOAuthData.getInternalData(this, mOAuthData.ACCESS_TOKEN));
            mOAuthData.setAccessTokenSecret(this, mOAuthData.getInternalData(this, mOAuthData.ACCESS_TOKEN_SECRET));
            Log.d(this.getClass().getSimpleName(),"accessToken:"+mOAuthData.getAccessToken());
            Log.d(this.getClass().getSimpleName(),"accessTokenSecret:"+mOAuthData.getAccessTokenSecret());
            Log.i(this.getClass().getSimpleName(),"OAuth data already set");
            finaliseIntent(true);
        }

    }

    protected boolean verifyTwitterCredentials()
    {
        Twitter twitter;
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(mOAuthData.getConsumerKey());
        cb.setOAuthConsumerSecret(mOAuthData.getConsumerSecret());
        cb.setOAuthAccessToken(mOAuthData.getAccessToken());
        cb.setOAuthAccessTokenSecret(mOAuthData.getAccessTokenSecret());


        TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();

        try {
            User user = twitter.verifyCredentials();
            return true;

        } catch (TwitterException te) {
            return false;
        }
    }

    protected void finaliseIntent(Boolean success)
    {
        if(success){
            setResult(RESULT_OK);

        }else{
            setResult(RESULT_CANCELED);
        }

        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {

        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

                Log.i(this.getClass().getSimpleName(),"Scan success");

                String contents = intent.getStringExtra("SCAN_RESULT");
                String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
                Log.i(this.getClass().getSimpleName(),contents);

                String[] consumer_oauth = contents.split("\\|");
                String access_token = consumer_oauth[0];
                String access_token_secret = consumer_oauth[1];

                mOAuthData.setInternalData(this, mOAuthData.ACCESS_TOKEN, access_token);
                mOAuthData.setAccessToken(this,access_token);
                mOAuthData.setInternalData(this, mOAuthData.ACCESS_TOKEN_SECRET, access_token_secret);
                mOAuthData.setAccessTokenSecret(this,access_token_secret);

                finaliseIntent(true);

            } else if (resultCode == RESULT_CANCELED) {

                Log.i(this.getClass().getSimpleName(),"Scan failed");
                finaliseIntent(false);

            }
        }

    }


}
