package com.slt.peotv.lmsmangmentservice.exceptions;

public class BulkApprovalException extends RuntimeException {
    public BulkApprovalException(String message) {
        super(message);
    }

    public BulkApprovalException(String message, Throwable cause) {
        super(message, cause);
    }
}