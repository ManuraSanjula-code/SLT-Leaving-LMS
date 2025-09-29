package com.slt.peotv.userservice.lms;

import com.slt.peotv.userservice.lms.entity.TempUser;
import com.slt.peotv.userservice.lms.repository.TempUserRepo;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@Service
public class TempUserCleanupService {
    private static final Logger logger = LoggerFactory.getLogger(TempUserCleanupService.class);

    private final TempUserRepo tempUserRepo;
    private final ConcurrentHashMap<Long, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @Autowired
    private TaskScheduler taskScheduler;

    public TempUserCleanupService(TempUserRepo tempUserRepo) {
        this.tempUserRepo = tempUserRepo;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing temp user cleanup service");
        checkAndCleanExpiredUsers();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledCleanup() {
        checkAndCleanExpiredUsers();
    }

    @Transactional
    public void checkAndCleanExpiredUsers() {
        Date now = new Date();
        logger.info("Checking for expired temp users at {}", now);

        // Process already expired users
        List<TempUser> expiredUsers = tempUserRepo.findByExpireTimeBefore(now);
        if (!expiredUsers.isEmpty()) {
            logger.info("Deleting {} expired users", expiredUsers.size());
            tempUserRepo.deleteAll(expiredUsers);
        }

        // Schedule future expirations
        List<TempUser> futureUsers = tempUserRepo.findByExpireTimeAfter(now);
        logger.info("Scheduling {} future expirations", futureUsers.size());

        futureUsers.forEach(user -> {
            long delay = user.getExpireTime().getTime() - now.getTime();
            ScheduledFuture<?> future = taskScheduler.schedule(
                    () -> deleteUserById(user.getId()),
                    new Date(user.getExpireTime().getTime())
            );
            scheduledTasks.put(user.getId(), future);
        });
    }

    private void deleteUserById(Long userId) {
        try {
            tempUserRepo.deleteById(userId);
            scheduledTasks.remove(userId);
            logger.info("Deleted temp user with id: {}", userId);
        } catch (Exception e) {
            logger.error("Error deleting user {}: {}", userId, e.getMessage());
        }
    }
}