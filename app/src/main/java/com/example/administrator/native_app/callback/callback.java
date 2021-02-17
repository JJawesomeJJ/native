package com.example.administrator.native_app.callback;

import okhttp3.ResponseBody;

/**
 * Created by Administrator on 2019/10/11 0011.
 */

public interface callback {
    public String current_url="";
    abstract void fail(String message,String url);
    abstract void seccess(byte[] data,String url);
}
