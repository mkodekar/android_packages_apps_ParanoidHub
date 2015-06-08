package com.paranoid.paranoidhub.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceHelper {
    public static final String PROPERTY_CHECK_TIME = "checktime";
    public static final String PROPERTY_DRAWER_IMAGE = "drawerImage";

    public static final String DOWNLOAD_ROM_ID = "download_rom_id";
    public static final String DOWNLOAD_ROM_MD5 = "download_rom_md5";
    public static final String DOWNLOAD_ROM_FILENAME = "download_rom_filaname";

    public static final int DEFAULT_CHECK_TIME = 18000000; // five hours

    private static SharedPreferences settings;

    public PreferenceHelper(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void setDownloadRomId(Long id, String md5, String fileName) {
        if (id == null) {
            removePreference(DOWNLOAD_ROM_ID);
            removePreference(DOWNLOAD_ROM_MD5);
            removePreference(DOWNLOAD_ROM_FILENAME);
        } else {
            setPreference(DOWNLOAD_ROM_ID, String.valueOf(id));
            setPreference(DOWNLOAD_ROM_MD5, md5);
            setPreference(DOWNLOAD_ROM_FILENAME, fileName);
        }
    }

    public long getDownloadRomId() {
        return Long.parseLong(settings.getString(DOWNLOAD_ROM_ID, "-1"));
    }

    public String getDownloadRomMd5() {
        return settings.getString(DOWNLOAD_ROM_MD5, null);
    }

    public String getDownloadRomName() {
        return settings.getString(DOWNLOAD_ROM_FILENAME, null);
    }

    public static String getPreference(String key, String defaultValue) {
        return settings.getString(key, defaultValue);
    }

    public static int getPreference(String key, int defaultValue) {
        return settings.getInt(key, defaultValue);
    }

    public static void setPreference(String preference, String value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(preference, value);
        editor.apply();
    }

    public static void setPreference(String preference, int value) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(preference, value);
        editor.apply();
    }

    public static void removePreference(String preference) {
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(preference);
        editor.apply();
    }

    public static class FirstRunPreference {

        public static final String FIRST_TIME_PREFERENCES_KEY = "FirstRun";
        public static final String FIRST_TIME_COUNTDOWN_KEY = "FirstCountdownKey";
        private static final int INT_ERROR = -1;
        private final SharedPreferences firstrun;
        private Context mContext;

        public FirstRunPreference(Context mContext) {
            this.mContext = mContext;

            firstrun = this.mContext.getSharedPreferences(
                    FIRST_TIME_PREFERENCES_KEY, Context.MODE_PRIVATE);

        }

        /**
         * @param key
         * @return the how many times the code will be executed yet
         */
        public int getCountDown(String key) {
            return firstrun.getInt(key + FIRST_TIME_COUNTDOWN_KEY,
                    INT_ERROR);
        }

        /**
         * @param key - Countdown default 0
         * @return true the first time
         */
        public boolean runTheFirstTime(String key) {
            return runTheFirstNTimes(key, 0);
        }

        /**
         * @param key
         * @param countdown
         * @return true for the first <countdown> times
         */
        public boolean runTheFirstNTimes(String key, int countdown) {
            int countDownPref = getCountDown(key);

            switch (countDownPref) {
                case 0:
                    setFalse(key);
                    break;

                case INT_ERROR:
                    if (countdown != 0) {
                        setCountDown(key, countdown - 1);
                    } else {
                        setCountDown(key, 0);
                    }
                    break;

                default:
                    setCountDown(key, countDownPref - 1);
                    break;
            }

            return firstrun.getBoolean(key, true);
        }

        private void setFalse(String key) {
            SharedPreferences.Editor editor = firstrun.edit();
            editor.putBoolean(key, false);
            editor.apply();
        }

        private void setCountDown(String key, int countDown) {
            SharedPreferences.Editor editor = firstrun.edit();
            editor.putInt(key + FIRST_TIME_COUNTDOWN_KEY, countDown);
            editor.apply();
        }
    }
}
