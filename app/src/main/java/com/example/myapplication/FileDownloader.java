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

    private OkHttpClient client = new OkHttpClient();

    public void downloadFile(String url, String filePath,DownloadCallback callback) {
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
                callback.callback(false);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()){
                    Log.e(TAG, "response   " + response);
                    callback.callback(false);
                }

                ResponseBody responseBody = response.body();
                if (responseBody == null) return;

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
                callback.callback(true);
            }
        });
    }
}


interface DownloadCallback{
    void callback(boolean isSuccess);
}

// 在你的Activity或Fragment中调用它
// new FileDownloader().downloadFile("http://example.com/file.zip", "/sdcard/Download/", "file.zip");
// 注意：从Android 10（API 级别 29）开始，对于外部存储的访问方式发生了变化，你可能需要使用MediaStore API或Scoped Storage