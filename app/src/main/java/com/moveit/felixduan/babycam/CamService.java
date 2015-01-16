package com.moveit.felixduan.babycam;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CamService extends Service implements SurfaceHolder.Callback {
    private static final String TAG = "CamService";
    SurfaceView mSurface;
    Camera mCamera;
    WindowManager mWindowManger;
    private ViewGroup mFloatView;

    public static boolean isRunning = false;

    // Prevent system sleep
    // TODO merge with isRunning
    private PowerManager.WakeLock mLock;

    private static final int TAKE_PIC = 1;
    private int mLapse = 5000;
    Handler mWorker = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAKE_PIC:
                    Utils.log("handleMessage TAKE_PIC");
                    // get an image from the camera
                    mCamera.takePicture(null, null, mPicture);
                    mWorker.sendEmptyMessageDelayed(TAKE_PIC, mLapse);
                    break;
                default:
                    break;
            }
        }
    };

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Utils.log("onPictureTaken");
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null) {
                Utils.log("Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Utils.log("File not found: " + e.getMessage());
            } catch (IOException e) {
                Utils.log("Error accessing file: " + e.getMessage());
            }

            mCamera.startPreview();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Utils.log(TAG + " onCreate");
        mSurface = new SurfaceView(this);
        mSurface.getHolder().addCallback(this);
        mWindowManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Utils.log(TAG + " onStartCommand");
        addView();
        //try {
        mCamera = Camera.open();
        //mCamera.setPreviewDisplay(mSurface.getHolder());
        setLargestPictureSize();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //    Utils.log(TAG + " IOException " + e);
        //}
        //mCamera.startPreview();

        mWorker.sendEmptyMessageDelayed(TAKE_PIC, 1000);
        isRunning = true;
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BabyCam");
        mLock.acquire();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Utils.log(TAG + " onDestroy");
        stopShooting();
        removeView();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        mFloatView = null;

        mLock.release();
        isRunning = false;
        super.onDestroy();
    }

    private void addView() {
        Utils.log(TAG + " addView");
        mFloatView = getFloatView(this);
        FrameLayout container = (FrameLayout) mFloatView.findViewById(R.id.camera_preview);
        container.addView(mSurface);
        WindowManager.LayoutParams param = new WindowManager.LayoutParams();
        param.width = 1052; // Magic hack
        param.height = 780;// Magic hack
        param.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        param.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        param.gravity = Gravity.CENTER;
        param.setTitle("CamSurface");
        mWindowManger.addView(mFloatView, param);
    }

    private ViewGroup getFloatView(Context context) {
        return (ViewGroup) LayoutInflater.from(context).inflate(R.layout.float_view, null);
    }

    private void removeView() {
        Utils.log(TAG + " removeView");
        if (mSurface != null) {
            mSurface.getHolder().removeCallback(this);
            //mWindowManger.removeView(mSurface);
            mWindowManger.removeView(mFloatView);
            mSurface = null;
        }
    }

    private void stopShooting() {
        mWorker.removeMessages(TAKE_PIC);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Utils.log(TAG + " onBind");
        return null;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Utils.log(TAG + " surfaceCreated");
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Utils.log("Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Utils.log(TAG + " surfaceChanged");
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.

        if (holder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

        } catch (Exception e) {
            Utils.log("Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Utils.log(TAG + " surafceDestroyed");

    }


    private List<Camera.Size> mSizes;

    private List<Camera.Size> getSupportedPictureSizes() {
        if (mCamera == null)
            return null;
        return mCamera.getParameters().getSupportedPictureSizes();
    }

    private void setLargestPictureSize() {
        mSizes = getSupportedPictureSizes();
        if (mCamera == null || mSizes == null) return;
        Camera.Parameters params = mCamera.getParameters();
        int maxSize = 0;
        int height = 0;
        int width = 0;
        for (int i = 0; i < mSizes.size(); i++) {
            Camera.Size size = mSizes.get(i);
            if (maxSize <= size.height * size.width) {
                height = size.height;
                width = size.width;
                maxSize = height * width;
            }
        }
        params.setPictureSize(width, height);

        Utils.log("size = " + params.getPictureSize().width + " " + params.getPictureSize().height);
        mCamera.setParameters(params);
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile(int type) {
        Utils.log("getOutputMediaFile");

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "BabyCam");
        //        File mediaStorageDir = Environment.getExternalStoragePublicDirectory(
        //                Environment.DIRECTORY_DCIM);
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Utils.log("failed to create directory: MyCameraApp");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "BABYPIC_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    static Intent sIntent;

    // Unify the intent to start this service
    public static Intent getIntent() {
        Utils.log(TAG + " getIntent() 11");
        if (sIntent != null) return sIntent;
        Utils.log(TAG + " getIntent() 22");
        sIntent = new Intent("felix.duan.CamService");
        sIntent.setClassName("com.moveit.felixduan.babycam", "com.moveit.felixduan.babycam.CamService");
        return sIntent;
    }
}
