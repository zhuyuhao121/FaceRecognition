package com.zhilai.facelibrary.zlfacerecog.faceutil;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class FileUtil {

    public static boolean copyRaw(Context context, int rawId, String destPath) {
        InputStream is = context.getResources().openRawResource(rawId);
        byte[] buffer = new byte[128];
        try {
            FileOutputStream fos = new FileOutputStream(destPath);
            int size;

            while ((size = is.read(buffer)) != -1) {
                fos.write(buffer, 0, size);
            }

            is.close();
            fos.close();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean copyRaw(Context context, int rawId, File destPath) {
        return copyRaw(context, rawId, destPath.getAbsolutePath());
    }

    public static void createDir(String dir) {
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
        }
    }
}
