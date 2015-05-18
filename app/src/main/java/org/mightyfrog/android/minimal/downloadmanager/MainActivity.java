package org.mightyfrog.android.minimal.downloadmanager;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.URLUtil;
import android.widget.TextView;

import java.io.File;

/**
 * DownloadManager sample code.
 *
 * @author Shigehiro Soejima
 */
public class MainActivity extends Activity {
    private static final String URL = "http://i.imgur.com/nI1jvVU.jpg";

    private BroadcastReceiver mBroadcastReceiver;
    private final IntentFilter mIntentFilter = new IntentFilter() {
        {
            // interested in this action only
            addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
//            addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
//            addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
        }
    };

    private DownloadManager mDownloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())) {
                    onDownloadComplete(intent);
                }
            }
        };

        enqueueDownloadRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(mBroadcastReceiver); // make sure to unregister to avoid leak
    }

    //
    //
    //

    /**
     *
     */
    private void enqueueDownloadRequest() {
        final DownloadManager.Request req = new DownloadManager.Request(Uri.parse(URL));
        final String filename = URLUtil.guessFileName(URL, null, "image/*");
        final File file = new File(Environment.getExternalStorageDirectory(), filename);
        req.setDestinationUri(Uri.fromFile(file));
        try {
            mDownloadManager.enqueue(req);
        } catch (IllegalArgumentException e) {
            // invalid URL
        }
    }

    /**
     * @param intent The intent.
     */
    private void onDownloadComplete(Intent intent) {
        final long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        final DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        final Cursor c = mDownloadManager.query(query);
        try {
            c.moveToFirst();
            if (DownloadManager.STATUS_SUCCESSFUL
                    == c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                final String uriStr =
                        c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                final File f = new File(Uri.parse(uriStr).getPath());

                ((TextView) findViewById(R.id.text_view)).setText("downloaded: " + f);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}
