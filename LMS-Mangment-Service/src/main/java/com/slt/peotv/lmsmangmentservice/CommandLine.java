package com.slt.peotv.lmsmangmentservice;

import com.nimbusds.jwt.SignedJWT;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.security.TokenCreator;
import com.slt.peotv.lmsmangmentservice.service.AccessLogService;
import com.slt.peotv.lmsmangmentservice.service.Main_Service;
import com.slt.peotv.lmsmangmentservice.utils.service.LeaveManagementService;
import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CommandLine implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(CommandLine.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds
    private static final int BATCH_SIZE = 100;
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    private volatile boolean initializationComplete = false;
    private volatile boolean initializationInProgress = false;
    private LocalDateTime lastRetryTime;

    @Autowired private AccessLogService accessLogService;
    @Autowired private Main_Service checkService;
    @Autowired private EmployeeRepo employeeRepo;
    @Autowired private UserClient userClient;
    @Autowired private TokenCreator tokenCreator;
    @Autowired private LeaveManagementService leaveManagementService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
    public void run(String... args) {
        logger.info("Starting employee data initialization...");
        try {
            initializeEmployeeData();
            logger.info("Employee data initialization completed successfully.");
        } catch (Exception e) {
            logger.error("Critical error during initialization: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelay = 3600000)
    public void periodicRetry() {
        if (!initializationComplete && !initializationInProgress) {
            if (lastRetryTime == null || 
                LocalDateTime.now().isAfter(lastRetryTime.plusSeconds(RETRY_DELAY_MS / 1000))) {
                logger.info("Attempting periodic retry of employee data initialization...");
                initializeEmployeeData();
            }
        }
    }

    private synchronized void initializeEmployeeData() {
        if (initializationComplete || initializationInProgress) {
            return;
        }
        
        initializationInProgress = true;
        lastRetryTime = LocalDateTime.now();
        
        try {
            if (employeeRepo.count() > 0) {
                logger.info("Employees already exist in database. Skipping initialization.");
                initializationComplete = true;
                return;
            }

            logger.info("Fetching employees from external service...");
            String token = generateAndEncryptToken();
            if (token == null) {
                return;
            }

            List<UserRest> externalEmployees = fetchEmployeesWithRetry(token);
            if (externalEmployees == null || externalEmployees.isEmpty()) {
                logger.warn("No employees received from external service.");
                return;
            }

            processEmployeesInBatches(externalEmployees);
            initializationComplete = true;
        } catch (Exception e) {
            logger.error("Error during employee data initialization (will retry later): {}", e.getMessage());
        } finally {
            initializationInProgress = false;
        }
    }

    private String generateAndEncryptToken() {
        try {
            SignedJWT signToken = tokenCreator.createSignedJWT("lms@slt.com");
            return "Bearer " + tokenCreator.encryptToken(signToken);
        } catch (Exception e) {
            logger.error("Failed to generate auth token: {}", e.getMessage(), e);
            return null;
        }
    }

    @Retryable(maxAttempts = MAX_RETRIES, 
               backoff = @Backoff(delay = 1000, multiplier = 2),
               recover = "recoverFromFeignFailure")
    private List<UserRest> fetchEmployeesWithRetry(String token) {
        try {
            return userClient.getAllEmployee(token);
        } catch (FeignException e) {
            logger.error("Failed to fetch employees (Status: {}): {}", e.status(), e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Unexpected error fetching employees: {}", e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    private List<UserRest> recoverFromFeignFailure(String token, Exception e) {
        logger.warn("All retry attempts failed for fetching employees. Will try again later.");
        return null;
    }

    private void processEmployeesInBatches(List<UserRest> employees) {
        if (employees == null) return;
        
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger errorCount = new AtomicInteger();

        for (int i = 0; i < employees.size(); i += BATCH_SIZE) {
            List<UserRest> batch = employees.subList(i, Math.min(i + BATCH_SIZE, employees.size()));
            processBatch(batch, successCount, errorCount);
        }

        logger.info("Batch processing completed. Success: {}, Errors: {}", successCount, errorCount);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processBatch(List<UserRest> batch, AtomicInteger successCount, AtomicInteger errorCount) {
        batch.forEach(employee -> {
            try {
                processSingleEmployee(employee);
                successCount.incrementAndGet();
            } catch (Exception e) {
                errorCount.incrementAndGet();
                logger.error("Error processing employee {}: {}",
                        employee.getEmployeeId(), e.getMessage(), e);
            }
        });
    }

    private void processSingleEmployee(UserRest userRest) throws Exception {
        EmployeeEntity employee = mapToEmployeeEntity(userRest);
        EmployeeEntity savedEmployee = employeeRepo.save(employee);
        leaveManagementService.allocateLeaves(savedEmployee);
    }

    private EmployeeEntity mapToEmployeeEntity(UserRest userRest) {
        EmployeeEntity employee = new EmployeeEntity();
        employee.setEmail(userRest.getEmail());
        employee.setEmployeeId(userRest.getEmployeeId());
        employee.setPublicId(userRest.getUserId());
        employee.setProfilePic(userRest.getProfilePic());
        employee.setSltId(userRest.getSltId());
        employee.setFirstName(userRest.getFirstName());
        employee.setLastName(userRest.getLastName());
        employee.setJoin_date(userRest.getJoiningDate());
        employee.setRoaster(userRest.getRoaster() != null ? userRest.getRoaster() : false);
        employee.setGender(userRest.getGender());
        return employee;
    }
}