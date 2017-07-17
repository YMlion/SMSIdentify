package com.ymlion.smsidentify;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by YMlion on 2017/7/13.
 */
public class BootService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}