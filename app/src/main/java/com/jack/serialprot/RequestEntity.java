package com.jack.serialprot;

import java.nio.ByteBuffer;

/**
 * 命令请求
 * @author jack
 */
public class RequestEntity {
    /** 发送命令 */
    public ByteBuffer cmd;
    /** 发送数据后，返回数据的长度 */
    public int recvLen;
    public SerialPortCallback<byte[]> callback;
    /** 读超时时间 */
    public int readTimeoutMillis;

    public RequestEntity(ByteBuffer cmd, int recvLen, int readTimeoutMillis, SerialPortCallback<byte[]> callback) {
        this.cmd = cmd;
        this.recvLen = recvLen;
        this.callback = callback;
        this.readTimeoutMillis = readTimeoutMillis;
    }

}
