package com.jack.utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

/**
 * IO相关操作，简单封装
 * @author jack
 */
public class IOUtils {
    public static void safeClose(Closeable closeable) {
        if(closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
