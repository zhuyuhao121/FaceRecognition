package com.zhilai.facelibrary.zlfacerecog.faceutil;

import com.zhilai.driver.log.ZLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {
    public static byte[] getFileMD5(File file) {
        if (!file.isFile()) {
            return null;
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
            return digest.digest();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static byte[] getbytesMD5(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        MessageDigest digest = null;
        ByteArrayInputStream in = null;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new ByteArrayInputStream(bytes);
            int len;
            byte buffer[] = new byte[1024];
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            ZLog.e(e);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    ZLog.e(e);
                }
            }
        }
    }
}
