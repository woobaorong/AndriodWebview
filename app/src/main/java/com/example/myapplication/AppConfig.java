package com.example.myapplication;

import android.content.Context;
import android.os.Environment;

import com.yanzhenjie.andserver.annotation.Config;
import com.yanzhenjie.andserver.framework.config.WebConfig;
import com.yanzhenjie.andserver.framework.website.AssetsWebsite;
import com.yanzhenjie.andserver.framework.website.StorageWebsite;

import java.io.File;

@Config
public class AppConfig implements WebConfig {

    @Override
    public void onConfig(Context context, Delegate delegate) {
        //增加一个位于assets的web目录的网站
        //delegate.addWebsite(new AssetsWebsite(context, "/dist"));

        File file = new File(context.getFilesDir(), "dist");
        String websiteDirectory = file.getAbsolutePath();
        StorageWebsite wesite = new StorageWebsite(websiteDirectory);
        delegate.addWebsite(wesite);

        //增加一个位于/sdcard/Download/AndServer/目录的网站
        //delegate.addWebsite(new StorageWebsite(context, "/sdcard/Download/AndServer/"));
    }
}
