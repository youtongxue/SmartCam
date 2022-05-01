package com.services.smartcam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

public class SplashActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //隐藏状态栏和底部导航栏
            ImmersionBar.with(this)
                    .hideBar(BarHide.FLAG_HIDE_BAR)
                    .init();

            View view = View.inflate(this, R.layout.activity_splash,null);
            setContentView(view);
            AlphaAnimation a = new AlphaAnimation(0.1f,1.0f);//渐变动画
            a.setDuration(150);//持续时间
            view.startAnimation(a);
            a.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //这儿按理来说，是应该采用Android中的限定符来做显示不同的视图的判定，但是考虑到TV 的输入方式为遥控器，所以业务逻辑代码也需要更改，就单独写了个TvActivity
                    //这儿是根据屏幕的宽度来判定是否为大屏幕，（这个方式局限性很大也不够准确）
                    Intent intent;
                    int w = DisplayUtil.getRealScreenRelatedInformation(SplashActivity.this);
                    if (w > 1080) {
                        intent = new Intent(SplashActivity.this, TvActivity.class);
                    } else {
                        intent = new Intent(SplashActivity.this, MainActivity.class);
                    }
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    finish();
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }


}