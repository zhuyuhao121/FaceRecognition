package com.zhilai.facelibrary.zlfacerecog.faceutil.camera;



import com.zhilai.driver.log.ZLog;

import java.nio.ByteBuffer;

public class ImageStack {
    private final static String TAG = ImageStack.class.getSimpleName();
    private boolean isReading = false;
    private ImageInfo imageOne = null;
    private ImageInfo imageTwo = null;

    public ImageStack(int width, int height) {
        imageOne = new ImageInfo(width, height);
        imageTwo = new ImageInfo(width, height);
    }

    public ImageInfo pullImageInfo() {
//        log("pullImageInfo() isReading:" + isReading);
        isReading = true;
//        log("pullImageInfo() imageOne.isNew:" + imageOne.isNew());
        if (imageOne.isNew()) {
            imageTwo.setImage(imageOne.getData());
            imageTwo.setTime(imageOne.getTime());
            imageTwo.setNew(true);
            imageOne.setNew(false);
        } else {
            imageTwo.setNew(false);
        }
        isReading = false;
        return imageTwo;
    }

    public void pushImageInfo(byte[] imgData, long time) {
//        log("pushImageInfo(byte[]) isReading:" + isReading);
        if (!isReading) {
            imageOne.setImage(imgData);
            imageOne.setTime(time);
            imageOne.setNew(true);
        }
    }


    public void pushImageInfo(ByteBuffer buffer, long time) {
        //log("pushImageInfo(ByteBuffer) isReading:" + isReading);
        if (!isReading) {
            imageOne.setImage(buffer);
            imageOne.setTime(time);
            imageOne.setNew(true);
            //log("pullImageInfo() imageOne.setNew(true)");
        }
    }

    public void clearAll() {
        imageOne.setNew(false);
        imageTwo.setNew(false);
    }

    public static void log(String msg) {
        ZLog.i(TAG + msg);
    }
}
