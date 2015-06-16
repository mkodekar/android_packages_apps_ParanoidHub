package com.paranoid.paranoidhub;

import android.app.Application;
import android.content.Context;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.utils.Constants;

public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
        Instabug.initialize(this, Constants.INSTABUG_TOKEN)
                .setInvocationEvent(Instabug.IBGInvocationEvent.IBGInvocationEventNone)
                .setShowIntroDialog(false)
                .setEnableOverflowMenuItem(false);
    }
}