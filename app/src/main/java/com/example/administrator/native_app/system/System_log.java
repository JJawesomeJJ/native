package com.example.administrator.native_app.system;

import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.common.common;

/**
 * Created by Administrator on 2020/1/30 0030.
 */

public class System_log {
    /**
     * @description 日志
     * @param msg
     */
    public static void log(String msg){
        try {
            file.appendInfoToFile(config.log_path+common.get_current_time("yyyy-MM-dd")+"_log.txt","[msg]"+msg+"["+common.get_current_time("yyyy-MM-dd HH:mm:ss")+"]"+"\n");
        }
        catch (Exception e){

        }
    }

    /**
     * @description 错误日志
     * @param error
     */
    public static void log_error(String error){
        try {
            file.appendInfoToFile(config.error_path+common.get_current_time("yyyy-MM-dd")+"_error.txt","[error]"+error+"["+common.get_current_time("yyyy-MM-dd HH:mm:ss")+"]"+"\n");
        }
        catch (Exception e){

        }
    }

    /**
     * @description 将数据推送到服务器
     * @param msg
     */
    public static void push_to_server(String msg){

    }
}
