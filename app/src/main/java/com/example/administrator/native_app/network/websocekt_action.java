package com.example.administrator.native_app.network;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.ValueCallback;

import com.alibaba.fastjson.JSONObject;
import com.example.administrator.native_app.activity.ActivityCollector;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.config.*;
import com.example.administrator.native_app.play_native;
import com.example.administrator.native_app.system.System_log;

import java.util.HashMap;

/**
 * Created by Administrator on 2020/1/29 0029.
 */

public class websocekt_action {
    private static websocekt_action instance;
    public synchronized static websocekt_action SingleTon(){
        if(instance==null){
            instance=new websocekt_action();
        }
        return instance;
    }
    private websocekt_action(){}
    public void token(JSONObject jsonObject){
        try {
            localstorege.singleton().set(config.server, "_token", jsonObject.get("_token").toString());
            localstorege.singleton().set(config.server, "fd", jsonObject.get("fd").toString());
        }
        catch (Exception e){
            System_log.log_error(e.getMessage());
        }
    }
    public void barrage(final JSONObject jsonObject){
        if(ActivityCollector.getInstance().getRun_at() instanceof play_native){
            final play_native run_at=(play_native) ActivityCollector.getInstance().getRun_at();
            send_to_main_process(run_at.mhander, new callback() {
                @Override
                public void fail(String message, String url) {
                    Log.i("fail","fdf");
                }
                @Override
                public void seccess(byte[] data, String url) {
                    final int version = Build.VERSION.SDK_INT;
// 因为evaluateJavascript方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
                    if (version < 18) {
                        run_at.barrage.loadUrl(String.format("javascript:push_ele('%s')",jsonObject.toJSONString()));
                    } else {
                        run_at.barrage.evaluateJavascript(String.format("javascript:push_ele('%s')",jsonObject.toJSONString()), new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //此处为 js 返回的结果
                            }
                        });
                    }
//                    run_at.toast(jsonObject.toJSONString());
                }
            },jsonObject);
        }
    }
    public void common(final JSONObject jsonObject){
        if(ActivityCollector.getInstance().getRun_at() instanceof play_native){
            final play_native run_at=(play_native) ActivityCollector.getInstance().getRun_at();
            send_to_main_process(run_at.mhander, new callback() {
                @Override
                public void fail(String message, String url) {
                    Log.i("fail","fdf");
                }
                @Override
                public void seccess(byte[] data, String url) {
                    String action=(String) jsonObject.get("action");
                    jsonObject.remove("action");
                    final int version = Build.VERSION.SDK_INT;
// 因为evaluateJavascript方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
                    if (version < 18) {
                        run_at.barrage.loadUrl(String.format("javascript:%s('%s')",action,jsonObject.toJSONString()));
                    } else {
                        run_at.barrage.evaluateJavascript(String.format("javascript:%s('%s')",action,jsonObject.toJSONString()), new ValueCallback<String>() {
                            @Override
                            public void onReceiveValue(String value) {
                                //此处为 js 返回的结果
                            }
                        });
                    }
//                    run_at.toast(jsonObject.toJSONString());
                }
            },jsonObject);
        }
    }
    private void send_to_main_process(Handler handler,callback callback,JSONObject jsonObject){
        Message msg_obejct = Message.obtain();
        Bundle bundle = new Bundle();
        HashMap hashMap=new HashMap();
        hashMap.put("callback",callback);
        hashMap.put("data",jsonObject.toJSONString().getBytes());
        hashMap.put("method","suceess");
        hashMap.put("url",config.server);
        msg_obejct.what=1;
        msg_obejct.obj=hashMap;
        handler.sendMessage(msg_obejct);
    }
}
