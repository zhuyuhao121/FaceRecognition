package com.zhilai.facelibrary.zlfacerecog.bll;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.zhilai.facelibrary.zlfacerecog.MApp;
import com.zhilai.facelibrary.zlfacerecog.faceutil.FaceUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.SerialExecutor;
import com.zhilai.facelibrary.zlfacerecog.faceutil.Util;
import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.CameraView;
import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLErrorEnum;
import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLException;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogRecord;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogUser;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.ModuleDataHelper;
import com.zhilai.facelibrary.zlfacerecog.faceutil.yuemian.ReadSenseSendPacket;
import com.zhilai.facelibrary.zlfacerecog.faceutil.yuemian.Ym;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import zhilai.serialport.SerialPortFinder;

public class FaceBll {

    private static final String TAG = "FaceBll";

    @SuppressLint("StaticFieldLeak")
    private volatile static FaceBll faceBll;

    private String FACE_ADDR;//摄像头串口号 /dev/ttyS1
    private String LIGHT_DEV;//补光灯串口号 /dev/ttyUSBFAC1

    private byte[] mCloseInfraredBytes = new byte[]{(byte) 0xa5, 0x03, 0x00, 0x02, 0x00, 0x00, 0x06, (byte) 0xa2};
    private byte[] mHead = "READSENSE".getBytes();
    private byte[] mLightHead = new byte[]{(byte) 0xa5, 0x03, 0x00, 0x02};
    private int mFaceBaudrate = 115200;
    //    private int rst = -1;
    private String errorMsg = "串口异常，请稍后再试...";

    private FaceBll() {

    }

    public static FaceBll getInstance() {
        if (faceBll == null) {
            synchronized (FaceBll.class) {
                if (faceBll == null) {
                    faceBll = new FaceBll();
                }
            }
        }
        return faceBll;
    }

    /**
     * 初始化 此方法是动态获取并设置的设备串口号以及补光灯串口号
     */
    public void init(Context mContext, OnFindDevListener onFindDevListener) {
        MApp.init(mContext);
        findFaceDev(onFindDevListener);
    }

    /**
     * 初始化 此方法需要传入设备串口号以及补光灯串口号
     *
     * @param face_addr 人脸设备串口号
     * @param light_dev 补光灯串口号
     */
    public void init(Context mContext, String face_addr, String light_dev) {
        FACE_ADDR = face_addr;
        LIGHT_DEV = light_dev;
        MApp.init(mContext);
        ModuleDataHelper.getInstance().setSerialPort(face_addr, light_dev);
    }

    public boolean isInit() {
        return !TextUtils.isEmpty(FACE_ADDR) && !TextUtils.isEmpty(LIGHT_DEV);
    }

