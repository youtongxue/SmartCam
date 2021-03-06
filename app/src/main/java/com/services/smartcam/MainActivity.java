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

/**
 *fix ?????????Mqtt??????????????????????????????Pop????????????????????????
 * */

public class MainActivity extends AppCompatActivity implements View.OnClickListener, WindowSizeChangeNotifier.OnWindowSizeChangedListener, EZUIPlayer.EZUIPlayerCallBack {
    //Mqtt
    private final String TAG = "AiotMqtt";
    /* ??????Topic, ?????????????????? */
    final private String PUB_TOPIC = "anfang";
    /* ??????Topic, ?????????????????? */
    final private String SUB_TOPIC = "machineStatus";

    final String host = "tcp://1.14.68.248:1883";
    private String clientId = "MEIZU 16s Pro";
    private String userName = "username";
    private String passWord = "123456";
    private String publishContent;//???????????????
    private String nowStatus;//????????????
    private boolean nodeMCU = false;//??????nodeMCU???????????????
    private boolean first = true;//????????????app???????????????????????????
    private boolean device = false;
    private String receiveContent = null;//??????nodeMCU??????????????????

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
    //?????????layout
    private View elevatorLayout;
    private View lightLayout;
    private View nodeMcuLayout;
    private View recLayout;
    //??????
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

    /* ??????ID?????? */
    private final int[] mImageId = new int[] {R.drawable.ic_device, R.drawable.ic_error};
    /* ?????????????????? */
    private final String[] mTitle = new String[] {"???????????????", "???????????????"};


    //mqtt

    private MqttAndroidClient mqtt_client;                   //????????????mqtt_client??????
    /**
     * onresume?????????????????????
     */
    private boolean isResumePlay = false;

    private MyOrientationDetector mOrientationDetector;

    /**
     *  ??????????????????Appkey
     */
    private String appkey = "b5ad3b35cb054865a96e58046bc00716";
    private String appSecret = "b3e23f9aeea28642ecfa59ea9e924273";
    /**
     *  ??????accesstoken
     */
    private String accesstoken = "at.21lftjwn4f30u2360n53nmfra9oa8i5p-3nqdswxoix-0r2n2o6-obnusch6p";
    /**
     *  ??????url???ezopen??????
     */
    private String playUrl = "ezopen://open.ys7.com/172124816/1.hd.live";

