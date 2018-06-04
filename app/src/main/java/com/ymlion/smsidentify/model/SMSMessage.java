package com.ymlion.smsidentify.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * sms bean
 * Created by YMlion on 2018/1/16.
 */

public class SMSMessage {
    public long id;
    public long date;
    public String address;
    public String body;
    /**
     * 0：未读；1：已读
     */
    public int read;
    public int status;
    /**
     * 1：接收的消息；2：已发送消息
     */
    public int type;
    /**
     * 是否已被选择
     */
    public boolean checked = false;

    @Override
    public String toString() {
        return "SMSMessage{" + " id=" + id + ", date=" + formatDate(date) +
                ", address='" + address + '\'' +
                ", body='" + body + '\'' +
                ", read='" + getMessageRead(read) + '\'' +
                ", status='" + getMessageStatus(status) + '\'' +
                ", type='" + getMessageType(type) + '\'' +
                '}';
    }

    private String getMessageRead(int anInt) {
        if (1 == anInt) {
            return "已读";
        }
        if (0 == anInt) {
            return "未读";
        }
        return null;
    }

    private String getMessageType(int anInt) {
        if (1 == anInt) {
            return "收到的";
        }
        if (2 == anInt) {
            return "已发出";
        }
        return null;
    }

    private String getMessageStatus(int anInt) {
        switch (anInt) {
            case -1:
                return "接收";
            case 0:
                return "complete";
            case 64:
                return "pending";
            case 128:
                return "failed";
            default:
                break;
        }
        return null;
    }

    public static String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(time));
    }

    public boolean isReceived() {
        return type == 1;
    }

    public boolean isUnread() {
        return read == 0;
    }
}
