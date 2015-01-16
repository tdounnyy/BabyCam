package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

// TODO Refactor code
// TODO Preview size chop & adjust layout
// TODO Storage take up calculate
// TODO Photo save location toast
// TODO POWER_BTN behavior

public class MainActivity extends Activity
        implements AdapterView.OnItemSelectedListener {

    private CameraHelper mCameraHelper;
    private ToggleButton mShootBtn;
    private Spinner mLapseSpinner;
    private int[] mLapseValues;

    private static final int MSG_OPEN_CAMERA = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_OPEN_CAMERA:
                    openCamera();
                    break;
                default:
                    break;
            }
            return false;
        }
    });

    private View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.log("button onClick " + CamService.isRunning);
            switch (v.getId()) {
                case R.id.button_capture:
                    if (mShootBtn.isChecked()) {
                        // get an image from the camera
                        if (mCameraHelper != null) {
                            mCameraHelper.takePictureDelay(0);
                            mShootBtn.setTextColor(0xffff0000);
                        }
                    } else {
                        if (mCameraHelper != null) {
                            mCameraHelper.takePictureDelay(-1);
                            mShootBtn.setTextColor(0xffffffff);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("onCreate");


        setContentView(R.layout.activity_main);

        // Add a listener to the Capture button
        mShootBtn = (ToggleButton) findViewById(R.id.button_capture);
        mShootBtn.setOnClickListener(mClickListener);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lapse_items, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLapseValues = getResources().getIntArray(R.array.lapse_items_value);
        // Apply the adapter to the spinner
        mLapseSpinner = (Spinner) findViewById(R.id.lapse_spinner);
        mLapseSpinner.setAdapter(adapter);
        mLapseSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("onResume");
        openCamera();
    }

    private void openCamera() {
        Utils.log("openCamera");
        Camera c;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            if (c != null) {
                mCameraHelper = new CameraHelper(this, c);
                // Create our Preview view and set it as the content of our activity.
                FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
                preview.addView(mCameraHelper.mPreview);
            } else {
                Utils.log("camera is null");
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Utils.log("open camera fail " + e);
            mHandler.sendEmptyMessageDelayed(MSG_OPEN_CAMERA, 300);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("onPause");
        if (mCameraHelper != null) {
            mCameraHelper.stopShooting();
            mCameraHelper.resetCamera();
        }

        if (mShootBtn.isChecked())
            startService(CamService.getIntent());
        mShootBtn.setChecked(false);
        mShootBtn.setTextColor(0xffffffff);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Utils.log("onStart serviceLive = " + CamService.isRunning);
        stopService(CamService.getIntent());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Utils.log("onRestart");
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Utils.log("onPostResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Utils.log("onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Utils.log("onDestroy");
    }


    /*
     * TODO: Here`s something I don`t understand:
     * MainActivity is configured landscape mode.
     * If android:configChanges="orientation|screenSize" is NOT set,
     * pressing on power btn will trigger re-orientation and re-run
     * onCreate ... onStop process.
     * AND, after that, unlock device will not bring back this activity.
     *
     * Some say because before locking the device, the orientation has
     * changed. But I see this as a flaw.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Utils.log("onConfigurationChanged");
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Utils.log("onItemSelected " + view.getId() + " " + position + " " + id
                + " " + mLapseValues[position]);
        if (mCameraHelper != null) {
            mCameraHelper.setTimeLapse(mLapseValues[position]);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Utils.log("onNothingSelected");
    }
}
