package com.jack.utils;

import android.util.Log;

/**
 * 线程简单的封装
 * @author jack
 */
public class WorkThread extends Thread {
    private String TAG = WorkThread.class.getSimpleName();
    protected volatile boolean isStop = false;
    protected volatile boolean isPause = false;
    final Object control = new Object();

    public WorkThread() {}

    public WorkThread(Runnable runnable) {
        super(runnable);
    }

    /**
     * 新创建的线程，还未来得及运行，isAlive返回false，修改新创建的线程视为已经运行
     * @return
     */
    public boolean isRunning() {
        Log.e(TAG, "##### state = " + getState());
        if(getState() == State.NEW) {
            return true;
        }
        return isAlive();
    }

    public void pauseThread() {
        synchronized (control) {
            while (isPause) {
                try {
                    control.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop() {
        isStop = true;
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause() {
        isPause = true;
    }

    public void resumeThread() {
        if (isPause) {
            isPause = false;
            synchronized (control) {
                control.notifyAll();
            }
        }
    }

    public void stopThread() {
        isStop = true;
        resumeThread();
        interrupt();
    }
}
