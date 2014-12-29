package com.moveit.felixduan.babycam;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

// TODO Code refactor
// TODO Preview size chop & adjust layout
// TODO Prevent system sleep
// TODO Dim screen to save power and protect
// TODO Time lape picker
// TODO Storage take up calculate
// TODO Photo save location toast

public class MainActivity extends Activity {

    private CameraHelper mCameraHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.log("onCreate");
        setContentView(R.layout.activity_main);

        mCameraHelper = new CameraHelper(this);

        // Create our Preview view and set it as the content of our activity.
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraHelper.mPreview);

        // Add a listener to the Capture button
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.log("button onClick");
                        // get an image from the camera
                        mCameraHelper.takePictureDelay(0);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.log("onPause");
        mCameraHelper.stopShooting();
        mCameraHelper.releaseCamera();
    }
}
