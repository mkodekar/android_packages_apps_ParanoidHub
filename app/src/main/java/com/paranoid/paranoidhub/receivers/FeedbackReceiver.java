package com.paranoid.paranoidhub.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.updater.RomUpdater;
import com.paranoid.paranoidhub.utils.OTAUtils;
import com.paranoid.paranoidhub.utils.Utils;

public class FeedbackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case "com.paranoidhub.FEEDBACK":
                Instabug instabug = Instabug.getInstance();
                instabug.reportBug(null, Utils.getDeviceInfo(), Utils.getEmail(),
                        new Instabug.OnSendBugReportListener() {
                            @Override
                            public void onBugReportSent(boolean success, String result) {
                                if (success) {
                                    Utils.createToast(App.getContext().getString(R.string.feedback_success));
                                } else {
                                    Utils.createToast(App.getContext().getString(R.string.feedback_failure));
                                }
                            }
                });
                break;
        }
    }
}