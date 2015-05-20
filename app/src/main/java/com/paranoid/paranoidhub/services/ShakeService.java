package com.paranoid.paranoidhub.services;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.widget.EditText;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.Utils;
import com.paranoid.paranoidhub.listeners.ShakeListener;

// Service that binds the ShakeListener and receives the Shakes
public class ShakeService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ShakeListener mShakeDetector = new ShakeListener();
        mShakeDetector.setOnShakeListener(new ShakeListener.OnShakeListener() {

            @Override
            public void onShake(int count) {
                // Avoid non-desired shaking! (2 or more shakes and screenOn)
                if (count > 1 && Utils.isScreenOn()) {
                    Utils.doVibrate(150);
                    // TODO: Implement global screenshot and send it as bitmap
                    Utils.createInputDialog(App.getContext().getString(R.string.feedback_title), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Instabug instabug = Instabug.getInstance();
                            EditText inputText = (EditText) Utils.myDialogView.findViewById(R.id.dialog_input);
                            String text = inputText.getText().toString() + " " + Utils.getDeviceInfo();
                            instabug.reportBug(null, text, Utils.getEmail(),
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
                        }
                    }, null);
                }
            }
        });
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}