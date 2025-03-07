package com.slt.peotv.lmsmangmentservice.utils;

import java.time.*;
import java.util.concurrent.*;
import org.apache.commons.net.ntp.NTPUDPClient;
import java.net.InetAddress;
import java.util.Date;

public class DailyTaskScheduler {

    private static final int THREAD_POOL_SIZE = 200;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final String TIME_SERVER = "pool.ntp.org";
    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    public static void scheduleDailyTask(Runnable userTask) {
        long initialDelay = getInitialDelay();
        long period = 24 * 60 * 60; // Repeat every 24 hours

        scheduler.scheduleAtFixedRate(() -> runTask(userTask), initialDelay, period, TimeUnit.SECONDS);
    }

    private static void runTask(Runnable userTask) {
        LocalTime currentTime = getCurrentTime();
        if (currentTime.isAfter(LocalTime.of(18, 0))) {
            System.out.println("Running user-defined function at: " + currentTime);
            CompletableFuture.runAsync(userTask, threadPool);
        } else {
            System.out.println("Not 6:00 PM yet. Current Time: " + currentTime);
        }
    }

    private static LocalTime getCurrentTime() {
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.open();
            InetAddress address = InetAddress.getByName(TIME_SERVER);
            return new Date(client.getTime(address).getReturnTime())
                    .toInstant()
                    .atZone(SRI_LANKA_ZONE)
                    .toLocalTime();
        } catch (Exception e) {
            System.err.println("Error fetching time from NTP server. Using system time.");
            return LocalTime.now(SRI_LANKA_ZONE);
        }
    }

    private static long getInitialDelay() {
        LocalTime now = getCurrentTime();
        LocalTime targetTime = LocalTime.of(18, 0);
        if (now.isAfter(targetTime)) {
            return 0; // Run immediately if it's past 6:00 PM
        }
        return Duration.between(now, targetTime).getSeconds();
    }
}
