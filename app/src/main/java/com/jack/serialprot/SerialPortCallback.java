package com.jack.serialprot;

/**
 * 串口操作后，回调接口
 * @author jack
 * @param <T>
 */
public interface SerialPortCallback<T> {
    void success(T result);
    void failed(int errCode, String errInfo, String result);
}
