package com.ymlion.smsidentify;

import android.app.Application;
import com.ymlion.smsidentify.util.CrashHandler;

/**
 * Created by YMlion on 2018/5/16.
 */
public class AppContext extends Application {

    @Override public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
    }
}
