package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.repository.AttendanceRepoV1;
import com.slt.peotv.lmsmangmentservice.service.impl.Check_Service_Impl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceUtils {

    @Autowired
    private AttendanceRepoV1 attendanceRepo;
    private static final Logger log = LoggerFactory.getLogger(AttendanceUtils.class);

    public boolean isDuplicateAttendance(AttendanceEntity newAttendance) {
        try {
            // STEP 1: Quick check - if no records for this employee+date, definitely not duplicate
            long count = attendanceRepo.countByEmployeeIDAndDate(
                    newAttendance.getEmployeeID(),
                    newAttendance.getDate()
            );

            if (count == 0) {
                return false; // No existing records, definitely not duplicate
            }

            // STEP 2: Get only potential duplicates (much smaller dataset)
            List<AttendanceEntity> potentialDuplicates = attendanceRepo.findByEmployeeIDAndDate(
                    newAttendance.getEmployeeID(),
                    newAttendance.getDate()
            );
            // STEP 3: Check each potential duplicate
            return potentialDuplicates.stream()
                    .anyMatch(existing -> areAttendanceRecordsEqual(existing, newAttendance));

        } catch (Exception e) {
            log.error("Error checking for duplicate attendance: {}", e.getMessage(), e);
            return false; // If error occurs, allow saving (safer approach)
        }
    }

    // Improved comparison method with proper null handling
    private boolean areAttendanceRecordsEqual(AttendanceEntity existing, AttendanceEntity newRecord) {
        // Compare all fields with null-safe comparison
        return safeEquals(existing.getPublicId(), newRecord.getPublicId()) &&
                safeEquals(existing.getDate(), newRecord.getDate()) &&
                safeEquals(existing.getEmployeeID(), newRecord.getEmployeeID()) &&
                safeEquals(existing.getIsFullDay(), newRecord.getIsFullDay()) &&
                safeEquals(existing.getArrivalDate(), newRecord.getArrivalDate()) &&
                safeEquals(existing.getArrivalTime(), newRecord.getArrivalTime()) &&
                safeEquals(existing.getLeftTime(), newRecord.getLeftTime()) &&
                safeEquals(existing.getIsLate(), newRecord.getIsLate()) &&
                safeEquals(existing.getLateCover(), newRecord.getLateCover()) &&
                safeEquals(existing.getIsHalfDay(), newRecord.getIsHalfDay()) &&
                safeEquals(existing.getIsFullLeave(), newRecord.getIsFullLeave()) &&
                safeEquals(existing.getIsShortLeave(), newRecord.getIsShortLeave()) &&
                safeEquals(existing.getIsAbsent(), newRecord.getIsAbsent()) &&
                safeEquals(existing.getIsUnSuccessful(), newRecord.getIsUnSuccessful()) &&
                safeEquals(existing.getIsNoPay(), newRecord.getIsNoPay()) &&
                safeEquals(existing.getIssues(), newRecord.getIssues()) &&
                safeEquals(existing.getIsUnAuthorized(), newRecord.getIsUnAuthorized()) &&
                safeEquals(existing.getResolve(), newRecord.getResolve()) &&
                safeEquals(existing.getLeaveSuccess(), newRecord.getLeaveSuccess()) &&
                safeEquals(existing.getLeaveReq(), newRecord.getLeaveReq()) &&
                safeEquals(existing.getIssueDescription(), newRecord.getIssueDescription()) &&
                safeEquals(existing.getDueDateForUA(), newRecord.getDueDateForUA()) &&
                safeEquals(existing.getActive(), newRecord.getActive()) &&
                safeEquals(existing.getNopay(), newRecord.getNopay()) &&
                safeEquals(existing.getUserId(), newRecord.getUserId()) &&
                safeEquals(existing.getViaMovement(), newRecord.getViaMovement()) &&
                safeEquals(existing.getViaLeave(), newRecord.getViaLeave()) &&
                safeEquals(existing.getIsManual(), newRecord.getIsManual()) &&
                safeEquals(existing.getTerminalID(), newRecord.getTerminalID()) &&
                safeEquals(existing.getIsHoliday(), newRecord.getIsHoliday());
    }

    // Safe comparison method that handles nulls properly
    private boolean safeEquals(Object obj1, Object obj2) {
        if (obj1 == null && obj2 == null) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        return obj1.equals(obj2);
    }

    // Alternative: Hash-based approach for even better performance
    private String generateAttendanceHash(AttendanceEntity attendance) {
        StringBuilder sb = new StringBuilder();

        // Create a hash string from all field values
        sb.append(attendance.getPublicId() != null ? attendance.getPublicId() : "NULL").append("|");
        sb.append(attendance.getDate() != null ? attendance.getDate().toString() : "NULL").append("|");
        sb.append(attendance.getEmployeeID() != null ? attendance.getEmployeeID() : "NULL").append("|");
        sb.append(attendance.getIsFullDay() != null ? attendance.getIsFullDay().toString() : "NULL").append("|");
        sb.append(attendance.getArrivalDate() != null ? attendance.getArrivalDate().toString() : "NULL").append("|");
        sb.append(attendance.getArrivalTime() != null ? attendance.getArrivalTime().toString() : "NULL").append("|");
        sb.append(attendance.getLeftTime() != null ? attendance.getLeftTime().toString() : "NULL").append("|");
        sb.append(attendance.getIsLate() != null ? attendance.getIsLate().toString() : "NULL").append("|");
        sb.append(attendance.getLateCover() != null ? attendance.getLateCover().toString() : "NULL").append("|");
        sb.append(attendance.getIsHalfDay() != null ? attendance.getIsHalfDay().toString() : "NULL").append("|");
        sb.append(attendance.getIsFullLeave() != null ? attendance.getIsFullLeave().toString() : "NULL").append("|");
        sb.append(attendance.getIsShortLeave() != null ? attendance.getIsShortLeave().toString() : "NULL").append("|");
        sb.append(attendance.getIsAbsent() != null ? attendance.getIsAbsent().toString() : "NULL").append("|");
        sb.append(attendance.getIsUnSuccessful() != null ? attendance.getIsUnSuccessful().toString() : "NULL").append("|");
        sb.append(attendance.getIsNoPay() != null ? attendance.getIsNoPay().toString() : "NULL").append("|");
        sb.append(attendance.getIssues() != null ? attendance.getIssues().toString() : "NULL").append("|");
        sb.append(attendance.getIsUnAuthorized() != null ? attendance.getIsUnAuthorized().toString() : "NULL").append("|");
        sb.append(attendance.getResolve() != null ? attendance.getResolve().toString() : "NULL").append("|");
        sb.append(attendance.getLeaveSuccess() != null ? attendance.getLeaveSuccess().toString() : "NULL").append("|");
        sb.append(attendance.getLeaveReq() != null ? attendance.getLeaveReq().toString() : "NULL").append("|");
        sb.append(attendance.getIssueDescription() != null ? attendance.getIssueDescription() : "NULL").append("|");
        sb.append(attendance.getDueDateForUA() != null ? attendance.getDueDateForUA().toString() : "NULL").append("|");
        sb.append(attendance.getActive() != null ? attendance.getActive().toString() : "NULL").append("|");
        sb.append(attendance.getNopay() != null ? attendance.getNopay().toString() : "NULL").append("|");
        sb.append(attendance.getUserId() != null ? attendance.getUserId() : "NULL").append("|");
        sb.append(attendance.getViaMovement() != null ? attendance.getViaMovement().toString() : "NULL").append("|");
        sb.append(attendance.getViaLeave() != null ? attendance.getViaLeave().toString() : "NULL").append("|");
        sb.append(attendance.getIsManual() != null ? attendance.getIsManual().toString() : "NULL").append("|");
        sb.append(attendance.getTerminalID() != null ? attendance.getTerminalID() : "NULL").append("|");
        sb.append(attendance.getIsHoliday() != null ? attendance.getIsHoliday().toString() : "NULL");

        return sb.toString();
    }

    // Super fast hash-based duplicate check
    public boolean isDuplicateAttendanceByHash(AttendanceEntity newAttendance) {
        try {
            // Get potential duplicates (small dataset)
            List<AttendanceEntity> potentialDuplicates = attendanceRepo.findByEmployeeIDAndDate(
                    newAttendance.getEmployeeID(),
                    newAttendance.getDate()
            );

            if (potentialDuplicates.isEmpty()) {
                return false;
            }

            // Generate hash for new attendance
            String newHash = generateAttendanceHash(newAttendance);

            // Check if any existing record has the same hash
            return potentialDuplicates.stream()
                    .anyMatch(existing -> newHash.equals(generateAttendanceHash(existing)));

        } catch (Exception e) {
            log.error("Error checking for duplicate attendance by hash: {}", e.getMessage(), e);
            return false;
        }
    }
}
