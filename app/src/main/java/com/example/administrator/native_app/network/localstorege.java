package com.example.administrator.native_app.network;

import android.os.Bundle;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.file.file;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2019/10/13 0013.
 */

public class localstorege {
    private String localstorege_work_path;
    private HashMap<String,HashMap<String,String>> localstorege=new HashMap<>();
    private static localstorege localstorege_object;
    private localstorege(){
        localstorege_work_path= config.work_path()+"localstorege";
        file.file_path_create(localstorege_work_path);
        localstorege_work_path=localstorege_work_path+"/";
    }
    private String get_url_domain(String url){
        return url.replace("https://","").replace("http://","").split("/")[0];
    }
    public void set(String url,String key,String value){
        String domian=get_url_domain(url);
        load_localstorege(url);
        HashMap<String,String> local_url=localstorege.get(domian);
        local_url.put(key,value);
        localstorege.put(domian,local_url);
        store_localstorege(domian);
    }
    public void delete(String url,String key){
        load_localstorege(url);
        HashMap<String,String> local_url=localstorege.get(get_url_domain(url));
        if(local_url.containsKey(key)) {
            local_url.remove(key);
        }
        localstorege.put(get_url_domain(url),local_url);
        store_localstorege(get_url_domain(url));
    }
    public String get(String url,String key){
        load_localstorege(get_url_domain(url));
        if(!localstorege.containsKey(get_url_domain(url))){
            return null;
        }
        HashMap<String,String> url_localstorege=localstorege.get(get_url_domain(url));
        if(url_localstorege.containsKey(key)){
            return url_localstorege.get(key);
        }
        return null;
    }
    private void load_localstorege(String url){
        String domain=get_url_domain(url);
        if(!localstorege.containsKey(domain)){
            if(file.is_file(localstorege_work_path+domain+".localstorege")){
                try {
                    HashMap<String,String> data = JSON.parseObject(file.readFileSdcardFile(localstorege_work_path + domain + ".localstorege"),HashMap.class);
                    localstorege.put(domain,data);
                }
                catch (Exception e){
                    Log.i("localstorege_error",e.getMessage());
                    e.printStackTrace();
                }
            }else {
                localstorege.put(domain,new HashMap<String, String>());
            }
        }
    }
    private void store_localstorege(String url){
        String domain=get_url_domain(url);
        String data=JSON.toJSONString(localstorege.get(domain));
        try {
            file.writeFileSdcardFile(localstorege_work_path+domain+".localstorege",data);
        }
        catch (Exception E){
            Log.e("localstorege_error",E.getMessage());
        }
    }
    public static localstorege singleton() {
        if (localstorege_object==null){
            localstorege_object=new localstorege();
        }
        return localstorege_object;
    }
}
