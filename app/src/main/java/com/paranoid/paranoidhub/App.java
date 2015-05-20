package com.paranoid.paranoidhub;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.services.ShakeService;

public class App extends Application {
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
        Instabug.initialize(this, "f30ca8f22134b9cfcd6f1c2c1621be45")
                .setShowIntroDialog(false)
                .setEnableOverflowMenuItem(false)
                .setShowTutorial(false);

        if (!Utils.isServiceRunning(ShakeService.class)) {
            Intent serviceIntent = new Intent(getContext(), ShakeService.class);
            getContext().startService(serviceIntent);
        }
    }
}