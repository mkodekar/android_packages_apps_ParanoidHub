package com.paranoid.paranoidhub.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.paranoid.paranoidhub.R;
import com.paranoid.paranoidhub.utils.IOUtils;

import java.io.File;
import java.io.Serializable;
import java.util.List;

public class RequestFileActivity extends Activity {

    private static final String ROOT_ID_PRIMARY_EMULATED = "primary";
    private static final int REQUEST_PICK_FILE = 203;
    private static RequestFileCallback sCallback;

    public static void setRequestFileCallback(RequestFileCallback callback) {
        sCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PackageManager packageManager = getPackageManager();
        Intent test = new Intent(Intent.ACTION_GET_CONTENT);
        test.setType("application/zip*");
        List<ResolveInfo> list = packageManager.queryIntentActivities(test,
                PackageManager.GET_ACTIVITIES);
        if (list.size() > 0) {
            Intent intent = new Intent();
            intent.setType("application/zip");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, REQUEST_PICK_FILE);
        } else {
            Toast.makeText(this, R.string.file_manager_error, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PICK_FILE) {
            if (data == null) {
                finish();
                return;
            }

            Uri uri = data.getData();

            String filePath = uri.getPath();

            if (!(new File(filePath)).exists()) {
                ContentResolver cr = getContentResolver();
                try (Cursor cursor = cr.query(uri, null, null, null, null)) {
                    if (cursor.moveToNext()) {
                        int index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                        if (index >= 0) {
                            filePath = cursor.getString(index);
                        } else if (Build.VERSION.SDK_INT >= 19
                                && uri.toString().startsWith(ContentResolver.SCHEME_CONTENT)) {
                            String newUri = new Uri.Builder()
                                    .scheme(ContentResolver.SCHEME_CONTENT)
                                    .authority(uri.getAuthority()).appendPath("document")
                                    .build().toString();
                            String path = uri.toString();
                            index = filePath.indexOf(":");
                            if (path.startsWith(newUri) && index >= 0) {
                                String firstPath = filePath.substring(0, index);
                                filePath = filePath.substring(index + 1);
                                String storage = IOUtils.getPrimarySdCard();
                                if (!firstPath.contains(ROOT_ID_PRIMARY_EMULATED)) {
                                    storage = IOUtils.getSecondarySdCard();
                                }
                                filePath = storage + "/" + filePath;
                            } else {
                                filePath = null;
                            }

                        }
                    }
                }
            }

            if (sCallback != null) {
                sCallback.fileRequested(filePath);
            }

        }
        finish();
    }

    public interface RequestFileCallback extends Serializable {

        void fileRequested(String filePath);
    }
}