    /**
     * ????????????areaDomin
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
        getDeviceInfo();//???????????? ??????????????????????????????
        //?????????
        DialogX.init(this);
        DialogX.globalStyle = new IOSStyle();//?????????IOS??????
        //?????????IOS??????
        DialogX.onlyOnePopTip = true;


        elevator_status = findViewById(R.id.elevator_status_text);
        light_status = findViewById(R.id.light_status_text);
        nodeMCU_status = findViewById(R.id.nodeMCU_status_text);
        rec_status = findViewById(R.id.rec_status_text);
        date_text = findViewById(R.id.date);;

        //?????????layout??????
        elevatorLayout = findViewById(R.id.elevator_Layout);
        lightLayout = findViewById(R.id.light_Layout);
        nodeMcuLayout = findViewById(R.id.nodeMCU_Layout);
        recLayout = findViewById(R.id.rec_Layout);
        //??????
        elevatorImg = findViewById(R.id.elevator_mg);
        lightImg = findViewById(R.id.light_img);
        nodeMcuImg = findViewById(R.id.nodeMcu_img);
        recImg = findViewById(R.id.rec_img);

        elevatorStatus = findViewById(R.id.elevator_status_img);
        lightStatus = findViewById(R.id.light_status_img);
        nodeMCUStatus = findViewById(R.id.nodeMCU_status_img);
        recStatus = findViewById(R.id.rec_status_img);

        SetMargin();//???????????????????????????
        //init();//??????????????????
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

        //??????EZUIPlayer??????
        mPlayer = (EZUIPlayer) findViewById(R.id.player_ui);
        //
        stop = findViewById(R.id.stop);
        //???????????????????????????view
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

    //??????????????????????????????????????????????????? ?????????activity??????????????????????????? ?????? finish(); ??????????????????????????????????????????Splash??????
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
            moveTaskToBack(true);
        }
        return true;
    }



    //mqtt

    /**
     * @Description ??????????????????????????????
     * @Author ?????????
     * */
    public void init(String nowStatus){
        //??????
        Log.e(TAG, "????????????????????????:+++++++++++ "+nowStatus );
        if (nowStatus.equals("Device Offline!")){
            ElevatorStatus = 0;
            LightStatus = 0;
        }else {
            Mqtt mqtt = gson.fromJson(nowStatus,Mqtt.class);
            ElevatorStatus = mqtt.getElevatorStatus();
            LightStatus = mqtt.getLightStatus();

            Log.e(TAG, "setLightStatus: "+mqtt.getElevatorStatus() );
            date_text.setText("??????????????? "+mqtt.getDate());
        }
        if(nodeMCU){
            nodeMCU_status.setText("??????");
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_on));
        }else {
            nodeMCU_status.setText("??????");
            nodeMCUStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            nodeMcuImg.setImageDrawable(getDrawable(R.drawable.ic_nodemcu_off));
        }
        if (ElevatorStatus == 1){
            elevator_status.setText("??????");
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_run));

        }else if (ElevatorStatus == 0){
            elevator_status.setText("??????");
            elevatorStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            elevatorImg.setImageDrawable(getDrawable(R.drawable.ic_elevator_stop));
        }
        if (LightStatus == 0){
            light_status.setText("??????");
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_off));
        }else if (LightStatus == 1){
            light_status.setText("??????");
            lightStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            lightImg.setImageDrawable(getDrawable(R.drawable.ic_light_on));
        }
        if(nodeMCU){
            rec_status.setText("??????");
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_online));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec));
        }else {
            rec_status.setText("??????");
            recStatus.setImageDrawable(getDrawable(R.drawable.ic_offline));
            recImg.setImageDrawable(getDrawable(R.drawable.ic_rec_offline));
        }

        toast();


    }

    /**
     * ????????????????????????
     * */
    public void setElevatorStatus(){
        //??????????????????????????? api
        elevatorLayout.setOnClickListener(view -> {
            if (nodeMCU){
                if (ElevatorStatus == 1){
                    InputInfo in = new InputInfo();
                    in.setInputType(0x00000081);

                    //???????????????
                    new InputDialog("????????????", "??????????????????", "??????", "??????")
//                            .setInputText("test")
                            .setInputInfo(in)
                            .setCancelable(false)
                            .setOkButton((baseDialog, v, inputStr) -> {
                                //?????????????????????
                                WaitDialog.show("????????????...");

                                baseDialog.getInputText();
                                //??????????????????????????????
                                if (inputStr.equals("123456")){
                                    Log.e("test", "?????? 1 ?????????????????? Light"+LightStatus );

                                    date = new Date();
                                    timeInfo = CustomUtils.LongToString(date);
                                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                                    machine.setElevatorStatus(0);
                                    machine.setLightStatus(LightStatus);
                                    machine.setDate(time);
                                    machine.setDevice(clientId);//????????? ClientID???????????????????????????????????????
                                    //???Mqtt bean?????????json
                                    publishContent = gson.toJson(machine);
                                    //????????????
                                    publishMessage(publishContent);

                                    //???????????????????????????nodeMCU?????????????????????????????????????????????
                                    if (!receiveContent.isEmpty()){
                                        Mqtt mqtt = gson.fromJson(receiveContent,Mqtt.class);
                                        //????????????????????????????????????????????????
                                        if (mqtt.getElevatorStatus() == ElevatorStatus & mqtt.getLightStatus() == LightStatus){
                                            ElevatorStatus = 0;
                                            //??????UI?????????
//                                            init(receiveContent);

                                        }
                                    }
                                }else {
                                    CustomUtils.runDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.show("???????????????", WaitDialog.TYPE.ERROR);
                                        }
                                    }, 150);
                                }

                                return false;
                            })
                            .show();

                }else if (ElevatorStatus == 0){
                    InputInfo in = new InputInfo();
                    in.setInputType(0x00000081);

                    new InputDialog("????????????", "??????????????????", "??????", "??????")
//                            .setInputText("test")
                            .setInputInfo(in)
                            .setCancelable(false)
                            .setOkButton((baseDialog, v, inputStr) -> {
                                //?????????????????????
                                WaitDialog.show("?????????...");

                                baseDialog.getInputText();
                                //??????????????????????????????
                                if (inputStr.equals("123456")){
                                    date = new Date();
                                    timeInfo = CustomUtils.LongToString(date);
                                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                                    machine.setElevatorStatus(1);
                                    machine.setLightStatus(LightStatus);
                                    machine.setDate(time);
                                    machine.setDevice(clientId);//????????? ClientID???????????????????????????????????????
                                    //???Mqtt bean?????????json
                                    publishContent = gson.toJson(machine);
                                    //????????????
                                    publishMessage(publishContent);

                                    //???????????????????????????nodeMCU?????????????????????????????????????????????
                                    if (!receiveContent.isEmpty()){
                                        Mqtt mqtt = gson.fromJson(receiveContent,Mqtt.class);
                                        //????????????????????????????????????????????????
                                        if (mqtt.getElevatorStatus() == ElevatorStatus & mqtt.getLightStatus() == LightStatus){
                                            ElevatorStatus = 1;

                                            //??????UI?????????
//                                            init(receiveContent);

                                        }
                                    }
                                }else {
                                    CustomUtils.runDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            TipDialog.show("????????????", WaitDialog.TYPE.ERROR);
                                        }
                                    }, 150);
                                }

                                return false;
                            })
                            .show();

                }
            }else {
                PopTip.show("??????nodeMCU????????????");
            }

        });

    }

    /**
     * ???????????????????????????
     * */
    public void setLightStatus(){
        lightLayout.setOnClickListener(view -> {
            if (nodeMCU){
                //?????????????????????
                WaitDialog.show("????????????...");

                if (LightStatus == 0){
                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(ElevatorStatus);
                    machine.setLightStatus(1);
                    machine.setDate(time);
                    machine.setDevice(clientId);//????????? ClientID???????????????????????????????????????
                    //???Mqtt bean?????????json
                    publishContent = gson.toJson(machine);
                    //????????????
                    publishMessage(publishContent);

                    LightStatus = 1;
                    //??????UI?????????
//                    init(receiveContent);

                }else if (LightStatus == 1){
                    date = new Date();
                    timeInfo = CustomUtils.LongToString(date);
                    time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                    machine.setElevatorStatus(ElevatorStatus);
                    machine.setLightStatus(0);
                    machine.setDate(time);
                    machine.setDevice(clientId);//????????? ClientID???????????????????????????????????????
                    //???Mqtt bean?????????json
                    publishContent = gson.toJson(machine);
                    //????????????
                    publishMessage(publishContent);

                    LightStatus = 0;

                    //??????UI?????????
//                    init(receiveContent);
                }
            }else {
                PopTip.show("??????nodeMCU????????????");
            }

        });

    }

    /**
     * ??????????????????
     * */
    public void rec() {
        recLayout.setOnClickListener(view -> {
            if (nodeMCU){
                //?????????????????????
                WaitDialog.show("????????????...");

                date = new Date();
                timeInfo = CustomUtils.LongToString(date);
                time = timeInfo.getY_m_d()+" "+timeInfo.getHmString();
                machine.setElevatorStatus(1);
                machine.setLightStatus(0);
                machine.setDate(time);
                machine.setDevice(clientId);//????????? ClientID???????????????????????????????????????
                //???Mqtt bean?????????json
                publishContent = gson.toJson(machine);
                //????????????
                publishMessage(publishContent);

                ElevatorStatus = 1;
                LightStatus = 0;

                //??????UI?????????
//                init(receiveContent);
            }else {
                PopTip.show("??????nodeMCU????????????");
            }

        });

    }

    /**
     * ????????????view
     * @return
     */
    private View initProgressBar() {
        RelativeLayout relativeLayout = new RelativeLayout(this);
        relativeLayout.setBackgroundColor(Color.parseColor("#000000"));
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(lp);
        RelativeLayout.LayoutParams rlp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.CENTER_IN_PARENT);//addRule????????????RelativeLayout XML???????????????
        ProgressBar mProgressBar = new ProgressBar(this);
        mProgressBar.setIndeterminateDrawable(getResources().getDrawable(R.drawable.progress));
        relativeLayout.addView(mProgressBar,rlp);
        return relativeLayout;
    }

    /**
     * ????????????????????????
     */
    private void preparePlay(){
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);
        accesstoken = sp.getString("newEzToken", "");
        if (accesstoken.isEmpty()){
            //???????????????????????????token
            EzTokenApi api = NetworkFactory.ezToken();
            Call<EzToken> R = api.getToken(appkey,appSecret);
            R.enqueue(new Callback<EzToken>() {
                @Override
                public void onResponse(Call<EzToken> call, Response<EzToken> response) {
                    EzToken ezToken = response.body();
                    String newToken = ezToken.getData().getAccessToken();
                    Log.e(TAG, "??????onResponse: >>>>>>>>token"+newToken);
                    SharedPreferences.Editor editor = sp.edit();
                    //???????????????????????????
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
            //??????debug???????????????log??????
            EZUIKit.setDebug(true);
            //appkey?????????
            EZUIKit.initWithAppKey(this.getApplication(), appkey);
            //????????????accesstoken
            EZUIKit.setAccessToken(accesstoken);
            //????????????????????????
            mPlayer.setCallBack(this);
            mPlayer.setUrl(playUrl);
        }


    }
    @Override
    protected void onResume() {
        super.onResume();
        mOrientationDetector.enable();
        Log.d(TAG,"onResume");
        //??????stop???????????????????????????isResumePlay???????????????true???resume???????????????
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
        //??????stop???????????????????????????isResumePlay???????????????true?????????resume???????????????
        if (mPlayer.getStatus() != EZUIPlayer.STATUS_STOP) {
            isResumePlay = true;
        }
        //????????????
        mPlayer.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");

        //????????????
        mPlayer.releasePlayer();
    }

    @Override
    public void onPlaySuccess() {
        Log.d(TAG,"onPlaySuccess");
        //TipDialog.show("????????????", WaitDialog.TYPE.SUCCESS);
        // TODO: 2017/2/7 ??????????????????

    }

    @Override
    public void onPlayFail(EZUIError error) {
        preparePlay();
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);

        Log.d(TAG,"onPlayFail");
        // TODO: 2017/2/21 ??????????????????
        if (error.getErrorString().equals(EZUIError.UE_ERROR_INNER_VERIFYCODE_ERROR)){

        }else if(error.getErrorString().equalsIgnoreCase(EZUIError.UE_ERROR_NOT_FOUND_RECORD_FILES)){
            // TODO: 2017/5/12
            //?????????????????????
            Toast.makeText(this,"?????????",Toast.LENGTH_LONG).show();
        }

        if (error.getErrorString().equals(EZUIError.UE_ERROR_ACCESSTOKEN_ERROR_OR_EXPIRE)){
            WaitDialog.show("accesstoken???????????????");
            WaitDialog.show("????????????accesstoken");

            //???????????????????????????token
            EzTokenApi api = NetworkFactory.ezToken();
            Call<EzToken> R = api.getToken(appkey,appSecret);
            R.enqueue(new Callback<EzToken>() {
                @Override
                public void onResponse(Call<EzToken> call, Response<EzToken> response) {
                    EzToken ezToken = response.body();
                    String newToken = ezToken.getData().getAccessToken();
                    if (!newToken.isEmpty()){
                        TipDialog.show("????????????", WaitDialog.TYPE.SUCCESS);
                        Log.e(TAG, "onResponse: >>>>>>>>token"+newToken);
                        SharedPreferences.Editor editor = sp.edit();
                        //???????????????????????????
                        editor.putString("newEzToken", newToken);
                        editor.apply();
                        preparePlay();
                    }else {
                        TipDialog.show("????????????", WaitDialog.TYPE.ERROR);
                    }

                }


                @Override
                public void onFailure(Call<EzToken> call, Throwable t) {
                    TipDialog.show("????????????", WaitDialog.TYPE.ERROR);

                }
            });


        }
    }

    @Override
    public void onVideoSizeChange(int width, int height) {
        // TODO: 2017/2/16 ???????????????????????????
        Log.d(TAG,"onVideoSizeChange  width = "+width+"   height = "+height);
    }

    @Override
    public void onPrepared() {
        Log.d(TAG,"onPrepared");
        //??????
        mPlayer.startPlay();
    }

    @Override
    public void onPlayTime(Calendar calendar) {
        Log.d(TAG,"onPlayTime");
        if (calendar != null) {
            // TODO: 2017/2/16 ??????????????????
            Log.d(TAG,"onPlayTime calendar = "+calendar.getTime().toString());
        }
    }

    @Override
    public void onPlayFinish() {
        // TODO: 2017/2/16 ????????????
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
     * ??????????????????????????????
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
        //??????
        if (!isWideScrren) {
            //??????????????????????????????????????????????????????????????????????????????
            mPlayer.setSurfaceSize(dm.widthPixels, 0);
        } else {
            //???????????????????????????????????????????????????????????????????????????????????????????????????
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
                // 0???
                value = 0;
                return value;
            }
            if (orientation >= 45 && orientation < 135) {
                // 90???
                value = 90;
                return value;
            }
            if (orientation >= 135 && orientation < 225) {
                // 180???
                value = 180;
                return value;
            }
            if (orientation >= 225 && orientation < 315) {
                // 270???
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
        /* ??????MqttConnectOptions???????????????username???password */
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);//????????????????????????????????????????????????session
        mqttConnectOptions.setConnectionTimeout(10);
        mqttConnectOptions.setKeepAliveInterval(15);
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setUserName(userName);
        mqttConnectOptions.setPassword(passWord.toCharArray());


        /* ??????MqttAndroidClient??????, ????????????????????? */
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), host, clientId);//???????????????????????????ID????????????
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Log.i(TAG, "connection lost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Mqtt recContent = null;
                Log.e(TAG, "???????????????: >>>>>>>>>>>>>>>>>"+new String(message.getPayload()));
                Log.e(TAG, "topic: " + topic + ", msg: " + new String(message.getPayload()));
                receiveContent = new String(message.getPayload());
                Log.e(TAG, "messageArrived:?????? " +receiveContent);

                if (!receiveContent.equals("Device Offline!")){
                    //??? ???????????????????????? Mqtt?????????
                    Gson gson = new Gson();
                    recContent = gson.fromJson(receiveContent,Mqtt.class);
                }

                int qos = message.getQos();
                Log.e(TAG, "???????????????: "+qos );
                //??????nodeMCU????????????
                Log.e(TAG, "messageArrived:?????? 2222 " +receiveContent);
                if (receiveContent.equals("Device Offline!")){
                    Log.e(TAG, "???????????????????????????????????????   zl??????????????? ");
                    //?????????????????????????????????App????????????????????? ??????"?????????"????????????????????????????????????????????????"?????????"
                    if (first){
                        nodeMCU = false;
                        Log.e(TAG, "???????????????????????????????????????   if??????????????? ");
                        init(receiveContent);

                        PopTip.show("??????nodeMCU????????????");
                    }else {
                        nodeMCU = false;
                        init(receiveContent);
                        PopTip.show("nodeMCU????????????");
                    }

                    //???????????????????????? ??? ????????????
                } else if (recContent.getDevice().equals("??????")){
                    if (first){
                        Log.e(TAG, "nodeMCU?????? App??????????????? ???????????????");
                        nodeMCU = true;
                        PopTip.show("nodeMCU???????????????");
                    } else {
                        nodeMCU = true;
                        PopTip.show("nodeMCU ????????????");
                    }
                    //WaitDialog.show("?????????????????????");
                    nowStatus = new String(message.getPayload());
                    init(nowStatus);
                }else {
                    String device = null;
                    Log.e(TAG, "nodeMCU?????? ???????????????");
                    nodeMCU = true;

                    Log.e(TAG, "????????????????????????ID?????????"+recContent.getDevice() );

                    if (recContent.getDevice().equals("YOLO")){
                        device = "YOLO";
                    }else {
                        device = recContent.getDevice().substring(recContent.getDevice().indexOf("") , recContent.getDevice().indexOf(" 2"));
                    }

                    if (!clientId.equals(recContent.getDevice())){
                        PopTip.show(device+" ????????????");
                    } else {
                        PopTip.show("??????????????????");
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

        /* Mqtt?????? */
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
     * ?????????????????????
     * @param topic mqtt??????
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
     * ??????????????????/user/update????????????
     * @param payload ????????????
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
     * ???????????????
     * */
    public void Clear(){
        ImmersionBar.with(MainActivity.this)
                .transparentStatusBar()  //???????????????????????????????????????
                .navigationBarColor(R.color.white)
                .statusBarDarkFont(true)   //????????????????????????????????????????????????
                .init();

        //??????action bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();
    }

    /**
     * ??????????????????????????????layout???margin??????top???
     *
     * */
    public void SetMargin(){
        //?????????????????????
        int statusBarHeight1 = 0;
        //??????status_bar_height?????????ID
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //????????????ID????????????????????????
            statusBarHeight1 = getResources().getDimensionPixelSize(resourceId);
        }
        Log.e("TAG", "??????1???????????????:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + statusBarHeight1);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins(0, statusBarHeight1, 0, 0);

        RelativeLayout titleLayout = findViewById(R.id.titleRelative_richeng);
        titleLayout.setLayoutParams(lp);
    }

    /**
     * ???????????????????????????????????????????????????????????????ID???
     * */
    public String getDeviceInfo(){
        String deviceID = null;
        Date date = new Date();
        TimeInfo timeInfo = CustomUtils.LongToString(date);
        sp = getSharedPreferences("deviceInfo", Context.MODE_PRIVATE);

        //????????????value??????????????????
        if (sp.getString("deviceID", "").isEmpty()){
            deviceID = CustomUtils.getDeviceBrand()+" "+CustomUtils.getDeviceModel()+" "+timeInfo.getY_m_d()+" "+timeInfo.getHmString();
            SharedPreferences.Editor editor = sp.edit();
            //???????????????????????????
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
     * ??????????????????
     * */
    public void toast(){
        CustomUtils.runDelayed(new Runnable() {
            @Override
            public void run() {
                TipDialog.show("??????????????????", WaitDialog.TYPE.SUCCESS);
            }
        }, 150);
    }

    /**
     * ???????????????
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
                        .statusBarDarkFont(false)   //????????????????????????????????????????????????
                        .init();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                ImmersionBar.with(MainActivity.this)
                        .transparentStatusBar()  //???????????????????????????????????????
                        .navigationBarColor(R.color.white)
                        .statusBarDarkFont(true)   //????????????????????????????????????????????????
                        .init();

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    /**
     * ??????list view
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

        //????????????????????????
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

//                Toast.makeText(getContext(), "Click item???" + i, Toast.LENGTH_SHORT).show();


        });
    }

}