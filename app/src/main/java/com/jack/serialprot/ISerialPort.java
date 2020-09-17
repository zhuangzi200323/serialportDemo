package com.jack.serialprot;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/**
 * 串口控制相关接口
 * @author jack
 */
public interface ISerialPort {
    /**
     * 初始化，传入串口基本信息
     * @param config
     */
    void init(SerialPortConfig config);

    /**
     * 获取配置
     * @return
     */
    SerialPortConfig getSerialConfig();

    /**
     * 打开串口设备
     * @throws SecurityException
     * @throws IOException
     * @throws InvalidParameterException
     */
    void open() throws SecurityException, IOException, InvalidParameterException;

    /**
     * 发送命令
     * @param data 命令数据
     * @param recvLen 接收长度，必须
     * @param timeout 接收超时，非必须
     * @param callback 回调
     * @return
     */
    boolean post(ByteBuffer data, int recvLen, int timeout, SerialPortCallback<byte[]> callback);

    boolean post(RequestEntity requestEntity);

    boolean post(byte[] bytes, int recvLen, int timeout, SerialPortCallback<byte[]> callback);

    /** 是否打开 */
    boolean isOpen();

    /** 判断是否在运行 */
    boolean isThreadRun();

    /** 关闭 */
    void close();
}
