package com.paranoid.paranoidhub.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.activities.HubActivity;
import com.paranoid.paranoidhub.helpers.PreferenceHelper;
import com.paranoid.paranoidhub.receivers.NotificationAlarm;
import com.paranoid.paranoidhub.updater.Updater;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OTAUtils {

    public static final String FILES_INFO = "com.paranoid.paranoidhub.Utils.FILES_INFO";
    public static final String CHECK_DOWNLOADS_FINISHED = "com.paranoid.paranoidhub.Utils.CHECK_DOWNLOADS_FINISHED";
    public static final String CHECK_DOWNLOADS_ID = "com.paranoid.paranoidhub.Utils.CHECK_DOWNLOADS_ID";
    public static final String MOD_VERSION = "ro.modversion";
    public static final String RO_PA_VERSION = "ro.pa.version";
    public static final int ROM_ALARM_ID = 122303221;

    public static final int TWRP = 1;
    public static final int CWM_BASED = 2;

    public static Updater.PackageInfo[] sPackageInfosRom = new Updater.PackageInfo[0];

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

    /**
     * Method borrowed from OpenDelta. Using reflection voodoo instead calling
     * the hidden class directly, to dev/test outside of AOSP tree.
     *
     * @author Jorrit "Chainfire" Jongma
     * @author The OmniROM Project
     */
    public static boolean setPermissions(String path, int mode, int uid, int gid) {
        try {
            Class<?> FileUtils = Utils.class.getClassLoader().loadClass("android.os.FileUtils");
            Method setPermissions = FileUtils.getDeclaredMethod("setPermissions", String.class,
                    int.class,
                    int.class,
                    int.class);
            return ((Integer) setPermissions.invoke(
                    null,
                    path,
                    Integer.valueOf(mode),
                    Integer.valueOf(uid),
                    Integer.valueOf(gid)) == 0);
        } catch (Exception e) {
            // A lot of voodoo could go wrong here, return failure instead of
            // crash
            e.printStackTrace();
        }
        return false;
    }

    public static String getReadableVersion(String version) {
        try {
            String number = version.substring(version.indexOf("-") + 1, version.lastIndexOf("-"));
            String date = version.substring(version.lastIndexOf("-") + 1,
                    version.endsWith(".zip") ? version.lastIndexOf(".") : version.length());

            SimpleDateFormat curFormater = new SimpleDateFormat("yyyyMMdd");
            Date dateObj = null;
            try {
                dateObj = curFormater.parse(date);
            } catch (ParseException e) {
                // ignore
            }
            SimpleDateFormat postFormater = new SimpleDateFormat("MMMM dd, yyyy");

            if (dateObj == null) {
                return number;
            }
            String newDateStr = postFormater.format(dateObj);

            StringBuilder b = new StringBuilder(newDateStr);
            int i = 0;
            do {
                b.replace(i, i + 1, b.substring(i, i + 1).toUpperCase());
                i = b.indexOf(" ", i) + 1;
            } while (i > 0 && i < b.length());
            return number + " - " + b.toString();
        } catch (Exception ex) {
            // unknown version format
            return version;
        }
    }

    public static String getDateAndTime() {
        return new SimpleDateFormat("yyyy-MM-dd.HH.mm.ss").format(new Date(System
                .currentTimeMillis()));
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void setAlarm(Context context, boolean trigger) {
        PreferenceHelper preferenceHelper = new PreferenceHelper(context);
        setAlarm(context, preferenceHelper.getPreference(PreferenceHelper.PROPERTY_CHECK_TIME,
                PreferenceHelper.DEFAULT_CHECK_TIME), trigger);
    }

    public static void setAlarm(Context context, long time, boolean trigger) {
        Intent i = new Intent(context, NotificationAlarm.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pi = PendingIntent.getBroadcast(context,
                ROM_ALARM_ID, i,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        if (time > 0) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, trigger ? 0 : time, time, pi);
        }
    }

    public static boolean alarmExists(Context context) {
        return (PendingIntent.getBroadcast(context, ROM_ALARM_ID,
                new Intent(context, NotificationAlarm.class),
                PendingIntent.FLAG_NO_CREATE) != null);
    }

    public static void showToastOnUiThread(final Context context, final int resourceId) {
        ((Activity) context).runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(context, resourceId, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showToastOnUiThread(final Context context, final String string) {
        ((Activity) context).runOnUiThread(new Runnable() {

            public void run() {
                Toast.makeText(context, string, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showNotification(Context context, Updater.PackageInfo[] infosRom) {
        Resources resources = context.getResources();

        if (infosRom != null) {
            sPackageInfosRom = infosRom;
        } else {
            infosRom = sPackageInfosRom;
        }

        Intent intent = new Intent(context, HubActivity.class);
        NotificationInfo fileInfo = new NotificationInfo();
        fileInfo.mNotificationId = Updater.NOTIFICATION_ID;
        fileInfo.mPackageInfosRom = infosRom;
        intent.putExtra(FILES_INFO, fileInfo);
        PendingIntent pIntent = PendingIntent.getActivity(context, Updater.NOTIFICATION_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setContentTitle(resources.getString(R.string.new_system_update))
                .setSmallIcon(R.drawable.ic_launcher_mono)
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_launcher))
                .setContentIntent(pIntent);

        String filename = infosRom[0].getFilename();
        String contextText = resources.getString(R.string.new_package_name, new Object[]{
                filename
        });
        builder.setContentText(contextText);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.setBigContentTitle(context.getResources().getString(R.string.new_system_update));
        if (infosRom.length > 1) {
            inboxStyle.addLine(contextText);
        }
        for (int i = 0; i < infosRom.length; i++) {
            inboxStyle.addLine(infosRom[i].getFilename());
        }
        inboxStyle.setSummaryText(resources.getString(R.string.app_name));
        builder.setStyle(inboxStyle);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(Updater.NOTIFICATION_ID, builder.build());
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    public static String exec(String command) {
        try {
            Process p = Runtime.getRuntime().exec(command);
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("sync\n");
            os.writeBytes("exit\n");
            os.flush();
            p.waitFor();
            return getStreamLines(p.getInputStream());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static String getStreamLines(final InputStream is) {
        String out = null;
        StringBuffer buffer = null;
        final DataInputStream dis = new DataInputStream(is);

        try {
            if (dis.available() > 0) {
                buffer = new StringBuffer(dis.readLine());
                while (dis.available() > 0) {
                    buffer.append("\n").append(dis.readLine());
                }
            }
            dis.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (buffer != null) {
            out = buffer.toString();
        }
        return out;
    }

    public static class NotificationInfo implements Serializable {
        public int mNotificationId;
        public Updater.PackageInfo[] mPackageInfosRom;
    }
}