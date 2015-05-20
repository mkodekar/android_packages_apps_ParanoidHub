package com.paranoid.paranoidhub;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class Utils {
    public static View myDialogView = null;
    public static final String MOD_VERSION = "ro.modversion";
    public static final String PROPERTY_DEVICE = "ro.pa.device";
    public static final String PROPERTY_DEVICE_EXT = "ro.product.device";

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
        String device = getProp(PROPERTY_DEVICE);
        if (device == null || device.isEmpty()) {
            device = getProp(PROPERTY_DEVICE_EXT);
        }
        return device == null ? "" : device.toLowerCase();
    }

    public static String getDeviceInfo() {
        return "VERSION.RELEASE : " + Build.VERSION.RELEASE +
                "\nMODEL : " + Build.MODEL +
                "\nPRODUCT : " + Build.PRODUCT +
                "\nBUILD : " + getVersionString();
    }

    public static String getProp(String prop) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + prop);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            StringBuilder log = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                log.append(line);
            }
            return log.toString();
        } catch (IOException e) {
            // Runtime error
        }
        return null;
    }

    public static String getVersionString() {
        return getDevice() + "-" + getProp(MOD_VERSION);
    }

    public static boolean isBuildHigherThanVersion(int version) {
        if (Build.VERSION.SDK_INT >= version) {
            return true;
        } else {
            return false;
        }
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
}
