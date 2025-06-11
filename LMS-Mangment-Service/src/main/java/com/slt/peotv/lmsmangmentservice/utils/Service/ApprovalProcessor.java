package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import com.slt.peotv.lmsmangmentservice.repository.MovementsRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApprovalProcessor {
    @Autowired
    private BulkApprovalProcessor bulkApprovalProcessor;
    private static final Logger logger = LoggerFactory.getLogger(ApprovalProcessor.class);
    @Autowired
    private MovementsRepo movementsRepo;

    @Autowired
    private LeaveRepo leaveRepo;
    /**
     * Fixed allApproved method - replaces your existing one
     */
    public void allApproved(BulkApprovedReq bulkApprovedReq, String empId, boolean swap) {
        if (bulkApprovedReq == null) {
            logger.warn("Bulk approval request is null");
            return;
        }

        try {
            logger.info("Starting bulk approval process for {} items",
                    bulkApprovedReq.getApprovedIds() != null ? bulkApprovedReq.getApprovedIds().size() : 0);

            bulkApprovalProcessor.processBulkApprovals(bulkApprovedReq, empId, swap);

            logger.info("Bulk approval process completed successfully");

        } catch (Exception e) {
            logger.error("Error in bulk approval process", e);
            throw new RuntimeException("Failed to process bulk approvals", e);
        }
    }

    /**
     * Fixed allReject method - replaces your existing one
     */
    public void allReject(BulkApprovedReq bulkApprovedReq, boolean swap) {
        if (bulkApprovedReq == null) {
            logger.warn("Bulk rejection request is null");
            return;
        }

        try {
            logger.info("Starting bulk rejection process for {} items",
                    bulkApprovedReq.getApprovedIds() != null ? bulkApprovedReq.getApprovedIds().size() : 0);

            bulkApprovalProcessor.processBulkRejections(bulkApprovedReq, swap);

            logger.info("Bulk rejection process completed successfully");

        } catch (Exception e) {
            logger.error("Error in bulk rejection process", e);
            throw new RuntimeException("Failed to process bulk rejections", e);
        }
    }

    /**
     * Enhanced processMovement method with better error handling
     */
    public void processMovement(String moveId, String userId) {
        if (moveId == null || userId == null) {
            logger.warn("Invalid parameters for processMovement: moveId={}, userId={}", moveId, userId);
            return;
        }

        try {
            Optional<MovementsEntity> movementsEntity = movementsRepo.findByPublicId(moveId);
            if (movementsEntity.isPresent()) {
                MovementsEntity movementEntity = movementsEntity.get();

                // Add validation
                if (!movementEntity.getEmployee().getPublicId().equals(userId) &&
                        !movementEntity.getEmployee().getEmployeeId().equals(userId)) {
                    logger.warn("User {} not authorized to approve movement {}", userId, moveId);
                    return;
                }

                bulkApprovalProcessor.approvedMove(movementEntity, userId);
                logger.info("Successfully processed movement approval: {}", moveId);
            } else {
                logger.warn("Movement not found: {}", moveId);
            }
        } catch (Exception e) {
            logger.error("Error processing movement approval for ID: {}", moveId, e);
            throw new RuntimeException("Failed to process movement approval", e);
        }
    }

    /**
     * Enhanced processLeave method with better error handling
     */
    public void processLeave(String leaveId, String userId) {
        if (leaveId == null || userId == null) {
            logger.warn("Invalid parameters for processLeave: leaveId={}, userId={}", leaveId, userId);
            return;
        }

        try {
            Optional<LeaveEntity> leaveEntityOp = leaveRepo.findByPublicId(leaveId);
            if (leaveEntityOp.isPresent()) {
                LeaveEntity leaveEntity = leaveEntityOp.get();

                // Add validation
                if (!leaveEntity.getEmployee().getPublicId().equals(userId) &&
                        !leaveEntity.getEmployee().getEmployeeId().equals(userId)) {
                    logger.warn("User {} not authorized to approve leave {}", userId, leaveId);
                    return;
                }

                bulkApprovalProcessor.approvedLeave(leaveEntity, userId);
                logger.info("Successfully processed leave approval: {}", leaveId);
            } else {
                logger.warn("Leave not found: {}", leaveId);
            }
        } catch (Exception e) {
            logger.error("Error processing leave approval for ID: {}", leaveId, e);
            throw new RuntimeException("Failed to process leave approval", e);
        }
    }

    /**
     * Enhanced reject method with better validation
     */
    public void reject(String id, String userId, boolean swap) {
        if (id == null || userId == null) {
            logger.warn("Invalid parameters for reject: id={}, userId={}", id, userId);
            return;
        }

        try {
            if (swap) {
                Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
                if (movementsOpt.isPresent()) {
                    MovementsEntity movementsEntity = movementsOpt.get();
                    if (!movementsEntity.getEmployee().getPublicId().equals(userId)) {
                        logger.warn("User {} not authorized to reject movement {}", userId, id);
                        return;
                    }
                    movementsEntity.setRequestStatus(RequestStatus.REJECTED);
                    movementsRepo.save(movementsEntity);
                    logger.info("Successfully rejected movement: {}", id);
                } else {
                    logger.warn("Movement not found for rejection: {}", id);
                }
            } else {
                Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
                if (leaveEntityOpt.isPresent()) {
                    LeaveEntity leaveEntity = leaveEntityOpt.get();
                    if (!leaveEntity.getEmployee().getPublicId().equals(userId)) {
                        logger.warn("User {} not authorized to reject leave {}", userId, id);
                        return;
                    }
                    leaveEntity.setRequestStatus(RequestStatus.REJECTED);
                    leaveRepo.save(leaveEntity);
                    logger.info("Successfully rejected leave: {}", id);
                } else {
                    logger.warn("Leave not found for rejection: {}", id);
                }
            }
        } catch (Exception e) {
            logger.error("Error rejecting ID: {}", id, e);
            throw new RuntimeException("Failed to process rejection", e);
        }
    }
}
