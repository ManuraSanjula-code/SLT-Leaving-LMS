package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApprovalProcessor {
    @Autowired
    private BulkApprovalProcessor bulkApprovalProcessor;
    private static final Logger logger = LoggerFactory.getLogger(ApprovalProcessor.class);

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
}
