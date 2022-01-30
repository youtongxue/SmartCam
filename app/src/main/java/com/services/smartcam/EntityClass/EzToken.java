package com.services.smartcam.EntityClass;

/**
 * Auto-generated: 2021-09-30 20:7:21
 *
 * @author json.cn (i@json.cn)
 * @website http://www.json.cn/java2pojo/
 */
public class EzToken {

    private String msg;
    private String code;
    private Data data;
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setData(Data data) {
        this.data = data;
    }
    public Data getData() {
        return data;
    }

    public class Data {

        private String accessToken;
        private long expireTime;
        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
        public String getAccessToken() {
            return accessToken;
        }

        public void setExpireTime(long expireTime) {
            this.expireTime = expireTime;
        }
        public long getExpireTime() {
            return expireTime;
        }

    }

}