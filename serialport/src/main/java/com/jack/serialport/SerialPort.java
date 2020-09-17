/*
 * Copyright 2009 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package com.jack.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    private static final String TAG = "SerialPort";
    private static final String DEFAULT_SU_PATH = "/system/bin/su";
    private static String sSuPath = DEFAULT_SU_PATH;

    /**
     * Set the su binary path, the default su binary path is {@link #DEFAULT_SU_PATH}
     *
     * @param suPath su binary path
     */
    public static void setSuPath(String suPath) {
        if (suPath == null) {
            return;
        }
        sSuPath = suPath;
    }

    /**
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    public SerialPort(File device, int baudrate) throws SecurityException, IOException {
        this(device, baudrate, 0);
    }

    public SerialPort(String devicePath, int baudrate) throws SecurityException, IOException {
        this(new File(devicePath), baudrate, 0);
    }

    public SerialPort(File device, int baudrate, int flags) throws SecurityException, IOException {
        this(device, baudrate, 0, 8, 1, flags);
    }

    public SerialPort(String devicePath, int baudrate, int flags) throws SecurityException, IOException {
        this(new File(devicePath), baudrate, flags);
    }

    public SerialPort(String devicePath, int baudrate, int parity, int dataBits, int stopBits, int flags)
            throws SecurityException, IOException {
        this(new File(devicePath), baudrate, parity, dataBits, stopBits, flags);
    }

    public SerialPort(File device, int baudrate, int parity, int dataBits, int stopBits) throws SecurityException, IOException {
        this(device, baudrate, parity, dataBits, stopBits, 0);
    }

    public SerialPort(String devicePath, int baudrate, int parity, int dataBits, int stopBits) throws SecurityException, IOException {
        this(new File(devicePath), baudrate, parity, dataBits, stopBits, 0);
    }

    /**
     * 打开串口
     *@param device 串口设备文件
     *@param baudrate 波特率，一般是9600
     *@param parity 奇偶校验，0 None, 1 Odd, 2 Even
     *@param dataBits 数据位，5 - 8
     *@param stopBits 停止位，1 或 2
     * @param flags 0
     */
    public SerialPort(File device, int baudrate, int parity, int dataBits, int stopBits, int flags) throws SecurityException, IOException {
        /* Check access permission */
        if (!device.canRead() || !device.canWrite()) {
            try {
                /* Missing read/write permission, trying to chmod the file */
                Process su;
                su = Runtime.getRuntime().exec(sSuPath);
                String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
                        + "exit\n";
                su.getOutputStream().write(cmd.getBytes());
                if ((su.waitFor() != 0) || !device.canRead() || !device.canWrite()) {
                    throw new SecurityException();
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new SecurityException();
            }
        }

        mFd = open2(device.getAbsolutePath(), baudrate, parity, dataBits, stopBits, flags);
        if (mFd == null) {
            Log.e(TAG, "native open returns null");
            throw new IOException();
        }
        mFileInputStream = new FileInputStream(mFd);
        mFileOutputStream = new FileOutputStream(mFd);
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }

    // JNI
    private native static FileDescriptor open(String path, int baudrate, int flags);
    private native static FileDescriptor open2(String path, int baudrate, int parity, int dataBits, int stopBit, int flags);

    public native void close();

    static {
        System.loadLibrary("serial_port");
    }

    public static SerialPort.Builder newBuilder(File device, int baudrate) {
        return new SerialPort.Builder(device, baudrate);
    }

    public static SerialPort.Builder newBuilder(String devicePath, int baudrate) {
        return new SerialPort.Builder(devicePath, baudrate);
    }

    public static final class Builder {
        private File device;
        private int baudrate;
        private int dataBits;
        private int parity;
        private int stopBits;
        private int flags;

        private Builder(File device, int baudrate) {
            this.dataBits = 8;
            this.parity = 0;
            this.stopBits = 1;
            this.flags = 0;
            this.device = device;
            this.baudrate = baudrate;
        }

        private Builder(String devicePath, int baudrate) {
            this(new File(devicePath), baudrate);
        }

        public SerialPort.Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public SerialPort.Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        public SerialPort.Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public SerialPort.Builder flags(int flags) {
            this.flags = flags;
            return this;
        }

        public SerialPort build() throws SecurityException, IOException {
            return new SerialPort(this.device, this.baudrate, this.parity, this.dataBits, this.stopBits, this.flags);
        }
    }
}
