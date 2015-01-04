package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

// TODO Code refactor
// TODO Preview size chop & adjust layout
// TODO Prevent system sleep
// TODO Dim screen to save power and protect screen
// TODO Time lape picker
// TODO Storage take up calculate
// TODO Photo save location toast

public class MainActivity extends Activity {

    private CameraHelper mCameraHelper;
    private ToggleButton mShootBtn;
    private PowerManager.WakeLock mLock;
    private PowerManager pm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("onCreate");
        setContentView(R.layout.activity_main);
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BabyCam");
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

                                                        mLock.acquire(1000);

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
    }
}
