package com.paranoid.paranoidhub.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.instabug.library.Instabug;
import com.paranoid.paranoidhub.App;
import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.cards.DownloadCard;
import com.paranoid.paranoidhub.cards.FeedbackCard;
import com.paranoid.paranoidhub.cards.InstallCard;
import com.paranoid.paranoidhub.cards.SettingsCard;
import com.paranoid.paranoidhub.cards.SystemCard;
import com.paranoid.paranoidhub.cards.UpdatesCard;
import com.paranoid.paranoidhub.helpers.DownloadHelper;
import com.paranoid.paranoidhub.helpers.PreferenceHelper;
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

public class HubActivity extends AppCompatActivity
        implements Updater.UpdaterListener, DownloadHelper.DownloadCallback, AdapterView.OnItemClickListener,
        ImageView.OnClickListener {

    public static final int STATE_UPDATES = 0;
    public static final int STATE_DOWNLOAD = 1;
    public static final int STATE_INSTALL = 2;
    public static final int STATE_FEEDBACK = 3;

    private static final String CHANGELOG = "https://plus.google.com/+Aospal";
    private static final String COMMUNITY = "https://plus.google.com/communities/103106032137232805260";
    private static final String CROWDIN = "https://crowdin.com/project/aospa-legacy";
    private static final String GITHUB = "https://github.com/AOSPA-L";
    private static final String STATE = "STATE";

    private static final int select_photo = 1;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView mDrawerImage;
    private LinearLayout mCardsLayout;
    private FloatingActionButton mFloatingActionButton;

    private SystemCard mSystemCard;
    private UpdatesCard mUpdatesCard;
    private SettingsCard mSettingsCard;
    private DownloadCard mDownloadCard;
    private InstallCard mInstallCard;
    private FeedbackCard mFeedbackCard;

    private RebootHelper mRebootHelper;
    private DownloadHelper.DownloadCallback mDownloadCallback;
    private RomUpdater mRomUpdater;
    private OTAUtils.NotificationInfo mNotificationInfo;
    private Context mContext;
    private Bundle mSavedInstanceState;

    private int mState = STATE_UPDATES;

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);

        mContext = this;
        mSavedInstanceState = savedInstanceState;
        setContentView(R.layout.activity_hub);

        Resources res = getResources();
        List<String> itemText = new ArrayList<>();
        itemText.add(res.getString(R.string.updates));
        itemText.add(res.getString(R.string.install));
        itemText.add(res.getString(R.string.feedback));
        itemText.add(res.getString(R.string.changelog));
        itemText.add(res.getString(R.string.community));
        itemText.add(res.getString(R.string.crowdin));
        itemText.add(res.getString(R.string.github));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCardsLayout = (LinearLayout) findViewById(R.id.cards_layout);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);
        mDrawerImage = (ImageView) findViewById(R.id.drawer_header);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        Splash mSplash = (Splash) findViewById(R.id.splash_view);

        mDrawerList.setAdapter(new ArrayAdapter<>(
                this,
                R.layout.drawer_list_item,
                R.id.drawer_text,
                itemText));

        mDrawerList.setOnItemClickListener(this);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                             /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                toolbar,
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerImage.setOnClickListener(this);

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

            if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                if (mNotificationInfo != null) {
                    if (mNotificationInfo.mNotificationId != Updater.NOTIFICATION_ID) {
                        checkUpdates();
                    } else {
                        mRomUpdater.setLastUpdates(mNotificationInfo.mPackageInfosRom);
                    }
                } else {
                    checkUpdates();
                }
            } else {
                Utils.createToast(App.getContext().getString(R.string.no_connection));
            }

            if (DownloadHelper.isDownloading()) {
                setState(STATE_DOWNLOAD, true, false);
            } else if (mState != STATE_INSTALL) {
                setState(STATE_UPDATES, true, false);
            }
        } else {
            setState(mSavedInstanceState.getInt(STATE), false, true);
        }

        if (!OTAUtils.alarmExists(this)) {
            OTAUtils.setAlarm(this, true);
        }

        if (!PreferenceHelper.getPreference(PreferenceHelper.PROPERTY_SHOWN_INTRO, false)) {
            Intent introIntent = new Intent(this, Intro.class);
            startActivity(introIntent);
            PreferenceHelper.setPreference(PreferenceHelper.PROPERTY_SHOWN_INTRO, true);
        } else {
            mSplash.setVisibility(View.VISIBLE);
            mSplash.finish();
        }

        String customImagePath = PreferenceHelper.getPreference(PreferenceHelper.PROPERTY_DRAWER_IMAGE, null);
        if (customImagePath != null) {
            mDrawerImage.setImageBitmap(BitmapFactory.decodeFile(customImagePath));
        }
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

    @SuppressLint("MissingSuperCall")
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
        } else {
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
                updateFAB(View.GONE, null, null);
                break;
            case 1:
                if (mState == STATE_INSTALL) {
                    break;
                }
                setState(STATE_INSTALL, true, false);
                updateFAB(View.GONE, null, null);
                break;
            case 2:
                setState(STATE_FEEDBACK, true, false);
                updateFAB(View.VISIBLE, getDrawable(R.drawable.ic_feedback_white_48dp), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Utils.createInputDialog(App.getContext().getString(R.string.feedback_title), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Instabug instabug = Instabug.getInstance();
                                EditText inputText = (EditText) Utils.myDialogView.findViewById(R.id.dialog_input);
                                String text = inputText.getText().toString() + "\n" + Utils.getDeviceInfo();
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
                });
                break;
            case 3:
                Intent browserIntent;
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CHANGELOG));
                    startActivity(browserIntent);
                } else {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 4:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(COMMUNITY));
                    startActivity(browserIntent);
                } else {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 5:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(CROWDIN));
                    startActivity(browserIntent);
                } else {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
            case 6:
                if (Utils.isNetworkAvailable(this) || Utils.isOnWifi(this)) {
                    browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB));
                    startActivity(browserIntent);
                } else {
                    Utils.createToast(App.getContext().getString(R.string.no_connection));
                }
                break;
        }
        mDrawerLayout.closeDrawer(mDrawer);
    }

    // Properly handle back key event
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_BACK)
                && (event.getAction() == KeyEvent.ACTION_DOWN)) {
            if (mDrawerLayout.isDrawerOpen(mDrawer)) {
                mDrawerLayout.closeDrawer(mDrawer);
            } else finish();
            return true;
        } else return false;
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

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onResume() {
        super.onResume();
        DownloadHelper.registerCallback(this);
    }

    @SuppressLint("MissingSuperCall")
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
    public void onDownloadFinished(Uri uri, String md5) {
        if (mDownloadCallback != null) {
            mDownloadCallback.onDownloadFinished(uri, md5);
        }
        if (uri == null) {
            setState(STATE_UPDATES, true, false);
        } else {
            setState(STATE_INSTALL, true, null, uri, md5, false);
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

    public void setState(int state, boolean animate, boolean fromRotation) {
        setState(state, animate, null, null, null, fromRotation);
    }

    public void setState(int state, boolean animate, Updater.PackageInfo[] infos,
                         Uri uri, String md5, boolean fromRotation) {
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
                addCards(new Card[]{
                        mInstallCard
                }, !fromRotation, true);
                if (uri != null) {
                    mInstallCard.addFile(uri, md5);
                }
                break;
            case STATE_FEEDBACK:
                if (mFeedbackCard == null) {
                    mFeedbackCard = new FeedbackCard(mContext, null, mSavedInstanceState);
                }
                mCardsLayout.clearAnimation();
                mCardsLayout.removeAllViews();
                mCardsLayout.setAnimation(AnimationUtils.loadAnimation(this, R.anim.up_from_bottom));
                mCardsLayout.addView(mFeedbackCard);
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
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.updates);
                break;
            case STATE_INSTALL:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.install);
                break;
            case STATE_FEEDBACK:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setTitle(R.string.feedback);
                break;
        }
    }

    private void drawerImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, select_photo);
        mDrawerImage.setAnimation(AnimationUtils.loadAnimation(this, R.anim.selector_shake));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == select_photo && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            mDrawerImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            PreferenceHelper.setPreference(PreferenceHelper.PROPERTY_DRAWER_IMAGE, picturePath);
        }
    }

    @Override
    public void onClick(View v) {
        new AlertDialog.Builder(v.getContext()).setItems(v.getResources()
                .getStringArray(R.array.main_header_picture_items), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        drawerImagePicker();
                        break;
                    case 1:
                        PreferenceHelper.removePreference(PreferenceHelper.PROPERTY_DRAWER_IMAGE);
                        mDrawerImage.setImageResource(R.drawable.drawer_bg);
                        mDrawerImage.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.default_shake));
                        break;
                }

            }
        }).show();
    }

    private void updateFAB(int visibility, Drawable drawable, View.OnClickListener listener) {
        mFloatingActionButton.setVisibility(visibility);
        mFloatingActionButton.setBackgroundTintList(getResources().getColorStateList(R.color.red_700));
        mFloatingActionButton.setImageDrawable(drawable);
        mFloatingActionButton.setOnClickListener(listener);
    }
}
