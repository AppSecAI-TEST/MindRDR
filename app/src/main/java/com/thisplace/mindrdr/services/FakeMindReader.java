package com.thisplace.mindrdr.services;

import android.os.Handler;
import android.os.Message;

import com.neurosky.thinkgear.TGDevice;
import com.thisplace.mindrdr.services.MindReader;

public class FakeMindReader implements Runnable, MindReader {

    private Handler mHandler;
    private Boolean mConnected = false;
    private Boolean mStarted = false;
    private int mCount = 0;
    private Boolean mClosed = false;

    public FakeMindReader(Handler handler) {

        mHandler = handler;

    }

    @Override
    public void run() {
        if(!mClosed) {

            if(!mConnected) {

                connecting();

            }else if(mStarted) {

                doBrainReading();

            }

            mHandler.postDelayed(this, 1000);

        }

    }

    public void start() {
        mStarted = true;
    }

    public void stop() {
        mStarted = false;
    }

    public void close() {
        mConnected = false;
        mStarted = false;
        mCount = 0;
        mClosed = true;
    }

    public void connect(Boolean rawEnabled) {
        mConnected = false;
        mStarted = false;
        mCount = 0;
    }

    private void doBrainReading()
    {

        Message msg = new Message();

        msg.what = TGDevice.MSG_ATTENTION;
        msg.arg1 = 40 + (int) Math.round(Math.random() * 50);

        mHandler.handleMessage(msg);

        msg.what = TGDevice.MSG_MEDITATION;
        msg.arg1 = 30 + (int) Math.round(Math.random() * 40);

        mHandler.handleMessage(msg);

        msg.what = TGDevice.MSG_HEART_RATE;
        msg.arg1 = (int) Math.round(Math.random() * 160) + 40;

        mHandler.handleMessage(msg);
    }

    private void connecting()
    {
        mCount++;

        if(mCount > 5)
        {
            mConnected = true;

            Message msg = new Message();

            msg.what = TGDevice.MSG_STATE_CHANGE;
            msg.arg1 = TGDevice.STATE_CONNECTED;

            mHandler.handleMessage(msg);
        }
    }


}
