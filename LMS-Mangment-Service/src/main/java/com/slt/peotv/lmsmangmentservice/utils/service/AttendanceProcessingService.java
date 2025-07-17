package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.repository.LeaveRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.util.Date;

@Service
public class AttendanceProcessingService {

    @Autowired
    private LeaveRepo leaveRepository;
    @Autowired
    private InOutRepo inOutRepository;
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
        System.out.println("Employee Date: " + processDate);

        if (employee == null) return;

        String employeeId = employee.getEmployeeId();
        if (employeeId.isEmpty())
            return;


        leave.setNotUsed(false);
        leave.setDescription("Absent - Leave Used");
        LeaveTypeEntity leaveType = leave.getLeaveType();
        leave.setRequestStatus(RequestStatus.SUBMITTED);

        String user = leave.getEmployee().getEmployeeId();
        if (user != null) {
            UserLeaveTypeRemainingEntity currentLeave = getUserLeaveTypeRemaining(leaveType.getName(), user);
            if(currentLeave == null) return;
            if (currentLeave.getRemainingLeaves() > 1) {
                currentLeave.setRemainingLeaves(currentLeave.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(currentLeave);
            }
            leaveRepository.save(leave);
            checkService.reportAttendance(employeeId, true, false, false, false, false, false, false, true, true, true, true, false, true, helper.getYesterdayDate());

        }
    }
    private UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String name, String user) {
        return serviceEvent.getUserLeaveTypeRemaining(name, user);
    }


    private boolean checkLateArrival(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime().after(Time.valueOf("09:00:00"));
    }

    private boolean checkShortLeave(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime().before(Time.valueOf("16:00:00"));
    }

    private boolean checkHalfDay(InOutEntity inOut) {
        return (inOut.getPunchTime() != null && inOut.getPunchTypeTime() == null);
    }

    private boolean checkFullAttendance(InOutEntity inOut) {
        return inOut.getPunchTime() != null && inOut.getPunchTypeTime() != null;
    }
}
