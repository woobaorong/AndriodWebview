package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApkUpdater {

    public static final String FILE_NAME = "men_mao.apk";
    private static long downloadId;

    /**
     * 记住 包名和签名必须一致才能更新
     * @param url
     * @param context
     */
    public static void downloadAPK(String url, Context context) {
        try {
            // 设置下载请求
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle("下载APK");
            request.setDescription("正在下载应用");
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, FILE_NAME);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setMimeType("application/vnd.android.package-archive");
            // 获取下载服务
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            // 开始下载
            downloadId = downloadManager.enqueue(request);
            // 注册广播接收器以监听下载完成事件
            context.registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (id == downloadId) {
                        installAPK(FILE_NAME,context);
                    }
                }
            }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } catch (Exception e) {
            Log.e(TAG, "downloadAPK" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void installAPK(String fileName, Context context) {
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri fileUri;
            // For Android 7.0+ (API 24+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                fileUri = Uri.fromFile(file);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                fileUri = Uri.fromFile(file);
            }
            intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
