package com.thisplace.mindrdr.services;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import com.neurosky.thinkgear.TGDevice;

/**
 * Created by nickgerig on 22/07/2014.
 */
public class MindWaveMobileMindReader implements MindReader {

    private TGDevice tgDevice;

    public MindWaveMobileMindReader(BluetoothAdapter bluetoothAdaptor, Handler handler) {

        tgDevice = new TGDevice(bluetoothAdaptor, handler);

    }

    @Override

    public void start() {
        tgDevice.start();
    }

    @Override
    public void stop() {
        tgDevice.stop();
    }

    @Override
    public void close() {
        tgDevice.close();
    }

    @Override
    public void connect(Boolean rawEnabled) {
        tgDevice.connect(rawEnabled);
    }
}
