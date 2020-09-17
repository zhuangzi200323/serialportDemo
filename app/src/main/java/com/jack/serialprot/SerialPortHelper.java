package com.jack.serialprot;

import android.os.SystemClock;
import android.util.Log;

import com.jack.serialport.SerialPort;
import com.jack.utils.ErrorCode;
import com.jack.utils.IOUtils;
import com.jack.utils.ByteStringHexUtils;
import com.jack.utils.WorkThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * 串口控制 helper类，实现ISerialPort接口
 * @author jack
 */
public class SerialPortHelper implements ISerialPort {
    private static final String TAG = SerialPortHelper.class.getSimpleName();
    public static final int BUFF_SIZE = 512;
    /** 最大接收buffer 大小 */
    public static final int MAX_RECV_SIZE = 1024;
    /** 修改读取超时 3 秒，3秒读不到结果就认为操作失败 */
    public static final int READ_TIME_OUT_MS = 3 * 1000;
    public static final int READ_TIME_OUT_MS_10 = 10 * 1000;
    /** 最小发送间隔时间，单位 ms 如果连续发生命令太快，会导致发生失败 */
    public static final long MIN_SEND_INTERVAL = 1000;
    /** for debug */
    private static final boolean DEBUG = true;

    protected SerialPort serialPort = null;
    protected OutputStream outputStream = null;
    protected InputStream inputStream = null;
    protected volatile SendAndReturnResultThread sarrThread = null;
    /** 串口配置 */
    protected SerialPortConfig serialConfig;
    /** 判断串口是否打开 */
    protected volatile boolean isOpen = false;
    protected BlockingQueue<RequestEntity> cmdQueue = new LinkedBlockingQueue<>();

    @Override
    public void init(SerialPortConfig serialConfig) {
        this.serialConfig = serialConfig;
        startReadThread();
    }

    @Override
    public void open() throws SecurityException, IOException, InvalidParameterException {
        if (isOpen) {
            Log.d(TAG, "already open");
            return;
        }

        if (serialConfig == null) {
            throw new InvalidParameterException("serialConfig is null, please invoke init function before open.");
        }

        if (serialConfig.port == null || serialConfig.port.isEmpty()) {
            throw new InvalidParameterException("serialConfig port is null or empty, please set right path.");
        }

        SerialPort.Builder builder = SerialPort.newBuilder(new File(serialConfig.port), serialConfig.baudrate);
        builder.dataBits(serialConfig.dataBits);
        builder.parity(serialConfig.parity);
        builder.stopBits(serialConfig.stopBits);
        builder.flags(serialConfig.flags);

        //如果打不开设备，会抛异常
        serialPort = builder.build();

        outputStream = serialPort.getOutputStream();
        inputStream = serialPort.getInputStream();

        if (outputStream != null && inputStream != null) {
            isOpen = true;
        } else {
            Log.e(TAG, "open serial port error, outputStream is null or inputStream is null.");
        }
    }

    public void startReadThread() {
        if (sarrThread != null && sarrThread.isRunning()) {
            Log.d(TAG, "readThread already start");
            return;
        }

        sarrThread = new SendAndReturnResultThread();
        sarrThread.start();
    }

    protected boolean reopenDevice() {
        Log.i(TAG, "reopen serial port");
        isOpen = false;

        if (inputStream != null) {
            IOUtils.safeClose(inputStream);
            inputStream = null;
        }
        if (outputStream != null) {
            IOUtils.safeClose(outputStream);
            outputStream = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }

        //关闭之后延时1秒，确保关闭，再打开
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            open();
        } catch (Exception e) {
            Log.e(TAG, "open error ", e);
            return false;
        }

        Log.d(TAG, "reopenDevice ok");

