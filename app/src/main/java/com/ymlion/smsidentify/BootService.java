package com.ymlion.smsidentify;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * 保持后台运行，并检查短信权限
 * Created by YMlion on 2017/7/13.
 */
public class BootService extends AccessibilityService {

    @Override public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_SMS)
                    == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "请授予读取短信的权限", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                        Uri.parse("package:" + getPackageName()))
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
        //getContentResolver().registerContentObserver(Uri.parse("content://sms"), true,
        //        new SmsObserver(null, this));
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override public void onInterrupt() {
    }
}