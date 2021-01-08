package com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.zlfacerecog.MApp;
import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.CalUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.MD5;
import com.zhilai.facelibrary.zlfacerecog.faceutil.SerialExecutor;
import com.zhilai.facelibrary.zlfacerecog.faceutil.TimeUtil;
import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.CameraView;
import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.ImageInfo;
import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.ImageStack;
import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLException;
import com.zhilai.myapplication.jni.JniUtils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import zhilai.serialport.SerialPort;


public class ModuleDataHelper {

    private static final String TAG = "ModuleDataHelper";

    private byte RS_CMD_TEST = 0x00;
    private byte RS_CMD_PING = 0x10;
    private byte RS_CMD_VERSION = 0x11;
    private byte RS_CMD_MODE = 0x12;
    private byte RS_CMD_START = 0x13;
    private byte RS_CMD_STOP = 0x14;
    private byte RS_CMD_FW_PREPARE = 0x15;
    private byte RS_CMD_FW_DATA = 0x16;
    private byte RS_CMD_FW_UPDATE = 0x17;
    private byte RS_CMD_REBOOT = 0x1b;
    private byte RS_CMD_SET_SN = 0x1c;
    private byte RS_CMD_GET_SN = 0x1d;
    private byte RS_CMD_LIC_INFO = 0x1e;
    private byte RS_CMD_FILE_OPEN = 0x1f;
    private byte RS_CMD_FILE_DATA = 0x20;
    private byte RS_CMD_FILE_DONE = 0x21;
    private byte RS_CMD_FACE_PICTURE = 0x22;
    private byte RS_CMD_TAKE_PHOTO = 0x24;
    private byte RS_CMD_SET_ROTATE_ANGLE = 0x27;
    private byte RS_CMD_SET_ALGO_MODEL = 0x28;
    private byte RS_CMD_FACE_SWITCH = 0x29;
    private byte RS_CMD_RECO_AREA = 0x05;
    private byte RS_CMD_SET_COMPARE_PARAM = 0x2c;
    private byte RS_REPLY_RECO_AREA = 0x75;
    private byte RS_REPLY_SET_COMPARE_PARAM = (byte) 0x9c;
    private byte RS_REPLY_TEST = 0x70;
    private byte RS_REPLY_PING = (byte) 0x80;
    private byte RS_REPLY_VERSION = (byte) 0x81;
    private byte RS_REPLY_MODE = (byte) 0x82;
    private byte RS_REPLY_START = (byte) 0x83;
    private byte RS_REPLY_STOP = (byte) 0x84;
    private byte RS_REPLY_FW_PREPARE = (byte) 0x85;
    private byte RS_REPLY_FW_DATA = (byte) 0x86;
    private byte RS_REPLY_FW_UPDATE = (byte) 0x87;
    private byte RS_REPLY_REBOOT = (byte) 0x8b;
    private byte RS_REPLY_SET_SN = (byte) 0x8c;
    private byte RS_REPLY_GET_SN = (byte) 0x8d;
    private byte RS_REPLY_LIC_INFO = (byte) 0x8e;
    private byte RS_REPLY_FILE_OPEN = (byte) 0x8f;
    private byte RS_REPLY_FILE_DATA = (byte) 0x90;
    private byte RS_REPLY_FILE_DONE = (byte) 0x91;
    private byte RS_REPLY_FACE_PICTURE = (byte) 0x92;
    private byte RS_REPLY_TAKE_PHOTO = (byte) 0x94;
    private byte RS_REPLY_SET_ROTATE_ANGLE = (byte) 0x97;
    private byte RS_REPLY_SET_ALGO_MODEL = (byte) 0x98;
    private byte RS_REPLY_FACE_SWITCH = (byte) 0x99;
    private byte RS_REPLY_FACE = (byte) 0xa0;
    private byte RS_REPLY_PHOTO = (byte) 0xa1;
    private byte RS_REPLY_FACE_ATTR = (byte) 0xa2;
    private byte RS_REPLY_FACE_ATTR2 = (byte) 0xa3;
    private byte RS_REPLY_PEOPLE_STATUS = (byte) 0xa4;
    private byte RS_REPLY_PERSON_INFO = (byte) 0xa5;
    private byte RS_REPLY_FACE_ATTR3 = (byte) 0xa6;
    private byte RS_REPLY_ERROR_CODE = (byte) 0xb0;

    private byte RS_CMD_RECT_SWITCH = (byte) 0x2f;
    private byte RS_REPLY_RECT_SWITCH = (byte) 0x9f;
    private boolean loop;
    private SerialExecutor yuemian;
    private SerialPort serialPort;

    /**
     * 算法是否关闭
     */
    private boolean suanfaEnd;
    private boolean isRegister;
    private long currentId;

    private boolean canSwitchVideo = false;

    private int timeout = 30 * 1000;
    private int msgTimeout = 1;
    private int closeInfrared = 2;
    private int openInfrared = 3;
    CameraView cameraView;

    /**
     * 判断是否处理面部识别数据
     */
    public boolean takeFace;

    /**
     * 识别的相似度阈值
     */
    private float threshold = 0.6f;
    private int callbackSwitch;
    private String dev = "/dev/ttyUSBFAC0";
    private int baudrate = 115200;
    private String lightDev = "/dev/ttyUSBFAC1";
    private int lightBaud = 115200;
    private boolean gpioType;
    private int registerTakeCount;
    private int faceTakeCount;
    private int registerTakeCountMax = 3;
    private byte[] resultTem;
    private int lightlevel;//亮度等级

    private String gpio = "/dev/face_gpio";
    private int gpioPort = 9600;
    private SerialPort serialPortgpio;

    /**
     * 注册时多张照片的相似度阈值
     */
    public float registerThreshold = 0.7f;
    private int fileSizeLimit = 1024 * 500;

    public void rotate00() {
        sendDate(new byte[]{
                0x52, 0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45, 0x00,
                0x00, 0x27, 0x00,
                0x00, 0x00, 0x01, 0x00
        });
    }

    public void rotate1800() {
        sendDate(new byte[]{
                0x52, 0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45, 0x00,
                0x00, 0x27, 0x00,
                0x00, 0x00, 0x04, (byte) 0xB4, 0x00, 0x00, 0x00
        });
    }

    public void blackToColor() {
        sendDate(
                new byte[]{
                        0x52, 0x45, 0x41, 0x44, 0x53, 0x45,
                        0x4e, 0x53, 0x45, 0x00, 0x00,
                        0x25, 0x00, 0x00, 0x00, 0x01, 0x03
                }
        );
    }

    public void colorToBlack() {
        sendDate(
                new byte[]{
                        0x52, 0x45, 0x41, 0x44, 0x53, 0x45,
                        0x4e, 0x53, 0x45, 0x00, 0x00,
                        0x25, 0x00, 0x00, 0x00, 0x01, 0x04
                }
        );
    }

    ExecutorService executors = Executors.newSingleThreadExecutor();

