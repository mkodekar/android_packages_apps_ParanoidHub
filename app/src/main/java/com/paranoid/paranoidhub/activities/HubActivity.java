package com.paranoid.paranoidhub.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.cards.DownloadCard;
import com.paranoid.paranoidhub.cards.InstallCard;
import com.paranoid.paranoidhub.cards.SettingsCard;
import com.paranoid.paranoidhub.cards.SystemCard;
import com.paranoid.paranoidhub.cards.UpdatesCard;
import com.paranoid.paranoidhub.helpers.DownloadHelper;
import com.paranoid.paranoidhub.helpers.RebootHelper;
import com.paranoid.paranoidhub.helpers.RecoveryHelper;
import com.paranoid.paranoidhub.updater.RomUpdater;
import com.paranoid.paranoidhub.updater.Updater;
import com.paranoid.paranoidhub.utils.IOUtils;
import com.paranoid.paranoidhub.utils.OTAUtils;
import com.paranoid.paranoidhub.utils.Utils;
import com.paranoid.paranoidhub.widget.Card;
import com.paranoid.paranoidhub.widget.Splash;

import java.util.ArrayList;
import java.util.List;

public class HubActivity extends Activity
        implements Updater.UpdaterListener, DownloadHelper.DownloadCallback, AdapterView.OnItemClickListener {

    public static final int STATE_UPDATES = 0;
    public static final int STATE_DOWNLOAD = 1;
    public static final int STATE_INSTALL = 2;
    public static final int STATE_FEEDBACK = 3;
    private static final String CHANGELOG = "https://plus.google.com/+Aospal";
    private static final String COMMUNITY = "https://plus.google.com/communities/103106032137232805260";
    private static final String CROWDIN = "https://crowdin.com/project/aospa-legacy";
    private static final String GITHUB = "https://github.com/AOSPA-L";
    private static final String STATE = "STATE";
    private ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private RebootHelper mRebootHelper;
    private DownloadHelper.DownloadCallback mDownloadCallback;

    private SystemCard mSystemCard;
    private UpdatesCard mUpdatesCard;
    private SettingsCard mSettingsCard;
    private DownloadCard mDownloadCard;
    private InstallCard mInstallCard;

    private Splash mSplash;

    private RomUpdater mRomUpdater;
    private OTAUtils.NotificationInfo mNotificationInfo;

    private LinearLayout mCardsLayout;
    private CharSequence mTitle;

    private Context mContext;
    private Bundle mSavedInstanceState;

    private int mState = STATE_UPDATES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        mSavedInstanceState = savedInstanceState;
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        setContentView(R.layout.activity_hub);

        if ((actionBar = getActionBar()) != null) actionBar.setDisplayHomeAsUpEnabled(true);
        assert actionBar != null;
        actionBar.setHomeButtonEnabled(true);

        Resources res = getResources();
        List<String> itemText = new ArrayList<>();
        itemText.add(res.getString(R.string.updates));
        itemText.add(res.getString(R.string.install));
        itemText.add(res.getString(R.string.feedback));
        itemText.add(res.getString(R.string.changelog));
        itemText.add(res.getString(R.string.community));
        itemText.add(res.getString(R.string.crowdin));
        itemText.add(res.getString(R.string.github));

        mCardsLayout = (LinearLayout) findViewById(R.id.cards_layout);
        mTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mSplash = (Splash) findViewById(R.id.splash_view);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerList.setAdapter(new ArrayAdapter<>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                itemText));

        mDrawerList.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                updateTitle();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                actionBar.setTitle(R.string.app_name);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        RecoveryHelper mRecoveryHelper = new RecoveryHelper(this);
        mRebootHelper = new RebootHelper(mRecoveryHelper);

        mRomUpdater = new RomUpdater(this, false);
        mRomUpdater.addUpdaterListener(this);

        DownloadHelper.init(this, this);

        Intent intent = getIntent();
        onNewIntent(intent);

        if (mSavedInstanceState == null) {

            IOUtils.init(this);

            mCardsLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.up_from_bottom));

            if (mNotificationInfo != null) {
                if (mNotificationInfo.mNotificationId != Updater.NOTIFICATION_ID) {
                    checkUpdates();
                } else {
                    mRomUpdater.setLastUpdates(mNotificationInfo.mPackageInfosRom);
                }
            } else {
                checkUpdates();
            }
            if (DownloadHelper.isDownloading(true) || DownloadHelper.isDownloading(false)) {
                setState(STATE_DOWNLOAD, true, false);
            } else {
                if (mState != STATE_INSTALL) {
                    setState(STATE_UPDATES, true, false);
                }
            }
        } else {
            setState(mSavedInstanceState.getInt(STATE), false, true);
        }

        if (!OTAUtils.alarmExists(this)) {
            OTAUtils.setAlarm(this, true);
        }

        mSplash.finish();
    }

    public void setDownloadCallback(DownloadHelper.DownloadCallback downloadCallback) {
        mDownloadCallback = downloadCallback;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE, mState);
        switch (mState) {
            case STATE_UPDATES:
                mSystemCard.saveState(outState);
                mUpdatesCard.saveState(outState);
                mSettingsCard.saveState(outState);
                break;
            case STATE_DOWNLOAD:
                mDownloadCard.saveState(outState);
                break;
            case STATE_INSTALL:
                mInstallCard.saveState(outState);
                break;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void checkUpdates() {
        if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
            mRomUpdater.check();
        } else if (!Utils.isNetworkAvailable(this) || !Utils.isOnWifi(this)) {
            Utils.createToast(App.getContext().getString(R.string.no_connection));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                if (mState == STATE_UPDATES || mState == STATE_DOWNLOAD) {
                    break;
                }
                setState(STATE_UPDATES, true, false);
                break;
            case 1:
                if (mState == STATE_INSTALL) {
                    break;
                }
                setState(STATE_INSTALL, true, false);
                break;
            case 2:
                //TODO: Feedback
                break;
            case 3:
                Intent browserIntent;
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CHANGELOG));
                    startActivity(browserIntent);
                } else if (!Utils.isNetworkAvailable(this) || !Utils.isOnWifi(this)) {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 4:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(COMMUNITY));
                    startActivity(browserIntent);
                } else if (!Utils.isNetworkAvailable(this) || !Utils.isOnWifi(this)) {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 5:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CROWDIN));
                    startActivity(browserIntent);
                } else if (!Utils.isNetworkAvailable(this) || !Utils.isOnWifi(this)) {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 6:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB));
                    startActivity(browserIntent);
                } else if (!Utils.isNetworkAvailable(this) || !Utils.isOnWifi(this)) {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
        }
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        mNotificationInfo = null;
        if (intent != null && intent.getExtras() != null) {
            mNotificationInfo = (OTAUtils.NotificationInfo) intent.getSerializableExtra(OTAUtils.FILES_INFO);
            if (intent.getBooleanExtra(OTAUtils.CHECK_DOWNLOADS_FINISHED, false)) {
                DownloadHelper.checkDownloadFinished(this,
                        intent.getLongExtra(OTAUtils.CHECK_DOWNLOADS_ID, -1L));
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DownloadHelper.registerCallback(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DownloadHelper.unregisterCallback();
    }

    @Override
    public void onDownloadStarted() {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadStarted();
        }
    }

    @Override
    public void onDownloadProgress(int progress) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadProgress(progress);
        }
    }

    @Override
    public void onDownloadFinished(Uri uri, String md5, boolean isRom) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFinished(uri, md5, isRom);
        }
        if (uri == null) {
            if (!DownloadHelper.isDownloading(!isRom)) {
                setState(STATE_UPDATES, true, false);
            }
        } else {
            setState(STATE_INSTALL, true, null, uri, md5, isRom, false);
        }
    }

    @Override
    public void onDownloadError(String reason) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadError(reason);
        }
    }

    @Override
    public void startChecking() {
        setProgressBarIndeterminate(true);
        setProgressBarVisibility(true);
    }

    @Override
    public void versionFound(Updater.PackageInfo[] info) {

    }

    @Override
    public void checkError(String cause) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public void setState(int state) {
        setState(state, false, false);
    }

    public void setState(int state, boolean animate, boolean fromRotation) {
        setState(state, animate, null, null, null, false, fromRotation);
    }

    public void setState(int state, boolean animate, Updater.PackageInfo[] infos,
                         Uri uri, String md5, boolean isRom, boolean fromRotation) {
        mState = state;
        switch (state) {
            case STATE_UPDATES:
                if (mSystemCard == null) {
                    mSystemCard = new SystemCard(mContext, null, mRomUpdater, mSavedInstanceState);
                }
                if (mUpdatesCard == null) {
                    mUpdatesCard = new UpdatesCard(mContext, null, mRomUpdater, mSavedInstanceState);
                }
                if (mSettingsCard == null) {
                    mSettingsCard = new SettingsCard(mContext, null, mSavedInstanceState);
                }
                addCards(new Card[]{
                        mSystemCard, mUpdatesCard, mSettingsCard
                }, animate, true);
                break;
            case STATE_DOWNLOAD:
                if (mDownloadCard == null) {
                    mDownloadCard = new DownloadCard(mContext, null, infos, mSavedInstanceState);
                } else {
                    mDownloadCard.setInitialInfos(infos);
                }
                addCards(new Card[]{
                        mDownloadCard
                }, animate, true);
                break;
            case STATE_INSTALL:
                if (mInstallCard == null) {
                    mInstallCard = new InstallCard(mContext, null, mRebootHelper,
                            mSavedInstanceState);
                }
                if (!DownloadHelper.isDownloading(!isRom)) {
                    addCards(new Card[]{
                            mInstallCard
                    }, !fromRotation, true);
                } else {
                    addCards(new Card[]{
                            mInstallCard
                    }, true, false);
                }
                if (uri != null) {
                    mInstallCard.addFile(uri, md5);
                }
                break;
        }
        ((ArrayAdapter<String>) mDrawerList.getAdapter()).notifyDataSetChanged();
        updateTitle();
    }

    public void addCards(Card[] cards, boolean animate, boolean remove) {
        mCardsLayout.clearAnimation();
        if (remove) {
            mCardsLayout.removeAllViews();
        }
        if (animate) {
            mCardsLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.up_from_bottom));
        }
        for (Card card : cards) {
            mCardsLayout.addView(card);
        }
    }

    private void updateTitle() {
        switch (mState) {
            case STATE_UPDATES:
                actionBar.setTitle(R.string.updates);
                break;
            case STATE_INSTALL:
                actionBar.setTitle(R.string.install);
                break;
        }
    }

}
