package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class BulkApprovalProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BulkApprovalProcessor.class);

    @Autowired
    private MovementsRepo movementsRepo;

    @Autowired
    private LeaveRepo leaveRepo;

    @Autowired
    private ComponetAdminsRepo componetAdminsRepo;

    @Autowired
    private AttendanceRepo attendanceRepo;

    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    private final ConcurrentHashMap<String, Boolean> processingCache = new ConcurrentHashMap<>();

    public void processBulkApprovals(BulkApprovedReq bulkApprovedReq, String emp, boolean isMovement) {
        if (bulkApprovedReq == null ||
                bulkApprovedReq.getApprovedIds() == null ||
                bulkApprovedReq.getApprovedEmployeesToday() == null) {
            logger.warn("Invalid bulk approval request");
            return;
        }

        List<String> approvedIds = bulkApprovedReq.getApprovedIds();
        List<String> approvedEmployees = bulkApprovedReq.getApprovedEmployeesToday();

        logger.info("Processing bulk approvals for {} items, {} employees, type: {}",
                approvedIds.size(), approvedEmployees.size(), isMovement ? "Movement" : "Leave");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String id : approvedIds) {
            for (String employeeId : approvedEmployees) {
                String cacheKey = id + ":" + employeeId + ":" + isMovement;

                if (processingCache.putIfAbsent(cacheKey, true) != null) {
                    logger.debug("Skipping duplicate processing for key: {}", cacheKey);
                    continue;
                }

                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    try {
                        if (isMovement) {
                            processMovementApprovalWithNewTransaction(id,emp, employeeId);
                        } else {
                            processLeaveApprovalWithNewTransaction(id, emp, employeeId);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing approval for ID: {}, Employee: {}", id, employeeId, e);
                    } finally {
                        // Remove from cache when done
                        processingCache.remove(cacheKey);
                    }
                }, executorService);

                futures.add(future);
            }
        }

        try {
            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0])
            );

            allFutures.get(30, TimeUnit.SECONDS);
            logger.info("Bulk approval processing completed successfully");

        } catch (Exception e) {
            logger.error("Error in bulk approval processing", e);
            futures.forEach(future -> future.cancel(true));
        }
    }

    public void processBulkRejections(BulkApprovedReq bulkApprovedReq, boolean isMovement) {
        if (bulkApprovedReq == null || bulkApprovedReq.getApprovedIds() == null) {
            logger.warn("Invalid bulk rejection request");
            return;
        }

        List<String> approvedIds = bulkApprovedReq.getApprovedIds();
        logger.info("Processing bulk rejections for {} items, type: {}",
                approvedIds.size(), isMovement ? "Movement" : "Leave");

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (String id : approvedIds) {
            String cacheKey = "reject:" + id + ":" + isMovement;

            if (processingCache.putIfAbsent(cacheKey, true) != null) {
                continue;
            }

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    if (isMovement) {
                        rejectMovementWithNewTransaction(id);
                    } else {
                        rejectLeaveWithNewTransaction(id);
                    }
                } catch (Exception e) {
                    logger.error("Error rejecting ID: {}", id, e);
                } finally {
                    processingCache.remove(cacheKey);
                }
            }, executorService);

            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .get(30, TimeUnit.SECONDS);
            logger.info("Bulk rejection processing completed");
        } catch (Exception e) {
            logger.error("Error in bulk rejection processing", e);
            futures.forEach(future -> future.cancel(true));
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processMovementApprovalWithNewTransaction(String movementId, String employeeId, String userId) {
        try {
            Optional<MovementsEntity> movementOpt = movementsRepo.findByPublicId(movementId);
            if (!movementOpt.isPresent()) {
                logger.warn("Movement not found: {}", movementId);
                return;
            }

            MovementsEntity movement = movementOpt.get();

            if (!movement.getEmployee().getEmployeeId().equals(userId)) {
                logger.warn("Employee ID mismatch for movement: {}", movementId);
                return;
            }

            approvedMoveInternal(movement, employeeId);
            logger.debug("Processing movement approval for ID: {}, Employee: {}", movementId, employeeId);

        } catch (Exception e) {
            logger.error("Error in processMovementApprovalWithNewTransaction: {}", movementId, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processLeaveApprovalWithNewTransaction(String leaveId, String employeeId, String userId) {
        try {
            Optional<LeaveEntity> leaveOpt = leaveRepo.findByPublicId(leaveId);
            if (!leaveOpt.isPresent()) {
                logger.warn("Leave not found: {}", leaveId);
                return;
            }

            LeaveEntity leave = leaveOpt.get();
            // Validate employee matches
            if (!leave.getEmployee().getEmployeeId().equals(userId)) {
                logger.warn("Employee ID mismatch for leave: {}", leaveId);
                return;
            }

            // Call the approval logic
            approvedLeaveInternal(leave, employeeId);
            logger.debug("Processing leave approval for ID: {}, Employee: {}", leaveId, employeeId);

        } catch (Exception e) {
            logger.error("Error in processLeaveApprovalWithNewTransaction: {}", leaveId, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectMovementWithNewTransaction(String movementId) {
        try {
            Optional<MovementsEntity> movementOpt = movementsRepo.findByPublicId(movementId);
            if (movementOpt.isPresent()) {
                MovementsEntity movement = movementOpt.get();
                movement.setRequestStatus(RequestStatus.REJECTED);
                movementsRepo.save(movement);
                logger.debug("Rejected movement: {}", movementId);
            }
        } catch (Exception e) {
            logger.error("Error rejecting movement: {}", movementId, e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void rejectLeaveWithNewTransaction(String leaveId) {
        try {
            Optional<LeaveEntity> leaveOpt = leaveRepo.findByPublicId(leaveId);
            if (leaveOpt.isPresent()) {
                LeaveEntity leave = leaveOpt.get();
                leave.setRequestStatus(RequestStatus.REJECTED);
                leaveRepo.save(leave);
                logger.debug("Rejected leave: {}", leaveId);
            }
        } catch (Exception e) {
            logger.error("Error rejecting leave: {}", leaveId, e);
            throw e;
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvedMove(MovementsEntity movement, String userId) {
        try {
            approvedMoveInternal(movement, userId);
        } catch (Exception e) {
            logger.error("Error in approvedMove: {}", movement.getPublicId(), e);
            throw new RuntimeException("Failed to approve movement", e);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approvedLeave(LeaveEntity leave, String userId) {
        try {
            approvedLeaveInternal(leave, userId);
        } catch (Exception e) {
            logger.error("Error in approvedLeave: {}", leave.getPublicId(), e);
            throw new RuntimeException("Failed to approve leave", e);
        }
    }


    private void approvedMoveInternal(MovementsEntity movement, String userId) {
        if (movement.getRequestStatus().equals(RequestStatus.REJECTED) || movement.getRequestStatus().equals(RequestStatus.APPROVED) ||
                movement.getRequestStatus().equals(RequestStatus.CANCELLED)) return;

        if (movement == null || userId == null) {
            logger.warn("Invalid parameters for approvedMoveInternal");
            return;
        }

        try {
            AttendanceEntity attendance = movement.getAttendance();
            if (attendance == null) {
                logger.warn("No attendance found for movement: {}", movement.getPublicId());
                return;
            }

            List<ComponetAdminsEntity> admins_ = componetAdminsRepo.findByComponetID(movement.getPublicId());
            if (admins_ == null || admins_.isEmpty()) {
                logger.warn("No admins found for movement: {}", movement.getPublicId());
                return;
            }

            boolean isAuthorizedAdmin = admins_.stream()
                    .anyMatch(admin ->
                            userId.equals(admin.getEmployee().getPublicId()) ||
                                    userId.equals(admin.getEmployee().getSltId()) ||
                                    userId.equals(admin.getEmployee().getEmployeeId())
                    );

            if (!isAuthorizedAdmin) {
                logger.warn("User {} not authorized to approve movement {}", userId, movement.getPublicId());
                return;
            }

            List<ComponetAdminsEntity> admins = admins_.stream()
                    .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                    .toList();

            ComponetAdminsEntity currentAdmin = admins.stream()
                    .filter(admin ->
                            userId.equals(admin.getEmployee().getPublicId()) ||
                                    userId.equals(admin.getEmployee().getSltId()) ||
                                    userId.equals(admin.getEmployee().getEmployeeId()))
                    .findFirst()
                    .orElse(null);

            if (currentAdmin == null) {
                logger.warn("Current admin not found for user: {}", userId);
                return;
            }

            if (Boolean.TRUE.equals(currentAdmin.getIsAccepted())) {
                logger.debug("Admin {} already approved movement {}", userId, movement.getPublicId());
                return;
            }

            int currentAdminIndex = admins.indexOf(currentAdmin);

            boolean allLowerPriorityApproved = true;
            for (int i = 0; i < currentAdminIndex; i++) {
                if (admins.get(i).getApprovedDate() == null ||
                        !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                    allLowerPriorityApproved = false;
                    break;
                }
            }

            if (!allLowerPriorityApproved) {
                logger.warn("Lower priority admins have not approved movement {}", movement.getPublicId());
                return;
            }

            if (currentAdmin.getApprovedDate() == null) {
                System.out.println("Processing movement approval for ID: " + movement.getPublicId() + ", Employee: " + userId);
                currentAdmin.setApprovedDate(new Date());
                currentAdmin.setIsAccepted(true);
                componetAdminsRepo.save(currentAdmin);
            }

            boolean allApproved = admins.stream()
                    .allMatch(admin -> admin.getApprovedDate() != null &&
                            Boolean.TRUE.equals(admin.getIsAccepted()));

            if (allApproved || admins.isEmpty()) {
                movement.setRequestStatus(RequestStatus.APPROVED);
                attendance.setIsResolved(true);
                attendance.setDueDateForUA(null);
                attendance.setHasIssues(false);

                attendanceRepo.save(attendance);
                movementsRepo.save(movement);

                logger.info("Movement {} fully approved", movement.getPublicId());
            }
        } catch (Exception e) {
            logger.error("Error in approvedMoveInternal: {}", movement.getPublicId(), e);
        }
    }

    private void approvedLeaveInternal(LeaveEntity leave, String userId) {
        if (leave == null || userId == null) {
            logger.warn("Invalid parameters for approvedLeaveInternal");
            return;
        }
        if (leave.getRequestStatus().equals(RequestStatus.REJECTED) || leave.getRequestStatus().equals(RequestStatus.APPROVED) || leave.getRequestStatus().equals(RequestStatus.CANCELLED)) return;
        try {
            AttendanceEntity attendance = leave.getAttendance();
            if (!leave.getIsManualRequest() && attendance == null) {
                logger.warn("No attendance found for non-manual leave: {}", leave.getPublicId());
                return;
            }

            List<ComponetAdminsEntity> admins_ = componetAdminsRepo.findByComponetID(leave.getPublicId());
            if (admins_ == null || admins_.isEmpty()) {
                logger.warn("No admins found for leave: {}", leave.getPublicId());
                return;
            }

            boolean isAuthorizedAdmin = admins_.stream()
                    .anyMatch(admin ->
                            userId.equals(admin.getEmployee().getPublicId()) ||
                                    userId.equals(admin.getEmployee().getSltId()) ||
                                    userId.equals(admin.getEmployee().getEmployeeId())
                    );

            if (!isAuthorizedAdmin) {
                logger.warn("User {} not authorized to approve leave {}", userId, leave.getPublicId());
                return;
            }

            List<ComponetAdminsEntity> admins = admins_.stream()
                    .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                    .collect(Collectors.toList());

            ComponetAdminsEntity currentAdmin = admins.stream()
                    .filter(admin ->
                            userId.equals(admin.getEmployee().getPublicId()) ||
                                    userId.equals(admin.getEmployee().getSltId()) ||
                                    userId.equals(admin.getEmployee().getEmployeeId()))
                    .findFirst()
                    .orElse(null);

            if (currentAdmin == null) {
                logger.warn("Current admin not found for user: {}", userId);
                return;
            }

            if (Boolean.TRUE.equals(currentAdmin.getIsAccepted())) {
                logger.debug("Admin {} already approved leave {}", userId, leave.getPublicId());
                return;
            }

            int currentAdminIndex = admins.indexOf(currentAdmin);

            // Check if all lower priority admins have approved
            boolean allLowerPriorityApproved = true;
            for (int i = 0; i < currentAdminIndex; i++) {
                if (admins.get(i).getApprovedDate() == null ||
                        !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                    allLowerPriorityApproved = false;
                    break;
                }
            }

            if (!allLowerPriorityApproved) {
                logger.warn("Lower priority admins have not approved leave {}", leave.getPublicId());
                return;
            }

            if (currentAdmin.getApprovedDate() == null) {
                currentAdmin.setApprovedDate(new Date());
                currentAdmin.setIsAccepted(true);
                componetAdminsRepo.save(currentAdmin);
            }

            boolean allApproved = admins.stream()
                    .allMatch(admin -> admin.getApprovedDate() != null &&
                            Boolean.TRUE.equals(admin.getIsAccepted()));

            if (allApproved || admins.isEmpty()) {
                leave.setRequestStatus(RequestStatus.APPROVED);

                if (attendance != null) {
                    attendance.setIsResolved(true);
                    attendance.setDueDateForUA(null);
                    attendance.setHasIssues(false);
                    attendanceRepo.save(attendance);
                }
                leaveRepo.save(leave);

                logger.info("Leave {} fully approved", leave.getPublicId());
            }
        } catch (Exception e) {
            logger.error("Error in approvedLeaveInternal: {}", leave.getPublicId(), e);
        }
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}