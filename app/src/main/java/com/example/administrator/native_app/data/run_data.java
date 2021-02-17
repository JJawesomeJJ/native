package com.example.administrator.native_app.data;

import android.util.Log;

import java.lang.ref.PhantomReference;
import java.sql.Time;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2019/10/29 0029.
 */
//抽取公用的视图组件进行管理

public class run_data {
    private static run_data single;
    private String tag="run_data";
    private HashMap user_data;
    protected ConcurrentHashMap<String, Object> run_data_lock=new ConcurrentHashMap<>();
    private run_data(){
        user_data=new HashMap();
    }
    public static run_data singleton(){
        if(single==null){
            single=new run_data();
        }
        return single;
    }
    public void lock(String key){
        //为这个键加锁并添加超时时间为5秒防止死锁和死线程的出现
        while (run_data_lock.putIfAbsent(key,System.currentTimeMillis()/1000)!=null||(long)run_data_lock.putIfAbsent(key,System.currentTimeMillis()/1000)-System.currentTimeMillis()/1000<20){
            try {
                Log.i(tag,"LOCK");
                Thread.sleep(5);
            }
            catch (Exception e){
                Log.i(tag,e.getMessage());
                break;
            }
        }
        run_data_lock.remove(key);
        run_data_lock.putIfAbsent(key,System.currentTimeMillis()/1000);
    }
    public void unlock(String key){
        run_data_lock.remove(key);
    }
    public void set(String data_name,Object data_info){
        user_data.put(data_name,data_info);
    }
    public void delete(String key){
        if(user_data.containsKey(key)){
            user_data.remove(key);
        }
    }
    public Object get(String data_name){
        if(user_data.containsKey(data_name)){
            return user_data.get(data_name);
        }
        return null;
    }
}
