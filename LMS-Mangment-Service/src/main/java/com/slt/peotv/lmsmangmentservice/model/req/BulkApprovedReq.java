package com.slt.peotv.lmsmangmentservice.model.req;

import java.util.List;

public class BulkApprovedReq {
    List<String> approvedEmployeesToday;
    List<String> approvedIds;

    public List<String> getApprovedEmployeesToday() {
        return approvedEmployeesToday;
    }

    public void setApprovedEmployeesToday(List<String> approvedEmployeesToday) {
        this.approvedEmployeesToday = approvedEmployeesToday;
    }

    public List<String> getApprovedIds() {
        return approvedIds;
    }

    public void setApprovedIds(List<String> approvedIds) {
        this.approvedIds = approvedIds;
    }
}
