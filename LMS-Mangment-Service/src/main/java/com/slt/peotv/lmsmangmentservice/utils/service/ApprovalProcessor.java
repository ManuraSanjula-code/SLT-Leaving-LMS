package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.exceptions.BulkApprovalException;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


@Service
public class ApprovalProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ApprovalProcessor.class);

    private final BulkApprovalProcessor bulkApprovalProcessor;

    @Autowired
    public ApprovalProcessor(BulkApprovalProcessor bulkApprovalProcessor) {
        this.bulkApprovalProcessor = bulkApprovalProcessor;
    }


    public void allApproved(BulkApprovedReq bulkApprovedReq, String empId, boolean swap) {
        // Validate input parameters
        Assert.notNull(bulkApprovedReq, "Bulk approval request cannot be null");
        Assert.hasText(empId, "Employee ID cannot be empty");

        // Validate request contents
        validateBulkRequest(bulkApprovedReq);

        try {
            String requestType = swap ? "Movement" : "Leave";
            logger.info("Starting bulk {} approval process for {} items and {} employees by approver: {}",
                    requestType,
                    bulkApprovedReq.getApprovedIds().size(),
                    bulkApprovedReq.getApprovedEmployeesToday().size(),
                    empId);

            // Process the bulk approvals
            bulkApprovalProcessor.processBulkApprovals(bulkApprovedReq, empId, swap);

            logger.info("Bulk {} approval process completed successfully for approver: {}", requestType, empId);

        } catch (BulkApprovalException e) {
            logger.error("Bulk approval exception for employee {} processing {} requests",
                    empId, swap ? "movement" : "leave", e);
            throw e; // Re-throw as it's already a BulkApprovalException

        } catch (Exception e) {
            logger.error("Unexpected error in bulk approval process for employee {}", empId, e);
            throw new BulkApprovalException("Failed to process bulk approvals due to unexpected error", e);
        }
    }


    public void allRejected(BulkApprovedReq bulkApprovedReq, boolean swap) {
        // Validate input parameters
        Assert.notNull(bulkApprovedReq, "Bulk rejection request cannot be null");

        // Validate request contents
        validateBulkRequest(bulkApprovedReq);

        try {
            String requestType = swap ? "Movement" : "Leave";
            logger.info("Starting bulk {} rejection process for {} items",
                    requestType,
                    bulkApprovedReq.getApprovedIds().size());

            // Process the bulk rejections
            bulkApprovalProcessor.processBulkRejections(bulkApprovedReq, swap);

            logger.info("Bulk {} rejection process completed successfully", requestType);

        } catch (BulkApprovalException e) {
            logger.error("Bulk rejection exception for {} requests", swap ? "movement" : "leave", e);
            throw e; // Re-throw as it's already a BulkApprovalException

        } catch (Exception e) {
            logger.error("Unexpected error in bulk rejection process", e);
            throw new BulkApprovalException("Failed to process bulk rejections due to unexpected error", e);
        }
    }


    public String getProcessingStats() {
        return String.format("ApprovalProcessor is ready for processing requests. " +
                "Delegate: %s", bulkApprovalProcessor.getClass().getSimpleName());
    }


    private void validateBulkRequest(BulkApprovedReq bulkApprovedReq) {
        Assert.notEmpty(bulkApprovedReq.getApprovedIds(), "Approved IDs list cannot be empty");
        Assert.notEmpty(bulkApprovedReq.getApprovedEmployeesToday(), "Approved employees list cannot be empty");

        // Additional validation for data integrity
        if (bulkApprovedReq.getApprovedIds().stream().anyMatch(id -> id == null || id.trim().isEmpty())) {
            throw new IllegalArgumentException("Approved IDs list contains null or empty values");
        }

        if (bulkApprovedReq.getApprovedEmployeesToday().stream().anyMatch(emp -> emp == null || emp.trim().isEmpty())) {
            throw new IllegalArgumentException("Approved employees list contains null or empty values");
        }

        // Log request details for monitoring
        logger.debug("Validating bulk request with {} IDs and {} employees",
                bulkApprovedReq.getApprovedIds().size(),
                bulkApprovedReq.getApprovedEmployeesToday().size());
    }
}