package com.example.nonsugar.service;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * ネイティブ側から呼び出すクラス
 */
public class NativeNotifier {
    private static final String TAG = NativeNotifier.class.getSimpleName();


    /**
     * ネイティブからJavaへ通知を送る
     * ネイティブ側で呼び出すメソッド この呼び出しを受けてJavaではonEventで処理を行う
     * @param data ネイティブからJavaへ渡すデータ
     */
    @SuppressWarnings("UnusedDeclaration") // ネイティブから呼ばれる
    public static void notifyFromNative(BaseNativeData data) {
        EventBus.getDefault().post(data);
    }

    /**
     * ネイティブからの通知を受信
     * ネイティブからnotifyFromNative()を呼ぶとJava側はここで処理を受け取る
     * @param data ネイティブから受け取ったデータ
     */
    @Subscribe
    @SuppressWarnings("UnusedDeclaration") // EventBusから呼ばれる
    public void onEvent(BaseNativeData data) {
        NativeData nativeData = (NativeData)data;
        Log.d(TAG, "onEvent mIntValue:" + nativeData.getIntValue());
    }
}
