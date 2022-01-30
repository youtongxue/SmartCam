package com.services.smartcam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;
import com.services.smartcam.EntityClass.TimeInfo;

import java.util.Date;

public class DeviceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        SetMargin();
        Clear();
        getDeviceInfo();
        goBack();
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
     * 状态栏管理
     * */
    public void Clear(){
        ImmersionBar.with(DeviceActivity.this)
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
     * 图标返回
     * */
    public void goBack(){
        //图标
        LinearLayout back = findViewById(R.id.device_back_linear);

        back.setOnClickListener(v -> finish());
    }

    /**
     * 获取设备唯一信息，保证每台客户端，有唯一的ID号
     * */
    public String getDeviceInfo(){
        TextView textView = findViewById(R.id.deviceInfo);
        TextView textView1 = findViewById(R.id.EzToken_Text);
        SharedPreferences sp;
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

        }else {
            deviceID = sp.getString("deviceID", "");
            textView.setText("连接MQTT服务器设备号： \n\n"+deviceID);
        }
        //设置token
        textView1.setText("萤石云连接accessToken：\n\n"+sp.getString("newEzToken",""));

        return deviceID;
    }
}