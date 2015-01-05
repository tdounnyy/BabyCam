package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.ToggleButton;

// TODO Code refactor
// TODO Preview size chop & adjust layout
// TODO Prevent system sleep
// TODO Dim screen to save power and protect screen
// TODO Time lape picker
// TODO Storage take up calculate
// TODO Photo save location toast

public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    private CameraHelper mCameraHelper;
    private ToggleButton mShootBtn;
    private PowerManager.WakeLock mLock;
    private PowerManager pm;
    private Spinner mLapseSpinner;
    private int[] mLapseValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("onCreate");
        setContentView(R.layout.activity_main);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mLock = pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "BabyCam");


        //        mCameraHelper = new CameraHelper(this);
        //        // Create our Preview view and set it as the content of our activity.
        //        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        //        preview.addView(mCameraHelper.mPreview);

        // Add a listener to the Capture button
        mShootBtn = (ToggleButton) findViewById(R.id.button_capture);
        mShootBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.log("button onClick");
                switch (v.getId()) {
                    case R.id.button_capture:
                        if (mShootBtn.isChecked()) {
//                            pm.goToSleep(1000);
//                            WindowManager.LayoutParams params = getWindow().getAttributes();
//                            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//                            params.screenBrightness = 0;
//                            getWindow().setAttributes(params);

                            mLock.acquire();

                            // get an image from the camera
                            mCameraHelper.takePictureDelay(0);
                            mShootBtn.setTextColor(0xffff0000);
                        } else {

//                            WindowManager.LayoutParams params = getWindow().getAttributes();
//                            params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//                            params.screenBrightness = 50;
//                            getWindow().setAttributes(params);

                            mLock.release();

                            mCameraHelper.takePictureDelay(-1);
                            mShootBtn.setTextColor(0xffffffff);
                        }
                        break;
                }
            }
        });

        mLapseSpinner = (Spinner) findViewById(R.id.lapse_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.lapse_items, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLapseValues = getResources().getIntArray(R.array.lapse_items_value);
        // Apply the adapter to the spinner
        mLapseSpinner.setAdapter(adapter);
        mLapseSpinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.log("onResume");
        mCameraHelper = new CameraHelper(this);
        // Create our Preview view and set it as the content of our activity.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraHelper.mPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("onPause");
        mCameraHelper.stopShooting();
        mCameraHelper.releaseCamera();
        mShootBtn.setChecked(false);
        mShootBtn.setTextColor(0xffffffff);
        if (mLock.isHeld()) mLock.release();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Utils.log("onItemSelected " +  view.getId() + " " + position + " " + id
                + " " + mLapseValues[position]);
        mCameraHelper.setTimeLapse(mLapseValues[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Utils.log("onNothingSelected");
    }
}
