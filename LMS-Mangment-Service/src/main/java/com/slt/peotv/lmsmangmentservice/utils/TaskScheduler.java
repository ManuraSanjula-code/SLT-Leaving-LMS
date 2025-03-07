package com.slt.peotv.lmsmangmentservice.utils;

import java.time.*;
import java.util.concurrent.*;
import org.apache.commons.net.ntp.NTPUDPClient;
import java.net.InetAddress;
import java.util.Date;

public class TaskScheduler {
    private static final int THREAD_POOL_SIZE = 200;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private static final String TIME_SERVER = "pool.ntp.org";
    private static final ZoneId SRI_LANKA_ZONE = ZoneId.of("Asia/Colombo");

    public static void scheduleDailyTask(Runnable userTask) {
        long initialDelay = getInitialDelay();
        long period = 15; // Repeat every 15 minutes in minutes

        scheduler.scheduleAtFixedRate(() -> runTask(userTask), initialDelay, period, TimeUnit.MINUTES);
    }

    private static void runTask(Runnable userTask) {
        LocalTime currentTime = getCurrentTime();
        System.out.println("Running user-defined function at: " + currentTime);
        CompletableFuture.runAsync(userTask, threadPool);
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
        long delay = 0;

        // Calculate initial delay until the next 15-minute mark
        int minutes = now.getMinute();
        int nextQuarterHour = (minutes / 15 + 1) * 15;
        if (nextQuarterHour == 60) {
            nextQuarterHour = 0;
        }

        LocalTime nextRunTime = now.withMinute(nextQuarterHour).withSecond(0).withNano(0);
        if (now.isAfter(nextRunTime)) {
            nextRunTime = nextRunTime.plusHours(1);
        }

        delay = Duration.between(now, nextRunTime).getSeconds();
        return delay;
    }
}