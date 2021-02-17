package com.example.administrator.native_app.config;

/**
 * Created by Administrator on 2019/10/11 0011.
 */
import com.example.administrator.native_app.file.file;
public class config {
    public static String work_path(){
        String work_path="sdcard/titang/";
        file.file_path_create(work_path);
        return work_path;
    }
    public static String server="http://www.titang.shop/";
    public static String websocketport="6003";
    public static String log_path=work_path()+"log/";
    public static String error_path=work_path()+"error/";
}
