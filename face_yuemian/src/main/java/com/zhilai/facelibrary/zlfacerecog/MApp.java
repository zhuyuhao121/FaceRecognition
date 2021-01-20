package com.zhilai.facelibrary.zlfacerecog;

import android.content.Context;
import android.os.Environment;

import com.zhilai.driver.config.ConfigManager;
import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.zlfacerecog.faceutil.GreenDaoContext;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.DaoMaster;
import com.zhilai.facelibrary.zlfacerecog.faceutil.facerecog.DaoSession;

import org.greenrobot.greendao.database.Database;

import java.io.File;

public class MApp {
    public static String CONFIG_PATH = Environment.getExternalStorageDirectory()
            .getAbsolutePath().concat(File.separator).concat
                    ("ZhilaiTerminalDeliveryLocker/facelib");
    public static final String FACEPICDIR = "facepic";
    public static DaoSession daoSession;
    public static int CAMERA_IMAGE_WIDTH = 240;
    public static int CAMERA_IMAGE_HEIGHT = 320;
    private static boolean ENCRYPTED;
    public static final String pic = "pic";
    public static final String CONFIG_NAME = "config.ini";

    public static void createDir(String dir) {
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }

    private static void initFacePicDir() {
        createDir(MApp.CONFIG_PATH + File.separator + FACEPICDIR);
    }

    public static synchronized void init(Context context) {
        CONFIG_PATH = Environment.getExternalStorageDirectory()
                .getAbsolutePath().concat(File.separator)
                .concat(context.getString(context.getApplicationInfo().labelRes));
        if (daoSession == null) {
            ZLog.i("face module init");
            initGreenDao(context);
            initFacePicDir();
            ConfigManager.getInstance().init(context);
        }
    }

    private static void initGreenDao(Context context) {
//        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(new GreenDaoContext
//                (context), ENCRYPTED ? "zhilai-face-db-encrypted" : "zhilai-face-db.db");
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(context, ENCRYPTED ?
                "zhilai-face-db-encrypted" : "zhilai-face-db.db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper
                .getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }
}
