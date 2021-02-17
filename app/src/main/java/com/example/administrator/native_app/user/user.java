package com.example.administrator.native_app.user;

import android.app.Activity;
import android.app.admin.DeviceAdminInfo;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.administrator.native_app.MainActivity;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.login;
import com.example.administrator.native_app.network.localstorege;
import com.example.administrator.native_app.network.network;

import java.util.HashMap;
import java.util.Iterator;


/**
 * Created by Administrator on 2019/10/11 0011.
 */
 public class user {
    public static String server_host="http://www.titang.shop/";
    private static boolean user_status=false;
    public static void  is_login(Handler handler, final callback callback){
        final String user_info;
        if((user_info=localstorege.singleton().get("http://www.titang.shop","user_info"))==null){
            callback.fail("{'code':600,'message':'unlogin'}",server_host);
        }
        else {
            network.singleton(handler).get(server_host + "user/user_info", new HashMap<String, String>(), new callback() {
                @Override
                public void fail(String message, String url) {
                    user_status=false;
                    callback.fail(message,url);
                }

                @Override
                public void seccess(byte[] data, String url) {
                    HashMap user_info = JSON.parseObject(new String(data),HashMap.class);
                    if (Integer.valueOf(user_info.get("code")+"")==200){
                        callback.seccess(data,url);
                    }else {
                        callback.fail(new String(data),url);
                    }
                }
            });
        }
    }
}
