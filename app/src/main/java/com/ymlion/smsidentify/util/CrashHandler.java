package com.ymlion.smsidentify.util;

import android.content.Context;
import android.content.Intent;
import com.ymlion.smsidentify.SmsCleanActivity;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by YMlion on 2018/5/16.
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Context mContext;

    public CrashHandler(Context context) {
        mContext = context;
    }

    @Override public void uncaughtException(Thread t, Throwable e) {
        String msg = getCrashMsg(e);
        File crashDir = new File(mContext.getExternalCacheDir(), "crash");
        crashDir.mkdir();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String fileName = dateFormat.format(System.currentTimeMillis());
        File crashFile = new File(crashDir, fileName + ".txt");
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(crashFile));
            out.write(msg.getBytes(Charset.defaultCharset()));
            out.flush();
            out.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        Intent intent = new Intent(mContext, SmsCleanActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
        System.exit(1);
    }

    private String getCrashMsg(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        while (e != null) {
            e.printStackTrace(printWriter);
            e = e.getCause();
        }
        return stringWriter.toString();
    }
}
