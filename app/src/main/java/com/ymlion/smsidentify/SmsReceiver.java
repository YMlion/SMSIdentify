package com.ymlion.smsidentify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import static com.ymlion.smsidentify.util.SmsUtil.copyCode;
import static com.ymlion.smsidentify.util.SmsUtil.findCode;

/**
 * Created by YMlion on 2017/7/13.
 */

public class SmsReceiver extends BroadcastReceiver {

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
            copyCode(context, findCode(msgBody.toString()));
        }
    }
}
