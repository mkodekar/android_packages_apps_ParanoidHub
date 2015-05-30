package com.paranoid.paranoidhub;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.services.ShakeService;
import com.paranoid.paranoidhub.utils.Constants;
import com.paranoid.paranoidhub.utils.Utils;

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
                .setShowIntroDialog(false)
                .setEnableOverflowMenuItem(false);

        if (!Utils.isServiceRunning(ShakeService.class)) {
            Intent serviceIntent = new Intent(getContext(), ShakeService.class);
            getContext().startService(serviceIntent);
        }
    }
}