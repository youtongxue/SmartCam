package com.services.smartcam.EntityClass;

public class Mqtt {
    private int elevatorStatus;//电梯状态
    private int lightStatus;//预警灯状态
    private String date;//时间
    private String device;//设备标识

    public int getElevatorStatus() {
        return elevatorStatus;
    }

    public void setElevatorStatus(int elevatorStatus) {
        this.elevatorStatus = elevatorStatus;
    }

    public int getLightStatus() {
        return lightStatus;
    }

    public void setLightStatus(int lightStatus) {
        this.lightStatus = lightStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}