    public Handler mhandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == msgTimeout) {
                if (faceRegistCallback != null) {
                    faceRegistCallback.onError(5, "模块识别超时");
                }
                if (faceRecogCallback != null) {
                    faceRecogCallback.onError(5, "模块识别超时");
                }
                closeInfraredLightDelay();
            } else if (msg.what == closeInfrared) {
                closeInfraredLight();
            } else if (msg.what == openInfrared) {
                openInfraredLight();
            }
        }
    };

    public void setID(long id) {
        currentId = id;
    }

    public void setSerialPort(String dev, String lightDev, int baudrate) {
        this.dev = dev;
        this.lightDev = lightDev;
        this.baudrate = baudrate;
    }

    public void setSerialPort(String dev, String lightDev) {
        this.dev = dev;
        this.lightDev = lightDev;
    }

    public float getThreshold() {
        return threshold;
    }

    public void setThreshold(float threshold) {
        this.threshold = threshold;
    }

    public void setRegister(boolean isResigter) {
        this.isRegister = isResigter;
    }

    public void setCameraView(CameraView cameraView) {
        this.cameraView = cameraView;
        if (MApp.daoSession == null) {
            MApp.init(cameraView.getContext().getApplicationContext());
        }
    }

    private FaceRegistCallback faceRegistCallback;
    private FaceRecogCallback faceRecogCallback;

    public interface FaceRegistCallback {
        void onFaceRegistSuccess(long faceId, FaceRecogRecord faceRecogRecord, FaceRecogUser faceRecogUser);

        void onError(int i, String errorMsg);
    }

    public interface FaceRecogCallback {
        void onFaceResult(long faceId, float likelyhood);

        void onFaceRecogSuccess(long faceId, FaceRecogRecord faceRecogRecord, FaceRecogUser faceRecogUser);

        void onError(int i, String errorMsg);
    }

    public interface FaceDeleteCallback {
        void onError(int i, String errorMsg);

        void onFaceDeleteSuccess(long id);
    }

    private static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    private static byte[] intToByteArray(int a) {
        return new byte[]{
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    private static float getFloat(byte[] b) {
        // 4 bytes
        int accum = 0;
        for (int shiftBy = 0; shiftBy < 4; shiftBy++) {
            accum |= (b[shiftBy] & 0xff) << shiftBy * 8;
        }
        return Float.intBitsToFloat(accum);
    }

    static void readStream(byte[] target, InputStream is) throws IOException {
        int length = target.length;
        byte[] tem;
        if (length < 1024) {
            tem = new byte[length];
        } else {
            tem = new byte[1024];
        }
        int size;
        int remain = length;
        while (is != null && (size = is.read(tem, 0, remain > tem.length ? tem.length : remain)) != -1 && size <= remain) {
//            ZLog.i("tem:" + ByteUtil.bytesToHexString(tem, " ") + " size:" + size);
            System.arraycopy(tem, 0, target, length - remain, size);
            remain -= size;
//            ZLog.i("target:" + ByteUtil.bytesToHexString(target, " ") + " remain:" + remain);
            if (remain == 0) {
                break;
            }
        }
    }

    public static float compareFace(Object[] result, Object[] target) {
        long start = System.currentTimeMillis();
        float n = 0f;
        int length = Math.min(result.length, target.length);
        for (int i = 0; i < length; i++) {
            n += ((float) result[i]) * ((float) target[i]);
        }
        ZLog.i("compare take:" + (System.currentTimeMillis() - start));
        return n;
    }

    public static float compareFace(byte[] result, byte[] target) {
        long start = System.currentTimeMillis();
        float n = 0f;
        int length = Math.min(result.length, target.length);
        ZLog.i("compare length:" + length);
        for (int i = 0; i + 3 < length; i += 4) {
            n += getFloat(Arrays.copyOfRange(result, i, i + 4)) *
                    getFloat(Arrays.copyOfRange(target, i, i + 4));
        }
        ZLog.i("compare take:" + (System.currentTimeMillis() - start));
        return n;
    }

    public void deleteUser(final long id, final FaceDeleteCallback faceDeleteCallback) {
        if (MApp.daoSession == null) {
            return;
        }
        executors.execute(new Runnable() {
            @Override
            public void run() {
                synchronized (MApp.daoSession) {
                    FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);
                    if (load == null) {
                        if (faceDeleteCallback != null) {
                            faceDeleteCallback.onError(0, "用户人脸信息不存在");
                        }
                    } else {
                        FaceRecogRecord faceRecogRecord = new FaceRecogRecord(null, 2, new Date(), null, load.id, 1);
                        MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                        ZLog.i("FaceRecogRecord insert:" + faceRecogRecord);
                        MApp.daoSession.getFaceRecogUserDao().delete(load);
                        String facePicPath = load.getFacePicPath();
                        if (facePicPath != null) {
                            File file = new File(facePicPath);
                            if (file != null && file.exists()) {
                                file.delete();
                            }
                        }
                        ZLog.i("FaceRecogRecord delete:" + load);
                        if (faceDeleteCallback != null) {
                            faceDeleteCallback.onFaceDeleteSuccess(id);
                        }
                    }
                }
            }
        });
    }

    public boolean saveUser(final long id, final byte[] faceData, String facePicPath, final Date date) {
        if (MApp.daoSession == null) {
            return false;
        }
        synchronized (MApp.daoSession) {
            FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);
            if (load == null) {
                FaceRecogUser faceRecogUser = new FaceRecogUser(id, faceData, facePicPath, date);
                MApp.daoSession.getFaceRecogUserDao().insert(
                        faceRecogUser
                );
                ZLog.i("FaceRecogUser insert:" + faceRecogUser);
                return true;
            } else {
                return false;
            }
        }
    }

    public FaceRecogUser getFaceRecogUser(long id) {
        if (MApp.daoSession == null) {
            return null;
        }
        synchronized (MApp.daoSession) {
            FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);
            return load;
        }
    }

    public List<FaceRecogUser> getFaceRecogUsers() {
        return MApp.daoSession.getFaceRecogUserDao().loadAll();
    }

    public List<FaceRecogRecord> getFaceRecogRecords() {
        return MApp.daoSession.getFaceRecogRecordDao().loadAll();
    }

    public void clearRecord(int remain) {
        if (MApp.daoSession == null) {
            return;
        }
        ZLog.i("clearRecord:" + remain);
        List<FaceRecogRecord> faceRecogRecords = MApp.daoSession.getFaceRecogRecordDao().loadAll();
        int size = faceRecogRecords.size();
        long newIndex = 1;
        List<FaceRecogRecord> faceRecogRecordsNew = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i < (size - remain)) {
                String facePicPath = faceRecogRecords.get(i).getFacePicPath();
                if (facePicPath != null) {
                    File file = new File(facePicPath);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
            } else {
                FaceRecogRecord faceRecogRecord = faceRecogRecords.get(i);
                faceRecogRecord.setId(newIndex);
                faceRecogRecordsNew.add(faceRecogRecord);
                newIndex++;
            }
        }
        MApp.daoSession.getFaceRecogRecordDao().deleteAll();
        MApp.daoSession.getFaceRecogRecordDao().insertInTx(faceRecogRecordsNew);
    }

    public void clearAllFaceData() {
        if (MApp.daoSession == null) {
            return;
        }
        MApp.daoSession.getFaceRecogRecordDao().deleteAll();
        MApp.daoSession.getFaceRecogUserDao().deleteAll();
    }

    public void clearRecordIdUnchange(int remain) {
        if (MApp.daoSession == null) {
            return;
        }
        ZLog.i("clearRecordIdUnchange:" + remain);
        List<FaceRecogRecord> faceRecogRecords = MApp.daoSession.getFaceRecogRecordDao().loadAll();
        int size = faceRecogRecords.size();
        List<FaceRecogRecord> faceRecogRecordsNew = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            if (i < (size - remain)) {
                String facePicPath = faceRecogRecords.get(i).getFacePicPath();
                if (facePicPath != null) {
                    File file = new File(facePicPath);
                    if (file != null && file.exists()) {
                        file.delete();
                    }
                }
            } else {
                FaceRecogRecord faceRecogRecord = faceRecogRecords.get(i);
                faceRecogRecordsNew.add(faceRecogRecord);
            }
        }
        MApp.daoSession.getFaceRecogRecordDao().deleteAll();
        MApp.daoSession.getFaceRecogRecordDao().insertInTx(faceRecogRecordsNew);
    }

    public void clearRecordById(long id) {
        if (MApp.daoSession == null) {
            return;
        }
        FaceRecogRecord faceRecogRecord = MApp.daoSession.getFaceRecogRecordDao().load(id);
        ZLog.i("clearRecordById:" + id + "-" + faceRecogRecord);
        if (faceRecogRecord == null) {
            return;
        }
        String facePicPath = faceRecogRecord.getFacePicPath();
        if (facePicPath != null) {
            File file = new File(facePicPath);
            if (file != null && file.exists()) {
                file.delete();
            }
        }
        MApp.daoSession.getFaceRecogRecordDao().delete(faceRecogRecord);
    }

