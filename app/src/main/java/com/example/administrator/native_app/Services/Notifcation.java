package com.example.administrator.native_app.Services;

import android.app.Service;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.native_app.common.common;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.network.ClientWebSocketListener;

import java.util.Timer;
import java.util.TimerTask;

public class Notifcation extends Service {
    /** 标识服务如果被杀死之后的行为 */
    int mStartMode;

    /** 绑定的客户端接口 */
    IBinder mBinder;

    /** 标识是否可以使用onRebind */
    boolean mAllowRebind;

    /** 当服务被创建时调用. */
    @Override
    public void onCreate() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    file.writeFileSdcardFile(config.work_path()+ "run.txt",common.get_current_time("yyyy-MM-dd HH:mm:ss"));
                }
                catch (Exception e){

                }
            }
        };
        timer.schedule(timerTask,0,1000);
    }

    /** 调用startService()启动服务时回调 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return mStartMode;
    }

    /** 通过bindService()绑定到服务的客户端 */
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /** 通过unbindService()解除所有客户端绑定时调用 */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** 通过bindService()将客户端绑定到服务时调用*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** 服务不再有用且将要被销毁时调用 */
    @Override
    public void onDestroy() {
//        ClientWebSocketListener.Singleton().close(0,"service_close");
        try {
            file.writeFileSdcardFile(config.work_path()+ "close"+".txt","data");
        }
        catch (Exception e){}
    }
}
