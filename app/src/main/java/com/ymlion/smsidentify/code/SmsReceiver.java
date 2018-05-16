package com.ymlion.smsidentify.code;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import static com.ymlion.smsidentify.util.SmsUtil.copyCode;
import static com.ymlion.smsidentify.util.SmsUtil.findCode;

/**
 * new sms receiver
 *
 * Created by YMlion on 2017/7/13.
 */

public class SmsReceiver extends BroadcastReceiver {

    private String codeResult;
    private volatile int flag = 0;

    @Override public void onReceive(Context context, Intent intent) {
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
            findAndCopy(context, msgBody.toString());
        }
    }

    private void findAndCopy(Context context, String msg) {
        find(msg, "验证码");
        find(msg, "随机码");
        find(msg, "verification code");
        while (flag < 3) {
            // do nothing
        }
        copyCode(context, codeResult);
    }

    private void find(String msg, String key) {
        new Thread(() -> {
            if (flag >= 3) {
                return;
            }
            String code = findCode(msg, key);
            if (code != null) {
                codeResult = code;
                flag = 3;
            } else {
                flag++;
            }
        }).start();
    }
}
