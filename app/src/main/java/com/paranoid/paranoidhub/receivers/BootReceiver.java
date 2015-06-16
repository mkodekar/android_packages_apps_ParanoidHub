package com.paranoid.paranoidhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.paranoid.paranoidhub.utils.OTAUtils;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        OTAUtils.setAlarm(context, true);
    }
}
