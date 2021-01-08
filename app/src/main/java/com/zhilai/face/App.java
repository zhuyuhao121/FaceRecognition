package com.zhilai.face;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

public class App extends Application {

    public static Context localContext;

    @Override
    public void onCreate() {
        super.onCreate();
        localContext = getApplicationContext();
    }
}
