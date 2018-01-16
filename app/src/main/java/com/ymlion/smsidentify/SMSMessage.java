package com.ymlion.smsidentify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by YMlion on 2018/1/16.
 */

public class SMSMessage {
    long date;
    String address;
    String body;
    /**
     * 0：未读；1：已读
     */
    int read;
    int status;
    /**
     * 1：接收的消息；2：已发送消息
     */
    int type;
    /**
     * 是否已被选择
     */
    boolean checked = false;

    @Override
    public String toString() {
        return "SMSMessage{" +
                "date=" + formatDate(date) +
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

    /*private String getPerson(String address) {
        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, address);
            Cursor cursor;
            cursor = resolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        String name = cursor.getString(0);
                        return name;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public static String formatDate(long time) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return format.format(new Date(time));
    }

    public boolean isSent() {
        return type == 2;
    }

    public boolean isRead() {
        return read == 1;
    }
}
