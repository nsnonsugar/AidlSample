package com.example.nonsugar.service

import java.util.LinkedList

/**
 * バックグラウンドで処理を行うワークキュー
 */
class BackgroundQueue {
    /** 実行ブロック格納キュー */
    private val mQueue: LinkedList<Runnable> = LinkedList()

    /** キューに入っている処理を実行するスレッド */
    private val mThread = WorkerThread()

    /** mQueue用の排他制御ロック */
    private val mLock = java.lang.Object()

    /**
     * バックグラウンド実行用のスレッドを起動する
     */
    fun startThread() {
        mThread.start()
    }

    /**
     * バックグラウンド処理実行
     * @param  block mThread上で実行したい処理
     */
    fun enqueue(block: Runnable) {
        synchronized(mLock) {
            mQueue.addLast(block)
            mLock.notify()
        }
    }

    /**
     * ワーカースレッド
     */
    private inner class WorkerThread : Thread() {
        override fun run() {
            var block: Runnable? = null
            while (true) {
                synchronized(mLock) {
                    while (mQueue.isEmpty()) {
                        try {
                            mLock.wait()
                        } catch (ignored: InterruptedException) {
                            ignored.printStackTrace()
                        }

                    }
                    block = mQueue.removeFirst()
                }

                try {
                    block?.run()
                } catch (e: RuntimeException) {
                    e.printStackTrace()
                }
            }
        }
    }
}