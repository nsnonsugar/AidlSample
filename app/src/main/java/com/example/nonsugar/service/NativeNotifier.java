package com.example.nonsugar.service;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * ネイティブ側から呼び出すクラス
 */
public class NativeNotifier {
    private static final String TAG = NativeNotifier.class.getSimpleName();

    public static void notifyFromNative(NativeData data) {
        EventBus.getDefault().post(data);
    }

    @Subscribe
    public void onEvent(NativeData data) {
        int value = data.getIntValue();
        Log.d(TAG, "onEvent mIntValue:" + value);
    }
}
