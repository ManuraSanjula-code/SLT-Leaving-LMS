package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class AttendanceProcessingService {

    @Autowired
    private LeaveRepo leaveRepository;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private Check_Service checkService;
    @Autowired
    private Helper helper;

    @Transactional
    public void processEmployeeLeave(EmployeeEntity employee, LeaveEntity leave, Date processDate) {
        try {
            // Basic validation
            if (employee == null || leave == null || processDate == null) {
                System.err.println("Error: Null parameter in processEmployeeLeave");
                return;
            }

            String employeeId = employee.getEmployeeId();
            if (employeeId == null || employeeId.isEmpty()) {
                System.err.println("Error: Empty employee ID");
                return;
            }

            // Process leave
            processLeave(leave, employeeId);

            // Update attendance
            updateAttendance(employeeId);

        } catch (Exception e) {
            System.err.println("Error processing leave for employee " +
                    (employee != null ? employee.getEmployeeId() : "null") +
                    ": " + e.getMessage());
        }
    }

    private void processLeave(LeaveEntity leave, String employeeId) {
        try {
            LeaveTypeEntity leaveType = leave.getLeaveType();
            if (leaveType == null) {
                System.err.println("Error: No leave type for leave ID " + leave.getId());
                return;
            }

            String user = leave.getEmployee() != null ? leave.getEmployee().getEmployeeId() : null;
            if (user == null) {
                System.err.println("Error: No employee associated with leave ID " + leave.getId());
                return;
            }

            UserLeaveTypeRemainingEntity currentLeave = serviceEvent.getUserLeaveTypeRemaining(leaveType.getName(), user);
            if (currentLeave == null) {
                System.err.println("Error: No leave balance found for user " + user);
                return;
            }

            if (currentLeave.getRemainingLeaves() > 0) {
                leave.setNotUsed(false);
                leave.setDescription("Absent - Leave Used");
                leave.setRequestStatus(RequestStatus.SUBMITTED);

                currentLeave.setRemainingLeaves(currentLeave.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(currentLeave);
                leaveRepository.save(leave);
            } else {
                System.err.println("Warning: No remaining leaves for user " + user);
            }
        } catch (Exception e) {
            System.err.println("Error in processLeave: " + e.getMessage());
        }
    }

    private void updateAttendance(String employeeId) {
        try {
            Date yesterdayDate = helper.getYesterdayDate();
            if (yesterdayDate == null) {
                System.err.println("Error: Yesterday date is null");
                return;
            }

            checkService.reportAttendance(
                    employeeId,
                    true, false, false, false,
                    false, false, false, true,
                    true, true, true, false,
                    true, yesterdayDate
            );
        } catch (Exception e) {
            System.err.println("Error updating attendance for employee " + employeeId + ": " + e.getMessage());
        }
    }
}