        return true;
    }

    /** 关闭串口设备 */
    @Override
    public void close() {
        isOpen = false;
        if (inputStream != null) {
            IOUtils.safeClose(inputStream);
            inputStream = null;
        }
        if (outputStream != null) {
            IOUtils.safeClose(outputStream);
            outputStream = null;
        }
        if (sarrThread != null) {
            sarrThread.stopThread();
            sarrThread = null;
        }
        if (serialPort != null) {
            serialPort.close();
            serialPort = null;
        }
        cmdQueue.clear();
        Log.e(TAG, " close ");
    }

    @Override
    public SerialPortConfig getSerialConfig() {
        return serialConfig;
    }

    @Override
    public boolean isThreadRun() {
        if (sarrThread != null && sarrThread.isRunning()) {
            return true;
        }

        return false;
    }

    @Override
    public boolean post(RequestEntity cmdReqEntity) {
        try {
            //recvLen 不能小于 0，也就是每一命令必须有返回
            if (cmdReqEntity.recvLen <= 0) {
                if (cmdReqEntity.callback != null) {
                    cmdReqEntity.callback.failed(ErrorCode.CODE_ERROR, "post cmd failed recvLen <= 0", null);
                }
                return false;
            }
            //recvLen 也不能太大，目前限制 MAX_RECV_SIZE
            if (cmdReqEntity.recvLen >= MAX_RECV_SIZE) {
                if (cmdReqEntity.callback != null) {
                    cmdReqEntity.callback.failed(ErrorCode.CODE_ERROR, "post cmd failed recvLen >= " + MAX_RECV_SIZE, null);
                }
                return false;
            }

            //如果线程停止，发送命令之前开启
            if (!isThreadRun()) {
                //先清空一下之前的命令
                cmdQueue.clear();
                startReadThread();
            }

            //修改命令直接发送到 queue
            cmdQueue.put(cmdReqEntity);
            return true;
        } catch (Exception e) {
            if (cmdReqEntity.callback != null) {
                cmdReqEntity.callback.failed(ErrorCode.CODE_ERROR, "post cmd failed " + e, null);
            }
        }

        return false;
    }

    @Override
    public boolean post(ByteBuffer data, int recvLen, int timeout, SerialPortCallback<byte[]> callback) {
        return post(new RequestEntity(data, recvLen, timeout, callback));
    }

    @Override
    public boolean post(byte[] bytes, int recvLen, int timeout, SerialPortCallback<byte[]> callback) {
        return post(new RequestEntity(ByteBuffer.wrap(bytes), recvLen, timeout, callback));
    }

    private boolean send(byte[] bOutArray, int off, int size) {
        boolean ok = realSend(bOutArray, off, size);
        if (!ok) {
            //发送失败，重新打开设备并再试一次
            boolean startOk = reopenDevice();
            if (startOk) {
                ok = realSend(bOutArray, off, size);
            }
        }
        return ok;
    }

    private boolean realSend(byte[] bOutArray, int off, int size) {
        try {
            if (isOpen) {
                synchronized (this) {
                    outputStream.write(bOutArray, off, size);
                    outputStream.flush();
                }
                Log.i(TAG, "send data: " + ByteStringHexUtils.printByteToString(bOutArray));
            } else {
                Log.e(TAG, "send error isOpen = false");
                return false;
            }
        } catch (Throwable e) {
            Log.e(TAG, "send data error", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    private class SendAndReturnResultThread extends WorkThread {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFF_SIZE];

        int readTimeOutMs = READ_TIME_OUT_MS; //读取超时
        long lastSendTime = 0;

        @Override
        public void run() {
            Log.d(TAG, "ReadThread run");

            //如果是报错，只要用户没有停止，就继续运行
            while (!isStop()) {
                try {
                    doWork();
                } catch (Throwable e) {
                    Log.e(TAG, "thread error ", e);
                }
            }

            //清空之前的命令
            cmdQueue.clear();
            Log.e(TAG, "ReadThread stop");
        }

        private void doWork() throws IOException, InterruptedException {
            long now;

            while (!isStop()) {
                RequestEntity requestEntity = cmdQueue.take();
                if (requestEntity.cmd == null || requestEntity.cmd.limit() <= 0) {
                    if (requestEntity.callback != null) {
                        requestEntity.callback.failed(ErrorCode.CODE_ERROR, "cmd is empty ", null);
                    }
                    continue;
                }

                //如果连续发生命令太快，会导致发生失败，因此记录并且让两次命令间隔大于1000
                now = SystemClock.uptimeMillis();
                if (lastSendTime == 0) {
                    lastSendTime = now;
                } else {
                    long diff = now - lastSendTime;
                    if (diff >= 0 && diff < MIN_SEND_INTERVAL) {
                        Thread.sleep(MIN_SEND_INTERVAL);
                    }
                    lastSendTime = SystemClock.uptimeMillis();
                }

                boolean ok = send(requestEntity.cmd.array(), requestEntity.cmd.position(), requestEntity.cmd.limit());
                if (!ok) {
                    if (requestEntity.callback != null) {
                        requestEntity.callback.failed(ErrorCode.CODE_ERROR, "send failed isOpen = " + isOpen, null);
                    }

                    continue;
                }

                readData(requestEntity);
            }
        }

        void onError(RequestEntity requestEntity, String errorInfo) {
            if (requestEntity.callback != null) {
                requestEntity.callback.failed(ErrorCode.CODE_ERROR, errorInfo, "");
            }
        }

        public int readDataEx(InputStream inputStream, int readTimeOutMs, byte[] buffer) throws IOException, TimeoutException {
            boolean isTimeout = false;
            long startTime = SystemClock.uptimeMillis();
            int availableSize;
            long useTime;

            do {
                availableSize = inputStream.available();
                if (availableSize > 0) {
                    break;
                }

                try {
                    //从100ms 上调 300，因为日志显示读取超时
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                useTime = SystemClock.uptimeMillis() - startTime;

                isTimeout = useTime > readTimeOutMs;
                if (isTimeout) {
                    //读时间超时
                    throw new TimeoutException();
                }
            } while (!isStop());

            int readSize = 0;
            int n;

            //修改一直读，读空可用buffer
            do {
                n = inputStream.read(buffer, readSize, availableSize - readSize);
                if (n > 0) {
                    readSize += n;

                    if (readSize >= availableSize) {
                        break;
                    }
                } else {
                    //n <= 0 出错，返回size
                    return n;
                }
            } while (!isStop());

            return readSize;
        }

        private void readData(RequestEntity requestEntity) throws IOException {
            baos.reset();

            //避免发送之后立刻读取引起读取错误，添加延时，延时之后再去读
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (!isStop()) {
                if (inputStream == null) {
                    onError(requestEntity, "error inputStream == null");
                    return;
                }

                int size = 0;
                int readTimeOut = requestEntity.readTimeoutMillis;
                if (readTimeOut <= 0) {
                    readTimeOut = readTimeOutMs;
                }

                try {
                    size = readDataEx(inputStream, readTimeOut, buffer);
                } catch (TimeoutException e) {
                    //如果读取超时超过10秒, 读取超时, 按正常数据读取
                    if (requestEntity.readTimeoutMillis >= READ_TIME_OUT_MS_10 && baos.size() > 0) {
                        byte[] bytes = baos.toByteArray();
                        baos.reset();

                        onDataReceived(requestEntity, bytes);
                        break;
                    }

                    //读时间超时
                    String errInfo = "read time out " + readTimeOut + " ms";
                    Log.e(TAG, errInfo);
                    onError(requestEntity, errInfo);
                    break;
                }

                if (size <= 0) {
                    Log.e(TAG, "inputStream close");
                    onError(requestEntity, "error inputStream close ");
                    break;
                }

                if (size > 0) {
                    if ((baos.size() + size) > MAX_RECV_SIZE) {
                        String errInfo = "recv buff size() > MAX_RECV_SIZE error. buff.size= " + baos.size() + ", MAX_RECV_SIZE=" + MAX_RECV_SIZE;
                        Log.e(TAG, errInfo);

                        onError(requestEntity, errInfo);
                        break;
                    }

                    baos.write(buffer, 0, size);

                    if (requestEntity.recvLen > 0 && baos.size() >= requestEntity.recvLen) {
                        byte[] bytes = baos.toByteArray();
                        baos.reset();

                        onDataReceived(requestEntity, bytes);
                        break;
                    } else if (requestEntity.recvLen <= 0) {
                        byte[] bytes = baos.toByteArray();
                        baos.reset();

                        onDataReceived(requestEntity, bytes);
                        break;
                    }
                }
            }
        }

        //处理数据
        private void onDataReceived(RequestEntity requestEntity, byte[] data) {
            if (requestEntity.callback != null) {
                try {
                    requestEntity.callback.success(data);
                } catch (Throwable e) {
                    Log.e(TAG, "data error ", e);

                    requestEntity.callback.failed(ErrorCode.ERROR_EXCEPTION, "error " + e, null);
                }
            }
        }
    }
}
