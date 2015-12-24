package com.tester.photodetectortest;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Kevin on 12/7/2015.
 */
public class Preview extends SurfaceView implements SurfaceHolder.Callback{
    private Activity mActivity;
    private SurfaceHolder mHolder;
    private Camera mCamera;

    private int previewHeight = 0;
    private int previewWidth = 0;

    public Preview(Activity activity, Camera camera) {
        super(activity);

        mActivity = activity;
        mCamera = camera;

        mHolder = this.getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(1, info);
            int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            mCamera.setDisplayOrientation(result);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            // left blank for now
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            // intentionally left blank for a test
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mCamera.release();
    }
    @Override
    public void onMeasure(int widthSpec, int heightSpec) {
        int measureWidth = MeasureSpec.getSize(widthSpec);
        int measureHeight = MeasureSpec.getSize(heightSpec);
        int width;
        int height;
        if (previewHeight == 0 || previewWidth == 0) {
            width = measureWidth;
            height = measureHeight;
        } else {
            float viewAspectRatio = (float)measureWidth/measureHeight;
            float cameraPreviewAspectRatio = (float) previewWidth/previewHeight;

            if (cameraPreviewAspectRatio > viewAspectRatio) {
                width = measureWidth;
                height =(int) (measureWidth / cameraPreviewAspectRatio);
             } else {
                width = (int) (measureHeight * cameraPreviewAspectRatio);
                height = measureHeight;
             }
        }
        setMeasuredDimension(width,height);
     }
}
