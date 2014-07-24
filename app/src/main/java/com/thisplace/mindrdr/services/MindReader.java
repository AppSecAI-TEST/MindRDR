package com.thisplace.mindrdr.services;

/**
 * Created by nickgerig on 19/07/2014.
 */
public interface MindReader {

    public void start();
    public void stop();
    public void close();
    public void connect(Boolean rawEnabled);
}
