package com.example.nonsugar.ipcsample

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.content.Intent
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import com.example.nonsugar.service.SampleService

/** クラス名 */
private const val TAG = "MainActivity"

/**
 * サービスと同期・非同期通信を行う
 */
class MainActivity : AppCompatActivity() {
    /** プロセス間通信IF */
    private var mBinder: ISampleService? = null

    /** テキストビュー更新用 */
    private val mHandler = Handler()

    /** サービスから取得した文字列を表示するテキストビュー */
    private var mTextView: TextView? = null

    /**
     * ServiceConnectionの実装
     */
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            Log.d(TAG, "onServiceConnected")
            mBinder = ISampleService.Stub.asInterface(iBinder)
            // コールバックの登録
            mBinder?.registerCallback(mCallback)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            mBinder = null
        }
    }

    /**
     * サービスに登録するコールバック
     */
    private val mCallback = object : ISampleServiceCallback.Stub() {
        override fun asyncGetStringCallback(string: String) {
            Log.d(TAG, "asyncString:$string")

            mHandler.post {
                // サービスから取得した文字列をテキストビューに反映
                mTextView?.text = string
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        // サービスを起動
        val service = Intent(application, SampleService::class.java)
        startService(service)

        setContentView(R.layout.activity_main)
        mTextView = findViewById(R.id.sample_text)

        // 同期ボタン押下時の処理を登録
        findViewById<Button>(R.id.syncButton).setOnClickListener {
            Log.d(TAG, "SyncButton setOnClickListener")
            try {
                // サービスから文字列を取得する mBinderがnullの時は空文字を返す
                mTextView?.text = mBinder?.syncGetStringFromService() ?: ""
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 非同期ボタン押下時の処理を登録
        findViewById<Button>(R.id.asyncButton).setOnClickListener {
            Log.d(TAG, "AyncButton setOnClickListener")
            try {
                // 非同期でサービスから文字列を取得
                mBinder?.asyncGetStringFromService()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
