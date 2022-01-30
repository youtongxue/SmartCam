package com.services.smartcam.EntityClass;

//返回给树莓派的信息类
public class PieInfo {

    private String date;//当前请求时间
    private Integer elevatorStatus;//机器状态
    private Integer lightStatus;
    private Integer number;//当前请求次数

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getElevatorStatus() {
        return elevatorStatus;
    }

    public void setElevatorStatus(Integer elevatorStatus) {
        this.elevatorStatus = elevatorStatus;
    }

    public Integer getLightStatus() {
        return lightStatus;
    }

    public void setLightStatus(Integer lightStatus) {
        this.lightStatus = lightStatus;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }
}
