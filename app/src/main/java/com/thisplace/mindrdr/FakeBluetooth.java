package com.thisplace.mindrdr;

import android.os.Handler;
import android.os.Message;

import com.neurosky.thinkgear.TGDevice;

public class FakeBluetooth implements Runnable {

    private Handler mHandler;

    public FakeBluetooth(Handler handler) {

        mHandler = handler;

    }

    @Override
    public void run() {

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

        mHandler.postDelayed(this, 1000);
    }


}
