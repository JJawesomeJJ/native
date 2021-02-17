package com.example.administrator.native_app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baidu.panosdk.plugin.indoor.util.ScreenUtils;
import com.example.administrator.native_app.activity.BaseActivity;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.data.run_data;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.network.localstorege;
import com.example.administrator.native_app.network.network;
import com.example.administrator.native_app.view.AnnotateUtil;
import com.example.administrator.native_app.view.BindView;
import com.example.administrator.native_app.view.HorizontalListView;
import com.example.administrator.native_app.view.RoundImageView;
import com.hacknife.immersive.Immersive;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.lake.librestreaming.core.listener.RESConnectionListener;
import me.lake.librestreaming.filter.hardvideofilter.BaseHardVideoFilter;
import me.lake.librestreaming.filter.hardvideofilter.HardVideoGroupFilter;
import me.lake.librestreaming.ws.StreamAVOption;
import me.lake.librestreaming.ws.StreamLiveCameraView;
import me.lake.librestreaming.ws.filter.hardfilter.GPUImageBeautyFilter;
import me.lake.librestreaming.ws.filter.hardfilter.extra.GPUImageCompatibleFilter;

public class native_push extends BaseActivity implements View.OnClickListener{
    private String rtmpURL = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv"; //此处填写您的 rtmp 推流地址
    private boolean is_start=false;
    private Animation animation;
    private GridView gridView;
    private String type;
    private Boolean is_choose_type=false;
    private RoundImageView choose_cover;
    private StreamAVOption streamAVOption=new StreamAVOption();
    private HashMap<String,String> file_params=new HashMap<>();
    private SurfaceView mPreview;
    @BindView(id=R.id.user_header)
    public RoundImageView header;
//    @BindView(id=R.id.banner)
//    public Banner banner;  //轮播图模块
    @BindView(id=R.id.room_name)
    public TextView room_num;
    @BindView(id=R.id.stream_previewView)
    public StreamLiveCameraView streamLiveCameraView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_native_push);
        Immersive.setContentView(this, R.layout.activity_native_push, R.color.orange, R.color.green, false, false);
        AnnotateUtil.initBindView(this);
//        set_icon_size();
    }
    @Override
    public void init() {

    }

    @Override
    public void installData() {

    }
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
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
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
        animation = AnimationUtils.loadAnimation(native_push.this, R.anim.opacity);
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
            final File file_=new File(path);
            try {
                file.file_compress(path, getBaseContext(), mhander, new callback() {
                    @Override
                    public void fail(String message, String url) {

                    }

                    @Override
                    public void seccess(byte[] data, String url) {
                        try {
                            file.writeFileSdcardFile_Bytes(config.work_path()+"cache/"+file_.getName(),data,data.length);
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
                Toast.makeText(native_push.this,E.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
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
    private void init_native_type(){
        String info=null;
        if(run_data.singleton().get("native_type")!=null){
            log_list_view((List) run_data.singleton().get("native_type"));
            return;
        }
        if((info= localstorege.singleton().get(config.server,"native_type"))!=null){
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

}