//    public void blackToColor() {
//        sendDate(
//                new byte[]{
//                        0x52, 0x45, 0x41, 0x44, 0x53, 0x45,
//                        0x4e, 0x53, 0x45, 0x00, 0x00,
//                        0x25, 0x00, 0x00, 0x00, 0x01, 0x03
//                }
//        );
//    }
//
//    public void colorToBlack() {
//        sendDate(
//                new byte[]{
//                        0x52, 0x45, 0x41, 0x44, 0x53, 0x45,
//                        0x4e, 0x53, 0x45, 0x00, 0x00,
//                        0x25, 0x00, 0x00, 0x00, 0x01, 0x04
//                }
//        );
//    }
//
//    public void rotate00() {
//        sendDate(new byte[]{
//                0x52, 0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45, 0x00,
//                0x00, 0x27, 0x00,
//                0x00, 0x00, 0x01, 0x00
//        });
//    }
//
//    public void rotate1800() {
//        sendDate(new byte[]{
//                0x52, 0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45, 0x00,
//                0x00, 0x27, 0x00,
//                0x00, 0x00, 0x04, (byte) 0xB4, 0x00, 0x00, 0x00
//        });
//    }

    public boolean setEyedistance(float distance) {
        byte[] objects = new byte[12];
        System.arraycopy(intToByteAre(Float.floatToIntBits((float) 0.3)), 0, objects, 0, 4);
        System.arraycopy(intToByteAre(Float.floatToIntBits((float) 0.7)), 0, objects, 4, 4);
        System.arraycopy(intToByteAre(Float.floatToIntBits(distance)), 0, objects, 8, 4);

        ZLog.i("setArea");
        setdistance.dataBody = objects;
        ZLog.i("setdistance" + setdistance);
        ZLog.i("send rssp start:" + ByteUtil.bytesToHexString(parseSendPacket(setdistance), " ") + " timeout:" + timeout);
        return sendReadSenseSendPacket(setdistance, timeoutForYm);
    }

    public boolean setArea(int x1, int x2, int y1, int y2) {
        byte[] objects = new byte[16];
        System.arraycopy(intToByteAre(x1), 0, objects, 0, 4);
        System.arraycopy(intToByteAre(y1), 0, objects, 4, 4);
        System.arraycopy(intToByteAre(x2), 0, objects, 8, 4);
        System.arraycopy(intToByteAre(y2), 0, objects, 12, 4);
        ZLog.i("setArea");
        setAre.dataBody = objects;
        ZLog.i("setArea" + setAre);
        ZLog.i("send rssp start:" + ByteUtil.bytesToHexString(parseSendPacket(setAre), " ") + " timeout:" + timeout);
        return sendReadSenseSendPacket(setAre, timeoutForYm);
    }

    Set<Long> compareSet = new HashSet<>();

    public void setCompareSet(Set<Long> cs) {
        this.compareSet = cs;
    }

    public void setCompareSet(long... cs) {
        for (long tem : cs) {
            compareSet.add(tem);
        }
    }

    public void setCompareSetAll() {
        if (MApp.daoSession == null) {
            return;
        }
        ZLog.i("start set CompareSet");
        compareSet.clear();
        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
        for (FaceRecogUser tem : faceRecogUsers) {
            compareSet.add(tem.getId());
        }
        ZLog.i("finish set CompareSet");
    }

    public class ParseRecvThread extends Thread {
        @Override
        public void run() {
            super.run();
            ZLog.i(this + "running start");
            try {
//                InputStreamReader inputStreamReader = new InputStreamReader(serialPort.getInputStream());
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                int vacantTime = 0;
                int waitInterval = 200;
                int waitLimit = 60 * 60 * 1000;

                while (loop && serialPort != null) {
//                    if (serialPort.getInputStream().available() <= 0) {
//                        Thread.sleep(waitInterval);
//                        vacantTime += waitInterval;
//                        if (vacantTime > waitLimit) {
//                            release();
//                        }
//                        continue;
//                    }
//                    vacantTime = 0;
                    if (serialPort.getInputStream() == null) {
                        return;
                    }
                    int next = serialPort.getInputStream().read();
                    Log.d(TAG, "next===" + next);
//                    int next = 0;
//                    ZLog.i("read line:" + bufferedReader.readLine());
//                    if (next == 0) {
//                        continue;
//                    }
//                    ZLog.i("read:" + ByteUtil.bytesToHexString(new byte[]{(byte) next}, ""));
                    if ((byte) next == (byte) 0x52) {
                        byte[] yuemian = new byte[8];
                        if (serialPort == null || serialPort.getInputStream() == null) {
                            return;
                        }
                        /**
                         * 读取readsense数据头
                         */
                        readStream(yuemian, serialPort.getInputStream());
                        if (Arrays.equals(yuemian,
                                new byte[]{0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45})) {
                            //阅面标识符匹配
                            byte[] head = new byte[7];
                            readStream(head, serialPort.getInputStream());
                            byte cmd = head[2];
                            byte crc = head[1];
                            ZLog.i("result cmd:" + ByteUtil.bytesToHexString(new byte[]{cmd}, ""));

                            int bodysize = byteArrayToInt(Arrays.copyOfRange(head, 3, 7));
                            ZLog.i("result body:" + bodysize);
                            if (bodysize < 0 || bodysize > 3000) {
                                ZLog.i("bodysize error");
                                continue;
                            }
                            byte[] result = new byte[bodysize];

                            //如果收到人脸的特征数据，先立刻保存图片数据
                            ByteArrayOutputStream baos = null;
                            if (cmd == (byte) 0xa0 && takeFace) {
                                ImageStack imgStack = cameraView.getImgStack();

                                /**
                                 * 获取到特征数据时，相机还未开启，需要做非空判断
                                 * 新开线程开启相机引入的问题
                                 */
                                int waitStack = 0;
                                while (imgStack == null) {
                                    try {
                                        Thread.sleep(200);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    if (waitStack >= 10) {
                                        break;
                                    }
                                    waitStack++;
                                }
                                if (imgStack == null) {
                                    ZLog.i("img stack null");
                                } else {
                                    /**
                                     * 等待图像数据有新的push
                                     */
                                    int waitImage = 0;
                                    ImageInfo imageInfo = imgStack.pullImageInfo();
                                    while (!imageInfo.isNew()) {
                                        ZLog.i("image info old:" + waitImage);
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        waitImage++;
                                        imageInfo = imgStack.pullImageInfo();
                                        if (waitImage >= 10) {
                                            break;
                                        }
                                    }
                                    if (waitImage >= 10) {
                                        ZLog.e("image error");
//                                    faceCallback.onError(6, "图像获取异常");
                                    }

                                    if (takeFace
//                                        &&
//                                        /**
//                                         * take photo every recognize
//                                         *            the last register
//                                         */
//                                        (!isRegister || registerTakeCount >= registerTakeCountMax)
                                    ) {
                                        YuvImage yuvimage = new YuvImage(imageInfo.getData(), ImageFormat.NV21,
                                                imageInfo.getWidth(), imageInfo.getHeight(), null);
                                        baos = new ByteArrayOutputStream();
                                        yuvimage.compressToJpeg(new Rect(0, 0,
                                                        imageInfo.getWidth(), imageInfo.getHeight()),
                                                100, baos);
                                    }
                                }
                            }

                            if (serialPort == null || serialPort.getInputStream() == null) {
                                return;
                            }
                            /**
                             * 读取数据内容
                             */
                            readStream(result, serialPort.getInputStream());

                            byte calCrc = CalUtil.crc8Arrays(result);
//                            ZLog.i("crc check:calCrc:" + calCrc + " crc:" + crc);
                            if (calCrc != crc) {
                                if (cmd == (byte) 0xa0) {
                                    ZLog.i("crc error, resend take face data");
                                    if (isRegister) {
                                        startRegist(currentId);
                                    } else {
                                        startRecog();
                                    }
                                }
                                ZLog.i("check error");
                                continue;
                            }

                            String resultBody = ByteUtil.bytesToHexStringLimit(
                                    result, " ", 50);
                            ZLog.i("result body content:[" + resultBody + "]");

                            /**
                             *  读数据部分截止
                             */
                            ReadSenseSendPacket readSenseSendPacket = notifyMap.get(cmd);
                            if (readSenseSendPacket != null) {
                                synchronized (readSenseSendPacket) {
                                    readSenseSendPacket.response = new byte[result.length];
                                    System.arraycopy(
                                            result, 0,
                                            readSenseSendPacket.response, 0,
                                            result.length
                                    );
                                    readSenseSendPacket.reserve = 0;
                                    readSenseSendPacket.notifyAll();
                                }
                            }


                            if (cmd == (byte) 0xa0) {
                                mhandler.removeMessages(msgTimeout);
                                closeInfraredLightDelay();
                                /**
                                 * 人脸特征数据
                                 */
                                if (result[0] == 1 && takeFace) {
                                    Log.d(TAG, "==读取人脸特征数据==");
                                    long resultUser = -1;
                                    float likelyhood = -1;
                                    FaceRecogUser faceRecogUser = new FaceRecogUser();
                                    FaceRecogRecord faceRecogRecord = new FaceRecogRecord();
                                    if (isRegister) {
                                        ZLog.i("check in register");
                                        Log.d(TAG, "check in register");
                                        FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(currentId);
                                        if (load != null) {
                                            if (faceRegistCallback != null) {
                                                faceRegistCallback.onError(2, "此ID已注册");
                                            }
                                            takeFace = false;
                                            continue;
                                        }

                                        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
                                        ZLog.i("face compare set:" + compareSet);
                                        for (FaceRecogUser tem : faceRecogUsers) {
                                            if (!compareSet.contains(tem.getId())) {
                                                continue;
                                            }
                                            float v = compareFace(Arrays.copyOfRange(
                                                    result, 8, result.length), tem.getFaceData());
                                            if (v >= likelyhood) {
                                                likelyhood = v;
                                                resultUser = tem.getId();
                                            }
                                            ZLog.i("likelyhood:" + likelyhood + " current id:" + tem.getId() + " current result:" + v);
                                        }

                                        if (likelyhood > threshold) {
                                            faceRecogRecord.setType(3);
                                            faceRecogRecord.setMostLikelyId(resultUser);
                                            faceRecogRecord.setLikelyHood(likelyhood);
                                            faceRecogRecord.setRecogTime(new Date());
                                            MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                                            if (baos != null) {
                                                File picPath = new File(MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" +
                                                        TimeUtil.getNowDate(TimeUtil.DateTimePattern.ALL_TIME_INFILE) + ".jpg");
                                                FileOutputStream fileOutputStream = new FileOutputStream(picPath);
                                                fileOutputStream.write(baos.toByteArray());
                                                baos.close();
                                                fileOutputStream.close();
//                                                    if (!isRegister) {
                                                faceRecogRecord.setFacePicPath(picPath.getPath());
//                                                    }
                                                MApp.daoSession.getFaceRecogRecordDao().insertOrReplace(faceRecogRecord);
                                                ZLog.i("insert FaceRecogRecord when has register:" + faceRecogRecord);
                                            }
                                            if (faceRegistCallback != null) {
                                                faceRegistCallback.onError(3, "您已注册，您的ID是：" + resultUser);
                                            }
                                            takeFace = false;
                                            continue;
                                        }

                                        /**
                                         * when register, take photo for >1 times
                                         * compare with last face data
                                         */
                                        if (resultTem != null) {
                                            float v = compareFace(Arrays.copyOfRange(
                                                    result, 8, result.length), resultTem);
                                            ZLog.i("compare with last face data:" + v + " regist count:" + registerTakeCount);
                                            if (v >= registerThreshold) {
                                                if (registerTakeCount < registerTakeCountMax) {
                                                    registerTakeCount++;
                                                    Log.d(TAG, "还需要验证" + (registerTakeCountMax - registerTakeCount) + "次");
                                                    startRegist(currentId);
                                                    resultTem = Arrays.copyOfRange(
                                                            result, 8, result.length);
                                                    continue;
                                                } else {
                                                    registerTakeCount = 0;
                                                    resultTem = null;
                                                }
                                            } else {
                                                startRegist(currentId);
                                                registerTakeCount = 0;
                                                resultTem = null;
                                                continue;
                                            }
                                        } else {
                                            resultTem = Arrays.copyOfRange(
                                                    result, 8, result.length);
                                            startRegist(currentId);
                                            registerTakeCount++;
                                            continue;
                                        }

                                        faceRecogUser.setId(currentId);
                                        faceRecogUser.setCreateTime(new Date());
                                        faceRecogUser.setFaceData(Arrays.copyOfRange(
                                                result, 8, result.length));
                                        if (baos != null) {
                                            File picPath = new File(MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_user_" + currentId + ".jpg");
                                            FileOutputStream fileOutputStream = new FileOutputStream(picPath);
                                            fileOutputStream.write(baos.toByteArray());
                                            baos.close();
                                            fileOutputStream.close();
                                            faceRecogUser.setFacePicPath(picPath.getPath());
                                        }
                                        faceRecogRecord.setType(0);
                                        faceRecogRecord.setMostLikelyId(currentId);
                                        faceRecogRecord.setLikelyHood(1);
                                        synchronized (MApp.daoSession) {
                                            MApp.daoSession.getFaceRecogUserDao().insertOrReplace(faceRecogUser);
                                            ZLog.i("insert tFaceRecogUser:" + faceRecogUser);
                                        }
                                    } else {
                                        ZLog.i("check in recog");
                                        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
                                        ZLog.i("face compare set:" + compareSet);
                                        for (FaceRecogUser tem : faceRecogUsers) {
                                            if (!compareSet.contains(tem.getId())) {
                                                continue;
                                            }
                                            float v = compareFace(Arrays.copyOfRange(
                                                    result, 8, result.length), tem.getFaceData());
                                            if (v >= likelyhood) {
                                                likelyhood = v;
                                                resultUser = tem.getId();
                                                faceRecogUser = tem;
                                            }
                                            ZLog.i("likelyhood:" + likelyhood + " current id:" + tem.getId() + " current result:" + v);
                                        }

                                        faceRecogRecord.setType(1);
                                        faceRecogRecord.setMostLikelyId(resultUser);
                                        faceRecogRecord.setLikelyHood(likelyhood);
                                    }
                                    faceRecogRecord.setRecogTime(new Date());
                                    MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                                    if (baos != null) {
                                        File picPath = new File(MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" + System.currentTimeMillis() + ".jpg");
                                        FileOutputStream fileOutputStream = new FileOutputStream(picPath);
                                        fileOutputStream.write(baos.toByteArray());
                                        baos.close();
                                        fileOutputStream.close();
//                                        if (!isRegister) {
                                        faceRecogRecord.setFacePicPath(picPath.getPath());
//                                        }
                                        MApp.daoSession.getFaceRecogRecordDao().insertOrReplace(faceRecogRecord);
                                        ZLog.i("insert FaceRecogRecord:" + faceRecogRecord);
                                    }
                                    takeFace = false;
                                    if (isRegister) {
                                        Log.d(TAG, "注册成功==id为==" + faceRecogUser.getId());
                                        if (faceRegistCallback != null) {
                                            faceRegistCallback.onFaceRegistSuccess(faceRecogUser.getId()
                                                    , faceRecogRecord, faceRecogUser);
                                        }
                                    } else {
                                        if (faceRecogCallback != null) {
                                            faceRecogCallback.onFaceResult(resultUser, likelyhood);
                                        }
                                        if (likelyhood > threshold && faceRecogCallback != null) {
//                                                faceCallback.onFaceRecogSuccess(resultUser, faceRecogRecord.id);
                                            faceTakeCount = 0;
                                            faceRecogCallback.onFaceRecogSuccess(resultUser
                                                    , faceRecogRecord, faceRecogUser);
                                        } else {
                                            MApp.daoSession.getFaceRecogRecordDao().delete(faceRecogRecord);
                                            ZLog.i("startRecog:" + "无匹配用户，识别失败,识别次数：" + faceTakeCount);
//                                                faceCallback.onError(1, "无匹配用户，识别失败，" + faceRecogRecord.getId(), faceRecogRecord.getId());
                                            if (faceTakeCount > registerTakeCountMax) {
                                                faceTakeCount = 0;
                                                if (faceRecogCallback != null) {
                                                    faceRecogCallback.onError(1, "无匹配用户，识别失败");
                                                }
                                            } else {
                                                try {
                                                    sleep(500);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                startRecog();
                                                faceTakeCount++;
                                                continue;
                                            }
                                        }
                                    }
//                                    sendDataToModule(sendSuanfaE);
                                    registerTakeCount = 0;
                                    resultTem = null;
                                }
                            } else if (cmd == (byte) 0x81) {
                                /**
                                 * 版本号
                                 */
                                String kernal = new String(Arrays.copyOfRange(result, 0, 10));
                                String rootfs = new String(Arrays.copyOfRange(result, 10, 20));
                                String algo = new String(Arrays.copyOfRange(result, 20, 30));
                                ZLog.i("result: kernal:" + kernal + " rootfs:" + rootfs + " algo:" + algo);
                                Log.d(TAG, "版本号===" + "result: kernal:" + kernal + " rootfs:" + rootfs + " algo:" + algo);
//                                if (faceCallback != null) {
//                                    faceCallback.onVersion(kernal, rootfs, algo);
//                                }
////                                sendDataToModule(sendSuanfaE);
                            } else if (cmd == (byte) 0x99 && suanfaEnd) {
                                /**
                                 * 算法关（文档中写的算法开关的body不一样，实际返回的body好像是一样的）
                                 */
//                                closeSerial();
                                break;
                            }
//                            else if (cmd == (byte) 0x83) {
//                                sendDataToModule(sendSuanfaE);
//                            }
                            else if (cmd == (byte) 0x83) {
                                ZLog.i("camera prepared");
//                                faceCallback.onCameraPrepared();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (loop) {
                    ZLog.i("reopen serial:" + e.toString());
                    Log.d(TAG, "reopen serial:" + e.toString());
                    try {
                        openSerial();
                    } catch (ZLException e1) {
                        ZLog.e(e);
                    } catch (InterruptedException e1) {
                        ZLog.e(e);
                    }
                }
            }
//            catch (InterruptedException e) {
//                ZLog.e(e);
//            }
            ZLog.i(this + "running end");
        }
    }

    public void init() {
        sendDataToModule(sendSuanfaS);
    }

    public void release() {
        sendDataToModule(sendSuanfaE);
    }

    /**
     * 耗时操作
     * 不启用
     *
     * @param jpg
     * @param id
     */
    private boolean startRegistWithPhoto(byte[] jpg, long id) {
        if (MApp.daoSession == null) {
            return false;
        }
        FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);

        if (sendCmdFileOpen(ymPhotoPath)) {
            if (sendCmdFileDate(jpg)) {
                if (sendCmdFileDone(jpg)) {
                    if (sendGetFaceWithPhoto()) {
                        byte[] result = facePicture.response;
                        float likelyhood = -1;
                        long resultUser;

                        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
                        setCompareSetAll();
                        ZLog.i("face compare set:" + compareSet);
                        for (FaceRecogUser tem : faceRecogUsers) {
                            if (!compareSet.contains(tem.getId())) {
                                continue;
                            }
                            float v = compareFace(Arrays.copyOfRange(
                                    result, 8, result.length), tem.getFaceData());
                            if (v >= likelyhood) {
                                likelyhood = v;
                                resultUser = tem.getId();
                            }
                            ZLog.i("likelyhood:" + likelyhood + " current id:" + tem.getId() + " current result:" + v);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 耗时操作
     *
     * @param jpgPath
     * @param id
     * @return 返回注册成功的id，
     * 已存在则返回存在的id
     * -2 特征提取失败
     * -1 此id不可用
     */
    public long startRegistWithPhoto(String jpgPath, long id) {
        ZLog.i("startRegistWithPhoto jpgPath:" + jpgPath + " id:" + id);

        if (!checkSuanfa(false)) {
            return -2;
        }

        FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);
        if (load != null) {
            return -1;
        }
        if (sendCmdFileOpen(ymPhotoPath)) {
            if (sendCmdFileDate(jpgPath)) {
                if (sendCmdFileDone(jpgPath)) {
                    if (sendGetFaceWithPhoto()) {
                        byte[] result = facePicture.response;
                        float likelyhood = -1;
                        long resultUser = 0;

                        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
                        setCompareSetAll();
                        ZLog.i("face compare set:" + compareSet);
                        for (FaceRecogUser tem : faceRecogUsers) {
                            if (!compareSet.contains(tem.getId())) {
                                continue;
                            }
                            float v = compareFace(Arrays.copyOfRange(
                                    result, 8, result.length), tem.getFaceData());
                            if (v >= likelyhood) {
                                likelyhood = v;
                                resultUser = tem.getId();
                            }
                            ZLog.i("likelyhood:" + likelyhood + " current id:" + tem.getId() + " current result:" + v);
                        }

                        File recordFile = new File(MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" +
                                TimeUtil.getNowDate(TimeUtil.DateTimePattern.ALL_TIME_INFILE) + ".jpg");
                        copyFileUsingFileChannels(new File(jpgPath), recordFile);

                        if (likelyhood > threshold) {
                            /**
                             * 已注册用户
                             */
                            FaceRecogRecord faceRecogRecord = new FaceRecogRecord(null,
                                    6,
                                    new Date(),
                                    recordFile.getPath(),
                                    resultUser,
                                    likelyhood
                            );
                            MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                            ZLog.i("getFaceRecogRecordDao insert:" + faceRecogRecord);
                            return resultUser;
                        }

                        FaceRecogUser faceRecogUser = new FaceRecogUser(id,
                                Arrays.copyOfRange(
                                        result, 8, result.length),
                                jpgPath,
                                new Date());
                        MApp.daoSession.getFaceRecogUserDao().insertOrReplace(faceRecogUser);
                        ZLog.i("getFaceRecogUserDao insertOrReplace:" + faceRecogUser);

                        FaceRecogRecord faceRecogRecord = new FaceRecogRecord(null,
                                4,
                                new Date(),
                                recordFile.getPath(),
                                id,
                                1.0f
                        );
                        MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                        ZLog.i("getFaceRecogRecordDao insert:" + faceRecogRecord);

//                        /**
//                         *  todo for test
//                         */
//                        for (int i = 0; i < 1000; i++) {
//                            ZLog.i("add test data:" + i);
//                            faceRecogUser.setId(null);
//                            byte[] faceData = faceRecogUser.getFaceData();
//                            int randomChange = (int) (Math.random() * faceData.length);
//                            faceData[randomChange] = 0;
//                            faceRecogUser.setFaceData(faceData);
//                            faceRecogUser.setCreateTime(new Date());
//                            faceRecogUser.setFacePicPath(null);
//                            MApp.daoSession.getFaceRecogUserDao().insert(faceRecogUser);
//                        }
                        return id;
                    }
                }
            }
        }
        return -2;
    }

    private void copyFileUsingFileChannels(File source, File dest) {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();

            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } catch (FileNotFoundException e) {
            ZLog.e(e);
        } catch (IOException e) {
            ZLog.e(e);
        } finally {
            closeClosable(inputChannel);
            closeClosable(outputChannel);
        }
    }


    /**
     * @param jpgPath
     * @return -2 特征提取失败
     */
    public long startRecogWithPhoto(String jpgPath) {
        if (MApp.daoSession == null) {
            return -2;
        }
        ZLog.i("startRecogWithPhoto");

        if (!checkSuanfa(false)) {
            return -2;
        }

        if (sendCmdFileOpen(ymPhotoPath)) {
            if (sendCmdFileDate(jpgPath)) {
                if (sendCmdFileDone(jpgPath)) {
                    if (sendGetFaceWithPhoto()) {
                        byte[] result = facePicture.response;
                        float likelyhood = -1;
                        long resultUser = -1;

                        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
                        setCompareSetAll();
                        ZLog.i("face compare set:" + compareSet);
                        for (FaceRecogUser tem : faceRecogUsers) {
                            if (!compareSet.contains(tem.getId())) {
                                continue;
                            }
                            float v = compareFace(Arrays.copyOfRange(
                                    result, 8, result.length), tem.getFaceData());
                            if (v >= likelyhood) {
                                likelyhood = v;
                                resultUser = tem.getId();
                            }
                            ZLog.i("likelyhood:" + likelyhood + " current id:" + tem.getId() + " current result:" + v);
                        }

                        File recordFile = new File(MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" +
                                TimeUtil.getNowDate(TimeUtil.DateTimePattern.ALL_TIME_INFILE) + ".jpg");
                        copyFileUsingFileChannels(new File(jpgPath), recordFile);

                        FaceRecogRecord faceRecogRecord = new FaceRecogRecord(null,
                                5,
                                new Date(),
                                recordFile.getPath(),
                                resultUser,
                                likelyhood
                        );
                        MApp.daoSession.getFaceRecogRecordDao().insert(faceRecogRecord);
                        ZLog.i("getFaceRecogRecordDao insert:" + faceRecogRecord);

                        if (likelyhood > threshold) {
                            return resultUser;
                        } else {
                            return -1;
                        }
                    }
                }
            }
        }
        return -2;
    }

    String ymPhotoPath = "picture.jpeg\0";
    ReadSenseSendPacket fileOpen = new ReadSenseSendPacket(
            RS_CMD_FILE_OPEN,
            RS_REPLY_FILE_OPEN,
            ymPhotoPath.getBytes()
    );
    ReadSenseSendPacket fileData = new ReadSenseSendPacket(
            RS_CMD_FILE_DATA,
            RS_REPLY_FILE_DATA
    );
    ReadSenseSendPacket fileDone = new ReadSenseSendPacket(
            RS_CMD_FILE_DONE,
            RS_REPLY_FILE_DONE
    );
    ReadSenseSendPacket facePicture = new ReadSenseSendPacket(
            RS_CMD_FACE_PICTURE,
            RS_REPLY_FACE_PICTURE
    );
    ReadSenseSendPacket getVersion = new ReadSenseSendPacket(
            RS_CMD_VERSION,
            RS_REPLY_VERSION
    );

    ReadSenseSendPacket faceSwitch = new ReadSenseSendPacket(
            RS_CMD_FACE_SWITCH,
            RS_REPLY_FACE_SWITCH
    );
    ReadSenseSendPacket setAre = new ReadSenseSendPacket(
            RS_CMD_RECO_AREA,
            RS_REPLY_RECO_AREA
    );
    ReadSenseSendPacket setdistance = new ReadSenseSendPacket(
            RS_CMD_SET_COMPARE_PARAM,
            RS_REPLY_SET_COMPARE_PARAM
    );
    Map<Byte, ReadSenseSendPacket> notifyMap = new HashMap();

    {
        addNotifyMap(fileOpen);
        addNotifyMap(fileData);
        addNotifyMap(fileDone);
        addNotifyMap(facePicture);
        addNotifyMap(getVersion);
        addNotifyMap(faceSwitch);
        addNotifyMap(setAre);
        addNotifyMap(setdistance);
    }

    public boolean openFaceSwitch() {
        ZLog.i("openFaceSwitch");
        faceSwitch.dataBody = new byte[]{1};
        boolean b = sendReadSenseSendPacket(faceSwitch, timeoutForYm);
        if (b) {
            boolean b1 = faceSwitch.response[0] == 0;
            if (b1) {
                suanfaEnd = false;
            }
            return faceSwitch.response[0] == 0;
        } else {
            return false;
        }
    }

    public boolean closeFaceSwitch() {
        ZLog.i("closeFaceSwitch");
        faceSwitch.dataBody = new byte[]{0};
        boolean b = sendReadSenseSendPacket(faceSwitch, timeoutForYm);
        if (b) {
            boolean b1 = faceSwitch.response[0] == 0;
            if (b1) {
                suanfaEnd = true;
            }
            return b1;
        } else {
            return false;
        }
    }


    private void addNotifyMap(ReadSenseSendPacket readSenseSendPacket) {
        notifyMap.put(readSenseSendPacket.replyType,
                readSenseSendPacket);
    }

    public boolean sendGetFaceWithPhoto() {
        facePicture.dataBody = new byte[0];
        if (sendReadSenseSendPacket(facePicture, timeoutForYm)) {
            return facePicture.response[0] == 1;
        } else {
            return false;
        }
    }

    private long timeoutForYm = 3000;

    public boolean sendCmdFileOpen(String s) {
        fileOpen.dataBody = s.getBytes();
        return sendReadSenseSendPacket(fileOpen, timeoutForYm);
    }

    public boolean sendCmdFileDate(String filePath) {
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(filePath);

            int available = fin.available();
            ZLog.i(filePath + " size:" + available);
            if (available > fileSizeLimit) {
                return false;
            }


            byte[] buffer = new byte[1024 * 100];
            int readLen = 0;
            while ((readLen = fin.read(buffer, 0, buffer.length)) != -1) {
                byte[] bytes = Arrays.copyOfRange(buffer, 0, readLen);
                if (!sendCmdFileDate(bytes)) {
                    return false;
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            ZLog.e(e);
        } catch (IOException e) {
            ZLog.e(e);
        } catch (Exception e) {
            ZLog.e(e);
        } finally {
            closeClosable(fin);
        }
        return false;
    }

    public boolean sendCmdFileDate(byte[] bytes) {
        if (bytes != null) {
            fileData.dataBody = bytes;
            if (sendReadSenseSendPacket(fileData, timeoutForYm)) {
                return fileData.response[0] == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean sendCmdFileDone(String filePath) {
        fileDone.dataBody = MD5.getFileMD5(new File(filePath));
        if (fileDone.dataBody != null) {
            if (sendReadSenseSendPacket(fileDone, timeoutForYm)) {
                return fileDone.response[0] == 0;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean sendCmdFileDone(byte[] bytes) {
        fileDone.dataBody = MD5.getbytesMD5(bytes);
        if (sendReadSenseSendPacket(fileDone, timeoutForYm)) {
            return fileDone.response[0] == 0;
//            return true;
        } else {
            return false;
        }
    }

    private byte[] getBytes(String filePath) {
        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeClosable(fis);
            closeClosable(bos);
        }
        return buffer;
    }

    private void closeClosable(Closeable fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                ZLog.e(e);
            }
        }
    }

    private synchronized boolean sendReadSenseSendPacket(ReadSenseSendPacket rssp, long timeout) {
        ZLog.i("send rssp start:" + rssp + " timeout:" + timeout);
        if (rssp.dataBody == null) {
            return false;
        }

        if (timeout >= 0) {
            synchronized (rssp) {
                rssp.reserve = 1;
                rssp.response = null;
                sendDate(parseSendPacket(rssp));

                try {
                    rssp.wait(timeout);
                } catch (InterruptedException e) {
                    ZLog.e(e);
                }
                ZLog.i("send rssp finish:" + rssp);
            }
            if (rssp.reserve == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            sendDate(parseSendPacket(rssp));
            return true;
        }
    }

    private synchronized void openSerial() throws ZLException, InterruptedException, SecurityException {
        ZLog.i("open serial");
        Log.d(TAG, "===openSerial===");
        if (yuemian == null) {
//            yuemian = new SerialExecutor("/dev/ttymxc4", 115200);
            yuemian = new SerialExecutor(dev, baudrate);
//            yuemian = new SerialExecutor("/dev/ttyS3", 1500000);
            Log.d(TAG, "yuemian===" + yuemian);
        }
        serialPort = yuemian.openSerial();
        Log.d(TAG, "serialPort===" + serialPort);

        if (serialPort == null) {
            if (faceRegistCallback != null) {
                faceRegistCallback.onError(4, "串口通讯异常");
            }
            if (faceRecogCallback != null) {
                faceRecogCallback.onError(4, "串口通讯异常");
            }
            return;
        }
        loop = true;
        new ParseRecvThread().start();
        TimeUnit.MILLISECONDS.sleep(500);
    }

    private byte[] parseObjectArray(Object[] send1) {
        byte[] result = new byte[send1.length + 8];
        int start = 0;
        for (Object tem : send1) {
//            ZLog.i(tem.getClass().toString());
            if (tem instanceof String) {
                for (int i = 0; i < ((String) tem).length(); i++) {
                    result[start] = (byte) ((String) tem).charAt(i);
                    start++;
                }
            } else if (tem instanceof Integer) {
                result[start] = (byte) (int) tem;
                start++;
            } else {
                result[start] = (byte) tem;
                start++;
            }
        }
        return result;
    }

    public synchronized void sendDate(byte[] bytes) {
        if (serialPort == null) {
            try {
                openSerial();
            } catch (ZLException e) {
                ZLog.e(e);
            } catch (InterruptedException e) {
                ZLog.e(e);
            } catch (SecurityException e) {
                ZLog.e(e);
            }
        }
        if (serialPort == null) {
            return;
        }

        try {
            int offset = 0;
            int bufferSize = 1024;
            int remainSize = bytes.length - offset;
            while (remainSize > 0) {
                int count = remainSize > bufferSize ? bufferSize : remainSize;
                serialPort.getOutputStream().write(bytes, offset,
                        count);
                serialPort.getOutputStream().flush();

                ZLog.i("read sense send count:" + count + " data:" + ByteUtil.bytesToHexStringLimit(
                        Arrays.copyOfRange(bytes, offset, offset + count), " ", 20));

                Log.d(TAG, "发送的数据===" + "read sense send count:" + count + " data:" + ByteUtil.bytesToHexStringLimit(
                        Arrays.copyOfRange(bytes, offset, offset + count), " ", 20));
                remainSize -= count;
                offset += count;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startRegist(long faceId) {
        this.startRegist(faceId, faceRegistCallback);
    }

//    public void startRegist(long id, float registerThreshold) {
//        isRegister = true;
//        this.registerThreshold = registerThreshold;
//        this.startRegist(id, faceRegistCallback);
//    }

    public void startRegist(long faceId, float registerThreshold, FaceRegistCallback faceRegistCallback) {
        isRegister = true;
        this.registerThreshold = registerThreshold;
        this.faceRegistCallback = faceRegistCallback;
        this.startRegist(faceId, faceRegistCallback);
    }

    public void startRegist(long faceId, float registerThreshold, int lightlevel, FaceRegistCallback faceRegistCallback) {
        isRegister = true;
        this.registerThreshold = registerThreshold;
        this.lightlevel = lightlevel;
        this.faceRegistCallback = faceRegistCallback;
        this.startRegist(faceId, faceRegistCallback);
    }

    public void startRegist(long faceId, FaceRegistCallback faceRegistCallback) {
        isRegister = true;
        this.faceRegistCallback = faceRegistCallback;
        ZLog.i("startRegist:" + faceId);
        Log.d(TAG, "faceId===" + faceId);
        setCompareSetAll();
        if (!checkSuanfa(true)) {
            if (faceRegistCallback != null) {
                faceRegistCallback.onError(4, "串口通讯异常");
            }
            return;
        }
        openInfraredLightDelay();
        currentId = faceId;
        isRegister = true;
        takeFace = true;
        sendDataToModule(sendTake);
        mhandler.removeMessages(msgTimeout);
        mhandler.sendEmptyMessageDelayed(msgTimeout, timeout);
        Log.d(TAG, "currentId===" + currentId);
    }

    public void startRecog() {
        isRegister = false;
        this.startRecog(faceRecogCallback);
    }

    public void startRecog(float threshold) {
        isRegister = false;
        this.threshold = threshold;
        this.startRecog(faceRecogCallback);
    }

    public void startRecog(float threshold, FaceRecogCallback faceRecogCallback) {
        isRegister = false;
        this.threshold = threshold;
        this.faceRecogCallback = faceRecogCallback;
        this.startRecog(faceRecogCallback);
    }

    public void startRecog(float threshold, int lightlevel, FaceRecogCallback faceRecogCallback) {
        isRegister = false;
        this.threshold = threshold;
        this.lightlevel = lightlevel;
        this.faceRecogCallback = faceRecogCallback;
        this.startRecog(faceRecogCallback);
    }

    public void startRecog(FaceRecogCallback faceRecogCallback) {
        isRegister = false;
        this.faceRecogCallback = faceRecogCallback;
        ZLog.i("startRecog");
        if (!checkSuanfa(true)) {
            if (faceRecogCallback != null) {
                faceRecogCallback.onError(4, "串口通讯异常");
            }
            return;
        }
        setCompareSetAll();
        openInfraredLightDelay();
        isRegister = false;
        takeFace = true;
        sendDataToModule(sendTake);
        mhandler.removeMessages(msgTimeout);
        mhandler.sendEmptyMessageDelayed(msgTimeout, timeout);
    }

    public void openInfraredLightDelay() {
        ZLog.i("openInfraredLightDelay");
        mhandler.removeMessages(openInfrared);
        mhandler.removeMessages(closeInfrared);
        mhandler.sendEmptyMessageDelayed(openInfrared, 200);
    }

    public void openInfraredLight() {
        mhandler.removeMessages(closeInfrared);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZLog.i("openInfraredLight");
                    if (gpioType) {
                        JniUtils.openLight();
                    } else {
//                        openLightSerial();
                        Log.d(TAG, "lightlevel==" + lightlevel);
                        if (lightlevel == 0) {
                            Log.d(TAG, "打开补光灯");
                            openLightSerial();
                        } else {
                            Log.d(TAG, "设置补光灯亮度");
                            setLightLevelSerial(lightlevel);
                        }
                    }
                } catch (Exception e1) {
                    ZLog.e(e1);
                }
            }
        }).start();
    }

    /**
     * recv a5 03 00 02 00 01 06 a3
     */
    private void openLightSerial() {
        SerialExecutor serialExecutor = new SerialExecutor(lightDev, lightBaud);
        try {
            byte[] data = new byte[]{
                    (byte) 165, 3, 0, 2, 0, 1, 6, (byte) 163
            };
            byte[] bytes = serialExecutor.sendData(data);
        } catch (ZLException e) {
            e.printStackTrace();
            ZLog.e(e);
        }
    }

    /**
     * 设置补光灯亮度
     *
     * @param lightlevel
     * @return
     */
    public boolean setLightLevelSerial(int lightlevel) {
        this.lightlevel = lightlevel;
        SerialExecutor serialExecutor = new SerialExecutor(lightDev, lightBaud);
        try {
            byte[] data = new byte[]{(byte) 0xA5, 0x03, 0x00, 0x04, 0x00, (byte) lightlevel, 0x06, 0x00};
            data[7] = ByteUtil.xor(data, 0, 6);
            byte[] bytes = serialExecutor.sendData(data);
            if (bytes[5] == 0x01) {
                ZLog.i("设置成功");
                return true;
            } else {
                ZLog.i("设置失败");
                return false;
            }
        } catch (ZLException e) {
            e.printStackTrace();
            ZLog.e(e);
        }
        return false;
    }

    private void closeLightSerial() {
        SerialExecutor serialExecutor = new SerialExecutor(lightDev, lightBaud);
        try {
            byte[] data = new byte[]{
                    (byte) 165, 3, 0, 2, 0, 0, 6, (byte) 162
            };
            byte[] bytes = serialExecutor.sendData(data);
        } catch (ZLException e) {
            e.printStackTrace();
            ZLog.e(e);
        }
    }

    private void openGpio() {
        if (serialPortgpio == null) {
            try {
                serialPortgpio = new SerialPort(new File(gpio), gpioPort, 0);
            } catch (IOException e) {
                ZLog.e(e);
            }
        }
    }

    public void closeInfraredLightDelay() {
        ZLog.i("closeInfraredLightDelay");
        mhandler.removeMessages(closeInfrared);
        mhandler.sendEmptyMessageDelayed(closeInfrared, 3000);
    }

    public void closeInfraredLight() {
        mhandler.removeMessages(openInfrared);
        mhandler.removeMessages(closeInfrared);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ZLog.i("closeInfraredLight");
                    if (gpioType) {
                        JniUtils.closeLight();
                    } else {
                        closeLightSerial();
                    }
                } catch (Exception e1) {
                    ZLog.e(e1);
                }
            }
        }).start();
    }

    public void stop() {
        takeFace = false;
        closeInfraredLight();
//        sendDataToModule(sendSuanfaS);
    }

    private boolean checkSuanfa(boolean suanfaStatusShouldBe) {
        if (canSwitchVideo) {
            if (suanfaStatusShouldBe && suanfaEnd) {
                return openFaceSwitch();
            } else if (!suanfaStatusShouldBe && !suanfaEnd) {
                return closeFaceSwitch();
            }
        }
        return true;
    }

    public void getVersion() {
        sendDataToModule(getVer);
    }

    public String getVersionBlock() {
        getVersion.dataBody = new byte[0];
        boolean b = sendReadSenseSendPacket(getVersion, timeoutForYm);
        if (b) {

            String kernal = new String(Arrays.copyOfRange(getVersion.response, 0, 10));
            String rootfs = new String(Arrays.copyOfRange(getVersion.response, 10, 20));
            String algo = new String(Arrays.copyOfRange(getVersion.response, 20, 30));
            String version = "kernal:" + kernal + " rootfs:" + rootfs + " algo:" + algo;
            ZLog.i(version);
            Log.d(TAG, "版本号1===" + "kernal:" + kernal + " rootfs:" + rootfs + " algo:" + algo);
            return version;
        } else {
            return "";
        }
    }

    public void rotate0() {
        sendDataToModule(rotate0);
    }

    public void rotate180() {
        ZLog.i("rotate180");
        sendDataToModule(rotate180);
    }

    public void sendDataToModule(final Object[] sendTake) {
        executors.execute(new Runnable() {
            @Override
            public void run() {

                byte[] bytes3 = parseObjectArray(sendTake);
                ZLog.i("rotate" + ByteUtil.bytesToHexString(bytes3, ""));
                Log.d(TAG, "sendDataToModule===" + ByteUtil.bytesToHexString(bytes3, ""));
                if (Arrays.equals(sendTake, sendSuanfaE)) {
                    suanfaEnd = true;
                } else if (Arrays.equals(sendTake, sendSuanfaS)) {
                    suanfaEnd = false;
                }
                sendDate(bytes3);
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    public synchronized void closeSerial() {
        ZLog.i("close serial");
        if (yuemian == null) {
            return;
        }
        loop = false;
        try {
            yuemian.closeSerial(serialPort);
            yuemian = null;
            serialPort = null;

            closeInfraredLight();
//            serialPortgpio.close();
//            serialPortgpio = null;

            Thread.sleep(200);
        } catch (InterruptedException e) {
            ZLog.e(e);
        } catch (ZLException e) {
            ZLog.e(e);
        }
    }

    /**
     * String Integer Byte
     */
    Object[] getVer = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_VERSION,
            0,
            0,
            0,
            0
    };
    public Object[] sendStart = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_START,
            0,
            0,
            0,
            0
    };
    Object[] sendStop = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_STOP,
            0,
            0,
            0,
            0
    };
    public Object[] openFaceRect = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_RECT_SWITCH,
            0,
            0,
            0,
            0,
            1
    };
    public Object[] closeFaceRect = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_RECT_SWITCH,
            0,
            0,
            0,
            0,
            0
    };
    Object[] reboot = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_REBOOT,
            0,
            0,
            0,
            0
    };
    Object[] getSn = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_GET_SN,
            0,
            0,
            0,
            0
    };
    Object[] getLic = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_LIC_INFO,
            0,
            0,
            0,
            0
    };
    Object[] rotate0 = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_SET_ROTATE_ANGLE,
            0,
            0,
            0,
            1,
            0
    };
    Object[] rotate180 = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_SET_ROTATE_ANGLE,
            0,
            0,
            0,
            1,
            180
    };

    class ReadSenseSendPacket {
        byte cmdType;
        byte replyType;
        byte[] dataBody;
        int reserve;
        byte[] response;

        @Override
        public String toString() {
            return "ReadSenseSendPacket{" +
                    "cmdType=" + ByteUtil.bytesToHexString(new byte[]{cmdType}, "") +
                    ", replyType=" + ByteUtil.bytesToHexString(new byte[]{replyType}, "") +
                    ", reserve=" + reserve +
                    ", response=" + ByteUtil.bytesToHexStringLimit(response, " ", 20) +
                    ", dataBody=" + ByteUtil.bytesToHexStringLimit(response, " ", 20) +
                    '}';
        }

        public ReadSenseSendPacket(byte cmdType, byte[] dataBody) {
            this.cmdType = cmdType;
            this.dataBody = dataBody;
        }

        public ReadSenseSendPacket(byte cmdType, byte replyType) {
            this.cmdType = cmdType;
            this.replyType = replyType;
        }

        public ReadSenseSendPacket(byte cmdType, byte replyType, byte[] dataBody) {
            this.cmdType = cmdType;
            this.replyType = replyType;
            this.dataBody = dataBody;
        }
    }

    byte[] head = "READSENSE".getBytes();

    byte[] parseSendPacket(ReadSenseSendPacket rssp) {
        byte[] result = new byte[head.length + 7 + rssp.dataBody.length];
        System.arraycopy(head, 0,
                result, 0, head.length);
        result[head.length + 2] = rssp.cmdType;
        byte[] bytes = intToByte(rssp.dataBody.length);
        System.arraycopy(
                bytes, 0,
                result, head.length + 3, 4
        );
        System.arraycopy(
                rssp.dataBody, 0,
                result, head.length + 7, rssp.dataBody.length

        );
        return result;
    }

    public byte[] intToByte(int val) {
        byte[] b = new byte[4];
        b[3] = (byte) (val & 0xff);
        b[2] = (byte) ((val >> 8) & 0xff);
        b[1] = (byte) ((val >> 16) & 0xff);
        b[0] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    public byte[] intToByteAre(int val) {
        byte[] b = new byte[4];
        b[0] = (byte) (val & 0xff);
        b[1] = (byte) ((val >> 8) & 0xff);
        b[2] = (byte) ((val >> 16) & 0xff);
        b[3] = (byte) ((val >> 24) & 0xff);
        return b;
    }

    Object[] sendSuanfaS = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_FACE_SWITCH,
            0,
            0,
            0,
            1,
            1
    };
    Object[] sendSuanfaE = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_FACE_SWITCH,
            0,
            0,
            0,
            1,
            0
    };

    Object[] sendTake = new Object[]{
            "READSENSE",
            0,
            0,
            RS_CMD_TAKE_PHOTO,
            0,
            0,
            0,
            1,
            1
    };

//    private static final ModuleDataHelper ourInstance = new ModuleDataHelper();
//
//    static public ModuleDataHelper getInstance() {
//        return ourInstance;
//    }
//
//    private ModuleDataHelper() {
////        try {
////            dev = ConfigManager.getInstance().getIni().getValue(
////                    ConfigString.getInstance().app,
////                    "face_addr");
////            lightDev = ConfigManager.getInstance().getIni().getValue(
////                    ConfigString.getInstance().app,
////                    ConfigString.getInstance().faceLigntAddr);
////            gpioType = "0".equals(
////                    ConfigManager.getInstance().getIni().getValue(
////                            ConfigString.getInstance().app,
////                            ConfigString.getInstance().infraredType)
////            );
////        } catch (Exception e) {
////            ZLog.e(e);
////        }
//    }

    private volatile static ModuleDataHelper moduleDataHelper;

    public static ModuleDataHelper getInstance() {
        if (moduleDataHelper == null) {
            synchronized (ModuleDataHelper.class) {
                if (moduleDataHelper == null) {
                    moduleDataHelper = new ModuleDataHelper();
                }
            }
        }
        return moduleDataHelper;
    }
}
