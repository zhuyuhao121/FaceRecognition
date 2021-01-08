package com.zhilai.driver.config;

import android.content.Context;

import com.zhilai.driver.log.ZLog;
import com.zhilai.facelibrary.R;
import com.zhilai.facelibrary.zlfacerecog.MApp;
import com.zhilai.facelibrary.zlfacerecog.faceutil.FileUtil;

import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private IniReader iniReader;
    private SPHelper spHelper;

    private static ConfigManager configManager;
    private Context mContext;

    private ConfigManager() {
    }

    public void init(Context context) {
        mContext = context;

        File dir = new File(MApp.CONFIG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dirPic = new File(MApp.CONFIG_PATH + File.separator + MApp.pic);
        if (!dirPic.exists()) {
            dirPic.mkdirs();
        }

//        File configFile = new File(dir, MApp.CONFIG_NAME);
//        if (!configFile.exists()) {
//            ZLog.i("没有检测到配置文件,开始创建默认的配置");
//            FileUtil.copyRaw(mContext, R.raw.config, configFile.getAbsolutePath());
//        }
//        ZLog.i("读取配置文件：\n" + getIni().getContent());
    }

    public static ConfigManager getInstance() {
        if (configManager == null) {
            synchronized (ConfigManager.class) {
                if (configManager == null) {
                    configManager = new ConfigManager();
                }
            }
        }

        return configManager;
    }

    public synchronized IniReader getIni() {

        if (iniReader == null) {
            try {
                iniReader = new IniReader(MApp.CONFIG_PATH.concat(File.separator).concat(MApp.CONFIG_NAME));
            } catch (IOException e) {
                ZLog.e(e);
            }
        }

        return iniReader;
    }

    public synchronized SPHelper getSharedPreferences() {
        if (spHelper == null) {
            spHelper = new SPHelper(mContext.getApplicationContext(), "fc");
        }
        return spHelper;
    }


}
