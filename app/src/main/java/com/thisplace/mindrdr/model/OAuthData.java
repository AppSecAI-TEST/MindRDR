package com.thisplace.mindrdr.model;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

/**
 * Created by nickgerig on 15/07/2014.
 */
public class OAuthData {
    public static final String ACCESS_TOKEN = "AccessToken";
    public static final String ACCESS_TOKEN_SECRET = "AccessTokenSecret";
    private static OAuthData ourInstance = new OAuthData();
    private String consumerSecret = "****************";
    private String consumerKey = "*******************";
    private String accessToken = null;
    private String accessTokenSecret = null;

    private OAuthData() {


    }

    public static OAuthData getInstance() {
        return ourInstance;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getAccessToken() {

        return accessToken;
    }

    public void setAccessToken(Context context, String accessToken) {
        this.accessToken = accessToken;
        setInternalData(context, ACCESS_TOKEN, accessToken);
    }

    public String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    public void setAccessTokenSecret(Context context, String accessTokenSecret) {
        this.accessTokenSecret = accessTokenSecret;
        setInternalData(context, ACCESS_TOKEN_SECRET, accessTokenSecret);
    }

    public String getInternalData(Context context, String key) {


        StringBuffer stringBuffer = new StringBuffer();

        try {
            BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                    context.openFileInput(key)));
            String inputString;

            while ((inputString = inputReader.readLine()) != null) {
                stringBuffer.append(inputString);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return stringBuffer.toString();
    }


    public boolean setInternalData(Context context, String key, String data) {

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(key, Context.MODE_PRIVATE);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
