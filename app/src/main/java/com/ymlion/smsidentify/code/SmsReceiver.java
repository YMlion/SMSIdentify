package com.ymlion.smsidentify.code;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.ymlion.smsidentify.util.SmsUtil.copyCode;
import static com.ymlion.smsidentify.util.SmsUtil.findCode;

/**
 * new sms receiver
 *
 * Created by YMlion on 2017/7/13.
 */

public class SmsReceiver extends BroadcastReceiver {

    private static final int REGEX_TYPE = 4;
    private String codeResult;
    private volatile int flag = 0;
    private CountDownLatch mLatch;
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

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
        mLatch = new CountDownLatch(REGEX_TYPE);
        find(msg, "验证码");
        find(msg, "随机码");
        find(msg, "动态码");
        find(msg, "verification code");
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        copyCode(context, codeResult);
    }

    private void find(String msg, String key) {
        executor.submit(() -> {
            if (flag >= REGEX_TYPE) {
                mLatch.countDown();
                return;
            }
            String code = findCode(msg, key);
            if (code != null) {
                codeResult = code;
                flag = REGEX_TYPE;
            }
            mLatch.countDown();
        });
    }
}
