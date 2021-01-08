package com.zhilai.facelibrary.zlfacerecog.faceutil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtil {

    private static ExecutorService pool = Executors.newCachedThreadPool();

    public static ExecutorService getThreadPool(){
        return pool;
    }
}
