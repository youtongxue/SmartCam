package com.services.smartcam.EntityClass;

public class MqttReceive {
    private String topic;
    private int Qos;
    private String msg;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getQos() {
        return Qos;
    }

    public void setQos(int qos) {
        Qos = qos;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
