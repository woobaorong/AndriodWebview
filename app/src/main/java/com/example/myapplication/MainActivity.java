package com.example.myapplication;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.yanzhenjie.andserver.AndServer;
import com.yanzhenjie.andserver.Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {

    Server server;

    //String CDNUrl = "https://sanguo-web-mobile-2em0ex294551e7-1257352752.ap-shanghai.app.tcloudbase.com/";
    String CDNUrl = "http://192.168.1.3:8000/";

    //String url = "https://6f099278.sanguo-web-mobile.pages.dev/";
    //String url = "http://192.168.3.2:8000/";
    //String url = "https://www.baidu.com/";
    String url = "http://127.0.0.1:8080/index.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置为全屏（隐藏状态栏）
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(lp);

        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //如果data_files下没有文件就先整体拷贝一份过去
        File pg = new File(this.getFilesDir(), "dist/project.manifest");
        boolean reslult = true;
        if (!pg.exists()) {
            reslult = AssetCopier.copyAssetsRecursively(this, "dist");
        }
        if (!reslult) {
            Log.e(TAG, "Copy Project Exception");
            return;
        }

        server = AndServer.webServer(this)
                .port(8080)
                .timeout(10, TimeUnit.SECONDS)
                .listener(new Server.ServerListener() {
                    @Override
                    public void onStarted() {
                        Log.e(TAG, "onStarted" + server.getInetAddress());
                    }

                    @Override
                    public void onStopped() {
                        Log.e(TAG, "onStopped");
                    }

                    @Override
                    public void onException(Exception e) {
                        Log.e(TAG, "onException" + e.getMessage());
                    }
                })
                .build();

        server.startup();
        this.createWebView();
    }

    public void restart() {
       try {
           this.runOnUiThread(() -> {
               webView.loadUrl(url);
           });
       }catch (Exception e){
           Log.e(TAG, "restart " + e.toString());
       }
    }

    public void quit() {
        try {
            this.runOnUiThread(() -> {
                finish();
            });
        }catch (Exception e){
            Log.e(TAG, "restart " + e.toString());
        }
    }

    public WebView webView = null;

    /* 创建 WebView 实例 */
    @SuppressLint("SetJavaScriptEnabled")
    private void createWebView() {
        // 创建 WebView 实例并通过 id 绑定我们刚在布局中创建的 WebView 标签
        // 这里的 R.id.webview 就是 activity_main.xml 中的 WebView 标签的 id
        this.webView = (WebView) findViewById(R.id.webview);

        // 设置 WebView 允许执行 JavaScript 脚本
        webView.getSettings().setJavaScriptEnabled(true);

        // 确保跳转到另一个网页时仍然在当前 WebView 中显示
        // 而不是调用浏览器打开
        webView.setWebViewClient(new WebViewClient());


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        //设置缓存模式
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        // 开启DOM storage API 功能
        settings.setDomStorageEnabled(true);
        // 开启database storage API功能
        settings.setDatabaseEnabled(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);//不读取缓存
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        settings.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(this), "Android");
        /*webView.evaluateJavascript("Android.hehe()", new ValueCallback<String>(){
        @Override
        public void onReceiveValue(String s) {
            //s为 js 返回的结果
                Log.e(TAG, "ValueCallback " + s);
        }
        });*/

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
                    webView.goBack();
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(url);
        //webView.loadUrl("http://192.168.1.3:7456/");

    }

    @Override
    public void onBackPressed() {
       /* if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else {
            super.onBackPressed();
        }*/
    }


}

class WebAppInterface {
    MainActivity mContext;

    // 实例化接口并传入Context
    WebAppInterface(MainActivity c) {
        mContext = c;
    }

    // 显示Toast的JavaScript接口
    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void downloadFile(String url, String path) {
        FileDownloader.downloadFile(url, mContext.getFilesDir().getPath() + path, isSuccess -> {
            mContext.runOnUiThread(() -> {
                if (isSuccess)
                    mContext.webView.evaluateJavascript("globalThis.downloadCallback()", null);
            });
        });
    }

    @JavascriptInterface
    public String readDataJsonFile(String path) {
        File jsonFile = new File(mContext.getFilesDir(), path);
        if (jsonFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(jsonFile))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    @JavascriptInterface
    public void readRemoteJsonFile(String url) {
        FileDownloader.downloadJsonStr(url, (String url2, String str) -> {
            mContext.runOnUiThread(() -> {
                mContext.webView.evaluateJavascript("globalThis.readRemoteJsonFileCallback('" + url2 + "','" + str + "')", null);
            });
        });
    }

    @JavascriptInterface
    public void restart() {
        mContext.restart();
    }

    @JavascriptInterface
    public void quit() {
        mContext.quit();
    }

    @JavascriptInterface
    public String getVersion() {
       return "v1.0";
    }
}