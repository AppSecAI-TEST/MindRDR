package com.thisplace.mindrdr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.thisplace.mindrdr.R;
import com.thisplace.mindrdr.model.OAuthData;

import org.w3c.dom.Text;

public class TwitterConfigActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_config);

        Intent intent = getIntent();
        String token = intent.getStringExtra("token");
        String secret = intent.getStringExtra("secret");

        TextView secretTxt = (TextView) findViewById(R.id.secret_txt);
        TextView tokenTxt = (TextView) findViewById(R.id.token_txt);
        tokenTxt.setText("Token: "+token);
        secretTxt.setText("Secret:"+secret);

        OAuthData oAuthData = OAuthData.getInstance();
        oAuthData.setAccessToken(this,token);
        oAuthData.setAccessTokenSecret(this,secret);

        TextView finished_txt = (TextView) findViewById(R.id.finished_txt);
        finished_txt.setText("All Done!");
    }


    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }



}
