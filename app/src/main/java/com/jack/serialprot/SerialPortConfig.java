package com.jack.serialprot;

/**
 * 串口配置类
 * @author jack
 */
public class SerialPortConfig {
    /** 串口设备位置，比如/dev/ttyUART1 */
    public String port;
    /** 数据位，5 - 8 */
    public int dataBits = 8;
    /** 奇偶校验，0 None, 1 奇校验, 2 偶校验 */
    public int parity = 0;
    /** 停止位 1 或 2 */
    public int stopBits = 1;
    /** flag未使用到传0 */
    public int flags = 0;
    /** 波特率 */
    public int baudrate = 115200;

    @Override
    public String toString() {
        return "SerialPortConfig{" +
                "port='" + port + '\'' +
                ", dataBits=" + dataBits +
                ", parity=" + parity +
                ", stopBits=" + stopBits +
                ", flags=" + flags +
                ", baudrate=" + baudrate +
                '}';
    }
}
