package com.paranoid.paranoidhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.services.ShakeService;
import com.paranoid.paranoidhub.utils.OTAUtils;
import com.paranoid.paranoidhub.utils.Utils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Utils.isServiceRunning(ShakeService.class)) {
            Intent serviceIntent = new Intent(App.getContext(), ShakeService.class);
            App.getContext().startService(serviceIntent);
        }
        OTAUtils.setAlarm(context, true);
    }
}
