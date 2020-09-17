package com.jack.serialprotdemo;

import android.util.Log;

import com.jack.serialprot.ISerialPort;

/**
 * 串口设备管理类，通过此类可以获得所有串口的helper
 * @author jack
 */
public final class SerialPortMgr {
    private static final String TAG = SerialPortMgr.class.getSimpleName();
    private volatile static SerialPortMgr instance;
    volatile ISerialPort test1SerialPortHelper;

    public static SerialPortMgr getInstance() {
        if (null == instance) {
            synchronized (SerialPortMgr.class) {
                if (null == instance) {
                    instance = new SerialPortMgr();
                    instance.init();
                }
            }
        }
        return instance;
    }

    private SerialPortMgr() {
    }

    public void init() {
        Log.d(TAG, "init");
    }

    /** 获得Test1串口帮助类 */
    public ISerialPort getTest1SerialPortHelper() {
        if (null == test1SerialPortHelper) {
            initTest1SerialPortHelper();
        }

        return test1SerialPortHelper;
    }

    private synchronized void initTest1SerialPortHelper() {
        if (null == test1SerialPortHelper) {
            test1SerialPortHelper = new Test1SerialPortHelper();
        }
    }
}
