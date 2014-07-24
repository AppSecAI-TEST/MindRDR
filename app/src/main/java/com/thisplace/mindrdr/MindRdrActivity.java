package com.thisplace.mindrdr;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.neurosky.thinkgear.TGDevice;
import com.thisplace.mindrdr.model.MindSession;
import com.thisplace.mindrdr.services.MindReader;
import com.thisplace.mindrdr.services.MindReaderFactory;
import com.thisplace.mindrdr.model.OAuthData;
import com.thisplace.mindrdr.view.CameraFlashFragment;
import com.thisplace.mindrdr.view.MindReaderFragment;
import com.thisplace.mindrdr.view.PhotoPreviewFragment;
import com.thisplace.mindrdr.view.UploadingFragment;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class MindRdrActivity extends Activity {

    public final static String DEBUG_TAG = "MindRdrActivity";

    /*
    if FAKE_BLUETOOTH is true then no connection attempt will be made to the mind reader and all data will be simulated.
     */
    private static final boolean FAKE_BLUETOOTH = false;
    private static final boolean RAW_ENABLED = false;

    //ACTIVITY REQUEST CODES
    private static final int SPLASH_SCREEN_REQUEST_CODE = 0;
    private static final int OAUTH_REQUEST_CODE = 1;
    private static final int INTENSITY_TRIGGER = 80;
    private static final int INTENSITY_CANCEL_TRIGGER = 10;
    private static final int MIND_TAKING_PHOTO = 0;
    private static final int MIND_SHARING = 1;
    private static final int MIND_DISABLED = 2;
    private static final int MIND_INITIALISING = 3;
    private int mMindControlState = MIND_INITIALISING;
    BluetoothAdapter bluetoothAdapter;
    MindReader mindReaderService;
    private AudioManager mAudioManager;

    private Status mTwitterUpdateId = null;
    private MindSession mMindSession = new MindSession();
    private MindReaderFragment mMindFragment;
    private PhotoPreviewFragment mPhotoPreviewFragment;
    private UploadingFragment mUploadingFragment;
    private CameraFlashFragment mCameraFlashFragment;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            Log.d("DEBUG", "Connecting...");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            Log.d("DEBUG", "Connected.");
                            mindReaderService.start();
                            initializeCamera();
                     
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            Log.d("DEBUG", "Can't find");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            Log.d("DEBUG", "not paired");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            Log.d("DEBUG", "Disconnected mang");
                    }

                    break;
                case TGDevice.MSG_ATTENTION:
                    //Log.d("Intensity", Integer.toString(msg.arg1));
                    mMindSession.setAttention(msg.arg1);
                    updateView();
                    break;
                case TGDevice.MSG_MEDITATION:
                    mMindSession.setMeditation(msg.arg1);
                    break;

                case TGDevice.MSG_HEART_RATE:
                    mMindSession.setHeartRate(msg.arg1);
                    break;

                default:
                    break;
            }
        }
    };

    private String mPhotoFile;
    private File mPictureFile;
    public PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            mPictureFile = getOutputMediaFile();
            if (mPictureFile == null) {
                Log.d(DEBUG_TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(mPictureFile);
                fos.write(data);
                fos.close();
                mPhotoFile = mPictureFile.getAbsolutePath();

            } catch (FileNotFoundException e) {
                Log.d(DEBUG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(DEBUG_TAG, "Error accessing file: " + e.getMessage());
            }

            releaseCamera();
            showPhotoView();
        }

    };

    private Camera mCamera;
    private CameraPreview mPreview;


    private static File getOutputMediaFile() {

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "mindrdr");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("mindrdr", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "MindRDR" + timeStamp
                + ".jpg");

        return mediaFile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_mind_camera);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


        Intent intent = new Intent(this, SplashActivity.class);
        startActivityForResult(intent,SPLASH_SCREEN_REQUEST_CODE);

        //initializeCamera();

        //setupMindReader();

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPLASH_SCREEN_REQUEST_CODE) {

            Intent intent = new Intent(this, OAuthActivity.class);
            startActivityForResult(intent,OAUTH_REQUEST_CODE);

        }else if (requestCode == OAUTH_REQUEST_CODE) {

            Log.d("MindRdrActivity","OAUTH Activity Finished");

            if(resultCode == RESULT_OK) {


                setupMindReader();

                mMindFragment = MindReaderFragment.newInstance("Take Photo","Cancel");
                getFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, mMindFragment).commit();

            }else{

                Log.d("MindRdrActivity","OAUTH Activity Failed to scan data");

            }




        }
    }


    private void upload() {
        showUploadingView();
        new PhotoUploaderAsyncTask().execute(mPhotoFile);

    }

    private void initializeCamera() {
        // Create an instance of Camera
        Log.d("MindRdrActivity","initialiseCamera");


        if(mCamera == null){

        mCamera = Camera.open();

        // Create our Preview view and set it as the content of our activity.

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview);

        }


    }

    private void releaseCamera() {


        Log.d("MindRdrActivity","releaseCamera");


        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mPreview = null;


        }
    }

    private void initialiseMindReadings() {

        mMindSession = null;
        mMindSession = new MindSession();

    }



    private void updateView() {
        // check we are getting good values before hiding splash
        if (mMindControlState == MIND_INITIALISING) {
            if (mMindSession.getAttention() > 0) {

                initialiseMindReadings();
                mMindControlState = MIND_TAKING_PHOTO;

            }
            return;
        }

        if (mMindControlState != MIND_DISABLED) {
            mMindSession.updateMindData();
            mMindFragment.updateMindReading(mMindSession.getAttention());

            if (mMindSession.getAttention() >= INTENSITY_TRIGGER) {
                if (mMindControlState == MIND_TAKING_PHOTO) {
                    mMindControlState = MIND_DISABLED;
                    takePhoto();

                } else if (mMindControlState == MIND_SHARING) {
                    mMindControlState = MIND_DISABLED;
                    if (mindReaderService != null) {
                        mindReaderService.close();
                        mindReaderService = null;
                    }
                    upload();
                }
            } else if (mMindSession.getAttention() <= INTENSITY_CANCEL_TRIGGER && mMindControlState == MIND_SHARING) {

                initializeCamera();
                showTakePhotoView();
            }

        }

    }

    private void setupMindReader() {

        if (FAKE_BLUETOOTH == true) {

            mindReaderService = MindReaderFactory.getMindReader(null, handler);
            handler.postDelayed((Runnable) mindReaderService, 1000);
            return;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();

        } else {
            /* create the TGDevice */
            mindReaderService = MindReaderFactory.getMindReader(bluetoothAdapter, handler);
            mindReaderService.connect(RAW_ENABLED);


        }

    }

    @Override
    public void onDestroy() {
        try {
            if (mindReaderService != null) {
                mindReaderService.close();
                mindReaderService = null;
            }

            releaseCamera();

        } catch (NullPointerException e) {

        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {

        super.onPause();
        releaseCamera();

    }

    @Override
    public void onStop() {
        try {
            if (mindReaderService != null) {
                mindReaderService.close();
                mindReaderService = null;
            }

            releaseCamera();

        } catch (NullPointerException e) {

        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
        if (mCamera == null) {
            initializeCamera();
        }
        */
    }


    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Log.d("MindRdrActivity","Back detected shutting down");
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }


    private void takePhoto() {

        mAudioManager.playSoundEffect(Sounds.SUCCESS);
        mCamera.takePicture(null, null, mPicture);
        doCameraFlash();
    }

    private void showPhotoView() {


        mPhotoPreviewFragment = PhotoPreviewFragment.newInstance();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mPhotoPreviewFragment);
        transaction.commit();

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                showUploadView();
            }
        }.start();
    }

    private void showUploadView() {

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(mCameraFlashFragment);
        transaction.commit();
        mCameraFlashFragment = null;

        mMindFragment = MindReaderFragment.newInstance("Post to Twitter","Take another photo");
        transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, mMindFragment);
        transaction.commit();

        mMindControlState = MIND_SHARING;

    }

    private void showUploadingView() {

        mMindFragment.hideActionText();
        mUploadingFragment = UploadingFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mUploadingFragment).commit();

    }


    private void showSuccessView() {

        mUploadingFragment.showSuccess();
        initializeCamera();

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {

                showTakePhotoView();
            }
        }.start();

    }

    private void showTakePhotoView() {

        mMindFragment = null;
        mMindFragment = MindReaderFragment.newInstance("Take Photo","Cancel");
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.remove(mUploadingFragment);
        mUploadingFragment = null;
        transaction.replace(R.id.fragment_container, mMindFragment);
        transaction.commit();

        mMindControlState = MIND_INITIALISING;

        setupMindReader();

    }

    private void doCameraFlash() {

        mCameraFlashFragment = CameraFlashFragment.newInstance();
        getFragmentManager().beginTransaction()
                .add(R.id.fragment_container, mCameraFlashFragment).commit();

    }

    private class PhotoUploaderAsyncTask extends AsyncTask<String, Integer, String> {

        int serverResponseCode = 0;
        Twitter twitter;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

                return uploadToTwitter();

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // uploadResult(result.toString());
            showSuccessView();
        }

        private void checkExternalMedia() {
            boolean mExternalStorageAvailable = false;
            boolean mExternalStorageWriteable = false;
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // Can read and write the media
                mExternalStorageAvailable = mExternalStorageWriteable = true;
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // Can only read the media
                mExternalStorageAvailable = true;
                mExternalStorageWriteable = false;
            } else {
                // Can't read or write
                mExternalStorageAvailable = mExternalStorageWriteable = false;
            }
            Log.d("DEBUG", "External Media: readable=" + mExternalStorageAvailable + " writable="
                    + mExternalStorageWriteable);
        }

        public String uploadToTwitter() {

            OAuthData oAuthData = OAuthData.getInstance();

            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(oAuthData.getConsumerKey());
            cb.setOAuthConsumerSecret(oAuthData.getConsumerSecret());
            cb.setOAuthAccessToken(oAuthData.getAccessToken());
            cb.setOAuthAccessTokenSecret(oAuthData.getAccessTokenSecret());


            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();


            String[] mindrdrRecord = createMindrdrRecord().split("\\|");
            String tweet = mindrdrRecord[1];
            String mindrdrRecordId = mindrdrRecord[0];
            Log.d("mindrdrRecordId", mindrdrRecord[0]);

            try {
                //User user = twitter.verifyCredentials();
                StatusUpdate status = new StatusUpdate(tweet);
                status.setMedia(mPictureFile);
                twitter4j.Status tStatus = twitter.updateStatus(status);
                Log.d("Twitter", String.valueOf(tStatus.getId()));
                updateRecordWithTweetId(mindrdrRecordId, String.valueOf(tStatus.getId()));

            } catch (TwitterException te) {
                Log.d("Error", te.getMessage().toString());
                mTwitterUpdateId = null;
            }
            //we just ignore all errors for the moment and pretend everything is ok to the user
            return "OK";

        }

        private String updateRecordWithTweetId(String recordId, String tweetId) {

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://mindrdr-service");

            try {
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("recordId", recordId));
                nameValuePairs.add(new BasicNameValuePair("tweetId", tweetId));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpclient.execute(httppost);
                Log.d("updateRecordWithTweetId", response.toString());

            } catch (ClientProtocolException e) {
                Log.d("updateRecordWithTweetId", e.toString());
                return "Fail";
            } catch (IOException e) {
                Log.d("updateRecordWithTweetId", e.toString());
                return "Fail";
            }

            return "Success";
        }

        private String createMindrdrRecord() {


            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://mindrdr-service");

            try {

                httppost.setEntity(new UrlEncodedFormEntity(mMindSession.toArrayList()));

                HttpResponse response = httpclient.execute(httppost);
                String responseBody = EntityUtils.toString(response.getEntity());
                Log.d("createMindrdrRecord", responseBody);
                return responseBody;

            } catch (ClientProtocolException e) {
                Log.d("createMindrdrRecordError", e.toString());
                return "Fail";
            } catch (IOException e) {
                Log.d("createMindrdrRecordError", e.toString());
                return "Fail";
            }

        }




    }

}
