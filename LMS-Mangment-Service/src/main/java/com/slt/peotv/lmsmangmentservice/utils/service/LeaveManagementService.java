package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.LeaveTypeRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeRemainingRepo;
import com.slt.peotv.lmsmangmentservice.repository.UserLeaveTypeTotalRepo;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class LeaveManagementService {

    @Autowired
    private EmployeeRepo employeeRepository;

    @Autowired
    private LeaveTypeRepo leaveTypeRepository;

    @Autowired
    private UserLeaveTypeRemainingRepo remainingRepository;

    @Autowired
    private UserLeaveTypeTotalRepo totalRepository;

    @Autowired
    private ServiceEvent serviceEvent;

    private static final String ANNUAL_LEAVE = "Annual Leave";
    private static final String MEDICAL_LEAVE = "Medical Leave";
    private static final String CASUAL_LEAVE = "Casual Leave";
    private static final String MATERNITY_LEAVE = "Maternity Leave";
    private static final String SHORT_LEAVE = "Short Leave";
    private static final String DUTY_LEAVE = "Duty Leave";
    private static final String SPECIAL_LEAVE = "Special Leave";


    @JmsListener(destination = "employee.new.queue")
    @Transactional
    public void processNewEmployee(EmployeeEntity employee) {
        // Save employee to database if not already exists
        if (employeeRepository.findByEmployeeId(employee.getEmployeeId()) == null) {
            employeeRepository.save(employee);
        }
        
        // Process leave allocation for this employee
        allocateLeaves(employee);
    }


    public void allocateLeaves(EmployeeEntity employee) {
        // Calculate years of service
        LocalDate joinDate = employee.getJoin_date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate currentDate = LocalDate.now();
        
        long yearsOfService = ChronoUnit.YEARS.between(joinDate, currentDate);
        
        // Get or create leave types
        Map<String, LeaveTypeEntity> leaveTypes = getOrCreateLeaveTypes();
        
        // Allocate leaves based on years of service
        if (yearsOfService < 1) {
            // Less than 1 year: No annual leaves
            allocateLeavesForLessThanOneYear(employee, leaveTypes);
        } else if (yearsOfService < 2) {
            // Between 1 and 2 years: Annual leaves based on join month
            allocateLeavesForOneToTwoYears(employee, leaveTypes);
        } else if (yearsOfService < 3) {
            // Between 2 and 3 years: Annual leaves based on join month
            allocateLeavesForTwoToThreeYears(employee, leaveTypes);
        } else {
            // 3+ years: Standard allocation regardless of join month
            allocateLeavesForThreeOrMoreYears(employee, leaveTypes);
        }
    }


    private Map<String, LeaveTypeEntity> getOrCreateLeaveTypes() {
        Map<String, LeaveTypeEntity> leaveTypes = new HashMap<>();
        
        // Define leave types
        String[] types = {ANNUAL_LEAVE, MEDICAL_LEAVE, CASUAL_LEAVE, MATERNITY_LEAVE,SHORT_LEAVE,DUTY_LEAVE,SPECIAL_LEAVE};
        
        for (String type : types) {
            Optional<LeaveTypeEntity> leaveTypeOp = leaveTypeRepository.findByName(type);
            if (leaveTypeOp.isEmpty()) {
            	LeaveTypeEntity leaveType = new LeaveTypeEntity(); 
                leaveType.setName(type);
                leaveType.setPublicId(UUID.randomUUID().toString());
                leaveType = leaveTypeRepository.save(leaveType);
                leaveTypes.put(type, leaveType);
            }else {
                leaveTypes.put(type, leaveTypeOp.get());
            }
        }
        
        return leaveTypes;
    }


    private void allocateLeavesForLessThanOneYear(EmployeeEntity employee, Map<String, LeaveTypeEntity> leaveTypes) {
        saveLeaveAllocation(employee, leaveTypes.get(ANNUAL_LEAVE), 0);
        
        saveLeaveAllocation(employee, leaveTypes.get(MEDICAL_LEAVE), 14);
        saveLeaveAllocation(employee, leaveTypes.get(CASUAL_LEAVE), 7);
        saveLeaveAllocation(employee, leaveTypes.get(SHORT_LEAVE), 2);
        
        saveLeaveAllocation(employee, leaveTypes.get(DUTY_LEAVE), -1);
        saveLeaveAllocation(employee, leaveTypes.get(SPECIAL_LEAVE), -1);

        if(employee.getGender().equals("F"))
            saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);
    }


    private void allocateLeavesForOneToTwoYears(EmployeeEntity employee, Map<String, LeaveTypeEntity> leaveTypes) {
        // Annual leaves based on join month
        LocalDate joinDate = employee.getJoin_date().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        
        Month joinMonth = joinDate.getMonth();
        int annualLeaves;
        
        if (joinMonth.getValue() >= Month.JANUARY.getValue() && joinMonth.getValue() <= Month.MARCH.getValue()) {
            annualLeaves = 14;
        } else if (joinMonth.getValue() >= Month.APRIL.getValue() && joinMonth.getValue() <= Month.JUNE.getValue()) {
            annualLeaves = 10;
        } else if (joinMonth.getValue() >= Month.JULY.getValue() && joinMonth.getValue() <= Month.AUGUST.getValue()) {
            annualLeaves = 7;
        } else {
            annualLeaves = 4;
        }
        
        saveLeaveAllocation(employee, leaveTypes.get(ANNUAL_LEAVE), annualLeaves);
        
        saveLeaveAllocation(employee, leaveTypes.get(MEDICAL_LEAVE), 14);
        saveLeaveAllocation(employee, leaveTypes.get(CASUAL_LEAVE), 7);

        saveLeaveAllocation(employee, leaveTypes.get(DUTY_LEAVE), -1);
        saveLeaveAllocation(employee, leaveTypes.get(SPECIAL_LEAVE), -1);

        if(employee.getGender().equals("F"))
            saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);

        saveLeaveAllocation(employee, leaveTypes.get(SHORT_LEAVE), 2);
    }


    private void allocateLeavesForTwoToThreeYears(EmployeeEntity employee, Map<String, LeaveTypeEntity> leaveTypes) {
        allocateLeavesForOneToTwoYears(employee, leaveTypes);
    }


    private void allocateLeavesForThreeOrMoreYears(EmployeeEntity employee, Map<String, LeaveTypeEntity> leaveTypes) {

        saveLeaveAllocation(employee, leaveTypes.get(ANNUAL_LEAVE), 14);
        saveLeaveAllocation(employee, leaveTypes.get(MEDICAL_LEAVE), 14);
        saveLeaveAllocation(employee, leaveTypes.get(CASUAL_LEAVE), 7);
        
        saveLeaveAllocation(employee, leaveTypes.get(DUTY_LEAVE), -1);
        saveLeaveAllocation(employee, leaveTypes.get(SPECIAL_LEAVE), -1); 

        if(employee.getGender().equals("F"))
            saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);

        saveLeaveAllocation(employee, leaveTypes.get(SHORT_LEAVE), 2);
    }

    private void saveLeaveAllocation(EmployeeEntity employee, LeaveTypeEntity leaveType, int leaves) {
        // Save total leaves
        UserLeaveTypeTotalEntity totalEntity = totalRepository.findByEmployeeAndLeaveType(
                employee, leaveType);
        
        if (totalEntity == null) {
            totalEntity = new UserLeaveTypeTotalEntity();
            totalEntity.setEmployee(employee);
            totalEntity.setLeaveType(leaveType);
            totalEntity.setPublicId(UUID.randomUUID().toString());
        }
        
        totalEntity.setTotalLeaves(leaves);
        totalRepository.save(totalEntity);
        
        // Save remaining leaves (initially equal to total)
        UserLeaveTypeRemainingEntity remainingEntity = remainingRepository.findByEmployeeAndLeaveType(
                employee, leaveType);
        
        if (remainingEntity == null) {
        	remainingEntity = new UserLeaveTypeRemainingEntity();
            remainingEntity.setEmployee(employee);
            remainingEntity.setLeaveType(leaveType);
            remainingEntity.setPublicId(UUID.randomUUID().toString());
        }
        
        remainingEntity.setRemainingLeaves(leaves);
        remainingRepository.save(remainingEntity);
    }


    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void monthlyLeaveUpdate() {
        List<EmployeeEntity> allEmployees = (List<EmployeeEntity>) employeeRepository.findAll();
        LocalDate currentDate = LocalDate.now();
        
        for (EmployeeEntity employee : allEmployees) {
            LocalDate joinDate = employee.getJoin_date().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            long previousMonthYears = ChronoUnit.YEARS.between(
                    joinDate, 
                    currentDate.minusMonths(1)
            );
            
            long currentYears = ChronoUnit.YEARS.between(joinDate, currentDate);
            
            // Check if employee has crossed a service milestone this month
            if (currentYears > previousMonthYears) {
                // Employee has reached a new year of service
                allocateLeaves(employee);
            }
        }
    }

    @Scheduled(cron = "0 0 0 ? * 2#1")
    @Transactional
    public void monthlyShortLeaveUpdate() {
        employeeRepository.findAll().forEach(employee -> {
            UserLeaveTypeRemainingEntity remaining_short_Leaves = serviceEvent.getUserLeaveTypeRemaining("Short Leave", employee.getEmployeeId());
            if (remaining_short_Leaves.getRemainingLeaves() < 1) {
                Optional<LeaveTypeEntity> leaveTypeOp = leaveTypeRepository.findByName(SHORT_LEAVE);
                leaveTypeOp.ifPresent(leaveTypeEntity -> saveLeaveAllocation(employee, leaveTypeEntity, 2));
            }
        });
    }
}
