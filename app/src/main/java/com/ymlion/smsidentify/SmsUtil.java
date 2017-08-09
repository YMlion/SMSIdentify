package com.ymlion.smsidentify;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.widget.Toast;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by YMlion on 2017/8/9.
 */

class SmsUtil {
    static String findCode(String msg) {
        if (msg.length() > 3 && msg.contains("验证码") || msg.contains("verification code")) {
            String regEx = "(\\d{6})|(\\d{4})";
            Pattern p = Pattern.compile(regEx);
            Matcher m = p.matcher(msg);
            if (m.find()) {
                return m.group();
            }
        }

        return null;
    }

    static void copyCode(Context context, String code) {
        if (code != null) {
            ClipboardManager cm =
                (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("SMS", code);
            cm.setPrimaryClip(clip);
            Toast.makeText(context, "验证码：" + code + " 已经复制到剪贴板", Toast.LENGTH_SHORT).show();
        }
    }
}
