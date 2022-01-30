package com.services.smartcam.InterFace;

import com.services.smartcam.EntityClass.EzToken;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EzTokenApi {
    //定义请求接口部分URL地址以及请求方法
    @POST("/api/lapp/token/get")
    Call<EzToken> getToken(@Query("appKey") String appKey, @Query("appSecret") String appSecret);
}
