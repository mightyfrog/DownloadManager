package org.mightyfrog.android.minimal.downloadmanager

import android.Manifest
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


/**
 * DownloadManager sample code.
 *
 * @author Shigehiro Soejima
 */
@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private val broadcastReceiver: BroadcastReceiver
    private val intentFilter = IntentFilter().apply {
        // interested in this action only
        addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        //            addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        //            addAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
    }

    private val downloadManager: DownloadManager by lazy {
        getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    companion object {
        private const val URL = "https://i.imgur.com/D58Ncu3.jpg"

        private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1 + 0xDEAD
    }

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                    onDownloadComplete(intent)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { checkPermission() }
    }

    override fun onResume() {
        super.onResume()

        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onPause() {
        super.onPause()

        unregisterReceiver(broadcastReceiver) // make sure to unregister to avoid leak
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enqueueDownloadRequest()
        }
    }

    //
    //
    //

    /**
     *
     */
    private fun enqueueDownloadRequest() {
        activityCircle.visibility = View.VISIBLE
        val req = DownloadManager.Request(Uri.parse(URL)).apply {
            val filename = URLUtil.guessFileName(URL, null, "image/*")
            val file = File(Environment.getExternalStorageDirectory(), filename)
            setDestinationUri(Uri.fromFile(file))
        }
        try {
            downloadManager.enqueue(req)
        } catch (e: IllegalArgumentException) {
            // invalid URL
        }
    }

    /**
     * @param intent The intent.
     */
    private fun onDownloadComplete(intent: Intent) {
        val query = DownloadManager.Query()
        query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0))
        downloadManager.query(query)?.apply {
            use {
                moveToFirst()
                if (DownloadManager.STATUS_SUCCESSFUL == getInt(getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                    val uriStr = getString(getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                    BitmapFactory.decodeFile(Uri.parse(uriStr).path)?.apply {
                        imageView.setImageBitmap(this)
                    }
                }
            }
        }
        activityCircle.visibility = View.GONE
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            enqueueDownloadRequest()
        }
    }
}
