package com.services.smartcam;

import com.services.smartcam.InterFace.EzTokenApi;
import com.services.smartcam.InterFace.Pie;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkFactory {
    private static Pie api;
    public static EzTokenApi ez;
    private static final Retrofit retrofit;


    //创建retrofit实例
    static {
        retrofit = new Retrofit.Builder()
                //设置网络请求BaseUrl地址
                .baseUrl("https://open.ys7.com/")//这里是服务器的IP地址，域名映射需要ICN备案，更没有SSL证书
                //设置数据解析器
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    //图书馆
    public static Pie pieApi(){
        if (null == api){
            api = retrofit.create(Pie.class);
        }
        return api;
    }

    //萤石token
    public static EzTokenApi ezToken(){
        if (null == ez){
            ez = retrofit.create(EzTokenApi.class);
        }
        return ez;
    }

}
