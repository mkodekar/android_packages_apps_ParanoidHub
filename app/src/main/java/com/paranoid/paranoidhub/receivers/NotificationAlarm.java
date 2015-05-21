package com.paranoid.paranoidhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.paranoid.paranoidhub.updater.RomUpdater;
import com.paranoid.paranoidhub.utils.OTAUtils;

public class NotificationAlarm extends BroadcastReceiver {

    private RomUpdater mRomUpdater;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (mRomUpdater == null) {
            mRomUpdater = new RomUpdater(context, true);
        }
        if (OTAUtils.isNetworkAvailable(context)) {
            mRomUpdater.check();
        }
    }
}