package com.example.nonsugar.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList
import android.os.RemoteException
import android.util.Log

import com.example.nonsugar.ipcsample.ISampleService
import com.example.nonsugar.ipcsample.ISampleServiceCallback
import com.example.nonsugar.ipcsample.InitializeListener
import org.greenrobot.eventbus.EventBus


/** クラス名 */
private const val TAG: String = "SampleService"

/**
 * 同期・非同期で通信可能なサービス
 */
class SampleService : Service() {
    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    external fun callbackFromC()

    /** コールバックのリスト */
    private val mCallbacks = RemoteCallbackList<ISampleServiceCallback>()

    /** 初期化リスナー */
    private val mInitListener = RemoteCallbackList<InitializeListener>()

    /** 非同期処理を実行するスレッド */
    private val mThread = BackgroundQueue()

    private val mNotifier = NativeNotifier()

    /** プロセス間IF実装 */
    private val mBinder = object : ISampleService.Stub() {
        /**
         * コールバック登録
         * @param callback 登録するコールバック
         */
        override fun registerCallback(callback: ISampleServiceCallback) {
            // 複数スレッドから参照されそうだし排他が必要かも
            mCallbacks.register(callback)
        }

        /**
         * コールバック登録解除
         * @param callback 登録解除するコールバック
         */
        override fun unregisterCallback(callback: ISampleServiceCallback) {
            // 複数スレッドから参照されそうだし排他が必要かも
            mCallbacks.unregister(callback)
        }

        /**
         * サービスから同期で文字列を取得
         * @return 文字列
         */
        override fun syncGetStringFromService(): String {
            Log.d(TAG, "syncGetStringFromService")

            // テストでここで呼ぶ
            callbackFromC()

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

        override fun retisterInitializeLitener(listener: InitializeListener) {
            Log.d(TAG, "retisterInitializeLitener")
            // 複数スレッドから参照されそうだし排他が必要かも
            mInitListener.register(listener)
        }

        override fun unretisterInitializeLitener(listener: InitializeListener) {
            Log.d(TAG, "unretisterInitializeLitener")
            // 複数スレッドから参照されそうだし排他が必要かも
            mInitListener.unregister(listener)
        }
    }

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        // 非同期実行用スレッドを起動
        mThread.startThread()
        initialize()
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "onBind")
        EventBus.getDefault().register(mNotifier)
        return mBinder
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(mNotifier)
        super.onDestroy()
    }

    private fun initialize() {
        mThread.enqueue(Runnable {
            // 重めな初期化処理を行う
            try {
                Thread.sleep(1000L)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            // リスナー登録より先にここにきてしまう場合も想定しないとダメかも
            val n = mInitListener.beginBroadcast()
            for (i in 0 until n) {
                try {
                    mInitListener.getBroadcastItem(i).onInitializeComplete()
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            mInitListener.finishBroadcast()
        })
    }
}