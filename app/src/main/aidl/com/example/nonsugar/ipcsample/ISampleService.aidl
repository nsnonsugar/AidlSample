// ISampleService.aidl
package com.example.nonsugar.ipcsample;

import com.example.nonsugar.ipcsample.ISampleServiceCallback;

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
