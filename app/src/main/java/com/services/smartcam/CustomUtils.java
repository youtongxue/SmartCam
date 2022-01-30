package com.services.smartcam;

import android.os.Handler;

import com.services.smartcam.EntityClass.TimeInfo;

import java.util.Calendar;
import java.util.Date;

public class CustomUtils {
    /**
     * 将Long或Date类型时间戳转化成 2021-08-10 String格式
     * */
    public static TimeInfo LongToString(Object o){
        TimeInfo timeInfo = new TimeInfo();

        String ymd;//20210824
        String y_m_d;//2021-08-24
        String ymdString;//2021年08月24日
        String Week = null;//周二
        String y;//年
        String m;//月
        String d;//日
        String h;//时
        String min;//分
        String s;//秒
        String hm;//时分 0943
        String hmString;//时：分 09：43
        //将long转换成Calendar类型，获取int 型的 年 月 日，显示在UI
        //Date也可以获取，但是工具提示已经过时
        Calendar calendar = Calendar.getInstance();

        if (o instanceof Long){
            calendar.setTimeInMillis((Long) o);
        }else if (o instanceof Date){
            calendar.setTime((Date) o);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        int week = calendar.get(Calendar.DAY_OF_WEEK);

        //年
        y = String.valueOf(year);
        //月
        if (month < 9){
            m =  "0"+(month+1);
        }else {
            m = String.valueOf(month+1);
        }
        //日
        if (day < 10){
            d = "0"+day;
        }else {
            d = String.valueOf(day);
        }
        //时
        if (hour <10){
            h = "0"+hour;
        }else {
            h = String.valueOf(hour);
        }
        //分
        if (minute <10){
            min = "0"+minute;
        }else {
            min = String.valueOf(minute);
        }
        //秒
        if (second <10){
            s = "0"+second;
        }else {
            s = String.valueOf(second);
        }

        switch (week-1){
            case 1:
                Week = "周一";
                break;
            case 2:
                Week = "周二";
                break;
            case 3:
                Week = "周三";
                break;
            case 4:
                Week = "周四";
                break;
            case 5:
                Week = "周五";
                break;
            case 6:
                Week = "周六";
                break;
            case 7:
                Week = "周日";
                break;
        }

        ymd = y+m+d;
        y_m_d = y+"-"+m+"-"+d;
        ymdString = y+"年"+m+"月"+d+"日";
        hm = h+min;
        hmString = h+":"+min;

        timeInfo.setY(y);
        timeInfo.setM(String.valueOf(month+1));
        timeInfo.setMm(m);
        timeInfo.setD(String.valueOf(day));
        timeInfo.setDd(d);
        timeInfo.setH(h);
        timeInfo.setMin(min);
        timeInfo.setS(s);
        timeInfo.setYmd(ymd);
        timeInfo.setY_m_d(y_m_d);
        timeInfo.setYmdString(ymdString);
        timeInfo.setHm(hm);
        timeInfo.setHmString(hmString);
        timeInfo.setWeek(Week);



        return timeInfo;

    }

    /**
     * 指定延迟运行时间
     * */
    public static void runDelayed(Runnable runnable, long time) {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }, time);
    }

    /**
     * 获取手机品牌
     */
    public static String getDeviceBrand() {
        return android.os.Build.BRAND;
    }

    /**
     * 获取手机型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }




}
