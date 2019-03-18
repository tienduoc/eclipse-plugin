package com.nnthink.aixcoder.mina;

import java.io.IOException;
import java.io.Serializable;

import com.aixcoder.lib.JSON;

public class Message implements Serializable {
    private static final long serialVersionUID = 8213761673743652497L;
    public final static int sort = 1;
    public final static int notice = 2;
    public final static int active = 3;
    public final static int login = 4;
    int type = 0;//类型 0- 排序;1-通知;
    String data;//数据包
    private static Message message = new Message();

    public static Message build() {
        message.clear();
        return message;
    }

    public int getType() {
        return type;
    }

    public Message setType(int type) {
        this.type = type;
        return message;
    }

    public Message setData(String data) {
        this.data = data;
        return message;
    }

    private void clear() {
        type = 0;
        data = null;
    }

    public String getData() throws IOException {
        return this.data;
    }

    public String toJsonString() {
    	JSON json = new JSON();
    	json.put("type", type);
    	json.put("data", data);
        return json.toString();
    }

    public static Message toMessage(String s) {
    	JSON json = JSON.decode(s);
        Message m = new Message();
        m.type = json.getInt("type");
        m.data = json.getString("data");
        return m;
    }
}