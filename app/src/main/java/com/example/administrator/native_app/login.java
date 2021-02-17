package com.example.administrator.native_app;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.administrator.native_app.activity.BaseActivity;
import com.example.administrator.native_app.activity.OnPermissionCallbackListener;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.data.run_data;
import com.example.administrator.native_app.encrypt.encrypt;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.network.localstorege;
import com.example.administrator.native_app.network.network;
import com.example.administrator.native_app.config.config;



public class login extends BaseActivity implements View.OnClickListener{
    private network network;
    private ImageView code;
    private EditText name;
    private EditText password;
    private EditText code_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        installData();
        code=(ImageView)findViewById(R.id.code);
        network=network.singleton(mhander);
        load_code();
    }
    private void initPermission() {
        onRequestPermission(new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
        }, new OnPermissionCallbackListener() {
            @Override
            public void onGranted() {
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                finish();
            }
        });
    }
    public void init() {;
        //setContentView(R.layout.activity_main);
        initPermission();
    }
    public void installData(){
        code=(ImageView)findViewById(R.id.code);
        name=(EditText)findViewById(R.id.name);
        password=(EditText)findViewById(R.id.password);
        code_input=(EditText)findViewById(R.id.code_input);
    }
    public void load_code(){
        network.get("http://www.titang.shop/code/code", new HashMap<String, String>(), new callback() {
            @Override
            public void fail(String message,String current_url) {
                Log.i("data_",message);
//                Toast.makeText(login.this,message,Toast.LENGTH_LONG).show();
            }

            @Override
            public void seccess(byte[] data,String current_url) {
                try {
                    String message=new String(data);
//                    Toast.makeText(login.this,message,Toast.LENGTH_LONG).show();
                    file.writeFileSdcardFile_Bytes(config.work_path()+"code.jpg",data,data.length);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    code.setImageBitmap(bitmap);
                }
                catch (Exception e){
                    Toast.makeText(login.this,e.toString(),Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    protected void onDestroy(){
        Toast.makeText(login.this,"stop",Toast.LENGTH_LONG).show();
        super.onDestroy();
    }
    private void login(){
        HashMap form=new HashMap();
        if(name.getText().toString().trim().length()==0){
            show("请输入用户名");
            return;
        }
        if(password.getText().toString().trim().length()==0){
            show("请输入密码");
            return;
        }
        if(code_input.getText().toString().trim().length()==0){
            show("请输入验证码");
            return;
        }
        form.put("name",name.getText()+"");
        form.put("password", encrypt.getSHA256(password.getText()+""));
        form.put("code",code_input.getText()+"");
        network.post("http://www.titang.shop/user/login", form, new callback() {
            @Override
            public void fail(String message,String url) {
                show(message);
            }

            @Override
            public void seccess(byte[] data,String url) {
                HashMap<String,String> result= (HashMap<String, String>) JSON.parseObject((new String(data)),HashMap.class);
                if(!result.get("code").equals("200")){
                    show(result.get("data"));
                    if(result.get("data").equals("code_error")){
                        load_code();
                    }
                }
                else {
                    show("欢迎你"+name.getText());
                    result.put("name",name.getText()+"");
                    localstorege.singleton().set(config.server,"user_info",JSON.toJSONString(result));
                    run_data.singleton().set("user_info",result);
                    finish();
                }
            }
        });
    }
    public void show(String message){
        Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.code:
                load_code();
                break;
            case R.id.login:
                login();
                break;
        }
    }
}