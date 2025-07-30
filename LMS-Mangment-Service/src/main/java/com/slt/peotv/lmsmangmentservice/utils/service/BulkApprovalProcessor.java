package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.*;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.BulkApprovalException;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.repository.*;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class BulkApprovalProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BulkApprovalProcessor.class);
    private static final int DEFAULT_THREAD_POOL_SIZE = 5;
    private static final int DEFAULT_TIMEOUT_SECONDS = 30;
    private static final int CACHE_CLEANUP_INTERVAL_MINUTES = 5;
    private static final long CACHE_ENTRY_TIMEOUT_MINUTES = 10;

    private final MovementsRepo movementsRepo;
    private final LeaveRepo leaveRepo;
    private final ComponetAdminsRepo componetAdminsRepo;
    private final AttendanceRepo attendanceRepo;
    private final Helper helper;
    private final ExecutorService executorService;
    private final ConcurrentHashMap<String, Long> processingCache;
    private final ScheduledExecutorService cacheCleanupScheduler;

    @Autowired
    public BulkApprovalProcessor(
            MovementsRepo movementsRepo,
            LeaveRepo leaveRepo,
            ComponetAdminsRepo componetAdminsRepo,
            AttendanceRepo attendanceRepo,
            Helper helper,
            @Value("${bulk.approval.thread.pool.size:5}") int threadPoolSize) {

        this.movementsRepo = movementsRepo;
        this.leaveRepo = leaveRepo;
        this.componetAdminsRepo = componetAdminsRepo;
        this.attendanceRepo = attendanceRepo;
        this.helper = helper;

        int poolSize = threadPoolSize > 0 ? threadPoolSize : DEFAULT_THREAD_POOL_SIZE;
        this.executorService = Executors.newFixedThreadPool(poolSize);
        this.processingCache = new ConcurrentHashMap<>();
        this.cacheCleanupScheduler = Executors.newSingleThreadScheduledExecutor();

        scheduleCacheCleanup();
    }

    public void processBulkApprovals(BulkApprovedReq bulkApprovedReq, String approverId, boolean isMovement) {
        validateBulkRequest(bulkApprovedReq);
        Assert.hasText(approverId, "Approver ID cannot be empty");

        List<String> approvedIds = bulkApprovedReq.getApprovedIds();
        List<String> approvedEmployees = bulkApprovedReq.getApprovedEmployeesToday();

        logger.info("Processing bulk approvals for {} items, {} employees, type: {}",
                approvedIds.size(), approvedEmployees.size(), isMovement ? "Movement" : "Leave");

        List<CompletableFuture<Void>> futures = createApprovalFutures(bulkApprovedReq, approverId, isMovement);
        waitForCompletion(futures);
    }

    public void processBulkRejections(BulkApprovedReq bulkApprovedReq, boolean isMovement) {
        validateBulkRequest(bulkApprovedReq);

        List<String> approvedIds = bulkApprovedReq.getApprovedIds();
        logger.info("Processing bulk rejections for {} items, type: {}",
                approvedIds.size(), isMovement ? "Movement" : "Leave");

        List<CompletableFuture<Void>> futures = createRejectionFutures(bulkApprovedReq, isMovement);
        waitForCompletion(futures);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processMovementApprovalWithNewTransaction(String movementId, String approverId, String employeeId) {
        try {
            MovementsEntity movement = movementsRepo.findByPublicId(movementId)
                    .orElseThrow(() -> new BulkApprovalException("Movement not found: " + movementId));

            validateEmployeeMatch(movement.getEmployee().getEmployeeId(), employeeId, "Movement", movementId);
            approvedMoveInternal(movement, approverId);

            logger.debug("Processed movement approval for ID: {}, Employee: {}", movementId, employeeId);
        } catch (Exception e) {
            logger.error("Error in movement approval for ID: {}", movementId, e);
            throw new BulkApprovalException("Movement approval failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLeaveApprovalWithNewTransaction(String leaveId, String approverId, String employeeId) {
        try {
            LeaveEntity leave = leaveRepo.findByPublicId(leaveId)
                    .orElseThrow(() -> new BulkApprovalException("Leave not found: " + leaveId));

                /*validateEmployeeMatch(leave.getEmployee().getEmployeeId(), employeeId, "Leave", leaveId);*/            
                approvedLeaveInternal(leave, approverId);

            logger.debug("Processed leave approval for ID: {}, Employee: {}", leaveId, employeeId);
        } catch (Exception e) {
            logger.error("Error in leave approval for ID: {}", leaveId, e);
            throw new BulkApprovalException("Leave approval failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectMovementWithNewTransaction(String movementId) {
        try {
            movementsRepo.findByPublicId(movementId).ifPresent(movement -> {
                movement.setRequestStatus(RequestStatus.REJECTED);
                movementsRepo.save(movement);
                logger.debug("Rejected movement: {}", movementId);
            });
        } catch (Exception e) {
            logger.error("Error rejecting movement: {}", movementId, e);
            throw new BulkApprovalException("Movement rejection failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectLeaveWithNewTransaction(String leaveId) {
        try {
            leaveRepo.findByPublicId(leaveId).ifPresent(leave -> {
                leave.setRequestStatus(RequestStatus.REJECTED);
                leaveRepo.save(leave);
                logger.debug("Rejected leave: {}", leaveId);
            });
        } catch (Exception e) {
            logger.error("Error rejecting leave: {}", leaveId, e);
            throw new BulkApprovalException("Leave rejection failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvedMove(MovementsEntity movement, String approverId) {
        try {
            approvedMoveInternal(movement, approverId);
        } catch (Exception e) {
            logger.error("Error in approvedMove: {}", movement.getPublicId(), e);
            throw new BulkApprovalException("Movement approval failed", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvedLeave(LeaveEntity leave, String approverId) {
        try {
            approvedLeaveInternal(leave, approverId);
        } catch (Exception e) {
            logger.error("Error in approvedLeave: {}", leave.getPublicId(), e);
            throw new BulkApprovalException("Leave approval failed", e);
        }
    }

    @PreDestroy
    public void shutdown() {
        shutdownExecutor(cacheCleanupScheduler, "Cache Cleanup Scheduler");
        shutdownExecutor(executorService, "Main Approval Executor");
    }

    // Internal implementation methods
    private void approvedMoveInternal(MovementsEntity movement, String approverId) {
        if (movement == null || approverId == null) {
            logger.warn("Invalid parameters for approvedMoveInternal");
            return;
        }

        if (isRequestInFinalState(movement.getRequestStatus())) {
            return;
        }

        try {
            validateAttendanceForMovement(movement);
            List<ComponetAdminsEntity> admins = getAndValidateAdmins(movement.getPublicId(), approverId);

            ComponetAdminsEntity currentAdmin = findCurrentAdmin(admins, approverId);
            if (currentAdmin == null || Boolean.TRUE.equals(currentAdmin.getIsAccepted())) {
                return;
            }

            validateLowerPriorityApprovals(admins, currentAdmin);
            processAdminApproval(currentAdmin);

            if (allAdminsApproved(admins)) {
                completeMovementApproval(movement);
            }
        } catch (Exception e) {
            logger.error("Error in approvedMoveInternal: {}", movement.getPublicId(), e);
            throw new BulkApprovalException("Movement approval failed", e);
        }
    }

    private void approvedLeaveInternal(LeaveEntity leave, String approverId) {
        if (leave == null || approverId == null) {
            logger.warn("Invalid parameters for approvedLeaveInternal");
            return;
        }

        if (isRequestInFinalState(leave.getRequestStatus())) {
            return;
        }

        try {
            if (!leave.getIsManualRequest()) {
                validateAttendanceForLeave(leave);
            }

            List<ComponetAdminsEntity> admins = getAndValidateAdmins(leave.getPublicId(), approverId);
            ComponetAdminsEntity currentAdmin = findCurrentAdmin(admins, approverId);

            if (currentAdmin == null || Boolean.TRUE.equals(currentAdmin.getIsAccepted())) {
                return;
            }

            validateLowerPriorityApprovals(admins, currentAdmin);
            processAdminApproval(currentAdmin);

            if (allAdminsApproved(admins)) {
                completeLeaveApproval(leave);
            }
        } catch (Exception e) {
            logger.error("Error in approvedLeaveInternal: {}", leave.getPublicId(), e);
            throw new BulkApprovalException("Leave approval failed", e);
        }
    }

    // Helper methods
    private List<CompletableFuture<Void>> createApprovalFutures(BulkApprovedReq bulkApprovedReq,
                                                                String approverId, boolean isMovement) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String id : bulkApprovedReq.getApprovedIds()) {
            for (String employeeId : bulkApprovedReq.getApprovedEmployeesToday()) {
                String cacheKey = buildCacheKey(id, employeeId, isMovement);

                if (!tryAcquireProcessingLock(cacheKey)) {
                    logger.debug("Skipping duplicate processing for key: {}", cacheKey);
                    continue;
                }

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        if (isMovement) {
                            processMovementApprovalWithNewTransaction(id, approverId, employeeId);
                        } else {
                            processLeaveApprovalWithNewTransaction(id, approverId, employeeId);
                        }
                    } finally {
                        releaseProcessingLock(cacheKey);
                    }
                }, executorService);

                futures.add(future);
            }
        }

        return futures;
    }

    private List<CompletableFuture<Void>> createRejectionFutures(BulkApprovedReq bulkApprovedReq, boolean isMovement) {
        return bulkApprovedReq.getApprovedIds().stream()
                .map(id -> {
                    String cacheKey = "reject:" + id + ":" + isMovement;
                    if (!tryAcquireProcessingLock(cacheKey)) {
                        return null;
                    }

                    return CompletableFuture.runAsync(() -> {
                        try {
                            if (isMovement) {
                                rejectMovementWithNewTransaction(id);
                            } else {
                                rejectLeaveWithNewTransaction(id);
                            }
                        } finally {
                            releaseProcessingLock(cacheKey);
                        }
                    }, executorService);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void waitForCompletion(List<CompletableFuture<Void>> futures) {
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.error("Processing timed out after {} seconds", DEFAULT_TIMEOUT_SECONDS, e);
            cancelAllFutures(futures);
            throw new BulkApprovalException("Processing timed out", e);
        } catch (Exception e) {
            logger.error("Error in processing", e);
            cancelAllFutures(futures);
            throw new BulkApprovalException("Processing failed", e);
        }
    }

    private void validateBulkRequest(BulkApprovedReq bulkApprovedReq) {
        Assert.notNull(bulkApprovedReq, "Bulk approval request cannot be null");
        Assert.notEmpty(bulkApprovedReq.getApprovedIds(), "Approved IDs list cannot be empty");
        Assert.notEmpty(bulkApprovedReq.getApprovedEmployeesToday(), "Approved employees list cannot be empty");
    }

    private void validateEmployeeMatch(String entityEmployeeId, String expectedEmployeeId,
                                       String entityType, String entityId) {
        if (!entityEmployeeId.equals(expectedEmployeeId)) {
            logger.warn("Employee ID mismatch for {}: {}", entityType, entityId);
            throw new BulkApprovalException("Employee ID mismatch for " + entityType + ": " + entityId);
        }
    }

    private boolean tryAcquireProcessingLock(String cacheKey) {
        return processingCache.putIfAbsent(cacheKey, System.currentTimeMillis()) == null;
    }

    private void releaseProcessingLock(String cacheKey) {
        processingCache.remove(cacheKey);
    }

    private void scheduleCacheCleanup() {
        cacheCleanupScheduler.scheduleAtFixedRate(
                this::cleanupProcessingCache,
                CACHE_CLEANUP_INTERVAL_MINUTES,
                CACHE_CLEANUP_INTERVAL_MINUTES,
                TimeUnit.MINUTES
        );
    }

    private void cleanupProcessingCache() {
        try {
            long cutoff = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(CACHE_ENTRY_TIMEOUT_MINUTES);
            processingCache.entrySet().removeIf(entry -> entry.getValue() < cutoff);
            logger.debug("Cleaned up processing cache, current size: {}", processingCache.size());
        } catch (Exception e) {
            logger.error("Error during cache cleanup", e);
        }
    }

    private void cancelAllFutures(List<CompletableFuture<Void>> futures) {
        futures.forEach(future -> future.cancel(true));
    }

    private void shutdownExecutor(ExecutorService executor, String executorName) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            logger.info("{} shutdown completed", executorName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("{} shutdown interrupted", executorName);
        }
    }

    private String buildCacheKey(String id, String employeeId, boolean isMovement) {
        return id + ":" + employeeId + ":" + isMovement;
    }

    private boolean isRequestInFinalState(RequestStatus status) {
        return status == RequestStatus.REJECTED ||
                status == RequestStatus.APPROVED ||
                status == RequestStatus.CANCELLED;
    }

    private List<ComponetAdminsEntity> getAndValidateAdmins(String componentId, String approverId) {
        List<ComponetAdminsEntity> admins = componetAdminsRepo.findByComponetID(componentId);

        if (admins == null || admins.isEmpty()) {
            logger.warn("No admins found for component: {}", componentId);
            throw new BulkApprovalException("No admins found for component: " + componentId);
        }

        boolean isAuthorized = admins.stream().anyMatch(admin ->
                approverId.equals(admin.getEmployee().getPublicId()) ||
                        approverId.equals(admin.getEmployee().getSltId()) ||
                        approverId.equals(admin.getEmployee().getEmployeeId()));

        if (!isAuthorized) {
            logger.warn("User {} not authorized to approve component {}", approverId, componentId);
            throw new BulkApprovalException("User not authorized to approve component: " + componentId);
        }

        return admins.stream()
                .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());
    }

    private ComponetAdminsEntity findCurrentAdmin(List<ComponetAdminsEntity> admins, String approverId) {
        return admins.stream()
                .filter(admin -> approverId.equals(admin.getEmployee().getPublicId()) ||
                        approverId.equals(admin.getEmployee().getSltId()) ||
                        approverId.equals(admin.getEmployee().getEmployeeId()))
                .findFirst()
                .orElse(null);
    }

    private void validateLowerPriorityApprovals(List<ComponetAdminsEntity> admins, ComponetAdminsEntity currentAdmin) {
        int currentAdminIndex = admins.indexOf(currentAdmin);
        for (int i = 0; i < currentAdminIndex; i++) {
            ComponetAdminsEntity admin = admins.get(i);
            if (admin.getApprovedDate() == null || !Boolean.TRUE.equals(admin.getIsAccepted())) {
                logger.warn("Lower priority admin {} has not approved", admin.getEmployee().getEmployeeId());
                throw new BulkApprovalException("Lower priority admins have not approved");
            }
        }
    }

    private void processAdminApproval(ComponetAdminsEntity admin) {
        if (admin.getApprovedDate() == null) {
            admin.setApprovedDate(new Date());
            admin.setIsAccepted(true);
            componetAdminsRepo.save(admin);
        }
    }

    private boolean allAdminsApproved(List<ComponetAdminsEntity> admins) {
        return admins.stream().allMatch(admin ->
                admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));
    }

    private void validateAttendanceForMovement(MovementsEntity movement) {
        if (movement.getAttendance() == null) {
            logger.warn("No attendance found for movement: {}", movement.getPublicId());
            throw new BulkApprovalException("No attendance found for movement: " + movement.getPublicId());
        }
    }

    private void validateAttendanceForLeave(LeaveEntity leave) {
        if (leave.getAttendance() == null) {
            logger.warn("No attendance found for leave: {}", leave.getPublicId());
            throw new BulkApprovalException("No attendance found for leave: " + leave.getPublicId());
        }
    }

    private void completeMovementApproval(MovementsEntity movement) {
        movement.setRequestStatus(RequestStatus.APPROVED);
        AttendanceEntity attendance = movement.getAttendance();

        attendance.setIsResolved(true);
        attendance.setDueDateForUA(null);
        attendance.setHasIssues(false);
        attendance.setResolve(ResolveType.VIA_MOVEMENT);
        attendance.setAttendanceType(AttendanceType.FULL_DAY);

        switch (movement.getMovementType()) {
            case HOME_TO_OFFICE:
                attendance.setArrivalTime(helper.parseToSqlTime(movement.getInTime()));
                break;
            case OFFICE_TO_HOME:
                attendance.setLeftTime(helper.parseToSqlTime(movement.getOutTime()));
                break;
            default:
                attendance.setArrivalTime(helper.parseToSqlTime(movement.getInTime()));
                attendance.setLeftTime(helper.parseToSqlTime(movement.getOutTime()));
        }

        attendance.setIsUnauthorized(false);
        attendanceRepo.save(attendance);
        movementsRepo.save(movement);
        logger.info("Movement {} fully approved", movement.getPublicId());
    }

    private void completeLeaveApproval(LeaveEntity leave) {
        leave.setRequestStatus(RequestStatus.APPROVED);

        if (leave.getAttendance() != null) {
            AttendanceEntity attendance = leave.getAttendance();
            attendance.setIsResolved(true);
            attendance.setDueDateForUA(null);
            attendance.setHasIssues(false);
            attendanceRepo.save(attendance);
        }

        leaveRepo.save(leave);
        logger.info("Leave {} fully approved", leave.getPublicId());
    }
}