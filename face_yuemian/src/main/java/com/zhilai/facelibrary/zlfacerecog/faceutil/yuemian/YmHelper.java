//package com.zhilai.facelibrary.zlfacerecog.faceutil.yuemian;
//
//import android.os.Handler;
//import android.os.Looper;
//import android.os.Message;
//
//import com.zhilai.driver.config.ConfigManager;
//import com.zhilai.driver.config.ConfigString;
//import com.zhilai.driver.log.ZLog;
//import com.zhilai.facelibrary.zlfacerecog.MApp;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.ByteUtil;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.CalUtil;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.FaceUtil;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.MD5;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.SerialExecutor;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.ThreadUtil;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.TimeUtil;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.Util;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.camera.CameraView;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLErrorEnum;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.exception.ZLException;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogRecord;
//import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.FaceRecogUser;
//import com.zhilai.myapplication.jni.JniUtils;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import zhilai.serialport.SerialPortFinder;
//
//public class YmHelper {
//
//    /**
//     * 摄像头注册时的提取特征的次数
//     */
//    public int REGIST_TIMES = 3;
//
//    /**
//     * 摄像头注册与识别时 提取特征次数的最大值
//     */
//    public int TAKE_PHOTO_TIMES = 10;
//    public int COMPARE_FACE_AND_CAMERA_TIMES = 10;
//    public int mTimeoutForYm = 3000;
//    public int mSmallTimeoutForYm = 1000;
//
//    /**
//     * 算法是否关闭
//     */
//    private boolean mSuanfaEnd = true;
//
//    /**
//     * 变量说明：
//     * <p>
//     * 是否可以切换摄像头视频识别模式和图片识别模式
//     * <p>
//     * 摄像头视频识别时 需要开启阅面模块的人脸算法算法
//     * 图片识别时，如果开启了阅面模块的算法，阅面模块容易崩溃
//     * 所以需要关闭阅面模块的算法，再执行图片识别的操作
//     * <p>
//     * 但是老版本的阅面模块（7.9.4）
//     * 频繁开关人脸算法算法 会导致模块崩溃
//     */
//    public boolean canSwitchVideo = true;
//
//    /**
//     * 提取人脸特征的超时时间
//     */
//    public int timeout = 10 * 1000;
//
//    private int msgTimeout = 1;
//    private int closeInfrared = 2;
//    private int openInfrared = 3;
//
//    public String mFaceDev = "/dev/ttyUSB0";
//    private int mFaceBaudrate = 115200;
//    public String mLightDev = "/dev/ttyUSB1";
//    public int mLightBaud = 115200;
//
//    /**
//     * 补光灯是否使用GPIO
//     */
//    public boolean mGpioType;
//    byte[] mCloseInfraredBytes = new byte[]{(byte) 0xa5, 0x03, 0x00, 0x02, 0x00, 0x00, 0x06, (byte) 0xa2};
//    byte[] mOpenInfraredBytes = new byte[]{(byte) 0xa5, 0x03, 0x00, 0x02, 0x00, 0x01, 0x06, (byte) 0xa3};
//
//    private int fileSizeLimit = 1024 * 500;
//    public LoopCommu slc;
//
//    public Handler mhandler = new Handler(Looper.getMainLooper()) {
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == msgTimeout) {
//                closeInfraredLightDelay();
//            } else if (msg.what == closeInfrared) {
//                closeInfraredLight();
//            } else if (msg.what == openInfrared) {
//                openInfraredLight();
//            }
//        }
//    };
//
//    private CameraView mCameraView;
//
//    byte[] mHead = "READSENSE".getBytes();
//    byte[] mLightHead = new byte[]{(byte) 0xa5, 0x03, 0x00, 0x02};
//
//    /**
//     * 可限制人脸比对的数据集
//     */
//    Set<Long> mCompareSet = new HashSet<>();
//
//    String ymPhotoPath = "picture.jpeg\0";
//
//    ReadSenseSendPacket mFileOpen = new ReadSenseSendPacket(
//            Ym.RS_CMD_FILE_OPEN,
//            Ym.RS_REPLY_FILE_OPEN,
//            ymPhotoPath.getBytes()
//    );
//    ReadSenseSendPacket mFileData = new ReadSenseSendPacket(
//            Ym.RS_CMD_FILE_DATA,
//            Ym.RS_REPLY_FILE_DATA
//    );
//    ReadSenseSendPacket mFileDone = new ReadSenseSendPacket(
//            Ym.RS_CMD_FILE_DONE,
//            Ym.RS_REPLY_FILE_DONE
//    );
//    ReadSenseSendPacket mFacePicture = new ReadSenseSendPacket(
//            Ym.RS_CMD_FACE_PICTURE,
//            Ym.RS_REPLY_FACE_PICTURE
//    );
//    ReadSenseSendPacket mGetVersion = new ReadSenseSendPacket(
//            Ym.RS_CMD_VERSION,
//            Ym.RS_REPLY_VERSION
//    );
//
//    ReadSenseSendPacket mFaceSwitch = new ReadSenseSendPacket(
//            Ym.RS_CMD_FACE_SWITCH,
//            Ym.RS_REPLY_FACE_SWITCH
//    );
//    ReadSenseSendPacket mSetDebug = new ReadSenseSendPacket(
//            Ym.RS_CMD_SET_DEBUG_FLAG,
//            Ym.RS_REPLY_SET_DEBUG_FLAG
//    );
//    ReadSenseSendPacket mSystemInfo = new ReadSenseSendPacket(
//            Ym.RS_CMD_SYSTEM_INFO,
//            Ym.RS_REPLY_SYSTEM_INFO
//    );
//
//
//    /**
//     * 协议中这里是等待REPLY_TAKE_PHOTO的答复
//     * 实际使用中等待RS_REPLY_FACE的答复
//     * 便于人脸特征提取
//     */
//    ReadSenseSendPacket mTakePhoto = new ReadSenseSendPacket(
//            Ym.RS_CMD_TAKE_PHOTO,
////            Ym.RS_REPLY_TAKE_PHOTO,
//            Ym.RS_REPLY_FACE,
//            new byte[]{1}
//    );
//
//    ReadSenseSendPacket mRotate = new ReadSenseSendPacket(
//            Ym.RS_CMD_SET_ROTATE_ANGLE,
//            Ym.RS_REPLY_SET_ROTATE_ANGLE
//    );
//
//    /**
//     * 切换 红外 与 黑白 显示
//     */
//    ReadSenseSendPacket mUvcMode = new ReadSenseSendPacket(
//            Ym.RS_CMD_UVC_SOURCE,
//            Ym.RS_REPLY_UVC_SOURCE
//    );
//
//    /**
//     * true 跳过串口通信的代码，对于 人脸特征获取的方法，返回随机生成的数组
//     */
//    private boolean mPhoneDebug = false;
//
//    public void setmCameraView(CameraView mCameraView) {
//        this.mCameraView = mCameraView;
//    }
//
//    public static float compareFace(Object[] result, Object[] target) {
//        long start = System.currentTimeMillis();
//        float n = 0f;
//        int length = Math.min(result.length, target.length);
//        for (int i = 0; i < length; i++) {
//            n += ((float) result[i]) * ((float) target[i]);
//        }
//        ZLog.i("compare take:" + (System.currentTimeMillis() - start));
//        return n;
//    }
//
//    public static float compareFace(byte[] result, byte[] target) {
//        long start = System.currentTimeMillis();
//        float n = 0f;
//        int length = Math.min(result.length, target.length);
//        ZLog.i("compare length:" + length);
//        for (int i = 0; i + 3 < length; i += 4) {
//            n += FaceUtil.getFloat(Arrays.copyOfRange(result, i, i + 4)) *
//                    FaceUtil.getFloat(Arrays.copyOfRange(target, i, i + 4));
//        }
//        ZLog.i("compare take:%d, result:%f", (System.currentTimeMillis() - start), n);
//        return n;
//    }
//
//    public FaceRecogRecord compareFaceToDb(byte[] faceProperity) {
//        float mostLikelyHood = 0;
//        Long mostLikelyId = 0L;
//        float secondLikelyHood = 0;
//        Long secondLikelyId = 0L;
//        FaceRecogRecord faceRecogRecord = new FaceRecogRecord();
//
//        for (long temId : mCompareSet) {
//            FaceRecogUser tem = MApp.daoSession.getFaceRecogUserDao().load(temId);
//            float v = compareFace(faceProperity, tem.getFaceData());
//            if (v >= mostLikelyHood) {
//                secondLikelyHood = mostLikelyHood;
//                secondLikelyId = mostLikelyId;
//
//                mostLikelyHood = v;
//                mostLikelyId = tem.getId();
//            } else if (v >= secondLikelyHood) {
//                secondLikelyHood = v;
//                secondLikelyId = tem.getId();
//            }
//
//            ZLog.i("likelyhood:" + mostLikelyHood + " current id:" + tem.getId() + " current result:" + v);
//        }
//
//        faceRecogRecord.setMostLikelyHood(mostLikelyHood);
//        faceRecogRecord.setMostLikelyId(mostLikelyId);
//        faceRecogRecord.setSecondLikelyHood(secondLikelyHood);
//        faceRecogRecord.setSecondLikelyId(secondLikelyId);
//
//        ZLog.i("compareFaceToDb end:" + faceRecogRecord);
//        return faceRecogRecord;
//    }
//
//    public void blackToColor() throws Exception {
//        mUvcMode.dataBody = new byte[]{3};
//        sendReadSenseSendPacket(mUvcMode, mSmallTimeoutForYm);
//    }
//
//    public void colorToBlack() throws Exception {
//        mUvcMode.dataBody = new byte[]{4};
//        sendReadSenseSendPacket(mUvcMode, mSmallTimeoutForYm);
//    }
//
//    public void rotate0() throws Exception {
//        mRotate.dataBody = new byte[]{0};
//        sendReadSenseSendPacket(mRotate, mTimeoutForYm);
//    }
//
//    public void rotate180() throws Exception {
//        mRotate.dataBody = new byte[]{(byte) 180};
//        sendReadSenseSendPacket(mRotate, mTimeoutForYm);
//    }
//
//    public void setmCompareSet(Set<Long> cs) {
//        this.mCompareSet = cs;
//    }
//
//    public void setCompareSet(long... cs) {
//        for (long tem : cs) {
//            mCompareSet.add(tem);
//        }
//    }
//
//    /**
//     * 在特定的情景可能是和保存的人脸数据中的部分条目进行比较，
//     * 因此增加一个设置 人脸对比数据集 的方法
//     * 目前是与所有保存的数据进行比较
//     */
//    public void setCompareSetAll() {
//        ZLog.i("start set CompareSet");
//        mCompareSet.clear();
//        List<FaceRecogUser> faceRecogUsers = MApp.daoSession.getFaceRecogUserDao().loadAll();
//        for (FaceRecogUser tem : faceRecogUsers) {
//            mCompareSet.add(tem.getId());
//        }
//        ZLog.i("finish set CompareSet");
//    }
//
//    public void init() {
//        try {
//            getSystemInfo();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * 耗时操作
//     *
//     * @param jpgPath
//     * @param id
//     * @return 返回注册成功的id，
//     * 已存在则返回存在的id
//     * -2 特征提取失败
//     * -1 此id不可用
//     */
//    public long startRegistWithPhoto(String jpgPath, long id) throws Exception {
//        ZLog.i("startRegistWithPhoto jpgPath:" + jpgPath + " id:" + id);
//
//        if (!checkSuanfa(false)) {
//            return -2;
//        }
//
//        FaceRecogUser load = MApp.daoSession.getFaceRecogUserDao().load(id);
//        if (load != null) {
//            return -1;
//        }
//
//        byte[] faceProp = getFacePropFromFile(jpgPath);
//
//        float likelyhood;
//        long resultUser;
//
//        setCompareSetAll();
//
//        FaceRecogRecord recordTem = compareFaceToDb(faceProp);
//        likelyhood = recordTem.mostLikelyHood;
//        resultUser = recordTem.mostLikelyId;
//
//        String filePath = MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" +
//                TimeUtil.getNowDate(TimeUtil.DateTimePattern.STANDARD_TIME_NO_SPACE) + ".jpg";
//        File recordFile = new File(filePath);
//        FaceUtil.copyFileUsingFileChannels(new File(jpgPath), recordFile);
//
//        if (likelyhood >= FaceDataHelper.getInstance().mThreshold) {
//            /**
//             * 已注册用户
//             * 6 图片注册失败，已注册
//             */
//            recordTem.setType(6);
//            recordTem.setFacePicPath(filePath);
//            FaceUtil.saveFaceRecord(recordTem);
//            return resultUser;
//        } else {
//
//            /**
//             * 注册成功
//             * 4 图片注册
//             */
//            recordTem.setType(4);
//            recordTem.setMostLikelyId(id);
//            recordTem.setMostLikelyHood(1.0f);
//            recordTem.setFacePicPath(filePath);
//            FaceUtil.saveFaceRecord(recordTem);
//
//            FaceUtil.saveFaceUser(recordTem, faceProp, id);
//            return id;
//        }
////                        /**
////                         *  todo for test
////                         */
////                        for (int i = 0; i < 1000; i++) {
////                            ZLog.i("add test data:" + i);
////                            faceRecogUser.setId(null);
////                            byte[] faceData = faceRecogUser.getFaceData();
////                            int randomChange = (int) (Math.random() * faceData.length);
////                            faceData[randomChange] = 0;
////                            faceRecogUser.setFaceData(faceData);
////                            faceRecogUser.setCreateTime(new Date());
////                            faceRecogUser.setFacePicPath(null);
////                            Ym.daoSession.getFaceRecogUserDao().insert(faceRecogUser);
////                        }
//    }
//
//    /**
//     * @param jpgPath
//     * @return -2 特征提取失败
//     * -1 无匹配用户
//     */
//    public long startRecogWithPhoto(String jpgPath) throws Exception {
//        ZLog.i("startRecogWithPhoto");
//
//        if (!checkSuanfa(false)) {
//            return -2;
//        }
//
//        byte[] faceProp = getFacePropFromFile(jpgPath);
//
//        setCompareSetAll();
//
//        FaceRecogRecord recordTem = compareFaceToDb(faceProp);
//
//        String filePath = MApp.CONFIG_PATH + "/" + MApp.FACEPICDIR + "/pic_record_" +
//                TimeUtil.getNowDate(TimeUtil.DateTimePattern.STANDARD_TIME_NO_SPACE) + ".jpg";
//        File recordFile = new File(filePath);
//        FaceUtil.copyFileUsingFileChannels(new File(jpgPath), recordFile);
//
//        /**
//         * 识别完成
//         * 5 图片识别
//         */
//        recordTem.setType(5);
//        recordTem.setFacePicPath(filePath);
//        FaceUtil.saveFaceRecord(recordTem);
//
//        if (recordTem.mostLikelyHood >= FaceDataHelper.getInstance().mThreshold) {
//            return recordTem.mostLikelyId;
//        } else {
//            return -1;
//        }
//    }
//
//    public void setDebug(int flag) throws Exception {
//        ZLog.i("setDebug");
//        mSetDebug.dataBody = FaceUtil.intToByte(flag);
//        sendReadSenseSendPacket(mSetDebug, mTimeoutForYm);
//        checkCommonRsp0(mSetDebug);
//    }
//
//    public byte[] getSystemInfo() throws Exception {
//        ZLog.i("getSystemInfo");
//        sendReadSenseSendPacket(mSystemInfo, mTimeoutForYm);
//        return mSetDebug.response;
//    }
//
//    public boolean openFaceSwitch() {
//        ZLog.i("openFaceSwitch");
//        mFaceSwitch.dataBody = new byte[]{1};
//        boolean b = false;
//        try {
//            sendReadSenseSendPacket(mFaceSwitch, mTimeoutForYm);
//            b = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (b) {
//            boolean b1 = mFaceSwitch.response[0] == 0;
//            if (b1) {
//                mSuanfaEnd = false;
//            }
//            return mFaceSwitch.response[0] == 0;
//        } else {
//            return false;
//        }
//    }
//
//    /**
//     * 此处添加同步锁主要是为了保证每一次获取到的图片 尽可能对应 获取特征时间点 的图片
//     *
//     * @param id
//     * @return FaceRecogRecord
//     * <p>
//     * 阅面模块是提取特征数组进行本地处理
//     * 注册流程可以实现 覆盖同一id对应的特征数组 的功能
//     * 如果需要进行限制，不允许覆盖功能，可在调用方法前通过数据库查找进行判断，参见：
//     * @see com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.ModuleDataHelper#startRegist(long)
//     */
//    public synchronized FaceRecogRecord startRegist(long id) {
//        ZLog.i("startRegist start id:" + id);
//
//        FaceRecogRecord rst = null;
//
//        if (!checkSuanfa(true)) {
//            return null;
//        }
//
//        if (!checkCamera(mCameraView)) {
//            return rst;
//        }
//
//        /**
//         * 人脸注册流程中，
//         * 当前提取的人脸特征会与上一次提取的进行对比，判断是否为同一个人
//         * lastFace用于存放上一次提取的人脸特征
//         */
//        byte[] lastFace = null;
//        final byte[][] curFace = new byte[1][1];
//        long lastMostLikelyId = 0;
//
//        int count = 0;
//
//        setCompareSetAll();
//
//        openInfraredLight();
//
//        for (int i = 0; i < REGIST_TIMES && FaceDataHelper.getInstance().mShouldRun; i++) {
//            ZLog.i(String.format("regist count:%d,i:%d", count, i));
//
//            if (count >= TAKE_PHOTO_TIMES) {
//                /**
//                 * 特征提取次数超出限制，则退出循环，注册失败
//                 */
//                break;
//            }
//            count++;
//
//            boolean b = getFacePropAndCacheCameraPic(curFace);
//
//            if (!b) {
//                /**
//                 * 人脸特征提取失败
//                 */
//                break;
//            }
//
//            FaceRecogRecord recordTem = compareFaceToDb(curFace[0]);
//
//            if (recordTem.mostLikelyHood >= FaceDataHelper.getInstance().mThreshold) {
//
//                i = 0;
//                lastFace = null;
//
//                if (recordTem.secondLikelyHood >= FaceDataHelper.getInstance().mThreshold) {
//                    /**
//                     * 此流程说明
//                     * 人脸数据库中至少有两位用户相似度是匹配的
//                     * 此时需要重新识别
//                     * 连续两次对应的同一个人，才可以确定此人的身份
//                     */
//                    if (lastMostLikelyId == recordTem.mostLikelyId) {
//
//                    } else {
//                        lastMostLikelyId = recordTem.mostLikelyId;
//                        continue;
//                    }
//                }
//
//                /**
//                 * 注册用户已存在
//                 */
//                recordTem.setType(3);
//                recordTem.setFacePicPath(saveCurPic());
//                FaceUtil.saveFaceRecord(recordTem);
//                rst = recordTem;
//                break;
//            } else {
//                lastMostLikelyId = 0;
//            }
//
//            if (lastFace != null) {
//                float v = compareFace(lastFace, curFace[0]);
//                if (v >= FaceDataHelper.getInstance().mRegisterThreshold) {
//                    if (i >= (REGIST_TIMES - 1)) {
//                        /**
//                         * 多次拍照后，各特征值之间的相似度大于阈值，注册成功
//                         */
//                        recordTem.setType(0);
//                        recordTem.setMostLikelyId(id);
//                        recordTem.setMostLikelyHood(1.0f);
//                        recordTem.setFacePicPath(saveCurPic());
//                        FaceUtil.saveFaceRecord(recordTem);
//                        FaceUtil.saveFaceUser(recordTem, curFace[0], id);
//                        rst = recordTem;
//                        break;
//                    }
//                } else {
//                    /**
//                     *  前后两次获取的特征值不一致，则重新获取特征值
//                     *  获取特征值计数i设置为0
//                     */
//                    i = 0;
//                }
//            }
//            lastFace = curFace[0];
//        }
//        closeInfraredLightDelay();
//        ZLog.i("startRegist end id:" + id + ", rst:" + rst);
//        return rst;
//    }
//
//    /**
//     * 获取人脸特征
//     * 出现错误则重发一次
//     * 会缓存图片至
//     *
//     * @see FaceUtil#mBaos
//     */
//    public boolean getFacePropAndCacheCameraPic(final byte[][] curFace) {
//        if (mPhoneDebug) {
//            curFace[0] = generateRandomFace();
//            try {
//                FaceUtil.putImageStream(mCameraView);
//            } catch (ZLException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return true;
//        } else {
//            return Util.repeatDo(new Util.RepeatCallback() {
//                @Override
//                public boolean repeatWork(int index) throws Exception {
//                    byte[] rsp = slc.send(
//                            mFaceDev,
//                            0,
//                            parseSendPacket(mTakePhoto),
//                            timeout,
//                            mTakePhoto.cmdWrapper
//                    );
//
//                    FaceUtil.putImageStream(mCameraView);
//
//                    /**
//                     * 判断返回的人脸特征是否有效
//                     */
//                    if (rsp[1] != 1 || rsp.length < 10) {
//                        return false;
//                    } else {
//                        /**
//                         * 正常返回的数据中
//                         * 第一位是命令标识符
//                         * 第二位开始才是数据体
//                         * 人脸特征的数据体中前四位是特征是否有效的标识符
//                         * 后四位是特征长度
//                         * 之后才是人脸特征值
//                         */
//                        curFace[0] = Arrays.copyOfRange(rsp, 9, rsp.length);
//                        return true;
//                    }
//                }
//            }, 2, 100);
//        }
//    }
//
//
//    /**
//     * 会多次提取特征，最大提取次数为TAKE_PHOTO_TIMES，直到提取到对应特征为止
//     *
//     * @return
//     */
//    public synchronized FaceRecogRecord startRecog() {
//
//        FaceRecogRecord rst = null;
//
//        if (!checkSuanfa(true)) {
//            return null;
//        }
//
//        if (!checkCamera(mCameraView)) {
//            return rst;
//        }
//
//        final byte[][] curFace = new byte[1][1];
//
//        long lastMostLikelyId = 0;
//
//        setCompareSetAll();
//
//        openInfraredLight();
//
//        for (int i = 0; i < TAKE_PHOTO_TIMES && FaceDataHelper.getInstance().mShouldRun; i++) {
//            ZLog.i("recog i:" + i);
//            /**
//             * 获取人脸特征
//             * 出现错误则重发一次
//             */
//            boolean b = getFacePropAndCacheCameraPic(curFace);
//
//            if (!b) {
//                /**
//                 * 人脸特征提取失败
//                 * 对应
//                 * 无人脸情况或
//                 * 通讯或者硬件的异常
//                 */
//                break;
//            }
//
//            FaceRecogRecord recordTem = compareFaceToDb(curFace[0]);
//            rst = recordTem;
//
//            if (recordTem.mostLikelyHood >= FaceDataHelper.getInstance().mThreshold) {
//                if (recordTem.secondLikelyHood >= FaceDataHelper.getInstance().mThreshold) {
//                    /**
//                     * 此流程说明
//                     * 人脸数据库中至少有两位用户相似度是匹配的
//                     * 此时需要重新识别
//                     * 连续两次对应的同一个人，才可以说明识别成功
//                     */
//                    if (lastMostLikelyId == recordTem.mostLikelyId) {
//                        break;
//                    } else {
//                        lastMostLikelyId = recordTem.mostLikelyId;
//                    }
//                } else {
//                    break;
//                }
//            }
//        }
//        closeInfraredLightDelay();
//
//        if (rst != null) {
//            rst.setType(1);
//            rst.setFacePicPath(saveCurPic());
//            FaceUtil.saveFaceRecord(rst);
//        }
//
//        ZLog.i("startRecog end rst:" + rst);
//        return rst;
//    }
//
//    public int findFaceDev() {
//        int rst = -1;
//        SerialPortFinder serialPortFinder = new SerialPortFinder();
//        String[] sh = serialPortFinder.getAllDevicesPath();
//        if (sh == null || sh.length == 0) {
//            return rst;
//        }
//        ZLog.i("findFaceDev in ym:%s", Arrays.toString(sh));
//        boolean isFaceChanged = false;
//        boolean ifLightChanged = false;
//        /**
//         * 依次发送获取版本号指令，有正确返回则为对应的设备地址
//         */
//        for (int i = 0; i < sh.length && (!isFaceChanged || !ifLightChanged); i++) {
//            String path = sh[i];
//
//            if (!ifLightChanged) {
//                byte[] send = mCloseInfraredBytes;
//                SerialExecutor serialExecutor = new SerialExecutor(path, mLightBaud);
//                try {
//                    byte[] bytes = serialExecutor.sendData(send, mSmallTimeoutForYm);
//                    if (bytes != null && bytes.length > 4 &&
//                            Arrays.equals(Arrays.copyOfRange(bytes, 0, mLightHead.length),
//                                    mLightHead)) {
//                        mLightDev = path;
//                        ifLightChanged = true;
//                        ZLog.i("set light dev %s", mLightDev);
//                        rst += 1;
//                        continue;
//                    }
//                } catch (ZLException e) {
//                    ZLog.i(e);
//                    if (e.getErrorCode() != ZLErrorEnum.TIMEOUT_ERROR.getErrorCode()) {
//                        /**
//                         * 如果不是串口超时的错误
//                         * 则说明此串口不可用，
//                         * 也不用查验此path是否是模块的地址
//                         */
//                        continue;
//                    }
//                }
//            }
//
//            if (!isFaceChanged) {
//                byte[] send = parseSendPacket(mGetVersion);
//                SerialExecutor serialExecutor = new SerialExecutor(path, mFaceBaudrate);
//                try {
//                    byte[] bytes = serialExecutor.sendData(send, mSmallTimeoutForYm);
//                    if (bytes != null && bytes.length > 10 &&
//                            Arrays.equals(Arrays.copyOfRange(bytes, 0, mHead.length),
//                                    mHead)) {
//                        mFaceDev = path;
//                        isFaceChanged = true;
//                        ZLog.i("set face dev %s", mFaceDev);
//                        rst += 2;
//                        continue;
//                    }
//                } catch (ZLException e) {
//                    ZLog.i(e);
//                }
//            }
//        }
//        return rst;
//    }
//
//    public void destory() {
//        if (slc != null) {
//            slc.destory();
//        }
//    }
//
//    public synchronized int initSender() {
//        ZLog.i("initSender in ym");
//        destory();
//        int rst = -1;
//        if (FaceDataHelper.getInstance().mShouldAutoFindDev) {
//            rst = findFaceDev();
//        }
//        /**
//         * 此处有自定义拼接的返回数据
//         * send返回的数据为 返回指令号+数据内容，
//         * 通过sendReadSenseSendPacket提取数据内容
//         */
//        slc = new SerialLoopCommu(mFaceDev, mFaceBaudrate) {
//            @Override
//            protected void prepareSetCheck() {
//                addSetCheckInYm(mFileOpen);
//                addSetCheckInYm(mFileData);
//                addSetCheckInYm(mFileDone);
//                addSetCheckInYm(mFacePicture);
//                addSetCheckInYm(mGetVersion);
//                addSetCheckInYm(mFaceSwitch);
//                addSetCheckInYm(mTakePhoto);
//                addSetCheckInYm(mRotate);
//                addSetCheckInYm(mUvcMode);
//            }
//
//            public void addSetCheckInYm(final ReadSenseSendPacket readSenseSendPacket) {
//                mSetCheck.add(new CheckInterface() {
//                    @Override
//                    public boolean checkData(byte[] bytes, String s) {
//                        if (bytes.length > 1 && bytes[0] == readSenseSendPacket.replyType) {
//                            return true;
//                        }
//                        return false;
//                    }
//
//                    @Override
//                    public CmdWrapper[] findStringWait() {
//                        return new CmdWrapper[]{
//                                readSenseSendPacket.cmdWrapper
//                        };
//                    }
//                });
//            }
//
//            @Override
//            public byte[] send(String dev, int reserve, byte[] data, int timeout, CmdWrapper cmdWrapper) throws ZLException {
//                try {
//                    return super.send(dev, reserve, data, timeout, cmdWrapper);
//                } catch (ZLException e) {
//                    if (e.getErrorCode() == ZLErrorEnum.LOOP_ERROR.getErrorCode()) {
//                        /**
//                         * 串口地址可能出现重复挂载，地址变更的情况
//                         */
//                        ZLog.i("ym will reset serial sender:" +
//                                FaceDataHelper.getInstance().mShouldAutoFindDev);
//                        refreshDev();
//
//                        throw new ZLException(ZLErrorEnum.WAIT_O_ERROR);
//                    }
//                    throw e;
//                }
//            }
//
//            @Override
//            protected byte[] readStream() throws IOException, InterruptedException {
//                int next = serialPort.getInputStream().read();
//                ZLog.i("read stream next:" + next);
//                if ((byte) next == (byte) 0x52) {
//                    byte[] yuemian = new byte[8];
//                    /**
//                     * 读取readsense数据头
//                     */
//                    FaceUtil.readStream(yuemian, serialPort.getInputStream());
//                    if (Arrays.equals(yuemian,
//                            new byte[]{0x45, 0x41, 0x44, 0x53, 0x45, 0x4E, 0x53, 0x45})) {
//                        /**
//                         * 阅面标识符匹配
//                         */
//                        byte[] head = new byte[7];
//                        FaceUtil.readStream(head, serialPort.getInputStream());
//                        byte cmd = head[2];
//                        byte crc = head[1];
//                        ZLog.i("result cmd:" + ByteUtil.bytesToHexString(new byte[]{cmd}, ""));
//
//                        int bodysize = FaceUtil.byteArrayToInt(Arrays.copyOfRange(head, 3, 7));
//                        ZLog.i("result body size:" + bodysize);
//                        if (bodysize < 0 || bodysize > 3000) {
//                            ZLog.i("bodysize error");
//                            return null;
//                        }
//                        byte[] result = new byte[bodysize];
//
//                        /**
//                         * 读取数据内容
//                         */
//                        FaceUtil.readStream(result, serialPort.getInputStream());
//
//                        byte calCrc = CalUtil.crc8Arrays(result);
////                            ZLog.i("crc check:calCrc:" + calCrc + " crc:" + crc);
//                        if (calCrc != crc) {
//                            ZLog.i("check error");
//                            return mErrorBytes;
//                        }
//
//                        String resultBody = ByteUtil.bytesToHexStringLimit(
//                                result, " ", 50);
//                        ZLog.i("result body content:[" + resultBody + "]");
//                        byte[] rsp = new byte[result.length + 1];
//                        rsp[0] = cmd;
//                        System.arraycopy(result, 0, rsp, 1, result.length);
//                        return rsp;
//                    }
//                } else if (next == -1) {
//                    throw new IOException();
//                }
//                return null;
//            }
//        };
//
//        return rst;
//    }
//
//    public boolean closeFaceSwitch() {
//        ZLog.i("closeFaceSwitch");
//        mFaceSwitch.dataBody = new byte[]{0};
//        boolean rst = false;
//        try {
//            sendReadSenseSendPacket(mFaceSwitch, mTimeoutForYm);
//            rst = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (rst) {
//            boolean b1 = mFaceSwitch.response[0] == 0;
//            if (b1) {
//                mSuanfaEnd = true;
//            }
//            return b1;
//        } else {
//            return false;
//        }
//    }
//
//    public void sendGetFaceWithPhoto() throws Exception {
//        mFacePicture.dataBody = new byte[0];
//        sendReadSenseSendPacket(mFacePicture, mTimeoutForYm);
//        checkCommonRsp1(mFacePicture);
//    }
//
//    private void checkCommonRsp1(ReadSenseSendPacket rssp) throws ZLException {
//        if (rssp.response[0] != 1) {
//            throw new ZLException(ZLErrorEnum.RESPONSE_BYTES_ERROR);
//        }
//    }
//
//    public void sendCmdFileOpen(String s) throws Exception {
//        mFileOpen.dataBody = s.getBytes();
//        sendReadSenseSendPacket(mFileOpen, mTimeoutForYm);
//    }
//
//    public void sendCmdFileDate(String filePath) throws Exception {
//        FileInputStream fin = null;
//        try {
//            fin = new FileInputStream(filePath);
//
//            int available = fin.available();
//            ZLog.i(filePath + " size:" + available);
//            if (available > fileSizeLimit) {
//                throw new ZLException(ZLErrorEnum.ILLEGAL_ARGUMENT_ERROR);
//            }
//
//            byte[] buffer = new byte[1024 * 100];
//            int readLen = 0;
//            while ((readLen = fin.read(buffer, 0, buffer.length)) != -1) {
//                byte[] bytes = Arrays.copyOfRange(buffer, 0, readLen);
//                sendCmdFileDate(bytes);
//            }
//        } catch (Exception e) {
//            throw e;
//        } finally {
//            FaceUtil.closeClosable(fin);
//        }
//    }
//
//    public void sendCmdFileDate(byte[] bytes) throws Exception {
//        mFileData.dataBody = bytes;
//        sendReadSenseSendPacket(mFileData, mTimeoutForYm);
//        checkCommonRsp0(mFileData);
//    }
//
//    public void sendCmdFileDone(String filePath) throws Exception {
//        sendCmdFileDone(MD5.getFileMD5(new File(filePath)));
//    }
//
//    public void sendCmdFileDone(byte[] bytes) throws Exception {
//        mFileDone.dataBody = bytes;
//        sendReadSenseSendPacket(mFileDone, mTimeoutForYm);
//        checkCommonRsp0(mFileDone);
//    }
//
//    private void checkCommonRsp0(ReadSenseSendPacket rssp) throws ZLException {
//        if (rssp.response[0] != 0) {
//            throw new ZLException(ZLErrorEnum.RESPONSE_BYTES_ERROR);
//        }
//    }
//
//    /**
//     * 发送完成数据后，数据内容包裹在ReadSenseSendPacket对象的response中
//     *
//     * @param rssp
//     * @param timeout
//     * @throws Exception
//     */
//    private synchronized void sendReadSenseSendPacket(final ReadSenseSendPacket rssp, final int timeout) throws Exception {
//        ZLog.i("send rssp start:" + rssp + " timeout:" + timeout);
//        if (rssp.dataBody == null) {
//            throw new ZLException(ZLErrorEnum.ILLEGAL_ARGUMENT_ERROR);
//        }
//        Util.repeatDoThrow(new Util.RepeatCallback() {
//            @Override
//            public boolean repeatWork(int i) throws Exception {
//                rssp.response = null;
//                byte[] rsp = slc.send(
//                        mFaceDev,
//                        0,
//                        parseSendPacket(rssp),
//                        timeout,
//                        rssp.cmdWrapper
//                );
//                if (rsp != null && rsp.length >= 1) {
//                    rssp.response = Arrays.copyOfRange(rsp, 1, rsp.length);
//                    return true;
//                } else {
//                    throw new ZLException(ZLErrorEnum.RESPONSE_BYTES_ERROR);
//                }
//            }
//        }, 2, 100);
//    }
//
//    public void openInfraredLightDelay() {
//        ZLog.i("openInfraredLightDelay");
//        mhandler.removeMessages(openInfrared);
//        mhandler.removeMessages(closeInfrared);
//        mhandler.sendEmptyMessageDelayed(openInfrared, 200);
//    }
//
//    public void openInfraredLight() {
//        mhandler.removeMessages(closeInfrared);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ZLog.i("openInfraredLight");
//                    if (mGpioType) {
//                        JniUtils.openLight();
//                    } else {
//                        openLightSerial();
//                    }
//                } catch (Exception e1) {
//                    ZLog.e(e1);
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * recv a5 03 00 02 00 01 06 a3
//     * <p>
//     * send a5 03 00 02 00 01 06 a3
//     */
//    private void openLightSerial() {
//        SerialExecutor serialExecutor = new SerialExecutor(mLightDev, mLightBaud);
//        try {
//            serialExecutor.sendData(mOpenInfraredBytes, mTimeoutForYm);
//        } catch (ZLException e) {
//            e.printStackTrace();
//            ZLog.e(e);
//        }
//    }
//
//    /**
//     * send a5 03 00 02 00 00 06 a2
//     * recv A5 03 00 02 00 01 06 A3
//     */
//    private void closeLightSerial() {
//        SerialExecutor serialExecutor = new SerialExecutor(mLightDev, mLightBaud);
//        try {
//            serialExecutor.sendData(mCloseInfraredBytes, mTimeoutForYm);
//        } catch (ZLException e) {
//            e.printStackTrace();
//            ZLog.e(e);
//        }
//    }
//
//    public void closeInfraredLightDelay() {
//        ZLog.i("closeInfraredLightDelay");
//        mhandler.removeMessages(closeInfrared);
//        mhandler.sendEmptyMessageDelayed(closeInfrared, 2000);
//    }
//
//    public void closeInfraredLight() {
//        mhandler.removeMessages(openInfrared);
//        mhandler.removeMessages(closeInfrared);
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    ZLog.i("closeInfraredLight");
//                    if (mGpioType) {
//                        JniUtils.closeLight();
//                    } else {
//                        closeLightSerial();
//                    }
//                } catch (Exception e1) {
//                    ZLog.e(e1);
//                }
//            }
//        }).start();
//    }
//
//    /**
//     * @param suanfaStatusShouldBe true  打开阅面模组的摄像头采集算法
//     *                             false 关闭
//     * @return
//     */
//    private boolean checkSuanfa(boolean suanfaStatusShouldBe) {
//        if (!mPhoneDebug) {
//            if (canSwitchVideo) {
//                if (suanfaStatusShouldBe && mSuanfaEnd) {
//                    return openFaceSwitch();
//                } else if (!suanfaStatusShouldBe && !mSuanfaEnd) {
//                    return closeFaceSwitch();
//                }
//            }
//        }
//        return true;
//    }
//
//    public String getVersion() {
//        mGetVersion.dataBody = new byte[0];
//        try {
//            sendReadSenseSendPacket(mGetVersion, mTimeoutForYm);
//            String kernal = new String(Arrays.copyOfRange(mGetVersion.response, 0, 10));
//            String rootfs = new String(Arrays.copyOfRange(mGetVersion.response, 10, 20));
//            String algo = new String(Arrays.copyOfRange(mGetVersion.response, 20, 30));
////            String version = "kernal:" + kernal + " rootfs:" + rootfs + " algo:" + algo;
//            String version = kernal.substring(kernal.lastIndexOf(".") + 1) + "." +
//                    rootfs.substring(rootfs.lastIndexOf(".") + 1) + "." +
//                    algo.substring(algo.lastIndexOf(".") + 1);
//            ZLog.i(version);
//            return version;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "";
//        }
//    }
//
//    byte[] parseSendPacket(ReadSenseSendPacket rssp) {
//        byte[] result = new byte[mHead.length + 7 + rssp.dataBody.length];
//        System.arraycopy(mHead, 0,
//                result, 0, mHead.length);
//        result[mHead.length + 2] = rssp.cmdType;
//        byte[] bytes = FaceUtil.intToByte(rssp.dataBody.length);
//        System.arraycopy(
//                bytes, 0,
//                result, mHead.length + 3, 4
//        );
//        System.arraycopy(
//                rssp.dataBody, 0,
//                result, mHead.length + 7, rssp.dataBody.length
//        );
//        return result;
//    }
//
//    private static final YmHelper ourInstance = new YmHelper();
//
//    static public YmHelper getInstance() {
//        return ourInstance;
//    }
//
//    private YmHelper() {
//        try {
//            mFaceDev = ConfigManager.getInstance().getIni().getValue(
//                    ConfigString.getInstance().app,
//                    "face_addr");
//            mLightDev = ConfigManager.getInstance().getIni().getValue(
//                    ConfigString.getInstance().app,
//                    "face_light_addr");
//            /**
//             *  infrared_type
//             */
//            mGpioType = "0".equals(
//                    ConfigManager.getInstance().getIni().getValue(
//                            ConfigString.getInstance().app,
//                            "infrared_type")
//            );
//
//            initSender();
//
//        } catch (Exception e) {
//            ZLog.e(e);
//        }
//    }
//
//    public void refreshDev() {
//        ThreadUtil.getThreadPool().execute(new Runnable() {
//            @Override
//            public void run() {
//                initSender();
//            }
//        });
//    }
//
//    /**
//     * @param jpgPath
//     * @return -1 提取特征有误
//     * @throws Exception
//     */
//    public float compareFacePicAndCameraView(String jpgPath) throws Exception {
//        ZLog.i("compareFacePicAndCameraView:" + jpgPath);
//        float rst = -1;
//        /**
//         * 提取图片的特征数据时
//         * 视频流的算法应先关闭
//         */
//        if (!checkSuanfa(false)) {
//            return rst;
//        }
//
//        byte[] picFaceProp = getFacePropFromFile(jpgPath);
//
//        if (!checkSuanfa(true)) {
//            return rst;
//        }
//
//        if (!checkCamera(mCameraView)) {
//            return rst;
//        }
//
//        final byte[][] curFace = new byte[1][1];
//
//        openInfraredLight();
//
//        for (int i = 0; i < COMPARE_FACE_AND_CAMERA_TIMES && FaceDataHelper.getInstance().mShouldRun; i++) {
//            ZLog.i("compare i:" + i);
//
//            /**
//             * 获取人脸特征
//             * 出现错误则重发一次
//             */
//            boolean b = getFacePropAndCacheCameraPic(curFace);
//
//            if (!b) {
//                /**
//                 * 人脸特征提取失败
//                 * 对应
//                 * 无人脸情况或
//                 * 通讯或者硬件的异常
//                 */
//                break;
//            }
//
//            float v = compareFace(curFace[0], picFaceProp);
//            if (v > rst) {
//                rst = v;
//            }
//
//            if (v >= FaceDataHelper.getInstance().mPicThreshold) {
//                /**
//                 * 此流程说明相机采集的人脸特指与照片是同一个人，退出循环
//                 */
//                break;
//            }
//        }
//
//        closeInfraredLightDelay();
//
//        return rst;
//    }
//
//    public byte[] generateRandomFace() {
//        int count = 512;
//        byte[] rst = new byte[count];
//        for (int i = 0; i + 3 < count; i += 4) {
//            float random = ((float) Math.random()) / 9;
//            int tem = Float.floatToIntBits(random);
//            byte[] bytes = FaceUtil.intToByteArray(tem);
//            for (int j = 0; j < 4; j++) {
//                rst[i + j] = bytes[j];
//            }
//        }
//        return rst;
//    }
//
//    private byte[] getFacePropFromFile(String jpgPath) throws Exception {
//        if (!mPhoneDebug) {
//            sendCmdFileOpen(ymPhotoPath);
//            sendCmdFileDate(jpgPath);
//            sendCmdFileDone(jpgPath);
//            sendGetFaceWithPhoto();
//            return Arrays.copyOfRange(
//                    mFacePicture.response, 8, mFacePicture.response.length);
//        } else {
//            return generateRandomFace();
//        }
//    }
//
//    public float compareTwoPic(String facePicPathA, String facePicPathB) throws Exception {
//        ZLog.i("compareTwoPic:%s,%s", facePicPathA, facePicPathB);
//        float rst = -1;
//        /**
//         * 提取图片的特征数据
//         */
//        if (!checkSuanfa(false)) {
//            return rst;
//        }
//
//        byte[] facePropFromFileA = getFacePropFromFile(facePicPathA);
//        byte[] facePropFromFileB = getFacePropFromFile(facePicPathB);
//
//        return compareFace(facePropFromFileA, facePropFromFileB);
//    }
//}
