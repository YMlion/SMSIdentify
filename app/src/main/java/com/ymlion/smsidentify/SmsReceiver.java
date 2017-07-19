package com.ymlion.smsidentify;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YMlion on 2017/7/13.
 */

public class SmsReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        SmsMessage msg;

        if (null != bundle) {
            Object[] smsObj = (Object[]) bundle.get("pdus");
            if (smsObj == null) {
                return;
            }
            StringBuilder msgBody = new StringBuilder();
            for (Object object : smsObj) {
                msg = SmsMessage.createFromPdu((byte[]) object);
                msgBody.append(msg.getDisplayMessageBody());
            }
            String code = findCode(msgBody.toString());

            if (code != null) {
                ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("SMS", code);
                cm.setPrimaryClip(clip);
                Toast.makeText(context, "验证码：" + code + " 已经复制到剪贴板", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static String findCode(String msg) {
        if (msg.length() > 3 && msg.contains("验证码")) {
            String regEx="(\\d{6})|(\\d{4})";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(msg);
            if (m.find()) {
                return m.group();
            }
        }

        return null;
    }
}
