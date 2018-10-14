package com.example.nonsugar.ipcsample

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.nonsugar.service.SampleService

/** クラス名 */
private const val TAG = "SplashActivity"

/**
 * サービスの初期化を待って画面遷移する
 */
class SplashActivity : AppCompatActivity() {
    /** プロセス間通信IF */
    private var mBinder: ISampleService? = null

    /** テキストビュー更新用 */
    private val mHandler = Handler()

    /**
     * ServiceConnectionの実装
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "onServiceConnected")
            mBinder = ISampleService.Stub.asInterface(iBinder)
            // 初期化リスナーの登録
            // リスナーの登録より先に初期化が終わってしまったケースも考慮すべき
            mBinder?.retisterInitializeLitener(mListener)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            mBinder = null
        }
    }

    /**
     * 初期化リスナー
     */
    private val mListener = object : InitializeListener.Stub() {
        override fun onInitializeComplete() {
            Log.d(TAG, "onInitializeComplete")

            mHandler.post {
                intent = Intent(this@SplashActivity, MainActivity::class.java)
                startActivity(intent)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")

        // サービスをバインド
        intent = Intent(this, SampleService::class.java)
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")

        // アクティビティが停止するときはServiceとの接続を切る
        unbindService(mServiceConnection)
    }
}