package com.moveit.felixduan.babycam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;

public class CamService extends Service {
    private static final String TAG = "CamService";

    private CameraHelper mCameraHelper;
    WindowManager mWindowManger;
    private ViewGroup mFloatView;

    // Prevent system sleep
    private static PowerManager.WakeLock mLock;

    /*
      * Unify the intent to start this service
      */
    static Intent sIntent;

    public static Intent getIntent() {
        Utils.log(TAG + " getIntent() 11");
        if (sIntent != null) return sIntent;
        Utils.log(TAG + " getIntent() 22");
        sIntent = new Intent("felix.duan.CamService");
        sIntent.setClassName("com.moveit.felixduan.babycam",
                "com.moveit.felixduan.babycam.CamService");
        return sIntent;
    }

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

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(TAG + " onCreate");
        if (mLock != null)
            Utils.log(TAG + " mLock = " + mLock.isHeld());
        mWindowManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log(TAG + " onStartCommand");
        if (mLock == null || !mLock.isHeld()) {
            mHandler.sendEmptyMessage(MSG_OPEN_CAMERA);
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BabyCam");
            mLock.acquire();
        } else {
            Utils.log(TAG + " mLock = " + mLock.isHeld());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void openCamera() {
        Utils.log(TAG + " openCamera");
        Camera c;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            if (c != null) {
                mCameraHelper = new CameraHelper(this, c);
                // Create our Preview view and set it as the content of our activity.
                addView(mCameraHelper.mPreview);
                mCameraHelper.takePictureDelay(0);
            } else {
                Utils.log(TAG + " camera is null");
            }
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
            Utils.log(TAG + " open camera fail " + e);
            mHandler.sendEmptyMessageDelayed(MSG_OPEN_CAMERA, 300);
        }
    }

    private void addView(SurfaceView v) {
        Utils.log(TAG + " addView");
        mFloatView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.float_view, null);
        mFloatView.addView(v);
        WindowManager.LayoutParams param
                = new WindowManager.LayoutParams(1, 1,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSPARENT);
        param.gravity = Gravity.TOP;
        param.setTitle("CamSurface");
        mWindowManger.addView(mFloatView, param);
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG + " onDestroy");
        if (mCameraHelper != null) {
            mCameraHelper.stopShooting();
            mCameraHelper.resetCamera();
        }
        mFloatView = null;

        mLock.release();
        super.onDestroy();
    }

    public static boolean isRunning() {
        if (mLock == null) return false;
        return (mLock.isHeld() ? true : false);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Utils.log(TAG + " onBind");
        return null;
    }
}
