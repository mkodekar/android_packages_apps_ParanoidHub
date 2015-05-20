package com.paranoid.paranoidhub.activities;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.updater.RomUpdater;
import com.paranoid.paranoidhub.updater.Updater.PackageInfo;
import com.paranoid.paranoidhub.updater.Updater.UpdaterListener;

public class SystemActivity extends Activity implements UpdaterListener {

    private RomUpdater mRomUpdater;

    private PackageInfo mRom;

    private TextView mTitle;
    private TextView mMessage;
    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_system);

        mTitle = (TextView) findViewById(R.id.title);
        mMessage = (TextView) findViewById(R.id.message);
        mButton = (Button) findViewById(R.id.button);
        mButton.setVisibility(View.GONE);

        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                mRomUpdater.check(true);
            }

        });

        mRom = null;

        mRomUpdater = new RomUpdater(this, true);
        mRomUpdater.addUpdaterListener(this);

        mRomUpdater.check(true);
    }

    @Override
    public void startChecking() {
        setMessages(null);
    }

    @Override
    public void versionFound(PackageInfo[] info) {
        setMessages(info);
    }

    @Override
    public void checkError(String cause) {
    }

    private void setMessages(PackageInfo[] info) {
        if (info != null && info.length > 0) {
            mRom = info.length > 0 ? info[0] : null;
        }
        Resources res = getResources();
        boolean checking = mRomUpdater.isScanning();
        if (checking) {
            mTitle.setText(R.string.all_up_to_date);
            mMessage.setText(R.string.rom_scanning);
            mButton.setVisibility(View.GONE);
        } else {
            mButton.setVisibility(View.VISIBLE);
            if (mRom != null) {
                mTitle.setText(R.string.rom_new_version);
                mMessage.setText(res.getString(R.string.system_update_found,
                        new Object[]{
                                mRom.getFilename()
                        }));
            } else {
                mTitle.setText(R.string.all_up_to_date);
                mMessage.setText(R.string.no_updates);
            }
        }
    }
}