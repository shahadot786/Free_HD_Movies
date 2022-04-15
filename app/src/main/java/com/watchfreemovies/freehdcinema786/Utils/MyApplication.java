package com.watchfreemovies.freehdcinema786.Utils;

import android.app.Application;

import com.onesignal.OneSignal;

public class MyApplication extends Application {
    private static final String ONESIGNAL_APP_ID = "007f812e-81b6-4f24-aa89-7248f3f22f46";

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId(ONESIGNAL_APP_ID);
    }
}
