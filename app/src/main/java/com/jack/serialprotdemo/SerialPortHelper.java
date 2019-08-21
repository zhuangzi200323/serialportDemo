package com.jack.serialprotdemo;

import android.util.Log;

import com.jack.serialport.SerialPort;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * 操作串口helper类
 */
public class SerialPortHelper {
    private static final String TAG = SerialPortHelper.class.getSimpleName();
    private static volatile SerialPortHelper single;

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    private String mPort;
    private int mBaudRate = -1;
    private static boolean mIsOpen = false;

    public static SerialPortHelper getInstance() {
        if (null == single) {
            synchronized (SerialPortHelper.class) {
                if (null == single) {
                    single = new SerialPortHelper();
                }
            }
        }
        return single;
    }

    private SerialPortHelper() {
        this.mPort = "/dev/ttyS4";
        this.mBaudRate = 115200;
    }

    public void open() throws SecurityException, IOException, InvalidParameterException {
        mSerialPort = new SerialPort(new File(mPort), mBaudRate, 0);
        mOutputStream = mSerialPort.getOutputStream();
        mInputStream = mSerialPort.getInputStream();

        if (mReadThread == null) {
            mReadThread = new ReadThread();
            mReadThread.start();
        }
        if (mOutputStream != null && mInputStream != null) {
            mIsOpen = true;
        } else {
            Log.e(TAG, "open serial port err. ");
        }
    }

    public void close() {
        try {
            if (mReadThread != null) {
                mReadThread.interrupt();
            }
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mSerialPort != null) {
                mSerialPort.close();
                mSerialPort = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIsOpen = false;
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void sendTxt(String sTxt) {
        sTxt = sTxt.replace(" ", "");
        byte[] bOutArray = sTxt.getBytes();
        send(bOutArray);
    }

    public void send(final byte[] bOutArray) {
        try {
            if (mIsOpen) {
                mOutputStream.write(bOutArray);
                mOutputStream.flush();
                Log.i(TAG, "send data: " + Tool.printByteToString(bOutArray));
            } else {
                Log.i(TAG, "comm is closed");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ReadThread extends Thread {
        @Override
        public void run() {
            super.run();
            try {
                newRead(!isInterrupted());
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    private void newRead(boolean notInterrupted) throws IOException {
        if (mInputStream == null) {
            return;
        }

        byte[] buff = new byte[1024];
        int len;
        String resultData = "";
        while (notInterrupted && ((len = mInputStream.read(buff)) != -1)) {
            byte[] temp = new byte[len];
            System.arraycopy(buff, 0, temp, 0, len);
            resultData = Tool.ByteToString(temp);
            Log.e(TAG, "received data:" + resultData);
        }
    }

    private ISerialPortReceiveData iSerialPortReceiveData;

    public void setiSerialPortReceiveData(ISerialPortReceiveData iSerialPortReceiveData) {
        this.iSerialPortReceiveData = iSerialPortReceiveData;
    }

    public interface ISerialPortReceiveData {
        void onDataReceived(String data);
    }
}