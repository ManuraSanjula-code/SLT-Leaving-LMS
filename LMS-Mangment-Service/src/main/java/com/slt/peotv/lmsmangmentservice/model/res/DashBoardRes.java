package com.slt.peotv.lmsmangmentservice.model.res;

import java.util.HashMap;
import java.util.Map;

public class DashBoardRes {
    private int totalLeave;
    private int totalAttendance;
    private int leaveBalance;
    private String name;
    private Map<String, Integer> totalLeaveDistribution;
    private Map<String, Integer> remainLeaveDistribution;
    private Map<String, Integer> monthlyAttendanceDistribution;

    public DashBoardRes() {
        totalLeaveDistribution = new HashMap<>();
        totalLeaveDistribution.put("Casual Leave", 7);
        totalLeaveDistribution.put("Annual Leave", 14);
        totalLeaveDistribution.put("Maternity Leave", 180);
        totalLeaveDistribution.put("Medical Leave", 14);
        totalLeaveDistribution.put("Duty Leave", -1);
        totalLeaveDistribution.put("Special Leave", -1);
    }

    public Map<String, Integer> getMonthlyAttendanceDistribution() {
        return monthlyAttendanceDistribution;
    }

    public void setMonthlyAttendanceDistribution(Map<String, Integer> monthlyAttendanceDistribution) {
        this.monthlyAttendanceDistribution = monthlyAttendanceDistribution;
    }

    public int getTotalLeave() {
        return totalLeave;
    }

    public void setTotalLeave(int totalLeave) {
        this.totalLeave = totalLeave;
    }

    public int getTotalAttendance() {
        return totalAttendance;
    }

    public void setTotalAttendance(int totalAttendance) {
        this.totalAttendance = totalAttendance;
    }

    public int getLeaveBalance() {
        return leaveBalance;
    }

    public void setLeaveBalance(int leaveBalance) {
        this.leaveBalance = leaveBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Integer> getTotalLeaveDistribution() {
        return totalLeaveDistribution;
    }

    public void setTotalLeaveDistribution(Map<String, Integer> totalLeaveDistribution) {
        this.totalLeaveDistribution = totalLeaveDistribution;
    }

    public Map<String, Integer> getRemainLeaveDistribution() {
        return remainLeaveDistribution;
    }

    public void setRemainLeaveDistribution(Map<String, Integer> remainLeaveDistribution) {
        this.remainLeaveDistribution = remainLeaveDistribution;
    }
}
