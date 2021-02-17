package com.example.administrator.native_app;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import com.example.administrator.native_app.view.RoundImageView;
import com.hacknife.immersive.Immersive;

import java.io.File;
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

public class start_native extends BaseActivity implements View.OnClickListener{
    @BindView(id=R.id.stream_previewView)
    public StreamLiveCameraView streamLiveCameraView;
    private String rtmpURL = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv"; //此处填写您的 rtmp 推流地址
    private boolean is_start=false;
    private Animation animation;
    private RoundImageView choose_cover;
    private StreamAVOption streamAVOption=new StreamAVOption();
    private HashMap<String,String> file_params=new HashMap<>();
    private String type;
    @BindView(id=R.id.room_name)
    public TextView room_num;
    public GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_native);
        AnnotateUtil.initBindView(this);
        Immersive.setContentView(this, R.layout.activity_main, R.color.black, R.color.green, false, false);
    }

    @Override
    public void init() {

    }

    @Override
    public void installData() {

    }

    @Override
    public void onClick(View v) {

    }
    @Override
    protected void onPause(){
        super.onPause();
        if(streamLiveCameraView!=null&&streamLiveCameraView.isStreaming()){
            streamLiveCameraView.stopStreaming();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        if(streamLiveCameraView!=null&&!streamLiveCameraView.isStreaming()){
            streamLiveCameraView.startStreaming(rtmpURL);
        }
        super.onRestart();
    }
    private void init_native_type(){
        String info=null;
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
        animation = AnimationUtils.loadAnimation(getApplication(), R.anim.opacity);
        Window window = dialog.getWindow();
        //此处可以设置dialog显示的位置
        window.setWindowAnimations(R.style.AppTheme);
        dialog.show();
        //此处设置位置窗体大小，我这里设置为了手机屏幕宽度的7/8 注意一定要在show方法调用后再写设置窗口大小的代码，否则不起效果会
        dialog.getWindow().setLayout((ScreenUtils.getScreenWidth(this)/8*7), LinearLayout.LayoutParams.WRAP_CONTENT);
        init_native_type();
    }
    protected void rtmp_switch(){
        if(!is_start) {
//            streamLiveCameraView = (StreamLiveCameraView) findViewById(R.id.stream_previewView);

            //参数配置 start
            streamAVOption = new StreamAVOption();
            streamAVOption.streamUrl = rtmpURL;
            //参数配置 end

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
            //设置滤镜组
            LinkedList<BaseHardVideoFilter> files = new LinkedList<>();
            files.add(new GPUImageCompatibleFilter(new GPUImageBeautyFilter()));
            files.add(new GPUImageCompatibleFilter(new GPUImageAddBlendFilter()));
            streamLiveCameraView.setHardVideoFilter(new HardVideoGroupFilter(files));
            streamLiveCameraView.startStreaming(rtmpURL);
            is_start=true;
        }else {

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
                Toast.makeText(getApplication(),E.getMessage(),Toast.LENGTH_LONG).show();
            }
        }
    }
}
