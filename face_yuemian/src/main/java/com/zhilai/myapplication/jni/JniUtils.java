package com.zhilai.myapplication.jni;

public class JniUtils {
    static {
        System.loadLibrary("jniutil");
    }
    public static native String openLight();
    public static native String closeLight();
}
