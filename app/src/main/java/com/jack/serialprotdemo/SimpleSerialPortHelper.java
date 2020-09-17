package com.jack.serialprotdemo;

import android.os.SystemClock;
import android.util.Log;

import com.jack.serialport.SerialPort;
import com.jack.utils.ByteStringHexUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

/**
 * 操作串口简单helper类，单独的线程中进行读操作，可根据实际需要增加 interface，
 * 将接收到的数据回调到 ui线程进行相关处理
 * @author jack
 */
public class SimpleSerialPortHelper {
    private static final String TAG = SimpleSerialPortHelper.class.getSimpleName();
    private static volatile SimpleSerialPortHelper single;

    private SerialPort mSerialPort;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;

    private String mPort;
    private int mBaudRate = -1;
    private static boolean mIsOpen = false;

    public static SimpleSerialPortHelper getInstance() {
        if (null == single) {
            synchronized (SimpleSerialPortHelper.class) {
                if (null == single) {
                    single = new SimpleSerialPortHelper();
                }
            }
        }
        return single;
    }

    private SimpleSerialPortHelper() {
        this.mPort = "/dev/ttyUART1";
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

    public void open2() throws SecurityException, IOException, InvalidParameterException {
        mSerialPort = new SerialPort(new File(mPort), mBaudRate, 0, 8, 1, 0);
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

    public void sendHex(String sHex) {
        sHex = sHex.replace(" ", "");
        byte[] bOutArray = ByteStringHexUtils.HexToByteArr(sHex);
        send(bOutArray);
    }

    public void send(final byte[] bOutArray) {
        try {
            if (mIsOpen) {
                mOutputStream.write(bOutArray);
                mOutputStream.flush();
                Log.i(TAG, "send data: " + ByteStringHexUtils.printByteToString(bOutArray));
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
                read(!isInterrupted());
                //readNonBlock();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 阻塞方式读取
     * @param notInterrupted
     * @throws IOException
     */
    private void read(boolean notInterrupted) throws IOException {
        if (mInputStream == null) {
            return;
        }

        byte[] buff = new byte[1024];
        int len;
        String resultData = "";
        while (notInterrupted && ((len = mInputStream.read(buff)) != -1)) {
            byte[] temp = new byte[len];
            System.arraycopy(buff, 0, temp, 0, len);
            resultData = ByteStringHexUtils.ByteToString(temp);
            Log.e(TAG, "received data:" + resultData);
        }
    }

    /**
     * 轮询读
     * @throws IOException
     */
    private void readNonBlock() throws IOException {
        if (mInputStream == null) {
            return;
        }
        Log.e(TAG, "###### begin read thread ######");
        byte[] buff = new byte[1024];
        int len;
        int dataLen = -1;//所有数据长度
        byte sum = 0x00;
        String resultData = "";
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                break;
            }
            try {
                int available = mInputStream.available();
                if (available > 0) {
                    len = mInputStream.read(buff);
                    byte[] temp = new byte[len];
                    System.arraycopy(buff, 0, temp, 0, len);
                    resultData += ByteStringHexUtils.ByteToString(temp);
                    Log.e(TAG, "#######:" + resultData);
                } else {
                    // 暂停一点时间，免得一直循环造成CPU占用率过高
                    SystemClock.sleep(10);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Log.e(TAG, "###### end read thread ########");
    }
}