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

    public static void copyAssetsRecursively(Context context, String assetPath) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list(assetPath);
            if (files != null && files.length > 0) {
                // 创建一个新的目标文件夹（如果它还不存在）
                File destDir = new File(context.getFilesDir(), assetPath);
                if (!destDir.exists()) {
                    destDir.mkdirs();
                    Log.e(TAG, "path123   " + assetPath);
                }
                for (String filename : files) {
                    String fullPath =  assetPath + File.separator + filename;
                    copyAssetsRecursively(context, fullPath);
                }
            }else{
                InputStream in = assetManager.open(assetPath);
                File outFile = new File(context.getFilesDir(), assetPath);
                OutputStream out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }


    public boolean downloadFile(final String urlString, final String targetFilePath) {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(urlString)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            ResponseBody body = response.body();
            if (body != null) {
                File targetFile = new File(targetFilePath);
                // Ensure the directory exists before writing the file.
                File parentDirectory = targetFile.getParentFile();
                if (parentDirectory != null && !parentDirectory.exists()) {
                    parentDirectory.mkdirs();
                }
                try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                    fos.write(body.bytes());
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void downloadFile(String fileUrl, String saveDir, String fileName) {
        File dir = new File(saveDir);
        if (!dir.exists()) {
            // 如果目录不存在，则创建它，包括所有必需的父目录
            boolean result = dir.mkdirs();
            if (!result) {
                // 处理目录创建失败的情况
                // 例如，通过打印日志、显示错误消息或抛出异常
                System.err.println("Failed to create directory: " + saveDir);
                return;
            }
        }

        try {
            URL url = new URL(fileUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            int responseCode = httpURLConnection.getResponseCode();

            // 总是检查HTTP响应代码
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 输入流用于从URL读取数据
                InputStream inputStream = httpURLConnection.getInputStream();

                // 文件输出流用于将数据写入文件
                FileOutputStream fileOutputStream = new FileOutputStream(saveDir + fileName);

                int totalSize = httpURLConnection.getContentLength();
                int downloadedSize = 0;
                byte[] buffer = new byte[1024];
                int bufferLength = 0; // 用于存储实际读取的字节数

                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    // 这里可以添加进度条更新代码
                }

                // 关闭文件输出流
                fileOutputStream.close();

                // 关闭输入流
                inputStream.close();

                // 关闭HttpURLConnection
                httpURLConnection.disconnect();

                // 可以在这里处理下载完成的逻辑
            } else {
                // 处理错误响应码
            }
        } catch (Exception e) {
            e.printStackTrace();
            // 处理异常（例如，通过显示错误消息给用户）
        }
    }


}