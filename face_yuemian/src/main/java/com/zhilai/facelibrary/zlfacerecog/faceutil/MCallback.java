package com.zhilai.facelibrary.zlfacerecog.faceutil;

public interface MCallback {
    String onDo() throws Exception;

    void onResult(String result) throws Exception;
}
