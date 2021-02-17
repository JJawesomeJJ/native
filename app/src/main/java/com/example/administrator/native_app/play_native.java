package com.example.administrator.native_app;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.example.administrator.native_app.activity.BaseActivity;
import com.example.administrator.native_app.activity.OnPermissionCallbackListener;
import com.example.administrator.native_app.callback.callback;
import com.example.administrator.native_app.common.AndroidBug5497Workaround;
import com.example.administrator.native_app.common.common;
import com.example.administrator.native_app.config.config;
import com.example.administrator.native_app.file.file;
import com.example.administrator.native_app.native_view.design_native;
import com.example.administrator.native_app.network.ClientWebSocketListener;
import com.example.administrator.native_app.network.localstorege;
import com.example.administrator.native_app.network.network;
import com.example.administrator.native_app.system.System_log;
import com.example.administrator.native_app.view.AnnotateUtil;
import com.example.administrator.native_app.view.BindView;
import com.hacknife.immersive.Immersive;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
//import com.tencent.ijk.media.player.IjkMediaPlayer;
//import com.tencent.rtmp.TXLivePlayer;
//import com.tencent.rtmp.ui.TXCloudVideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class play_native extends BaseActivity {
//    private TXLivePlayer mLivePlayer;
//    private TXCloudVideoView mView;
    String flvUrl = "";
    Boolean is_play=true;//是否开始直播
    String push_url="";
    String playerid;
    private design_native videoPlayer;
    @BindView(id=R.id.barrage)
    public WebView barrage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_native);
        Immersive.setContentView(this, R.layout.activity_play_native, R.color.black_true, R.color.green, false, false);
        AnnotateUtil.initBindView(this);
        Bundle bundle = this.getIntent().getExtras();
        //接收name值
        flvUrl = bundle.getString("url");
        push_url=bundle.getString("rtmp_url");
        playerid=bundle.getString("playerid");
        init_barrage();
        init_player(flvUrl);
        HashMap<String,String> user_info=new HashMap();
        add_channel(push_url);
        AndroidBug5497Workaround.assistActivity(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);   //》》我用的这个
    }
    @Override
    public void installData() {

    }
    @Override
    public void init() {

    }
    public void init_barrage(){
        barrage.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                String fd=localstorege.singleton().get(config.server,"fd");
                String _token=localstorege.singleton().get(config.server,"_token");
                final int version = Build.VERSION.SDK_INT;
// 因为evaluateJavascript方法在 Android 4.4 版本才可使用，所以使用时需进行版本判断
                if (version < 18) {
                   barrage.loadUrl(String.format("javascript:init('%s','%s','%s')",fd,_token,push_url));
                } else {
                    barrage.evaluateJavascript(String.format("javascript:init('%s','%s','%s')",fd,_token,push_url), new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            //此处为 js 返回的结果
                        }
                    });
                }
                common.setCookie(config.server+"native/page",mhander,barrage);
            }
        });
        barrage.getSettings().setJavaScriptEnabled(true);
        barrage.getSettings().setDomStorageEnabled(true);
        barrage.getSettings().setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        barrage.getSettings().setAppCachePath(config.work_path()+"cache");
        barrage.getSettings().setAllowFileAccess(true);
        barrage.getSettings().setAppCacheEnabled(true);
        barrage.loadUrl(config.server+"native/page?playerid="+String.valueOf(playerid));
    }
    public void init_player(String flvUrl){
//            barrage.setBackgroundColor(0); // 设置背景色
            barrage.setBackgroundColor(0x00000000);
//            barrage.getBackground().setAlpha(0); // 设置填充透明度 范围：0-255
            videoPlayer =  (design_native) findViewById(R.id.videoPlayer);
            /**此中内容：优化加载速度，降低延迟*/
            VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_transport", "tcp");
            List<VideoOptionModel> list = new ArrayList<>();
            list.add(videoOptionModel);
            videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "rtsp_flags", "prefer_tcp");
            list.add(videoOptionModel);
            //  关闭播放器缓冲，这个必须关闭，否则会出现播放一段时间后，一直卡主，控制台打印 FFP_MSG_BUFFERING_START
            videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);
            list.add(videoOptionModel);
            GSYVideoManager.instance().setOptionModelList(list);
            String source1 = "http://5815.liveplay.myqcloud.com/live/5815_89aad37e06ff11e892905cb9018cf0d4_900.flv";
            videoPlayer.setUp(flvUrl, false, "");
            videoPlayer.setIsTouchWiget(true);
            videoPlayer.startPlayLogic();
            videoPlayer.hidden_progress();

//            videoPlayer.startWindowFullscreen(this,false,false);
            barrage.bringToFront();
    }
    public void send_barrage(String msg){
//        network.singleton(mhander).post();
        ClientWebSocketListener.Singleton().sendMessage();
    }
    public void switch_(){
        if(is_play) {

        }else {

        }
    }
    @Override
    public void onDestroy() {
        barrage.destroy();
        leave(flvUrl);
        GSYVideoManager.onPause();
        super.onDestroy();

//        mView.onDestroy();
    }
    @Override
    public void onPause(){
        GSYVideoManager.onPause();
        super.onPause();
    }
    @Override
    public void onResume(){
        super.onResume();
        GSYVideoManager.onResume();
    }
    public void push(String msg){
        toast(msg);
    }

    /**
     * @description 用户离开频道
     * @param channel_name
     */
    public void leave(String channel_name){
        HashMap<String,String> params=new HashMap<>();
        params.put("channel_name",channel_name);
        params.put("_token", localstorege.singleton().get(config.server,"_token"));
        params.put("fd", localstorege.singleton().get(config.server,"fd"));
        network.singleton(mhander).post(config.server + "/channel/leave", params, new callback() {
            @Override
            public void fail(String message, String url) {

            }

            @Override
            public void seccess(byte[] data, String url) {
//                JSONObject result=JSON.parseObject(data.toString());
            }
        });
    }

    /**
     * @description 用户加入频道
     * @param channel_name
     */
    public void add_channel(String channel_name){
        HashMap<String,String> params=new HashMap<>();
        params.put("channel_name",channel_name);
        params.put("_token", localstorege.singleton().get(config.server,"_token"));
        params.put("fd", localstorege.singleton().get(config.server,"fd"));
        network.singleton(mhander).post(config.server + "/channel/native", params, new callback() {
            @Override
            public void fail(String message, String url) {
            }

            @Override
            public void seccess(byte[] data, String url) {
                Log.i("postpost",new String(data));
//                JSONObject result=JSON.parseObject(data.toString());
            }
        });
    }
}
