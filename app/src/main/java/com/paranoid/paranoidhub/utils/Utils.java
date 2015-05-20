package com.paranoid.paranoidhub.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Utils {
    public static final String PROPERTY_DEVICE = "ro.pa.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";
    public static View myDialogView = null;

    public static void createToast(String string) {
        Toast toast = Toast.makeText(App.getContext(), string, Toast.LENGTH_LONG);
        toast.show();
    }

    public static void createInputDialog(String title, DialogInterface.OnClickListener positiveListener,
                                         DialogInterface.OnClickListener negativeListener) {
        LayoutInflater factory = LayoutInflater.from(App.getContext());
        myDialogView = factory.inflate(R.layout.feedback_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(App.getContext(),
                android.R.style.Theme_DeviceDefault_Light_Dialog_MinWidth)
                .setTitle(title)
                .setView(myDialogView)
                .setPositiveButton(App.getContext().getString(android.R.string.yes), positiveListener)
                .setNegativeButton(App.getContext().getString(android.R.string.no), negativeListener)
                .create();
        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialog.show();
    }

    public static void doVibrate(int time) {
        Vibrator v = (Vibrator) App.getContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(time);
    }

    public static String getEmail() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(App.getContext()).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                return account.name;
            }
        }
        return null;
    }

    private static String getDevice() {
        String device = OTAUtils.getProp(PROPERTY_DEVICE);
        if (device == null || device.isEmpty()) {
            device = OTAUtils.getProp(PROPERTY_DEVICE_EXT);
        }
        return device == null ? "" : device.toLowerCase();
    }

    public static String getDeviceInfo() {
        return "Android Version: " + Build.VERSION.RELEASE +
                "\nModel: " + Build.MODEL +
                "\nCodename: " + Build.PRODUCT +
                "\nBuild: " + getVersionString();
    }

    public static String getVersionString() {
        return "pa_" + getDevice() + "-" + OTAUtils.getProp(OTAUtils.MOD_VERSION);
    }

    public static boolean isBuildHigherThanVersion(int version) {
        return Build.VERSION.SDK_INT >= version;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static boolean isScreenOn() {
        PowerManager powerManager = (PowerManager) App.getContext().getSystemService(Context.POWER_SERVICE);
        if (isBuildHigherThanVersion(Build.VERSION_CODES.KITKAT_WATCH)) {
            return powerManager.isInteractive();
        } else {
            return powerManager.isScreenOn();
        }
    }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) App.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String getReadableDate(String fileDate) {
        try {
            Date currentDate = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            Date parsedDate = format.parse(fileDate);
            long diff = TimeUnit.MILLISECONDS.toDays(currentDate.getTime() - parsedDate.getTime());
            return diff > 1 ? diff + " days ago" : diff + " day ago";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
