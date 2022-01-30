package com.services.smartcam.InterFace;

import com.services.smartcam.EntityClass.PieInfo;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface Pie {
    //定义请求接口部分URL地址以及请求方法
    @POST("/pie/setmachineinfo")
    Call<PieInfo> setMachineStatus(@Query("ElevatorStatus") Integer ElevatorStatus,@Query("LightStatus") Integer LightStatus);

    @GET("/pie/getmachineinfo")
    Call<PieInfo> getMachineStatus();

}
