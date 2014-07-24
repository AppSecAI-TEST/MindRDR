package com.thisplace.mindrdr.services;

import android.bluetooth.BluetoothAdapter;
import android.os.Handler;

import com.neurosky.thinkgear.TGDevice;

/**
 * Created by nickgerig on 19/07/2014.
 */
public class MindReaderFactory {


    private MindReaderFactory() {
    }

    public static MindReader getMindReader(BluetoothAdapter bluetoothAdaptor, Handler handler) {

        if(bluetoothAdaptor == null) {
            return new FakeMindReader(handler);
        }else{
            return new MindWaveMobileMindReader(bluetoothAdaptor,handler);
        }


    }


}