    /**
     * 人脸注册
     *
     * @param faceRegistCallback 回调接口
     */
    public void faceReg(long faceId, ModuleDataHelper.FaceRegistCallback faceRegistCallback) {
        if (!isInit()) {
            faceRegistCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRegist(faceId, faceRegistCallback);
    }

    /**
     * 人脸注册
     *
     * @param faceId             人脸id
     * @param registerThreshold  注册时的阈值
     * @param faceRegistCallback 回调接口
     */
    public void faceReg(long faceId, float registerThreshold, ModuleDataHelper.FaceRegistCallback faceRegistCallback) {
        if (!isInit()) {
            faceRegistCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRegist(faceId, registerThreshold, faceRegistCallback);
    }

    /**
     * 人脸注册
     *
     * @param faceId             人脸id
     * @param registerThreshold  注册时的阈值
     * @param faceRegistCallback 回调接口
     */
    public void faceReg(long faceId, float registerThreshold, int lightlevel, ModuleDataHelper.FaceRegistCallback faceRegistCallback) {
        if (!isInit()) {
            faceRegistCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRegist(faceId, registerThreshold, lightlevel, faceRegistCallback);
    }

    /**
     * 人脸识别
     *
     * @param faceRecogCallback 回调接口
     */
    public void faceRecognition(ModuleDataHelper.FaceRecogCallback faceRecogCallback) {
        if (!isInit()) {
            faceRecogCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRecog(faceRecogCallback);
    }

    /**
     * 人脸识别
     *
     * @param threshold         识别时的阈值
     * @param faceRecogCallback 回调接口
     */
    public void faceRecognition(float threshold, int lightlevel, ModuleDataHelper.FaceRecogCallback faceRecogCallback) {
        if (!isInit()) {
            faceRecogCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRecog(threshold, lightlevel, faceRecogCallback);
    }

    /**
     * 人脸识别
     *
     * @param threshold         识别时的阈值
     * @param faceRecogCallback 回调接口
     */
    public void faceRecognition(float threshold, ModuleDataHelper.FaceRecogCallback faceRecogCallback) {
        if (!isInit()) {
            faceRecogCallback.onError(-1, errorMsg);
            return;
        }
        ModuleDataHelper.getInstance().startRecog(threshold, faceRecogCallback);
    }

    /**
     * 根据faceId删除人脸信息
     *
     * @param faceId             人脸id
     * @param faceDeleteCallback 回调接口
     */
    public void deleteUser(long faceId, ModuleDataHelper.FaceDeleteCallback faceDeleteCallback) {
        ModuleDataHelper.getInstance().deleteUser(faceId, faceDeleteCallback);
    }

    /**
     * 删除所有人脸数据
     */
    public void clearAllFaceData() {
        ModuleDataHelper.getInstance().clearAllFaceData();
    }

    /**
     * 初始化cameraView
     */
    public void initCamera(CameraView camera) {
        if (camera != null) {
            camera.init();
        }
    }

    /**
     * 释放cameraView
     */
    public void releaseCamera(CameraView camera) {
        if (camera != null) {
            camera.release();
        }
        ModuleDataHelper.getInstance().closeSerial();
    }

    private ReadSenseSendPacket mGetVersion = new ReadSenseSendPacket(
            Ym.RS_CMD_VERSION,
            Ym.RS_REPLY_VERSION
    );

    private void findFaceDev(final OnFindDevListener onFindDevListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                rst = -1;
                SerialPortFinder serialPortFinder = new SerialPortFinder();
                String[] sh = serialPortFinder.getAllDevicesPath();
                if (sh == null || sh.length == 0) {
//                    return rst;
                    return;
                }
                boolean isFaceChanged = false;
                boolean ifLightChanged = false;
                /**
                 * 依次发送获取版本号指令，有正确返回则为对应的设备地址
                 */
                for (int i = 0; i < sh.length && (!isFaceChanged || !ifLightChanged); i++) {
                    String path = sh[i];

                    int mSmallTimeoutForYm = 1000;
                    if (!ifLightChanged) {
                        byte[] send = mCloseInfraredBytes;
                        SerialExecutor serialExecutor = new SerialExecutor(path, mFaceBaudrate);
                        try {
                            byte[] bytes = serialExecutor.sendData(send, mSmallTimeoutForYm);
                            if (bytes != null && bytes.length > 4 &&
                                    Arrays.equals(Arrays.copyOfRange(bytes, 0, mLightHead.length),
                                            mLightHead)) {
                                LIGHT_DEV = path;
                                ifLightChanged = true;
                                Log.e(TAG, "LIGHT_DEV===" + LIGHT_DEV);
//                                rst += 1;
                                continue;
                            }
                        } catch (ZLException e) {
                            Log.e(TAG, "e.getMessage()===" + e.getMessage());
                            if (e.getErrorCode() != ZLErrorEnum.TIMEOUT_ERROR.getErrorCode()) {
                                /**
                                 * 如果不是串口超时的错误
                                 * 则说明此串口不可用，
                                 * 也不用查验此path是否是模块的地址
                                 */
                                continue;
                            }
                        }
                    }

                    if (!isFaceChanged) {
                        byte[] send = parseSendPacket(mGetVersion);
                        SerialExecutor serialExecutor = new SerialExecutor(path, mFaceBaudrate);
                        try {
                            byte[] bytes = serialExecutor.sendData(send, mSmallTimeoutForYm);
                            if (bytes != null && bytes.length > 10 &&
                                    Arrays.equals(Arrays.copyOfRange(bytes, 0, mHead.length),
                                            mHead)) {
                                FACE_ADDR = path;
                                isFaceChanged = true;
                                Log.e(TAG, "FACE_ADDR===" + FACE_ADDR);
//                                rst += 2;
                            }
                        } catch (ZLException e) {
                            Log.e(TAG, "e.getMessage()===" + e.getMessage());
                        }
                    }
                }
                if (onFindDevListener != null) {
                    onFindDevListener.onSuccess(FACE_ADDR, LIGHT_DEV);
                }
//                Log.d(TAG, "rst===" + rst);
                if (isInit()) {
                    Log.d(TAG, "==初始化串口==");
                    ModuleDataHelper.getInstance().setSerialPort(FACE_ADDR, LIGHT_DEV, mFaceBaudrate);

//                    ModuleDataHelper.getInstance().getVersion();
//                    ModuleDataHelper.getInstance().getVersionBlock();
                }
            }
        }).start();
//        return rst;
    }

    private byte[] parseSendPacket(ReadSenseSendPacket rssp) {
        byte[] result = new byte[mHead.length + 7 + rssp.dataBody.length];
        System.arraycopy(mHead, 0,
                result, 0, mHead.length);
        result[mHead.length + 2] = rssp.cmdType;
        byte[] bytes = FaceUtil.intToByte(rssp.dataBody.length);
        System.arraycopy(
                bytes, 0,
                result, mHead.length + 3, 4
        );
        System.arraycopy(
                rssp.dataBody, 0,
                result, mHead.length + 7, rssp.dataBody.length
        );
        return result;
    }

    public interface OnFindDevListener {
        void onSuccess(String faceAddr, String lightDev);
    }

    /**
     * 摄像头反向
     */
    public void rotate180() {
        ModuleDataHelper.getInstance().rotate1800();
    }

    /**
     * 摄像头正向
     */
    public void rotate0() {
        ModuleDataHelper.getInstance().rotate00();
    }

    /**
     * 彩色
     */
    public void blackToColor() {
        ModuleDataHelper.getInstance().blackToColor();
    }

    /**
     * 黑白
     */
    public void colorToBlack() {
        ModuleDataHelper.getInstance().colorToBlack();
    }

    /**
     * 摄像头区域重置
     */
    public boolean setArea() {
        return ModuleDataHelper.getInstance().setArea(0, 640, 0, 480);
    }

    /**
     * 设置摄像头区域
     * if (x1 < 0 || y1 < 0 || x2 > 640 || y2 > 480) 提示请输入正确的区域值
     */
    public boolean setArea(int x1, int x2, int y1, int y2) {
        return ModuleDataHelper.getInstance().setArea(x1, x2, y1, y2);
    }

    /**
     * 设置人眼距离
     * faceth 人脸阈值
     * distance 人眼距离
     */
    public boolean setEyedistance(float distance) {
        return ModuleDataHelper.getInstance().setEyedistance(distance);
    }

    /**
     * 设置补光灯亮度
     */
    public boolean setLightLevelSerial(int level) {
        return ModuleDataHelper.getInstance().setLightLevelSerial(level);
    }

    /**
     * 关闭补光灯
     */
    public void closeInfraredLight() {
        ModuleDataHelper.getInstance().closeInfraredLight();
    }

    /**
     * 延时3秒关闭补光灯
     */
    public void closeInfraredLightDelay() {
        ModuleDataHelper.getInstance().closeInfraredLightDelay();
    }

    /**
     * 保存人脸数据
     *
     * @param id       faceId
     * @param faceData 人脸数据
     * @param date     时间
     * @return
     */
    public boolean saveUser(long id, byte[] faceData, String facePicPath, Date date) {
        return ModuleDataHelper.getInstance().saveUser(id, faceData, facePicPath, date);
    }

    /**
     * 获取人脸数据记录
     *
     * @return
     */
    public List<FaceRecogRecord> getFaceRecogRecords() {
        return ModuleDataHelper.getInstance().getFaceRecogRecords();
    }

    /**
     * 获取所有人脸数据
     *
     * @return
     */
    public List<FaceRecogUser> getAllFaceRecogUsers() {
        return ModuleDataHelper.getInstance().getFaceRecogUsers();
    }

    /**
     * 获取faceId对应的人脸数据
     *
     * @return
     */
    public FaceRecogUser getFaceRecogUser(long faceId) {
        return ModuleDataHelper.getInstance().getFaceRecogUser(faceId);
    }
}

