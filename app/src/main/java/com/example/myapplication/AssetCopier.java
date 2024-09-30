package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class AssetCopier {

    public static boolean copyAssetsRecursively(Context context, String assetPath) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list(assetPath);
            if (files != null && files.length > 0) {
                // 创建一个新的目标文件夹（如果它还不存在）
                File destDir = new File(context.getFilesDir(), assetPath);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                    //Log.e(TAG, "path123   " + assetPath);
                }
                boolean result = true;
                for (String filename : files) {
                    String fullPath = assetPath + File.separator + filename;
                    if (!copyAssetsRecursively(context, fullPath)) result = false;
                }
                return result;
            } else {
                InputStream in = assetManager.open(assetPath);
                File outFile = new File(context.getFilesDir(), assetPath);
                OutputStream out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                out.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "copyAssetsRecursively  Exception " + e);
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}