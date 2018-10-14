package com.example.nonsugar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log

import com.example.nonsugar.ipcsample.ISampleService
import com.example.nonsugar.ipcsample.ISampleServiceCallback


/** クラス名 */
private const val TAG: String = "SampleService"

/**
 * 同期・非同期で通信可能なサービス
 */
class SampleService : Service() {
    /** コールバックのリスト */
    private val mCallbacks = RemoteCallbackList<ISampleServiceCallback>()

    /** 非同期処理を実行するスレッド */
    private val mThread = BackgroundQueue()

    /** プロセス間IF実装 */
    private val mBinder = object : ISampleService.Stub() {
        /**
         * コールバック登録
         * @param callback 登録するコールバック
         */
        override fun registerCallback(callback: ISampleServiceCallback) {
            mCallbacks.register(callback)
        }

        /**
         * コールバック登録解除
         * @param callback 登録解除するコールバック
         */
        override fun unregisterCallback(callback: ISampleServiceCallback) {
            mCallbacks.unregister(callback)
        }

        /**
         * サービスから同期で文字列を取得
         * @return 文字列
         */
        override fun syncGetStringFromService(): String {
            Log.d(TAG, "syncGetStringFromService")
            return "Sync String"
        }

        /**
         * サービスから非同期で文字列を取得
         * 文字列はregisterCallbackで登録されたコールバックから返される
         */
        override fun asyncGetStringFromService() {
            Log.d(TAG, "asyncGetStringFromService")
            // ワーカースレッド上で実行する処理をワークキューに入れる
            mThread.enqueue(Runnable {
                // 非同期実行っぽくするため少し待つ
                try {
                    Thread.sleep(3000L)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                // コールバック開始
                val n = mCallbacks.beginBroadcast()
                for (i in 0 until n) {
                    try {
                        mCallbacks.getBroadcastItem(i).asyncGetStringCallback("async string")
                    } catch (e: RemoteException) {
                        e.printStackTrace()
                    }
                }
                mCallbacks.finishBroadcast()
            })
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        // 非同期実行用スレッドを起動
        mThread.startThread()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind")
        return mBinder
    }
}