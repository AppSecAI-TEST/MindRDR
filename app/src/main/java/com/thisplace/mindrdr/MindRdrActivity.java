package com.thisplace.mindrdr;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.glass.media.Sounds;
import com.neurosky.thinkgear.TGDevice;
import com.thisplace.mindrdr.model.MindSession;

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

    public final static String DEBUG_TAG = "MindCameraActivity";

    /*
    if FAKE_BLUETOOTH is true then no connection attempt will be made to the mind reader and all data will be simulated.
     */
    private static final boolean FAKE_BLUETOOTH = true;
    private static final boolean RAW_ENABLED = false;
    private static final int LINE_START_Y = 80;
    private static final int LINE_END_Y = 280;
    private static final int LINE_RANGE = LINE_END_Y - LINE_START_Y;
    private static final int INTENSITY_TRIGGER = 80;
    private static final int LINE_VALUE_MULTIPLIER = LINE_RANGE / INTENSITY_TRIGGER;
    private static final int INTENSITY_CANCEL_TRIGGER = 10;
    private static final int MIND_TAKING_PHOTO = 0;
    private static final int MIND_SHARING = 1;
    private static final int MIND_DISABLED = 2;
    private static final int MIND_INITIALISING = 3;
    private int mMindControlState = MIND_INITIALISING;
    BluetoothAdapter bluetoothAdapter;
    TGDevice tgDevice;
    private AudioManager mAudioManager;
    private FakeBluetooth mFakeBluetooth;
    private Status mTwitterUpdateId = null;
    private MindSession mMindSession = new MindSession();


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
                            tgDevice.start();

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
                    Log.d("Intensity", Integer.toString(msg.arg1));
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
    private ImageView mPhotoPreview;
    private Camera mCamera;
    private CameraPreview mPreview;
    private ImageView mLine;
    private FrameLayout mBlueCover;
    private ImageView mCameraFlash;
    private ProgressBar mProgressSpinner;
    private LinearLayout mSuccessView;
    private TextView mSendText;
    private TextView mCancelText;
    private ImageView mPictureFrame;
    private ImageView mTakePhoto;
    private ImageView mSplash;
    private ImageView mInside;
    private ImageView mBlackBg;

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

        mLine = (ImageView) findViewById(R.id.line);
        mSplash = (ImageView) findViewById(R.id.splash);
        mProgressSpinner = (ProgressBar) findViewById(R.id.progress_spinner);
        mInside = (ImageView) findViewById(R.id.inside);
        mTakePhoto = (ImageView) findViewById(R.id.take_photo);
        mBlackBg = (ImageView) findViewById(R.id.black_bg);
        mBlueCover = (FrameLayout) findViewById(R.id.blue_cover);
        mSuccessView = (LinearLayout) findViewById(R.id.success);

        showSplash();

        initializeCamera();

        setupTG();

    }

    private void upload() {
        showUploadingView();
        new PhotoUploaderAsyncTask().execute(mPhotoFile);

    }

    private void initializeCamera()

    {
        // Create an instance of Camera
        mCamera = Camera.open();

        // Create our Preview view and set it as the content of our activity.

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeAllViews();
        preview.addView(mPreview);

    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;

        }
    }

    private void initialiseMindReadings() {

        if (mMindSession != null) {
            mMindSession = null;
        }

        mMindSession = new MindSession();
    }


    private void updateView() {
        // check we are getting good values before hiding splash
        if (mMindControlState == MIND_INITIALISING) {
            if (mMindSession.getAttention() > 0) {
                initialiseMindReadings();
                mSplash.setVisibility(View.INVISIBLE);
                mMindControlState = MIND_TAKING_PHOTO;
                mLine.setVisibility(View.VISIBLE);
                mTakePhoto.setVisibility(View.VISIBLE);
                mBlackBg.setVisibility(View.INVISIBLE);
                // mPreview.setVisibility(View.VISIBLE);

            }
            return;
        }

        if (mMindControlState != MIND_DISABLED) {
            mMindSession.updateMindData();

            int value = LINE_RANGE
                    - Math.min(LINE_RANGE, Math.round(mMindSession.getAttention() * LINE_VALUE_MULTIPLIER));
            mLine.setTranslationY(LINE_START_Y + value);

            if (mMindSession.getAttention() >= INTENSITY_TRIGGER) {
                if (mMindControlState == MIND_TAKING_PHOTO) {
                    mMindControlState = MIND_DISABLED;
                    takePhoto();

                } else if (mMindControlState == MIND_SHARING) {
                    mMindControlState = MIND_DISABLED;
                    if (tgDevice != null) {
                        tgDevice.close();
                    }
                    upload();
                }
            } else if (mMindSession.getAttention() <= INTENSITY_CANCEL_TRIGGER && mMindControlState == MIND_SHARING) {
                mCancelText.setVisibility(View.INVISIBLE);
                mSendText.setVisibility(View.INVISIBLE);

                initializeCamera();
                showTakePhotoView();
            }

        }

    }

    private void setupTG() {

        if (FAKE_BLUETOOTH == true) {

            mFakeBluetooth = new FakeBluetooth(handler);
            handler.postDelayed(mFakeBluetooth, 1000);
            return;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else {
            /* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
            tgDevice.connect(RAW_ENABLED);

        }

    }

    @Override
    public void onDestroy() {
        try {
            if (tgDevice != null) {
                tgDevice.close();
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
            if (tgDevice != null) {
                tgDevice.close();
            }

            releaseCamera();

        } catch (NullPointerException e) {

        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null) {
            initializeCamera();
        }
    }

    private void takePhoto() {

        mAudioManager.playSoundEffect(Sounds.SUCCESS);
        mCamera.takePicture(null, null, mPicture);
        doCameraFlash();
    }

    private void showPhotoView() {

        mPictureFrame = (ImageView) findViewById(R.id.picture_frame);
        mPictureFrame.setVisibility(View.VISIBLE);

        mLine = (ImageView) findViewById(R.id.line);
        mLine.setVisibility(View.INVISIBLE);
        mTakePhoto = (ImageView) findViewById(R.id.take_photo);
        mTakePhoto.setVisibility(View.INVISIBLE);

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

        mSendText = (TextView) findViewById(R.id.send);
        mSendText.setText("Post to Twitter");

        mCancelText = (TextView) findViewById(R.id.cancel);
        mCancelText.setText("Take another picture");

        mCancelText.setVisibility(View.VISIBLE);
        mSendText.setVisibility(View.VISIBLE);
        mLine.setVisibility(View.VISIBLE);
        mTakePhoto.setVisibility(View.VISIBLE);

        mPictureFrame.setVisibility(View.INVISIBLE);

        mMindControlState = MIND_SHARING;

    }

    private void showUploadingView() {
        mCancelText.setVisibility(View.INVISIBLE);
        mSendText.setVisibility(View.INVISIBLE);

        mBlueCover.setTranslationY(360);
        mBlueCover.setVisibility(View.VISIBLE);
        mProgressSpinner.setVisibility(View.VISIBLE);

        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animation = ObjectAnimator.ofFloat(mBlueCover, "translationY", 0);
        animation.setDuration(1000);

        animation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {

            }
        });
        set.play(animation);
        set.start();
    }

    // intro splash screen fades

    private void showSplash() {
        mMindControlState = MIND_DISABLED;
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
                mMindControlState = MIND_INITIALISING;
            }
        });
        set.play(animation);
        set.start();
    }

    private void showSuccessView() {

        mProgressSpinner.setVisibility(View.INVISIBLE);
        mSuccessView.setVisibility(View.VISIBLE);

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
        mBlueCover.setVisibility(View.INVISIBLE);
        mProgressSpinner.setVisibility(View.VISIBLE);
        mSuccessView.setVisibility(View.INVISIBLE);
        mCameraFlash.setVisibility(View.INVISIBLE);

        mLine.setVisibility(View.VISIBLE);
        mTakePhoto.setVisibility(View.VISIBLE);

        mMindControlState = MIND_INITIALISING;
        // mSplash.setVisibility(View.VISIBLE);
        setupTG();

    }

    private void doCameraFlash() {
        mCameraFlash = (ImageView) findViewById(R.id.camera_flash);
        mCameraFlash.setVisibility(View.VISIBLE);
        mCameraFlash.setImageAlpha(100);
        Animation cameraFlash = AnimationUtils.loadAnimation(this, R.anim.camera_flash);
        mCameraFlash.startAnimation(cameraFlash);

    }

    private class PhotoUploaderAsyncTask extends AsyncTask<String, Integer, String> {
        private static final String DEBUG_TAG = "PhotoUploaderAsyncTask";

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

            ConfigurationBuilder cb = new ConfigurationBuilder();

            //needs valid keys and secrets
            cb.setOAuthConsumerKey("*********");
            cb.setOAuthConsumerSecret("*********");
            cb.setOAuthAccessToken("************");
            cb.setOAuthAccessTokenSecret("*************");

            TwitterFactory tf = new TwitterFactory(cb.build());
            twitter = tf.getInstance();


            String[] mindrdrRecord = createMindrdrRecord().split("\\|");
            String tweet = mindrdrRecord[1];
            String mindrdrRecordId = mindrdrRecord[0];
            Log.d("mindrdrRecordId", mindrdrRecord[0]);

            try {
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

            //used to link a tweet with a private database
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(
                    "http://domain.com/updateData");

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
            HttpPost httppost = new HttpPost("http://domain.com/addData");

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
