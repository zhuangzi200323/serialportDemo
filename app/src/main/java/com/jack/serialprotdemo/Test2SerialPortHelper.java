package com.jack.serialprotdemo;

import android.util.Log;

import com.jack.serialprot.SerialPortConfig;
import com.jack.serialprot.SerialPortHelper;

/**
 * 测试串口类，根据实际需要修改port与baudrate
 * @author jack
 */
public class Test2SerialPortHelper extends SerialPortHelper {
    private static final String TAG = Test2SerialPortHelper.class.getSimpleName();

    public Test2SerialPortHelper() {
        init();
    }

    public void init() {
        super.init(createSerialConfig());
    }

    private SerialPortConfig createSerialConfig() {
        SerialPortConfig serialConfig = new SerialPortConfig();
        serialConfig.port = "/dev/ttyUART0";

        serialConfig.dataBits = 8;
        serialConfig.parity = 0;
        serialConfig.stopBits = 1;
        serialConfig.flags = 0;
        serialConfig.baudrate = 115200;

        Log.d(TAG, serialConfig.toString());

        return serialConfig;
    }
}
