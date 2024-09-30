package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.util.Log;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileDownloader {

    private static OkHttpClient client = new OkHttpClient();

    public static void downloadFile(String url, String filePath,DownloadCallback callback) {
        File file0 = new File(filePath);
        // 获取文件名
        String fileName = file0.getName();
        // 获取文件所在的目录路径
        String directoryPath = file0.getParent();

        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建目录，包括所有必需的父目录
        }

        File file = new File(dir, fileName);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 处理请求失败的情况
                callback.callback(false,filePath);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.e(TAG, "response   " + response);
                    callback.callback(false,filePath);
                    return;
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null){
                    callback.callback(false,filePath);
                    return;
                }

                try (InputStream inputStream = responseBody.byteStream();
                     FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                    byte[] buffer = new byte[2048];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, length);
                    }
                    fileOutputStream.flush();
                } finally {
                    responseBody.close();
                }

                // 在这里，文件已经被保存到磁盘上，你可以更新UI或进行其他操作
                callback.callback(true,filePath);
            }
        });
    }


    public static void downloadJsonStr(String url,DownloadJsonStrCallback callback) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                // 处理请求失败的情况
                callback.callback(url,null);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.e(TAG, "response   " + response);
                    callback.callback(url,null);
                    return;
                }
                ResponseBody responseBody = response.body();
                if (responseBody == null){
                    callback.callback(url,null);
                    return;
                }
                callback.callback(url,response.body().string());
            }
        });
    }
}


interface DownloadCallback{
    void callback(boolean isSuccess,String path);
}

interface DownloadJsonStrCallback{
    void callback(String url,String result);
}


