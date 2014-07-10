package com.thisplace.mindrdr.model;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class MindSession {

    private int attention = 0;

    private int previousAttention = 0;

    private int totalAttention = 0;

    private int totalAttentionChange = 0;

    private int biggestAttentionChange = 0;

    private int biggestAttention = 0;

    private int meditation = 0;

    private int previousMeditation = 0;

    private int totalMeditation = 0;

    private int totalMeditationChange = 0;

    private int biggestMeditationChange = 0;

    private int biggestMeditation = 0;

    private int heartRate = 0;

    private int previousHeartRate = 0;

    private int totalHeartRate = 0;

    private int totalHeartRateChange = 0;

    private int biggestHeartRateChange = 0;

    private int biggestHeartRate = 0;

    private int totalReadings = 0;

    public MindSession() {

    }


    public int getAvMeditation(){
        return Math.round(totalMeditation / totalReadings);
    }

    public int getAvAttention(){
        return Math.round(totalAttention / totalReadings);
    }

    public int getAvHeartRate(){
        return Math.round(totalHeartRate / totalReadings);
    }

    public int getAvAttentionChange(){
        return Math.round(totalAttentionChange / totalReadings);
    }

    public int getAvMeditationChange(){
        return Math.round(totalMeditationChange / totalReadings);
    }

    public int getAvHeartRateChange(){
        return Math.round(totalHeartRateChange / totalReadings);
    }

    public List<NameValuePair> toArrayList()
    {
        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("avAttention", String
                .valueOf(getAvAttention())));
        nameValuePairs.add(new BasicNameValuePair("avAttentionChange", String
                .valueOf(getAvAttentionChange())));
        nameValuePairs.add(new BasicNameValuePair("biggestAttentionChange", String
                .valueOf(getBiggestAttentionChange())));
        nameValuePairs.add(new BasicNameValuePair("biggestAttention", String
                .valueOf(getBiggestAttention())));

        nameValuePairs.add(new BasicNameValuePair("avMeditation", String
                .valueOf(getAvMeditation())));
        nameValuePairs.add(new BasicNameValuePair("avMeditationChange", String
                .valueOf(getAvMeditationChange())));
        nameValuePairs.add(new BasicNameValuePair("biggestMeditationChange", String
                .valueOf(biggestMeditationChange)));
        nameValuePairs.add(new BasicNameValuePair("biggestMeditation", String
                .valueOf(biggestMeditation)));

        nameValuePairs.add(new BasicNameValuePair("avHeartRate", String
                .valueOf(getAvHeartRate())));
        nameValuePairs.add(new BasicNameValuePair("avHeartRateChange", String
                .valueOf(getAvHeartRateChange())));
        nameValuePairs.add(new BasicNameValuePair("biggestHeartRateChange", String
                .valueOf(biggestHeartRateChange)));
        nameValuePairs.add(new BasicNameValuePair("biggestHeartRate", String
                .valueOf(biggestHeartRate)));

        return nameValuePairs;
    }

    public void updateMindData() {

        totalReadings++;

        totalAttention += attention;
        totalMeditation += meditation;
        totalHeartRate += heartRate;

        // meditation data
        int meditationChange = Math.abs(previousMeditation - meditation);
        totalMeditationChange += meditationChange;

        if (meditationChange > biggestMeditationChange) {
            biggestMeditationChange = meditationChange;
        }

        if (meditation > biggestMeditation) {
            biggestMeditation = meditation;
        }

        previousMeditation = meditation;

        // attention data

        int attentionChange = Math.abs(previousAttention - attention);
        totalAttentionChange += attentionChange;

        if (attentionChange > biggestAttentionChange) {
            biggestAttentionChange = attentionChange;
        }

        if (attention > biggestAttention) {
            biggestAttention = attention;
        }

        previousAttention = attention;

        // heart rate data

        int heartRateChange = Math.abs(previousHeartRate - heartRate);
        totalHeartRateChange += heartRateChange;

        if (heartRateChange > biggestHeartRateChange) {
            biggestHeartRateChange = heartRateChange;
        }

        if (heartRate > biggestHeartRate) {
            biggestHeartRate = heartRate;
        }

        previousHeartRate = heartRate;

    }

    public int getAttention() {
        return attention;
    }

    public void setAttention(int attention) {
        this.attention = attention;
    }

    public int getPreviousAttention() {
        return previousAttention;
    }

    public void setPreviousAttention(int previousAttention) {
        this.previousAttention = previousAttention;
    }

    public int getTotalAttention() {
        return totalAttention;
    }

    public void setTotalAttention(int totalAttention) {
        this.totalAttention = totalAttention;
    }

    public int getTotalAttentionChange() {
        return totalAttentionChange;
    }

    public void setTotalAttentionChange(int totalAttentionChange) {
        this.totalAttentionChange = totalAttentionChange;
    }

    public int getBiggestAttentionChange() {
        return biggestAttentionChange;
    }

    public void setBiggestAttentionChange(int biggestAttentionChange) {
        this.biggestAttentionChange = biggestAttentionChange;
    }

    public int getBiggestAttention() {
        return biggestAttention;
    }

    public void setBiggestAttention(int biggestAttention) {
        this.biggestAttention = biggestAttention;
    }

    public int getMeditation() {
        return meditation;
    }

    public void setMeditation(int meditation) {
        this.meditation = meditation;
    }

    public int getPreviousMeditation() {
        return previousMeditation;
    }

    public void setPreviousMeditation(int previousMeditation) {
        this.previousMeditation = previousMeditation;
    }

    public int getTotalMeditation() {
        return totalMeditation;
    }

    public void setTotalMeditation(int totalMeditation) {
        this.totalMeditation = totalMeditation;
    }

    public int getTotalMeditationChange() {
        return totalMeditationChange;
    }

    public void setTotalMeditationChange(int totalMeditationChange) {
        this.totalMeditationChange = totalMeditationChange;
    }

    public int getBiggestMeditationChange() {
        return biggestMeditationChange;
    }

    public void setBiggestMeditationChange(int biggestMeditationChange) {
        this.biggestMeditationChange = biggestMeditationChange;
    }

    public int getBiggestMeditation() {
        return biggestMeditation;
    }

    public void setBiggestMeditation(int biggestMeditation) {
        this.biggestMeditation = biggestMeditation;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getPreviousHeartRate() {
        return previousHeartRate;
    }

    public void setPreviousHeartRate(int previousHeartRate) {
        this.previousHeartRate = previousHeartRate;
    }

    public int getTotalHeartRate() {
        return totalHeartRate;
    }

    public void setTotalHeartRate(int totalHeartRate) {
        this.totalHeartRate = totalHeartRate;
    }

    public int getTotalHeartRateChange() {
        return totalHeartRateChange;
    }

    public void setTotalHeartRateChange(int totalHeartRateChange) {
        this.totalHeartRateChange = totalHeartRateChange;
    }

    public int getBiggestHeartRateChange() {
        return biggestHeartRateChange;
    }

    public void setBiggestHeartRateChange(int biggestHeartRateChange) {
        this.biggestHeartRateChange = biggestHeartRateChange;
    }

    public int getBiggestHeartRate() {
        return biggestHeartRate;
    }

    public void setBiggestHeartRate(int biggestHeartRate) {
        this.biggestHeartRate = biggestHeartRate;
    }

    public int getTotalReadings() {
        return totalReadings;
    }

    public void setTotalReadings(int totalReadings) {
        this.totalReadings = totalReadings;
    }

}
