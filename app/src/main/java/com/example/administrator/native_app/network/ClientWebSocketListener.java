package com.example.administrator.native_app.network;

import android.app.Activity;
import android.net.Uri;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.alibaba.fastjson.JSONObject;
import com.example.administrator.native_app.activity.ActivityCollector;
import com.example.administrator.native_app.common.common;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.system.System_log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by Administrator on 2020/1/29 0029.
 */

public class ClientWebSocketListener{
    private final String TAG = ClientWebSocketListener.class.getSimpleName();

    private websocekt_action websocekt_action;
    protected String url="";
    private static  ClientWebSocketListener ourInstance;
    private WebSocketClient mWebSocket;
    public synchronized static ClientWebSocketListener Singleton() {
        if(ourInstance==null){
            ourInstance=new ClientWebSocketListener();
        }
        return ourInstance;
    }

    /**
     * @description 建立连接
     * @param url
     */
    public void connect(String url){
        if(mWebSocket==null) {
            mWebSocket=new WebSocketClient(URI.create(url)) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Timer timer = new Timer();
                    TimerTask timerTask = new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                send("1");//，每隔10s发送心跳包
                            }
                            catch (Exception e){
                                System_log.log_error(e.getMessage());
                            }
                        }
                    };
                    timer.schedule(timerTask,0,10);
                }

                @Override
                public void onMessage(String message) {
                    Log.i("WEBSOCEKT",message);
                    dispatch(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                }

                @Override
                public void onError(Exception ex) {
                    Log.i("exception",ex.getMessage());
                }
            };
            mWebSocket.connect();
        }
    }

    /**
     * @description 关闭连接
     */
    public void disconnect(){
        if(!url.equals("")){
            mWebSocket.close();
            mWebSocket=null;
        }
    }
    private ClientWebSocketListener() {
        websocekt_action= com.example.administrator.native_app.network.websocekt_action.SingleTon();
    }

    public void sendMessage(String message){
        mWebSocket.send(message);
    }

    public void sendMessage(byte... data){
        mWebSocket.send(data);
    }

    /**
     * @description 处理来自服务器的数据
     * @param text
     */
    protected void dispatch(String text){
        JSONObject jsonObject = JSONObject.parseObject(text);
        try {
            Method[] method = websocekt_action.getClass().getDeclaredMethods();
            for(int i=0;i<method.length;i++){
                if(method[i].getName().equals(jsonObject.get("handle")+"")){
                    Log.i("methods",method[i].getName());
                    method[i].invoke(websocekt_action,jsonObject);
                }
            }
        }
        catch (Exception e){
            Log.i("ERE",e.toString());
            System_log.log_error(e.getMessage()+e.getLocalizedMessage());
            Toast.makeText(ActivityCollector.getInstance().getTopActivity(),"UnSupport Json",Toast.LENGTH_LONG).show();
        }
    }

    public void close(int code, String reason){
        mWebSocket.close();
    }
}
