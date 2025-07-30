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
        Assert.notNull(bulkApprovedReq, "Bulk approval request cannot be null");
        Assert.hasText(empId, "Employee ID cannot be empty");

        try {
            logger.info("Starting bulk approval process for {} items and {} employees",
                    bulkApprovedReq.getApprovedIds().size(),
                    bulkApprovedReq.getApprovedEmployeesToday().size());

            bulkApprovalProcessor.processBulkApprovals(bulkApprovedReq, empId, swap);

            logger.info("Bulk approval process completed successfully");
        } catch (Exception e) {
            logger.error("Error in bulk approval process for employee {}", empId, e);
            throw new BulkApprovalException("Failed to process bulk approvals", e);
        }
    }
}