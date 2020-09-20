package com.jack.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Utils {
    public static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(4);

    public static ScheduledExecutorService getExecutorService() {
        return executorService;
    }
}
