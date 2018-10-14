// ISampleServiceCallback.aidl
package com.example.nonsugar.ipcsample;

/**
 * サービスからのコールバック
 */
interface ISampleServiceCallback {
    /**
     * ISampleService.Stub#asyncGetStringFromServiceを呼んだ時のコールバック
     * @param string サービスから返される文字列
     */
    void asyncGetStringCallback(String string);
}
