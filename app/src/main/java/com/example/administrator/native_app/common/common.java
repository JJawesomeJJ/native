package com.example.administrator.native_app.common;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.alibaba.fastjson.JSON;
import com.example.administrator.native_app.network.network;

import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Administrator on 2019/10/15 0015.
 */

public class common {
    public static String rand(int length){
        String str="abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        String return_str="";
        for (int i=0;i<length;i++){
            int rand_num=random.nextInt(str.length()-1);
            return_str+=str.substring(rand_num,rand_num+1);
        }
        return return_str;

    }

    /**
     * fmt=yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String get_current_time(String fmt){
        SimpleDateFormat df = new SimpleDateFormat(fmt);//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }
    public static void setCookie(String url, Handler mhander, WebView webView) {
        final int version = Build.VERSION.SDK_INT;
// 因为evaluateJavascript方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
        network net=network.singleton(mhander);
        com.alibaba.fastjson.JSONObject jsonObject=net.get_url_cookies(url);
        if(jsonObject!=null) {
            for (Map.Entry entry : jsonObject.entrySet()) {
                HashMap cookie=(HashMap) jsonObject.get(entry.getKey());
                String key=(String) cookie.get("key");
                String value=(String) cookie.get("value");
                String expire=Long.toString((Long) cookie.get("expires"));
                if (version < 18) {
                    webView.loadUrl(String.format("javascript:setCookie('%s','%s','%s')",key,value,expire));
                } else {
                    webView.evaluateJavascript(String.format("javascript:setCookie('%s','%s','%s')",key,value,expire), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //此处为 js 返回的结果
                        }
                    });
                }
            }
        }
    }
}
