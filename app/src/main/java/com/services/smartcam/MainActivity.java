package com.services.smartcam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.ezvizuikit.open.EZUIError;
import com.ezvizuikit.open.EZUIKit;
import com.ezvizuikit.open.EZUIPlayer;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.InputDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.kongzue.dialogx.dialogs.TipDialog;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.style.IOSStyle;
import com.kongzue.dialogx.util.InputInfo;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, WindowSizeChangeNotifier.OnWindowSizeChangedListener, EZUIPlayer.EZUIPlayerCallBack {
    //Mqtt
    private final String TAG = "AiotMqtt";
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
    private TextView elevator_status;
    private TextView light_status;
    private TextView nodeMCU_status;
    private TextView rec_status;
    private TextView date_text;
    private TextView number;
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

    //listview
    public ListView mListView = null;

    /* 图片ID数组 */
    private final int[] mImageId = new int[] {R.drawable.ic_device, R.drawable.ic_error};
    /* 文字列表数组 */
    private final String[] mTitle = new String[] {"本机设备号", "萤石错误码"};


    //mqtt

    private MqttAndroidClient mqtt_client;                   //创建一个mqtt_client对象
    /**
     * onresume时是否恢复播放
     */
    private boolean isResumePlay = false;

    private MyOrientationDetector mOrientationDetector;

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

    /**
     * 海外版本areaDomin
     */
    private String mGlobalAreaDomain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initListView();
        showDrawer();
        getDeviceInfo();//获取设备 机型➕第一次打开时间
        //初始化
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();//设置为IOS主题
        //设置为IOS主题
        DialogX.onlyOnePopTip = true;


        elevator_status = findViewById(R.id.elevator_status_text);
        light_status = findViewById(R.id.light_status_text);
        nodeMCU_status = findViewById(R.id.nodeMCU_status_text);
        rec_status = findViewById(R.id.rec_status_text);
        date_text = findViewById(R.id.date);;

        //实例化layout布局
        elevatorLayout = findViewById(R.id.elevator_Layout);
        lightLayout = findViewById(R.id.light_Layout);
        nodeMcuLayout = findViewById(R.id.nodeMCU_Layout);
        recLayout = findViewById(R.id.rec_Layout);
        //图标
        elevatorImg = findViewById(R.id.elevator_img);
        lightImg = findViewById(R.id.light_img);
        nodeMcuImg = findViewById(R.id.nodeMcu_img);
        recImg = findViewById(R.id.rec_img);

        elevatorStatus = findViewById(R.id.elevator_status_img);
        lightStatus = findViewById(R.id.light_status_img);
        nodeMCUStatus = findViewById(R.id.nodeMCU_status_img);
        recStatus = findViewById(R.id.rec_status_img);

        SetMargin();//设置顶部导航栏高度
        //init();//初始化机器状
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
        mOrientationDetector = new MyOrientationDetector(this);
        new WindowSizeChangeNotifier(this, this);
        //mBtnPlay = (Button) findViewById(R.id.play);

        //获取EZUIPlayer实例
        mPlayer = (EZUIPlayer) findViewById(R.id.player_ui);
        //
        stop = findViewById(R.id.stop);
        //设置加载需要显示的view
        mPlayer.setLoadingView(initProgressBar());
        stop.setOnClickListener(this);
        mPlayer.setOnClickListener(this);
        preparePlay();
        setSurfaceSize();


    }
    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //重写返回键回掉方法，让按下返回键时 让当前activity隐藏到后台，而不是 调用 finish(); ，第二次打开时就不会再次加载Splash界面
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            moveTaskToBack(true);
        }
        return true;
    }



    //mqtt

    /**
     * @Description 打开软件时，读取信息
     * @Author 游同学
     * */
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

            Log.e(TAG, "setLightStatus: "+mqtt.getElevatorStatus() );
            date_text.setText("操作时间： "+mqtt.getDate());
        }
        if(nodeMCU){
            nodeMCU_status.setText("在线");
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_on));
        }else {
            nodeMCU_status.setText("离线");
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_off));
        }
        if (ElevatorStatus == 1){
            elevator_status.setText("运行");
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_run));

        }else if (ElevatorStatus == 0){
            elevator_status.setText("停止");
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_stop));
        }
        if (LightStatus == 0){
            light_status.setText("关闭");
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_off));
        }else if (LightStatus == 1){
            light_status.setText("开启");
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_on));
        }
        if(nodeMCU){
            rec_status.setText("正常");
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec));
        }else {
            rec_status.setText("离线");
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec_offline));
        }

        toast();


    }

    /**
     * 控制电梯网络请求
     * */
    public void setElevatorStatus(){
        //实例化一个请求对象 api
        elevatorLayout.setOnClickListener(view -> {
            if (nodeMCU){
                if (ElevatorStatus == 1){
                    InputInfo in = new InputInfo();
                    in.setInputType(0x00000081);

                    //验证授权码
                    new InputDialog("关闭电梯", "请输入授权码", "确定", "取消")
//                            .setInputText("test")
                            .setInputInfo(in)
                            .setCancelable(false)
                            .setOkButton((baseDialog, v, inputStr) -> {
                                //显示等待进度框
                                WaitDialog.show("正在处理...");

                                baseDialog.getInputText();
                                //判断输入密码是否正确
                                if (inputStr.equals("123456")){
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
//                                            init(receiveContent);

                                        }
                                    }
                                }else {
                                    CustomUtils.runDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.show("授权码错误", WaitDialog.TYPE.ERROR);
                                        }
                                    }, 150);
                                }

                                return false;
                            })
                            .show();

                }else if (ElevatorStatus == 0){
                    InputInfo in = new InputInfo();
                    in.setInputType(0x00000081);

                    new InputDialog("开启电梯", "请输入授权码", "确定", "取消")
//                            .setInputText("test")
                            .setInputInfo(in)
                            .setCancelable(false)
                            .setOkButton((baseDialog, v, inputStr) -> {
                                //显示等待进度框
                                WaitDialog.show("处理中...");

                                baseDialog.getInputText();
                                //判断输入密码是否正确
                                if (inputStr.equals("123456")){
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
//                                            init(receiveContent);

                                        }
                                    }
                                }else {
                                    CustomUtils.runDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.show("密码错误", WaitDialog.TYPE.ERROR);
                                        }
                                    }, 150);
                                }

                                return false;
                            })
                            .show();

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
            mPlayer.setCallBack(this);
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

    /**
     * mqtt test
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
     * 状态栏管理
     * */
    public void Clear(){
        ImmersionBar.with(MainActivity.this)
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
     * 获取状态栏高度，设置layout的margin——top值
     *
     * */
    public void SetMargin(){
        //获取状态栏高度
        int statusBarHeight1 = 0;
        //获取status_bar_height资源的ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        Log.e("TAG", "方法1状态栏高度:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + statusBarHeight1);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins(0, statusBarHeight1, 0, 0);

        RelativeLayout titleLayout = findViewById(R.id.titleRelative_richeng);
        titleLayout.setLayoutParams(lp);
    }

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
     * 侧边菜单栏
     * */
    @SuppressLint("WrongConstant")
    public void showDrawer(){
        LinearLayout more = findViewById(R.id.more);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        more.setOnClickListener(view -> {
            drawerLayout.openDrawer(Gravity.START);

        });

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                ImmersionBar.with(MainActivity.this)
                        .statusBarDarkFont(false)   //状态栏字体是深色，不写默认为亮色
                        .init();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                ImmersionBar.with(MainActivity.this)
                        .transparentStatusBar()  //透明状态栏，不写默认透明色
                        .navigationBarColor(R.color.white)
                        .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                        .init();

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    /**
     * 设置list view
     * */

    public void initListView() {

        mListView = findViewById(R.id.listview);

        List<Map<String, Object>> mListItems = new ArrayList<>();
        for (int i = 0; i < mImageId.length; i++) {
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("image", mImageId[i]);
            mMap.put("title", mTitle[i]);
            mListItems.add(mMap);
        }

        SimpleAdapter mAdapter = new SimpleAdapter(MainActivity.this, mListItems, R.layout.drawer_listview_item, new String[]{"title", "image"}, new int[]{R.id.item_text, R.id.item_image});
        mListView.setAdapter(mAdapter);

        //设置点击监听事件
        mListView.setOnItemClickListener((adapterView, view, i, l) -> {

            if (i == 0){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, com.services.smartcam.DeviceActivity.class);
                startActivity(intent);

            }else if (i == 1){
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,EZerrorActivity.class);
                startActivity(intent);

            }

//                Toast.makeText(getContext(), "Click item：" + i, Toast.LENGTH_SHORT).show();


        });
    }

}