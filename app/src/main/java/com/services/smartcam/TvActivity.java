package com.services.smartcam;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.style.IOSStyle;
import com.monkeyliu.smartfocus.AutoFocusFrameLayout;
import com.monkeyliu.smartfocus.ColorFocusBorder;
import com.services.smartcam.EntityClass.EzToken;
import com.services.smartcam.EntityClass.Mqtt;
import com.services.smartcam.EntityClass.TimeInfo;
import com.services.smartcam.InterFace.EzTokenApi;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvActivity extends AppCompatActivity implements View.OnClickListener, WindowSizeChangeNotifier.OnWindowSizeChangedListener, EZUIPlayer.EZUIPlayerCallBack {
    //Mqtt
    private final String TAG = "TvMqtt";
    /* 自动Topic, 用于上报消息 */
    final private String PUB_TOPIC = "anfang";
    /* 自动Topic, 用于接受消息 */
    final private String SUB_TOPIC = "machineStatus";

    final String host = "tcp://1.14.68.248:1883";
    private String clientId = "MEIZU 16s Pro";
    private String userName = "username";
    private String passWord = "123456";
    private String publishContent;//发送的消息
    private String nowStatus;//订阅消息
    private boolean nodeMCU = false;//设置nodeMCU默认为离线
    private boolean first = true;//判断是否app第一次打开中间变量
    private boolean device = false;
    private String receiveContent = null;//订阅nodeMCU接收到的消息

    MqttAndroidClient mqttAndroidClient;

    private Mqtt machine = new Mqtt();
    private Date date;
    private TimeInfo timeInfo;
    private String time;

    public Gson gson = new Gson();
    private Integer ElevatorStatus = 1;
    private Integer LightStatus = 0;
    //
    private TextView date_tv_text;
    //实列化layout
    private View elevatorLayout;
    private View lightLayout;
    private View nodeMcuLayout;
    private View recLayout;
    //图标
    private ImageView elevatorImg;
    private ImageView lightImg;
    private ImageView nodeMcuImg;
    private ImageView recImg;
    private ImageView elevatorStatus;
    private ImageView lightStatus;
    private ImageView nodeMCUStatus;
    private ImageView recStatus;
    public EZUIPlayer mPlayer;
    private ImageView stop;
    private SharedPreferences sp;

    /**
     * onresume时是否恢复播放
     */
    private boolean isResumePlay = false;

    private TvActivity.MyOrientationDetector mOrientationDetector;

    /**
     *  开发者申请的Appkey
     */
    private String appkey = "b5ad3b35cb054865a96e58046bc00716";
    private String appSecret = "b3e23f9aeea28642ecfa59ea9e924273";
    /**
     *  授权accesstoken
     */
    private String accesstoken = "at.21lftjwn4f30u2360n53nmfra9oa8i5p-3nqdswxoix-0r2n2o6-obnusch6p";
    /**
     *  播放url：ezopen协议
     */
    private String playUrl = "ezopen://open.ys7.com/172124816/1.hd.live";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tv);

        //操作事件
        date_tv_text = findViewById(R.id.text_tv_date);
        //实例化layout布局
        elevatorLayout = findViewById(R.id.elevator_tv_Layout);
        lightLayout = findViewById(R.id.light_tv_Layout);
        nodeMcuLayout = findViewById(R.id.nodeMCU_tv_Layout);
        recLayout = findViewById(R.id.rec_tv_Layout);
        //监听事件
        elevatorLayout.setOnClickListener(this);
        lightLayout.setOnClickListener(this);
        nodeMcuLayout.setOnClickListener(this);
        recLayout.setOnClickListener(this);
        //图标
        elevatorImg = findViewById(R.id.elevator_tv_mg);
        lightImg = findViewById(R.id.light_tv_img);
        nodeMcuImg = findViewById(R.id.nodeMcu_tv_img);
        recImg = findViewById(R.id.rec_tv_img);
        //圆点指示图标
        elevatorStatus = findViewById(R.id.elevator_status_tv_img);
        lightStatus = findViewById(R.id.light_status_tv_img);
        nodeMCUStatus = findViewById(R.id.nodeMCU_status_tv_img);
        recStatus = findViewById(R.id.rec_status_tv_img);
        //初始化DialogX并设置主题
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();//设置为IOS主题
        //设置为IOS主题
        DialogX.onlyOnePopTip = true;

        getDeviceInfo();
        checked();
        getPermission();//获取sdcard读写权限
        setElevatorStatus();
        setLightStatus();
        rec();
        MqttInit();
        Clear();


        if (TextUtils.isEmpty(appkey)
                || TextUtils.isEmpty(accesstoken)
                || TextUtils.isEmpty(playUrl)){
            Toast.makeText(this,"appkey,accesstoken or playUrl is null",Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        mOrientationDetector = new TvActivity.MyOrientationDetector(this);
        new WindowSizeChangeNotifier(this, this);
        //mBtnPlay = (Button) findViewById(R.id.play);

        //获取EZUIPlayer实例
        mPlayer = (EZUIPlayer) findViewById(R.id.player_tv_ui1);
        //
        stop = findViewById(R.id.stop);
        //设置加载需要显示的view
        mPlayer.setLoadingView(initProgressBar());
        stop.setOnClickListener(this);
        mPlayer.setOnClickListener(this);
        preparePlay();
        setSurfaceSize();

    }

    public void checked(){
        //自定义焦点框的效果
        AutoFocusFrameLayout autoFocusFrameLayout = findViewById(R.id.focus_test);
        autoFocusFrameLayout.setFocusBorderBuilder(new ColorFocusBorder.Builder(this)
                .borderWidth(4) //border宽度
                .borderColor(Color.WHITE) //border颜色
                .borderRadius(100) //border圆角半径
                //.shadowWidth(45) //shadow半径
                //.shadowColor(Color.WHITE) //shadow颜色
                .padding(0) //内边距
                .scaleX(1.1f) //X方向缩放倍数
                .scaleY(1.1f) //Y方向缩放倍数
                //.enableShimmer()//使用闪光特效
                );
    }

    /**
     * 状态栏管理
     * */
    public void Clear(){
        ImmersionBar.with(TvActivity.this)
                .transparentStatusBar()  //透明状态栏，不写默认透明色
                .navigationBarColor(R.color.white)
                .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                .init();

        //隐藏action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }


    /**
     * 创建加载view
     * @return
     */
    private View initProgressBar() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(lp);
        RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);//addRule参数对应RelativeLayout XML布局的属性
        ProgressBar mProgressBar = new ProgressBar(this);
        mProgressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress));
        relativeLayout.addView(mProgressBar,rlp);
        return relativeLayout;
    }

    /**
     * 准备播放资源参数
     */
    private void preparePlay(){
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);
        accesstoken = sp.getString("newEzToken", "");
        if (accesstoken.isEmpty()){
            //发起网络请求获取新token
            EzTokenApi api = NetworkFactory.ezToken();
            Call<EzToken> R = api.getToken(appkey,appSecret);
            R.enqueue(new Callback<EzToken>() {
                @Override
                public void onResponse(Call<EzToken> call, Response<EzToken> response) {
                    EzToken ezToken = response.body();
                    String newToken = ezToken.getData().getAccessToken();
                    Log.e(TAG, "为空onResponse: >>>>>>>>token"+newToken);
                    SharedPreferences.Editor editor = sp.edit();
                    //记住用户名、密码、
                    editor.putString("newEzToken", newToken);
                    editor.apply();

                    preparePlay();
                }
                @Override
                public void onFailure(Call<EzToken> call, Throwable t) {

                    preparePlay();
                }
            });

        }else {
            //设置debug模式，输出log信息
            EZUIKit.setDebug(true);
            //appkey初始化
            EZUIKit.initWithAppKey(this.getApplication(), appkey);
            //设置授权accesstoken
            EZUIKit.setAccessToken(accesstoken);
            //设置播放资源参数
            mPlayer.setCallBack((EZUIPlayer.EZUIPlayerCallBack) this);
            mPlayer.setUrl(playUrl);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        mOrientationDetector.enable();
        Log.d(TAG,"onResume");
        //界面stop时，如果在播放，那isResumePlay标志位置为true，resume时恢复播放
        if (isResumePlay) {
            isResumePlay = false;
            mPlayer.startPlay();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mOrientationDetector.disable();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop + "+mPlayer.getStatus());
        //界面stop时，如果在播放，那isResumePlay标志位置为true，以便resume时恢复播放
        if (mPlayer.getStatus() != EZUIPlayer.STATUS_STOP) {
            isResumePlay = true;
        }
        //停止播放
        mPlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");

        //释放资源
        mPlayer.releasePlayer();
    }

    @Override
    public void onPlaySuccess() {
        Log.d(TAG,"onPlaySuccess");
        //TipDialog.show("播放成功", WaitDialog.TYPE.SUCCESS);
        // TODO: 2017/2/7 播放成功处理

    }

    @Override
    public void onPlayFail(EZUIError error) {
        preparePlay();
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);

        Log.d(TAG,"onPlayFail");
        // TODO: 2017/2/21 播放失败处理
        if (error.getErrorString().equals(EZUIError.UE_ERROR_INNER_VERIFYCODE_ERROR)){

        }else if(error.getErrorString().equalsIgnoreCase(EZUIError.UE_ERROR_NOT_FOUND_RECORD_FILES)){
            // TODO: 2017/5/12
            //未发现录像文件
            Toast.makeText(this,"未找到",Toast.LENGTH_LONG).show();
        }

        if (error.getErrorString().equals(EZUIError.UE_ERROR_ACCESSTOKEN_ERROR_OR_EXPIRE)){
            WaitDialog.show("accesstoken异常或失效");
            WaitDialog.show("正在获取accesstoken");

            //发起网络请求获取新token
            EzTokenApi api = NetworkFactory.ezToken();
            Call<EzToken> R = api.getToken(appkey,appSecret);
            R.enqueue(new Callback<EzToken>() {
                @Override
                public void onResponse(Call<EzToken> call, Response<EzToken> response) {
                    EzToken ezToken = response.body();
                    String newToken = ezToken.getData().getAccessToken();
                    if (!newToken.isEmpty()){
                        TipDialog.show("获取成功", WaitDialog.TYPE.SUCCESS);
                        Log.e(TAG, "onResponse: >>>>>>>>token"+newToken);
                        SharedPreferences.Editor editor = sp.edit();
                        //记住用户名、密码、
                        editor.putString("newEzToken", newToken);
                        editor.apply();
                        preparePlay();
                    }else {
                        TipDialog.show("获取失败", WaitDialog.TYPE.ERROR);
                    }

                }


                @Override
                public void onFailure(Call<EzToken> call, Throwable t) {
                    TipDialog.show("网络出错", WaitDialog.TYPE.ERROR);

                }
            });


        }
    }

    @Override
    public void onVideoSizeChange(int width, int height) {
        // TODO: 2017/2/16 播放视频分辨率回调
        Log.d(TAG,"onVideoSizeChange  width = "+width+"   height = "+height);
    }

    @Override
    public void onPrepared() {
        Log.d(TAG,"onPrepared");
        //播放
        mPlayer.startPlay();
    }

    @Override
    public void onPlayTime(Calendar calendar) {
        Log.d(TAG,"onPlayTime");
        if (calendar != null) {
            // TODO: 2017/2/16 当前播放时间
            Log.d(TAG,"onPlayTime calendar = "+calendar.getTime().toString());
        }
    }

    @Override
    public void onPlayFinish() {
        // TODO: 2017/2/16 播放结束
        Log.d(TAG,"onPlayFinish");
    }

    @Override
    public void onClick(View view) {
        if (view != mPlayer) {
            if(view == stop){
                stop.setVisibility(View.INVISIBLE);
                mPlayer.startPlay();
            }
        } else {
            if (mPlayer.getStatus() == EZUIPlayer.STATUS_PLAY){
                stop.setVisibility(View.VISIBLE);
                mPlayer.stopPlay();
            }
        }
    }


    /**
     * 屏幕旋转时调用此方法
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged");
        setSurfaceSize();
    }

    private void setSurfaceSize(){
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        boolean isWideScrren = mOrientationDetector.isWideScrren();
        //竖屏
        if (!isWideScrren) {
            //竖屏调整播放区域大小，宽全屏，高根据视频分辨率自适应
            mPlayer.setSurfaceSize(dm.widthPixels, 0);
        } else {
            //横屏屏调整播放区域大小，宽、高均全屏，播放区域根据视频分辨率自适应
            mPlayer.setSurfaceSize(dm.widthPixels,dm.heightPixels);
        }
    }

    @Override
    public void onWindowSizeChanged(int w, int h, int oldW, int oldH) {
        if (mPlayer != null) {
            setSurfaceSize();
        }
    }

    public class MyOrientationDetector extends OrientationEventListener {

        private WindowManager mWindowManager;
        private int mLastOrientation = 0;

        public MyOrientationDetector(Context context) {
            super(context);
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }

        public boolean isWideScrren() {
            Display display = mWindowManager.getDefaultDisplay();
            Point pt = new Point();
            display.getSize(pt);
            return pt.x > pt.y;
        }
        @Override
        public void onOrientationChanged(int orientation) {
            int value = getCurentOrientationEx(orientation);
            if (value != mLastOrientation) {
                mLastOrientation = value;
                int current = getRequestedOrientation();
                if (current == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        || current == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }
        }

        private int getCurentOrientationEx(int orientation) {
            int value = 0;
            if (orientation >= 315 || orientation < 45) {
                // 0度
                value = 0;
                return value;
            }
            if (orientation >= 45 && orientation < 135) {
                // 90度
                value = 90;
                return value;
            }
            if (orientation >= 135 && orientation < 225) {
                // 180度
                value = 180;
                return value;
            }
            if (orientation >= 225 && orientation < 315) {
                // 270度
                value = 270;
                return value;
            }
            return value;
        }
    }

    //
    /**
     * 获取设备唯一信息，保证每台客户端，有唯一的ID号
     * */
    public String getDeviceInfo(){
        String deviceID = null;
        Date date = new Date();
        TimeInfo timeInfo = CustomUtils.LongToString(date);
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);

        //判断如果value为空，则获取
        if (sp.getString("deviceID", "").isEmpty()){
            deviceID = CustomUtils.getDeviceBrand()+" "+CustomUtils.getDeviceModel()+" "+timeInfo.getY_m_d()+" "+timeInfo.getHmString();
            SharedPreferences.Editor editor = sp.edit();
            //记住用户名、密码、
            editor.putString("deviceID", deviceID);
            editor.apply();
            Log.e(TAG, "getDeviceInfo: >>>>>>>>>>>>>>>>>>>>"+deviceID );
        }else {
            deviceID = sp.getString("deviceID", "");
        }
        clientId = deviceID;
        Log.e(TAG, "getDeviceInfo:?>>>>>>>>>>>>>>> "+clientId );

        return deviceID;
    }

    /**
     * mqtt 初始化
     * */
    public void MqttInit(){
        /* 创建MqttConnectOptions对象并配置username和password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);//告诉服务器，断开链接时，不要清除session
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(15);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());


        /* 创建MqttAndroidClient对象, 并设置回调接口 */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);//这里的和上次访问的ID需要一致
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Mqtt recContent = null;
                Log.e(TAG, "收到消息为: >>>>>>>>>>>>>>>>>"+new String(message.getPayload()));
                Log.e(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                receiveContent = new String(message.getPayload());
                Log.e(TAG, "messageArrived:内容 " +receiveContent);

                if (!receiveContent.equals("Device Offline!")){
                    //非 离线信息才转换成 Mqtt实体类
                    Gson gson = new Gson();
                    recContent = gson.fromJson(receiveContent,Mqtt.class);
                }

                int qos = message.getQos();
                Log.e(TAG, "消息等级为: "+qos );
                //判断nodeMCU是否离线
                Log.e(TAG, "messageArrived:内容 2222 " +receiveContent);
                if (receiveContent.equals("Device Offline!")){
                    Log.e(TAG, "》》》》》》》》》》》》》   zl方法被调用 ");
                    //这里判断是否第一次打开App，如果是第一次 则为"未在线"，在打开之后接收到离线消息则提示"已断线"
                    if (first){
                        nodeMCU = false;
                        Log.e(TAG, "》》》》》》》》》》》》》   if方法被调用 ");
                        init(receiveContent);

                        PopTip.show("当前nodeMCU未在线！");
                    }else {
                        nodeMCU = false;
                        init(receiveContent);
                        PopTip.show("nodeMCU已断线！");
                    }

                    //如果获取到到消息 非 离线消息
                } else if (recContent.getDevice().equals("开机")){
                    if (first){
                        Log.e(TAG, "nodeMCU在线 App第一次打开 方法被调用");
                        nodeMCU = true;
                        PopTip.show("nodeMCU连接成功！");
                    } else {
                        nodeMCU = true;
                        PopTip.show("nodeMCU 已上线！");
                    }
                    //WaitDialog.show("正在初始化状态");
                    nowStatus = new String(message.getPayload());
                    init(nowStatus);
                }else {
                    String device = null;
                    Log.e(TAG, "nodeMCU在线 方法被调用");
                    nodeMCU = true;

                    Log.e(TAG, "这条消息来自机型ID号为："+recContent.getDevice() );

                    if (recContent.getDevice().equals("YOLO")){
                        device = "YOLO";
                    }else {
                        device = recContent.getDevice().substring(recContent.getDevice().indexOf("") , recContent.getDevice().indexOf(" 2"));
                    }

                    if (!clientId.equals(recContent.getDevice())){
                        PopTip.show(device+" 改变状态");
                    } else {
                        PopTip.show("本机改变状态");
                    }
                    init(receiveContent);
                }

                first = false;
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Log.i(TAG, "msg delivered");
            }
        });

        /* Mqtt建连 */
        try {
            mqttAndroidClient.connect(mqttConnectOptions,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "connect succeed");

                    subscribeTopic(SUB_TOPIC);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "connect failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    /**
     * 订阅特定的主题
     * @param topic mqtt主题
     */
    public void subscribeTopic(String topic) {
        try {
            mqttAndroidClient.subscribe(topic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "subscribed succeed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "subscribed failed");
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向默认的主题/user/update发布消息
     * @param payload 消息载荷
     */
    public void publishMessage(String payload) {
        try {
            if (mqttAndroidClient.isConnected() == false) {
                mqttAndroidClient.connect();
            }

            MqttMessage message = new MqttMessage();
            message.setPayload(payload.getBytes());
            message.setQos(0);
            mqttAndroidClient.publish(PUB_TOPIC, message,null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i(TAG, "publish succeed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i(TAG, "publish failed!");
                }
            });
        } catch (MqttException e) {
            Log.e(TAG, e.toString());
            e.printStackTrace();
        }
    }

    /**
     * @Description 打开软件时，读取信息
     * @Author 游同学
     * */
    @SuppressLint("UseCompatLoadingForDrawables")
    public void init(String nowStatus){
        //订阅
        Log.e(TAG, "当前初始值状态为:+++++++++++ "+nowStatus );
        if (nowStatus.equals("Device Offline!")){
            ElevatorStatus = 0;
            LightStatus = 0;
        }else {
            Mqtt mqtt = gson.fromJson(nowStatus,Mqtt.class);
            ElevatorStatus = mqtt.getElevatorStatus();
            LightStatus = mqtt.getLightStatus();

            Log.e(TAG, "setElevatorStatus: "+mqtt.getElevatorStatus() );
            date_tv_text.setText("操作时间： "+mqtt.getDate());
        }

        if(nodeMCU){
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_on));
        }else {
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_off));
        }
        if (ElevatorStatus == 1){
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_run));

        }else if (ElevatorStatus == 0){
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_stop));
        }
        if (LightStatus == 0){
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_off));
        }else if (LightStatus == 1){
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_on));
        }
        if(nodeMCU){
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec));
        }else {
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec_offline));
        }

        toast();

    }

    /**
     * 更新完成弹窗
     * */
    public void toast(){
        CustomUtils.runDelayed(new Runnable() {
            @Override
            public void run() {
                TipDialog.show("数据更新完成", WaitDialog.TYPE.SUCCESS);
            }
        }, 150);
    }

    /**
     * 这个代码贴在这儿，Mqtt库在连接时会对Sdcard文件进行读写， 实验室的小米电视 Xiaomi MiTV4-ANSM0 安卓版本 6.0.1 API23,单独在AndroidManifest中申请 EXTERNAL_STORAGE 不行，电视不会弹窗
     * 使用 java 代码 ActivityCompat.requestPermissions 类进行动态获取权限，还是不能弹窗
     * 测试 XXPermissions 权限申请库，可以成功调出弹窗
     *
     * 解决方案为：动态申请 MANAGE_EXTERNAL_STORAGE 权限，低于23不能申请WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE,,minni sdk 设置为 minSdk 23
     *
     * 如下是复现不能写入文件的 Kotlin 代码
     * */
    /*fun testit() {
        // ask Android where we can put files
        var myDir: File? = this.getExternalFilesDir(TAG)

        if (myDir == null) {
            // No external storage, use internal storage instead.
            myDir = this.getDir(TAG, MODE_PRIVATE)
            Log.i(TAG, "write to internal")
            if (myDir == null) {
                Log.w(TAG, "write to null")
            }
        } else {
            Log.i(TAG, "write to external")
        }

        Log.i(TAG, myDir!!.absolutePath)

        var dataDir : File? = File(myDir!!.absolutePath)
        Log.i(TAG, "external canwrite?" + dataDir?.canWrite())

        myDir = this.getDir(TAG, MODE_PRIVATE)
        dataDir = File(myDir!!.absolutePath)
        Log.i(TAG, "internal dir canwrite?" + dataDir?.canWrite())
    }*/
    public void getPermission() {
        if (!XXPermissions.isGranted(TvActivity.this, Permission.MANAGE_EXTERNAL_STORAGE) && Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Log.e("AiMqtt", "运行申请权限");
            XXPermissions.with(this)
                    // 申请单个权限
                    .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                    // 申请多个权限
                    //.permission(Permission.Group.STORAGE)
                    // 设置权限请求拦截器（局部设置）
                    //.interceptor(new PermissionInterceptor())
                    // 设置不触发错误检测机制（局部设置）
                    //.unchecked()
                    .request(new OnPermissionCallback() {

                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            if (all) {
                                Toast.makeText(TvActivity.this,"获取sdcard权限成功", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(TvActivity.this,"获取部分权限成功，但部分权限未正常授予", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            if (never) {
                                Toast.makeText(TvActivity.this,"被永久拒绝授权，请手动授予sdcard权限", Toast.LENGTH_LONG).show();
                                // 如果是被永久拒绝就跳转到应用权限系统设置页面
                                XXPermissions.startPermissionActivity(TvActivity.this, permissions);
                            } else {
                                Toast.makeText(TvActivity.this,"获取sdcard权限失败", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }

    }

    /**
     * 控制电梯网络请求,电视去除了密码验证
     * */
    public void setElevatorStatus(){
        //实例化一个请求对象 api
        elevatorLayout.setOnClickListener(view -> {
            if (nodeMCU){
                if (ElevatorStatus == 1){

                    Log.e("test", "电梯 1 》》》》》》 Light"+LightStatus );

                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(0);
                    machine.setLightStatus(LightStatus);
                    machine.setDate(time);
                    machine.setDevice(clientId);//这里的 ClientID为机型加第一次打开软件时间
                    //将Mqtt bean转换为json
                    publishContent = gson.toJson(machine);
                    //发布消息
                    publishMessage(publishContent);

                    //发布之后需要判断，nodeMCU返回的信息，来判断是否操作成功
                    if (!receiveContent.isEmpty()){
                        Mqtt mqtt = gson.fromJson(receiveContent,Mqtt.class);
                        //判断接收到到结果与发送的是否相同
                        if (mqtt.getElevatorStatus() == ElevatorStatus & mqtt.getLightStatus() == LightStatus){
                            ElevatorStatus = 0;
                            //设置UI层显示
//                          init(receiveContent);

                        }
                    }

                }else if (ElevatorStatus == 0){

                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(1);
                    machine.setLightStatus(LightStatus);
                    machine.setDate(time);
                    machine.setDevice(clientId);//这里的 ClientID为机型加第一次打开软件时间
                    //将Mqtt bean转换为json
                    publishContent = gson.toJson(machine);
                    //发布消息
                    publishMessage(publishContent);

                    //发布之后需要判断，nodeMCU返回的信息，来判断是否操作成功
                    if (!receiveContent.isEmpty()){
                        Mqtt mqtt = gson.fromJson(receiveContent,Mqtt.class);
                        //判断接收到到结果与发送的是否相同
                        if (mqtt.getElevatorStatus() == ElevatorStatus & mqtt.getLightStatus() == LightStatus){
                            ElevatorStatus = 1;

                            //设置UI层显示
//                          init(receiveContent);

                        }
                    }


                }
            }else {
                PopTip.show("当前nodeMCU未在线！");
            }

        });

    }

    /**
     * 控制预警灯网络请求
     * */
    public void setLightStatus(){
        lightLayout.setOnClickListener(view -> {
            if (nodeMCU){
                //显示等待进度框
                WaitDialog.show("正在处理...");

                if (LightStatus == 0){
                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(ElevatorStatus);
                    machine.setLightStatus(1);
                    machine.setDate(time);
                    machine.setDevice(clientId);//这里的 ClientID为机型加第一次打开软件时间
                    //将Mqtt bean转换为json
                    publishContent = gson.toJson(machine);
                    //发布消息
                    publishMessage(publishContent);

                    LightStatus = 1;
                    //设置UI层显示
//                    init(receiveContent);

                }else if (LightStatus == 1){
                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(ElevatorStatus);
                    machine.setLightStatus(0);
                    machine.setDate(time);
                    machine.setDevice(clientId);//这里的 ClientID为机型加第一次打开软件时间
                    //将Mqtt bean转换为json
                    publishContent = gson.toJson(machine);
                    //发布消息
                    publishMessage(publishContent);

                    LightStatus = 0;

                    //设置UI层显示
//                    init(receiveContent);
                }
            }else {
                PopTip.show("当前nodeMCU未在线！");
            }

        });

    }

    /**
     * 全部复原方法
     * */
    public void rec() {
        recLayout.setOnClickListener(view -> {
            if (nodeMCU){
                //显示等待进度框
                WaitDialog.show("正在处理...");

                date = new Date();
                timeInfo = CustomUtils.LongToString(date);
                time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                machine.setElevatorStatus(1);
                machine.setLightStatus(0);
                machine.setDate(time);
                machine.setDevice(clientId);//这里的 ClientID为机型加第一次打开软件时间
                //将Mqtt bean转换为json
                publishContent = gson.toJson(machine);
                //发布消息
                publishMessage(publishContent);

                ElevatorStatus = 1;
                LightStatus = 0;

                //设置UI层显示
//                init(receiveContent);
            }else {
                PopTip.show("当前nodeMCU未在线！");
            }

        });

    }
}