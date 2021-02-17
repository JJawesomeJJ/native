package com.example.administrator.native_app;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.example.administrator.native_app.Services.Notifcation;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.common.common;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.data.run_data;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.network.*;
import com.example.administrator.native_app.system.System_log;
import com.example.administrator.native_app.user.user;
import com.example.administrator.native_app.view.AnnotateUtil;
import com.example.administrator.native_app.view.BindView;
import com.example.administrator.native_app.view.HorizontalListView;
import com.example.administrator.native_app.view.RoundImageView;
import com.hacknife.immersive.Immersive;
import com.shuyu.gsyvideoplayer.render.effect.BrightnessEffect;
//import com.tencent.rtmp.TXLivePusher;
//import com.tencent.rtmp.ui.TXCloudVideoView;
import com.example.administrator.native_app.activity.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jp.co.cyberagent.android.gpuimage.GPUImageAddBlendFilter;
import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.ws.StreamAVOption;
import me.lake.librestreaming.ws.StreamLiveCameraView;
import me.lake.librestreaming.ws.filter.hardfilter.GPUImageBeautyFilter;
import me.lake.librestreaming.ws.filter.hardfilter.extra.GPUImageCompatibleFilter;


public class MainActivity extends BaseActivity implements View.OnClickListener{
    private ArrayList<String> list_path;
    private ArrayList<String> list_title;
    private String licenceURL = "http://license.vod2.myqcloud.com/license/v1/f792062a930284df5541d047a350f215/TXLiveSDK.licence"; // 获取到的 licence url
    private String licenceKey = "b1f94ecbf41b51901565b12aaf2024d8"; // 获取到的 licence key
//    TXLivePusher mLivePusher;//推流对象
//    private TXCloudVideoView mPusherView;//摄像头对象
    private String rtmpURL = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv"; //此处填写您的 rtmp 推流地址
    private boolean is_start=false;
    private Animation animation;
    private GridView gridView;
    private String type;
    private TextView room_num;
    private Boolean is_choose_type=false;
    private RoundImageView choose_cover;
    private StreamLiveCameraView streamLiveCameraView;
    private StreamAVOption streamAVOption=new StreamAVOption();
    private HashMap<String,String> file_params=new HashMap<>();
    private SurfaceView mPreview;
    @BindView(id=R.id.user_header)
    public RoundImageView header;
//    @BindView(id=R.id.banner)
//    public Banner banner;  //轮播图模块
    @BindView(id=R.id.webview1)
    public WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Immersive.setContentView(this, R.layout.activity_main, R.color.orange, R.color.green, false, false);
        AnnotateUtil.initBindView(this);
        load_url();
        set_icon_size();
        installData();
        init_header();
        file.file_path_create(config.work_path()+"cache");
        file.file_path_create(config.log_path);
        file.file_path_create(config.error_path);
//        startService(new Intent(getBaseContext(), Notifcation.class));
        ClientWebSocketListener.Singleton().connect(config.server.substring(0,config.server.length()-1)+":6003");
    }
    public void login(){
        Intent intent=new Intent(MainActivity.this, login.class);
        startActivity(intent);
    }
    public void show(String message){
        Toast.makeText(getBaseContext(),message,Toast.LENGTH_LONG).show();
    }
    private void initPermission() {
        onRequestPermission(new String[]{
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.CAMERA
        }, new OnPermissionCallbackListener() {
            @Override
            public void onGranted() {
                user.is_login(mhander, new callback() {
                    @Override
                    public void fail(String message, String url) {
                        Log.i("user_info",message);
                        login();
                    }
                    @Override
                    public void seccess(byte[] data, String url) {
                        show("user");
                        run_data.singleton().set("user_info",JSONObject.parse(localstorege.singleton().get(config.server,"user_info")));
                        init_head_img();
                    }
                });
            }

            @Override
            public void onDenied(List<String> deniedPermissions) {
                finish();
            }
        });
    }
    public void init() {;
        initPermission();
    }
    public void load_url(){
        webView=(WebView)findViewById(R.id.webview1);
        webView.setWebViewClient(new WebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        webView.getSettings().setAppCachePath(config.work_path()+"cache");
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.addJavascriptInterface(this,"native_push");
        webView.loadUrl("http://www.titang.shop/native/index");
//        common.setCookie("http://www.titang.shop/native/index",mhander,webView);
    }
    public void rotate(){
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }
    public void installData(){
        room_num=(TextView)findViewById(R.id.room_name);
        streamLiveCameraView=(StreamLiveCameraView)findViewById(R.id.stream_previewView);
        header=(RoundImageView)findViewById(R.id.user_header);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.photo:
//                init_player();
//                init_view();
                showDialog();
                break;
            case R.id.online:
//                Intent intent=new Intent();
//                intent.setClass(getApplicationContext(),native_view.class);
//                startActivityForResult(intent,101);//第二个参数为request_code，是一个整型的识别码，自己随便写，主要用于和其它intent发出的请
                break;
        }
    }
    protected void rtmp_switch(){
        if(!is_start) {
            streamAVOption.streamUrl=rtmpURL.trim();
            streamLiveCameraView.init(this, streamAVOption);
            streamLiveCameraView.addStreamStateListener(new RESConnectionListener() {
                @Override
                public void onOpenConnectionResult(int i) {

                }

                @Override
                public void onWriteError(int i) {

                }

                @Override
                public void onCloseConnectionResult(int i) {

                }
            });
            LinkedList<BaseHardVideoFilter> files = new LinkedList<>();
            files.add(new GPUImageCompatibleFilter(new GPUImageBeautyFilter()));
//f
            streamLiveCameraView.setHardVideoFilter(new HardVideoGroupFilter(files));
            streamLiveCameraView.startStreaming(rtmpURL);
//            int ret = mLivePusher.startPusher(rtmpURL.trim());
//            if (ret == -5) {
//                Log.i("test", "startRTMPPush: license 校验失败");
//                is_start=false;
//                Toast.makeText(MainActivity.this,"验证失败",Toast.LENGTH_SHORT).show();
//            }
            is_start=true;
        }else {
//            mLivePusher.stopPusher();
//            mLivePusher.stopCameraPreview(true); //如果已经启动了摄像头预览，请在结束推流时将其关闭。
//            is_start=false;
        }
    }
    private void showDialog(){
        final View view = LayoutInflater.from(this).inflate(R.layout.showdialog,null,false);
        gridView=view.findViewById(R.id.list_view);
        final AlertDialog dialog = new AlertDialog.Builder(this).setView(view).create();
        final Button btn_cancel_high_opion = view.findViewById(R.id.btn_cancel_high_opion);
        final Button btn_agree_high_opion = view.findViewById(R.id.btn_agree_high_opion);
        final LinearLayout choose_img_container=view.findViewById(R.id.choose_picture_container);
        final LinearLayout choose_type=view.findViewById(R.id.choose_type);
        choose_cover=view.findViewById(R.id.choose_picture);
        choose_cover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        });
        btn_cancel_high_opion.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        btn_agree_high_opion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btn_agree_high_opion.getText().equals("下一步")) {
                    choose_type.setVisibility(View.GONE);
                    choose_img_container.setVisibility(View.VISIBLE);
                    btn_agree_high_opion.setText("开始直播");
                }
                else {
                    HashMap params=new HashMap();
                    params.put("type", type);
                    network.singleton(mhander).post(config.server + "native/start", params,file_params, new callback() {
                        @Override
                        public void fail(String message, String url) {

                        }
                        @Override
                        public void seccess(byte[] data, String url) {
                            String info = new String(data);
                            Log.i("url_message", info);
                            JSONObject jsonObject = JSON.parseObject(info);
                            rtmpURL = jsonObject.get("rtmp_url") + "";
                            room_num.setText(jsonObject.get("room") + "");
                            dialog.dismiss();
                            rtmp_switch();
                        }
                    });
                }
            }
        });
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.opacity);
        Window window = dialog.getWindow();
        //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.AppTheme);
        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的7/8 注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/8*7), LinearLayout.LayoutParams.WRAP_CONTENT);
        init_native_type();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String path="";
            Uri uri = data.getData();
            Cursor cursor = getContentResolver().query(uri, null, null, null,null);
            if (cursor != null && cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            }
            final  File file_=new File(path);
            try {
                file.file_compress(path, getBaseContext(), mhander, new callback() {
                    @Override
                    public void fail(String message, String url) {

                    }

                    @Override
                    public void seccess(byte[] data, String url) {
                        try {
                            file.writeFileSdcardFile_Bytes(config.work_path()+"cache/"+file_.getName(),data,data.length);
                            show("ok");
                            file_params.put("cover",config.work_path()+"cache/"+file_.getName());
                        }
                        catch (Exception E){

                        }
                    }
                });
                Bitmap bitmap=file.file_to_bitmap(path);
                choose_cover.setImageBitmap(bitmap);
            }
            catch (Exception E)
            {
                Toast.makeText(MainActivity.this,E.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
    private void init_native_type(){
        String info=null;
        show("load");
        if(run_data.singleton().get("native_type")!=null){
            log_list_view((List) run_data.singleton().get("native_type"));
            return;
        }
        if((info=localstorege.singleton().get(config.server,"native_type"))!=null){
            final JSONObject local=JSON.parseObject(info);
            final HashMap params=new HashMap();
            params.put("version",true);
            network.singleton(mhander).get(config.server + "native/type", params, new callback() {
                @Override
                public void fail(String message, String url) {

                }
                @Override
                public void seccess(byte[] data, String url) {
                    HashMap response=JSON.parseObject((new String(data)),HashMap.class);
                    if(!response.get("version").equals(local.get("version"))){
                        update_native_type();
                    }else {
                        run_data.singleton().set("native_type",local.get("data"));
                        log_list_view((JSONArray)local.get("data"));
                    }
                }
            });
        }
        else {
            update_native_type();
        }
    }
    private void update_native_type(){
        network.singleton(mhander).get(config.server+"native/type", new HashMap<String, String>(), new callback() {
            @Override
            public void fail(String message, String url) {

            }

            @Override
            public void seccess(byte[] data, String url) {
                String info=new String(data);
                Log.i("resre",info);
                JSONObject type_list=JSON.parseObject(info);
                run_data.singleton().set("native_type",type_list);
                localstorege.singleton().set(url,"native_type",info);
                log_list_view((JSONArray)type_list.get("data"));
            }
        });
    }
    public void log_list_view(final List data){
        BaseAdapter adapter = new BaseAdapter() {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final JSONObject hashMap=(JSONObject) data.get(position);
                View layout=View.inflate(getBaseContext(), R.layout.choose, null);
                final RoundImageView face = (RoundImageView) layout.findViewById(R.id.src);
                TextView name =(TextView)layout.findViewById(R.id.name);
                if(file.is_file(config.work_path()+"cache/"+hashMap.get("name")+".jpg")){
                    try {
                        Bitmap bitmap=file.file_to_bitmap(config.work_path()+"cache/"+hashMap.get("name")+".jpg");
                        face.setImageBitmap(bitmap);
                    }
                    catch (Exception E){

                    }
                }
                else {
                    network.singleton(mhander).get(hashMap.get("url")+"", new HashMap<String, String>(), new callback() {
                        @Override
                        public void fail(String message, String url) {
                            face.setImageResource(R.drawable.lol);
                        }

                        @Override
                        public void seccess(byte[] data, String url) {
                            Log.i("data_url",url);
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                                file.writeFileSdcardFile_Bytes(config.work_path()+"cache/"+hashMap.get("name")+".jpg",data,data.length);
                                face.setImageBitmap(bitmap);
                            }
                            catch (Exception E){
                                Log.i("ERROR_URL",url);
                                face.setImageResource(R.drawable.lol);
                            }
                        }
                    });
                }
                name.setText(hashMap.get("name")+"");
                return layout;
            }

            @Override
            public long getItemId(int position) {
                // TODO 自动生成的方法存根
                return position;
            }

            @Override
            public Object getItem(int position) {
                // TODO 自动生成的方法存根
                return data.get(position);
            }

            @Override
            public int getCount() {
                // TODO 自动生成的方法存根
                return data.size();
            }
        };///new BaseAdapter()
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id)
            {
                Map map=(Map) data.get(position);
                type=map.get("type")+"";
            }
        });
    }
    @JavascriptInterface
    public void start(){
        Intent intent=new Intent(getApplicationContext(), native_push.class);
        startActivity(intent);
    }
    //加载头像
    private void init_head_img(){
        if(run_data.singleton().get("user_info")!=null){
            Map user_info=(Map) run_data.singleton().get("user_info");
            file.load_network_file(user_info.get("head_img")+"",mhander,header);
        }
    }
    
    public void test(){
        show("test");
    }
    //加载顶部的
    private void init_header(){
        //加载顶部直播类型
        if(run_data.singleton().get("native_list")==null){
            network.singleton(mhander).get(config.server + "native/online_type", null, new callback() {
                @Override
                public void fail(String message, String url) {

                }
                @Override
                public void seccess(byte[] data, String url) {
                    JSONArray native_list=JSONArray.parseArray(new String(data));
                    run_data.singleton().set("native_list",native_list);
                    init_native_type_view(native_list);
                }
            });
        }
        else {
            init_native_type_view((List) run_data.singleton().get("native_list"));
        }
    }
    //设置搜索栏的样式
    private void set_icon_size(){
        EditText etUserName = (EditText) findViewById(R.id.serach);
        Drawable serach = getResources().getDrawable(R.drawable.serach);
        Drawable scan = getResources().getDrawable(R.drawable.scan);
        scan .setBounds(0, 0, 50, 50);//第一个 0 是距左边距离，第二个 0 是距上边距离，40 分别是长宽
        serach .setBounds(0, 0, 60, 60);//第一个 0 是距左边距离，第二个 0 是距上边距离，40 分别是长宽
        etUserName.setCompoundDrawables(serach , null, scan, null);//只放左边
    }
    //设置顶部直播类型
    private void init_native_type_view(final List data){
        HorizontalListView listView = (HorizontalListView) findViewById(R.id.native_list);
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return data.size();
            }

            @Override
            public Object getItem(int position) {
                return data.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View layout=View.inflate(getBaseContext(), R.layout.native_type_list, null);
                TextView title =(TextView)layout.findViewById(R.id.title);
                title.setText(data.get(position)+"");
                return layout;
            }
        });
    }
    @JavascriptInterface
    public void start_player(String play_url,String push_url,String playerid){
        Intent intent=new Intent();
        Bundle bundle=new Bundle();
        bundle.putString("url",play_url);
        bundle.putString("rtmp_url",push_url);
        bundle.putString("playerid",playerid);
        intent.putExtras(bundle);
        intent.setClass(this, play_native.class);
        startActivity(intent);
    }
    @Override
    public void onDestroy(){
        System_log.log("process has been closed at"+common.get_current_time("yyyy-MM-dd HH:mm:ss"));
        ClientWebSocketListener.Singleton().close(0,"shutdown");
        super.onDestroy();
    }
}
