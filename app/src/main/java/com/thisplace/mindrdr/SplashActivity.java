package com.thisplace.mindrdr;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class SplashActivity extends Activity {

    private ImageView mSplash;
    private ImageView mInside;
    private ImageView mBlackBg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSplash = (ImageView) findViewById(R.id.splash);
        mInside = (ImageView) findViewById(R.id.inside);
        mBlackBg = (ImageView) findViewById(R.id.black_bg);

        showSplash();
    }

    // intro splash screen fades

    private void showSplash() {

        mSplash.setVisibility(View.INVISIBLE);
        mInside.setVisibility(View.VISIBLE);
        mInside.setImageAlpha(0);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animation = ObjectAnimator.ofInt(mInside, "ImageAlpha", 0, 100);
        animation.setDuration(700);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeOutInside();
            }
        });

        set.play(animation);
        set.start();
    }

    private void fadeOutInside() {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animation = ObjectAnimator.ofInt(mInside, "ImageAlpha", 100, 0);
        animation.setDuration(700);
        animation.setStartDelay(2000);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                fadeInSplash();
            }
        });

        set.play(animation);
        set.start();
    }

    private void fadeInSplash() {
        mSplash.setImageAlpha(0);
        mSplash.setVisibility(View.VISIBLE);
        mInside.setVisibility(View.INVISIBLE);

        AnimatorSet set = new AnimatorSet();
        // Using property animation
        ObjectAnimator animation = ObjectAnimator.ofInt(mSplash, "ImageAlpha", 0, 100);
        animation.setDuration(700);
        animation.setStartDelay(1000);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setResult(RESULT_OK);
                finish();
            }
        });
        set.play(animation);
        set.start();
    }



}
