package com.rengv.pixelgymfabric.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.rengv.pixelgymfabric.PixelGymFabric;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ThreadUtil {
    public static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE = Executors.newScheduledThreadPool(5,
            new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("pixelgym_%d")
                    .setUncaughtExceptionHandler(
                            (t, e) -> PixelGymFabric.LOGGER.error("Uncaught exception in async thread: " + t.getName())
                    ).build());

    public static void runAsync(Runnable runnable) {
        SCHEDULED_EXECUTOR_SERVICE.execute(runnable);
    }
}