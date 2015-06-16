package com.paranoid.paranoidhub.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.paranoid.paranoidhub.App;

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
        Toast.makeText(App.getContext(), string, Toast.LENGTH_LONG)
                .show();
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

    //check if device is connected to mobile network
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // check if device is connected to mobile network
    public static boolean isOnWifi(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mNetworkInfo != null && mNetworkInfo.isConnected();
    }
}
