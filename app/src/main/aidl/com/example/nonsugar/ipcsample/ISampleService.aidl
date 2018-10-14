// ISampleService.aidl
package com.example.nonsugar.ipcsample;

import com.example.nonsugar.ipcsample.ISampleServiceCallback;
import com.example.nonsugar.ipcsample.InitializeListener;

 /**
 * SampleServiceとのプロセス間通信を行うIF
 */
interface ISampleService {
    /**
     * 同期でサービスから文字列を取得する
     */
    String syncGetStringFromService();

    /**
     * 非同期でサービスから文字列を取得する
     */
    void asyncGetStringFromService();

    /**
     * 初期化リスナー登録
     * @param listener 登録するリスナー
     */
     oneway void retisterInitializeLitener(InitializeListener listener);

     /**
      * 初期化リスナー登録解除
      * @param listener 登録解除するリスナー
      */
      oneway void unretisterInitializeLitener(InitializeListener listener);

    /**
     * コールバック登録
     * @param callback 登録するコールバック
     */
    oneway void registerCallback(ISampleServiceCallback callback);

    /**
     * コールバック登録解除
     * @param callback 登録解除するコールバック
     */
    oneway void unregisterCallback(ISampleServiceCallback callback);
}
