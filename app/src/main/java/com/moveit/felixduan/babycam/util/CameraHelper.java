package com.moveit.felixduan.babycam.util;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by felixduan on 14/12/23.
 */
public class CameraHelper {

    private boolean mBg; // Preview running in background
    private boolean mPreviewTouch; // Focus & take pic flag
    private PrefHelper mPrefHelper;
    private Context mContext;
    private Camera mCamera;
    private List<Size> mSizes;
    public CameraPreview mPreview;

    private View.OnClickListener mSurfaceOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Utils.log("surface view onclick");
            mPreviewTouch = true;
            mCamera.autoFocus(mAFCallback);
        }
    };

    // Focus every shot, is not a good practise & a waite most of time
    private AutoFocusCallback mAFCallback = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (mPreviewTouch) {
                mPreviewTouch = false;
                return;
            }
            // get an image from the camera
            mCamera.takePicture(null, null, mPicture);
            Toast.makeText(mContext, "take picture", Toast.LENGTH_SHORT).show();
            mWorker.sendEmptyMessageDelayed(TAKE_PIC, mLapse);
        }
    };

    public CameraHelper(Context context, Camera c, boolean background) {
        Utils.log("CameraHelper   constructor");
        mContext = context;
        mBg = background;
        mPrefHelper = new PrefHelper(context);
        resetCamera();
        mCamera = c;
        mPreview = new CameraPreview(mContext, mCamera);
        mPreview.setOnClickListener(mSurfaceOnClickListener);
        mSizes = getSupportedPictureSizes();
        setLargestPictureSize();
        setPreviewSize();
        setAutoFocus();
    }

    public void resetCamera() {
        Utils.log("resetCamera");
        if (mCamera != null) {
            // stop preview before making changes
            try {
                mPreview.getHolder().removeCallback(mPreview);
                mCamera.stopPreview();
            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
            }
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private List<Size> getSupportedPictureSizes() {
        if (mCamera == null)
            return null;
        return mCamera.getParameters().getSupportedPictureSizes();
    }

    private void setLargestPictureSize() {
        if (mCamera == null || mSizes == null) return;
        Camera.Parameters params = mCamera.getParameters();
        int maxSize = 0;
        int height = 0;
        int width = 0;
        for (int i = 0; i < mSizes.size(); i++) {
            Size size = mSizes.get(i);
            if (maxSize <= size.height * size.width) {
                height = size.height;
                width = size.width;
                maxSize = height * width;
            }
        }
        params.setPictureSize(width, height);
        mPrefHelper.setResolution(width, height);
        //for (int i : mPrefHelper.getResolution()) {
        //    Utils.log("resolution " + i);
        //}

        Utils.log("size = " + params.getPictureSize().width + " " + params.getPictureSize().height);
        mCamera.setParameters(params);
    }

    private void setPreviewSize() {
        if (mCamera == null) return;
        List<Size> list = mCamera.getParameters().getSupportedPreviewSizes();
        Size previewSize;
        // Show lowest preview when background shoting
        if (mBg) {
            previewSize = getOptimalPreviewSize(list, 1, 1);
        } else {
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            previewSize = getOptimalPreviewSize(list, metrics.widthPixels, metrics.heightPixels);
        }
        if (previewSize != null)
            mCamera.getParameters().setPreviewSize(previewSize.width, previewSize.height);
    }

    // Wheel invented by others
    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;

        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;

        // Find size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        Utils.log("getOptimalPreviewSize " + optimalSize.width + " " + optimalSize.height);
        return optimalSize;
    }

    private void setAutoFocus() {
        List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
        for (String mode : focusModes) {
            Utils.log("setAutoFocus  support: " + mode);
        }
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.getParameters().setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
    }

    /**
     * Take Picture with a certain delay.
     *
     * @param delay < 0, stop shooting.
     *              delay == 0, refresh lapse before shooting
     *              delay > 0, shoot after delay
     */
    public void takePictureDelay(int delay) {
        Utils.log("takePictureDelay");
        if (delay < 0) {
            stopShooting();
            return;
        }
        if (delay == 0)
            mLapse = 1000 * mPrefHelper.getLapse();
        mWorker.sendEmptyMessageDelayed(TAKE_PIC, delay);
    }

    public void stopShooting() {
        Utils.log("stopShooting");
        mWorker.removeMessages(TAKE_PIC);
    }

    private static final int TAKE_PIC = 1;
    private int mLapse = 5000;
    Handler mWorker = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TAKE_PIC:
                    Utils.log("handleMessage TAKE_PIC");
                    mCamera.autoFocus(mAFCallback);
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

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
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

    // In seconds
    public void setTimeLapse(int lapse) {
        Utils.log("setTimeLapse");
        mLapse = lapse * 1000;
    }
}
