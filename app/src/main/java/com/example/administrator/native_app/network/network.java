package com.example.administrator.native_app.network;



import android.app.Activity;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.ViewAnimationUtils;

import com.example.administrator.native_app.callback.callback;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.file.file;
import com.google.gson.Gson;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2019/10/11 0011.
 */

public class network {
    private Handler handler;
    OkHttpClient okHttpClient;
    private HashMap cookie_container;
    private String work_path;
    private HashMap<String,HashMap<String,String>> localstorege=new HashMap<>();
    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
    private String current_domain;//the last request url domain
    private String current_url;//the last reuqest
    private static HashMap<String,network> network_object=new HashMap<>();
    private network(Handler handler){
        work_path=config.work_path()+"cookies/";
        cookie_container=new HashMap();
        this.handler=handler;
        okHttpClient=  new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.host(), cookies);
                        if(cookies.size()==0){
                            return;
                        }
                        cookieStore.put(url.host(), cookies);
                        HashMap data;
                        if(cookie_container.containsKey(((Cookie)cookies.get(0)).domain())){
                            data=(HashMap)cookie_container.get(((Cookie)cookies.get(0)).domain());
                        }else {
                            data=new HashMap();
                        }
                        String domain="";
                        for (Cookie cookie : cookies)
                        {
                            domain=cookie.domain();
                            String[] cookie_=cookies.toString().replace("[","").replace("]","").replace("httponly,","").split(";");
                            Log.i("cookie",cookie.toString());
                            HashMap cookie_message=new HashMap();
                            cookie_message.put("key",cookie.name());
                            cookie_message.put("value",cookie.value());
                            cookie_message.put("expires",cookie.expiresAt());
                            data.put(cookie.name(),cookie_message);
                        }
                        cookie_container.put(domain,data);
                        finish();
                    }
                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.host());
                        if(cookies==null){
                        }
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).build();
    }
    public void get(final String url, HashMap<String,String> params, final callback callback){
        current_domain=get_url_domain(url);
        current_url=url;
        load_cookies(url);
        HttpUrl.Builder urlBuilder = HttpUrl.parse(url)
                .newBuilder();
        Log.i("URL_PARAMS",url_build(url,params));
        final Request request = new Request.Builder().addHeader("Cookie",parse_cookie(urlBuilder.toString()))
                .url(url_build(url,params))
                .build();
        Call call = okHttpClient.newCall(request);
        //4.请求加入调度，重写回调方法
        call.enqueue(new Callback() {
            //请求失败执行的方法
            @Override
            public void onFailure(Call call, IOException e) {
                String err = e.getMessage().toString();
                message(1,callback,"fail",err,url);
            }
            //请求成功执行的方法
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                message(1,callback,"suceess",response.body().bytes(),url);
            }
        });
    }
    private String url_build(String url,Map<String,String> params){
        if(params==null){
            return url;
        }
        if(params.size()!=0){
            url+="?";
        }
        Iterator iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = entry.getKey() + "";
            String value = entry.getValue()+"";
            url+=key+"="+value+"&";
        }
        if(params.size()>0) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
    public void post(final String url, HashMap<String, String > paramsMap, final callback callback){  //这里没有返回，也可以返回string
        current_domain=get_url_domain(url);
        current_url=url;
        load_cookies(url);
        OkHttpClient mOkHttpClient = new OkHttpClient();
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        Set<String> keySet = paramsMap.keySet();
        if(paramsMap!=null) {
            for (String key : keySet) {
                String value = paramsMap.get(key);
                if(value==null){
                    value="";
                }
                formBodyBuilder.add(key, value);
            }
        }
        FormBody formBody = formBodyBuilder.build();
        Request request = new Request
                .Builder()
                .post(formBody)
                .url(url).addHeader("Cookie",parse_cookie(url))
                .build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String err = e.getMessage().toString();
                message(1,callback,"fail",err,url);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                byte[] data=response.body().bytes();
                Log.i("post_url:"+url,"response_body:"+new String(data));
                message(1,callback,"suceess",data,url);
            }
        });
    }
    public void post(final String url, HashMap<String, String > paramsMap,HashMap<String,String> file_params,final callback callback){  //这里没有返回，也可以返回string
        current_domain=get_url_domain(url);
        current_url=url;
        MultipartBody.Builder multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(paramsMap!=null) {
            Set<String> keySet=paramsMap.keySet();
            for (String key : keySet) {
                Log.i("params;;",key);
                String value = paramsMap.get(key);
                multipartBody.addFormDataPart(key,value);
            }
        }
        if(file_params!=null){
            Set<String> keySet = file_params.keySet();
            for (String key : keySet) {
                String value = file_params.get(key);
                if(file.is_file(value)){
                    File file_object=new File(value);
                    RequestBody body = RequestBody.create(MediaType.parse(file.get_file_type(value)),file_object);
                    multipartBody.addFormDataPart(key,file_object.getName(),body);
                }
            }
        }
        Request request = new Request
                .Builder()
                .post(multipartBody.build()).addHeader("Cookie",parse_cookie(url))
                .url(url)
                .header("Content-Type","multipart/form-data; boundary=----WebKitFormBoundaryb0GZcypmEqOvNHIY")
                .build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String err = e.getMessage().toString();
                message(1,callback,"fail",err,url);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                message(1,callback,"suceess",response.body().bytes(),url);
            }
        });
    }
    private void message(int msg,callback callback,String method,String data,String current_url){
        Message msg_obejct = Message.obtain();
        Bundle bundle = new Bundle();
        HashMap hashMap=new HashMap();
        hashMap.put("method",method);
        hashMap.put("callback",callback);
        hashMap.put("data",data);
        hashMap.put("url",current_url);
        msg_obejct.what=msg;
        msg_obejct.obj=hashMap;
        handler.sendMessage(msg_obejct);
    }
    private void message(int msg,callback callback,String method,byte[] data,String current_url){
        Message msg_obejct = Message.obtain();
        Bundle bundle = new Bundle();
        HashMap hashMap=new HashMap();
        hashMap.put("method",method);
        hashMap.put("callback",callback);
        hashMap.put("data",data);
        hashMap.put("url",current_url);
        msg_obejct.what=msg;
        msg_obejct.obj=hashMap;
        handler.sendMessage(msg_obejct);
    }
    private synchronized void finish( )
    {
        Iterator iterator= cookie_container.entrySet().iterator();
        file.file_path_create(config.work_path()+"cookies");
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String key = entry.getKey()+"";
            key=key.replace("/","-");
            HashMap value=(HashMap) entry.getValue();
            String data=JSON.toJSON(JSONObject.toJSONString(value)).toString();
            try {
                file.writeFileSdcardFile(work_path+key+".cookie",data);
            }
            catch (Exception E){
                Log.i("error",E.getMessage());
            }
        }
    }
    public String parse_cookie(String url){
        String cookie="";
        url=get_url_domain(url);
        load_cookies(url);
        if(cookie_container.containsKey(url)) {
            HashMap cookie_list = (HashMap) cookie_container.get(url);
            Iterator iterator = cookie_list.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();
                String key = entry.getKey() + "";
                String value = ((HashMap) entry.getValue()).get("value") + "";
                cookie += key + "=" + value + ";";
            }
        }
        return cookie;
    }
    public String get_url_domain(String url){
        return url.replace("https://","").replace("http://","").replace("//","/").split("/")[0];
    }
    private synchronized void load_cookies(String url){
        String domain=get_url_domain(url);
        if(!cookie_container.containsKey(domain)){
            file.is_file(work_path+domain+".cookie");
            try {
                String cookies=file.readFileSdcardFile(work_path+domain+".cookie");
                HashMap hashMap=(HashMap) JSON.parseObject(cookies,HashMap.class);
                Iterator iterator = hashMap.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    String key = entry.getKey() + "";
                    HashMap value =(HashMap)JSON.parseObject(entry.getValue()+"",HashMap.class);
                    hashMap.put(key,value);
                }
                cookie_container.put(domain,hashMap);
            }
            catch (Exception e){
            }
        }
    }

    public  JSONObject get_url_cookies(String url){
        load_cookies(url);
        String domain=get_url_domain(url);
        if(cookie_container.containsKey(domain)){
            JSONObject jsonObject=new JSONObject((HashMap)cookie_container.get(domain));
            return jsonObject;
        }
        return null;
    }
    public synchronized static network singleton(Handler mhandler){
        if(!network_object.containsKey(mhandler.toString())) {
            network network=new network(mhandler);
            network_object.put(mhandler.toString(),network);
        }
        return network_object.get(mhandler.toString());
    }

}
