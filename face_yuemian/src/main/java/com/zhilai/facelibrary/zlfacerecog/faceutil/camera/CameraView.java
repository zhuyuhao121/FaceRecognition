package com.zhilai.facelibrary.zlfacerecog.faceutil.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.zlfacerecog.MApp;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.ModuleDataHelper;

import java.io.IOException;

public class CameraView extends SurfaceView implements Callback, PreviewCallback {
    private String mTag = "CameraView";
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private static ImageStack imgStack;
    private boolean isPreview = false;
    private boolean frameCallback = true;

    static boolean isFirstOpen = true;

    Camera.Parameters mParameters;
    static public int cameraIndex = 0;

    public CameraView(Context context) {
        super(context);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView() {
        isPreview = false;
        frameCallback = true;
        getHolder().addCallback(this);
    }

    public void init() {
        isPreview = false;
        frameCallback = true;
        getHolder().addCallback(this);
        ModuleDataHelper.getInstance().setCameraView(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
    }

    public void releaseView() {
        getHolder().removeCallback(this);
    }

//    @Override
//    protected void onAttachedToWindow() {
//        super.onAttachedToWindow();
//        log("onAttachedToWindow");
//        initView();
//        getHolder().addCallback(this);
//    }
//
//    @Override
//    protected void onDetachedFromWindow() {
//        super.onDetachedFromWindow();
//        log("onDetachedFromWindow");
//
//    }

    public void openCamera(int cameraIndex) {
        log("openCamera() Camera:" + mTag);
        surfaceHolder = getHolder();
//        surfaceHolder.addCallback(this);
        if (cameraIndex >= 0) {
            log("Camera.open(cameraIndex) cameraIndex:" + cameraIndex);
            try {
                mCamera = Camera.open(cameraIndex);
            } catch (Exception ex) {
                log(ex.getLocalizedMessage());
                if (mCamera != null) {
                    mCamera.release();
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    log("repeat Camera.open(cameraIndex) cameraIndex:" + cameraIndex);
                    mCamera = Camera.open(cameraIndex);
                } catch (Exception exce) {
                    log(exce.getLocalizedMessage());
//                    new AlertDialog.Builder(getContext())
//                            .setTitle("相机开启错误，请断电重启！")
//                            .create().show();
                    if (mCamera != null) {
                        mCamera.release();
                    }
                    mCamera = null;
                }
            }
        } else {
            mCamera = null;
        }
        isPreview = false;
        /**
         * self camera check
         *
         */
        if (mCamera != null) {
            mParameters = mCamera.getParameters();
//            String[] s = mParameters.get("preview-size-values").split(",");
//            if (s != null) {
//                String[] param = s[s.length - 1].split("×");
//                if (param!=null&&param.length==2) {
//                    MApp.CAMERA_IMAGE_WIDTH = Integer.parseInt(param[0]);
//                    MApp.CAMERA_IMAGE_HEIGHT = Integer.parseInt(param[1]);
//                    ZLog.i("CAMERA_IMAGE_WIDTH:" + MApp.CAMERA_IMAGE_WIDTH + " CAMERA_IMAGE_HEIGHT:" + MApp.CAMERA_IMAGE_HEIGHT);
//                }
//            }else {
//                ZLog.e("parse camera error");
//            }

            /**
             * preview size modify fail!
             */
//            Camera.Size previewSize = mParameters.getPreviewSize();
//            List<Camera.Size> supportedPreviewSizes = mParameters.getSupportedPreviewSizes();
//            for (Camera.Size sizeTem : supportedPreviewSizes) {
//                if ((sizeTem.width == 240) && (sizeTem.height == 320)) {
//                    previewSize = sizeTem;
//                }
//            }
//            Camera.Size optimalPreviewSize = CameraUtil.getOptimalPreviewSize(
//                    (Activity) getContext(),
//                    supportedPreviewSizes,
//                    (double) previewSize.width / previewSize.height
//            );
//            Camera.Size original = mParameters.getPreviewSize();
//            if (!original.equals(optimalPreviewSize)) {
//                mParameters.setPreviewSize(
//                        optimalPreviewSize.width,
//                        optimalPreviewSize.height
//                );
//            }

//            mParameters.setPreviewSize(MApp.CAMERA_IMAGE_WIDTH, MApp.CAMERA_IMAGE_HEIGHT);
//            mParameters.setRotation(180);
//            mCamera.setDisplayOrientation(180);
//            mCamera.setParameters(mParameters);

            MApp.CAMERA_IMAGE_WIDTH = mParameters.getPreviewSize().width;
            MApp.CAMERA_IMAGE_HEIGHT = mParameters.getPreviewSize().height;
            ZLog.i("CAMERA_IMAGE_WIDTH:" + MApp.CAMERA_IMAGE_WIDTH + " CAMERA_IMAGE_HEIGHT:" + MApp.CAMERA_IMAGE_HEIGHT);

            imgStack = new ImageStack(MApp.CAMERA_IMAGE_WIDTH, MApp.CAMERA_IMAGE_HEIGHT);

            if (!isPreview) {
                openPreview(surfaceHolder);
            }
        }
    }

    public synchronized void releaseCamera() {
        log("releaseCamera() Camera:" + mTag);
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            if (isPreview) {
                log("camera.stopPreview()");
                mCamera.stopPreview();
            }
            mCamera.release();
            mCamera = null;
        }
        isPreview = false;
    }

    private void openPreview(SurfaceHolder holder) {
        if (holder.getSurface() == null || mCamera == null) {
            return;
        }
//        mCamera.stopPreview();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        isPreview = false;
        try {
            ZLog.i("surfaceHolder.surface:" + surfaceHolder.getSurface());
            mCamera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            ZLog.e(e.toString());
        }
        mCamera.setPreviewCallback(this);

//        testInitCamera(mCamera);

        mCamera.startPreview();
        isPreview = true;

        mCamera.setErrorCallback(new Camera.ErrorCallback() {
            @Override
            public void onError(int error, Camera camera) {
                ZLog.e(error + " " + camera.toString());
//                try {
//                    if (mCamera != null) {
//                        new AlertDialog.Builder(getContext())
//                                .setTitle("相机错误，请断电重启！")
//                                .create().show();
//                        releaseCamera();
//                    }
//                } catch (Exception e) {
//                    ZLog.e("camera error AlertDialog:" + e);
//                }
            }
        });
        ZLog.i("openPreview end");
    }

    private void testInitCamera(Camera mCamera) {

    }

    /**
     * none -> visible
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        log("surfaceChanged() width " + width + " height " + height + " format " + format + " " + getStack());
    }

    String getStack() {
//        return Arrays.toString(Thread.currentThread().getStackTrace());
//        return Thread.currentThread() + "\n" + Log.getStackTraceString(new Throwable());
        return "";
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        log("surfaceCreated() " + getStack());
        new Thread(new Runnable() {
            @Override
            public void run() {
                startCamera();
            }
        }).start();
    }

    private synchronized void startCamera() {
        ZLog.i("startCamera");
        openCamera(cameraIndex);
        if (isFirstOpen) {
            releaseCamera();
            isFirstOpen = false;
            openCamera(cameraIndex);
            ModuleDataHelper.getInstance().init();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        log("surfaceDestroyed() " + getStack());
        releaseCamera();
    }

    long lastTime;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        System.out.println("cameraview fps:" + (1000 / (System.currentTimeMillis() - lastTime)));
        lastTime = System.currentTimeMillis();
        //log("onPreviewFrame(byte[] data, Camera camera) isLoading:" + isLoading);
//        ZLog.i("onPreviewFrame");
        if (frameCallback && data != null) {
//            ZLog.i("imgStack.pushImageInfo(data, " + System.currentTimeMillis() + " start");
            imgStack.pushImageInfo(data, System.currentTimeMillis());
//            ZLog.i("imgStack.pushImageInfo(data, " + System.currentTimeMillis() + " end");
        }
    }

    public void release() {
//        ModuleDataHelper.getInstance().release();
        ZLog.i("release");
        releaseView();
        new Thread(new Runnable() {
            @Override
            public void run() {
                releaseCamera();
            }
        }).start();
        ModuleDataHelper.getInstance().takeFace = false;
        ModuleDataHelper.getInstance().closeInfraredLight();
//        ModuleDataHelper.getInstance().closeSerial();
//        ZLog.i("release end");
    }

    public ImageStack getImgStack() {
        return imgStack;
    }

    public void setTag(String tag) {
        mTag = tag;
    }

    public boolean isPreview() {
        return isPreview;
    }


    public boolean isFrameCallback() {
        return frameCallback;
    }

    public void setFrameCallback(boolean isCallback) {
        this.frameCallback = isCallback;
    }

    public void log(String msg) {
        ZLog.i(mTag + ":" + msg);
    }
}
