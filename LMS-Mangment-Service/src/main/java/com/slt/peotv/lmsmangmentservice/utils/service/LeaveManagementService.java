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

    private static final String ANNUAL_LEAVE = "Annual Leave";
    private static final String MEDICAL_LEAVE = "Medical Leave";
    private static final String CASUAL_LEAVE = "Casual Leave";
    private static final String MATERNITY_LEAVE = "Maternity Leave";
    private static final String SHORT_LEAVE = "Short Leave";
    private static final String DUTY_LEAVE = "Duty Leave";
    private static final String SPECIAL_LEAVE = "Special Leave";

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

    @JmsListener(destination = "employee.new.queue")
    @Transactional
    public void processNewEmployee(EmployeeEntity employee) {
        try {
            if (employee == null || employee.getEmployeeId() == null) {
                System.err.println("Error: Invalid employee data");
                return;
            }

            if (employeeRepository.findByEmployeeId(employee.getEmployeeId()) == null) {
                employeeRepository.save(employee);
            }
            allocateLeaves(employee);
        } catch (Exception e) {
            System.err.println("Error processing new employee: " + e.getMessage());
        }
    }

    public void allocateLeaves(EmployeeEntity employee) {
        try {
            if (employee == null || employee.getJoin_date() == null) {
                System.err.println("Error: Invalid employee or join date");
                return;
            }

            LocalDate joinDate = employee.getJoin_date().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            long yearsOfService = ChronoUnit.YEARS.between(joinDate, LocalDate.now());

            Map<String, LeaveTypeEntity> leaveTypes = getOrCreateLeaveTypes();

            if (yearsOfService < 1) {
                allocateLeavesForLessThanOneYear(employee, leaveTypes);
            } else if (yearsOfService < 2) {
                allocateLeavesForOneToTwoYears(employee, leaveTypes);
            } else if (yearsOfService < 3) {
                allocateLeavesForTwoToThreeYears(employee, leaveTypes);
            } else {
                allocateLeavesForThreeOrMoreYears(employee, leaveTypes);
            }
        } catch (Exception e) {
            System.err.println("Error allocating leaves for employee " +
                    (employee != null ? employee.getEmployeeId() : "null") +
                    ": " + e.getMessage());
        }
    }

    private Map<String, LeaveTypeEntity> getOrCreateLeaveTypes() {
        Map<String, LeaveTypeEntity> leaveTypes = new HashMap<>();
        String[] types = {ANNUAL_LEAVE, MEDICAL_LEAVE, CASUAL_LEAVE, MATERNITY_LEAVE, SHORT_LEAVE, DUTY_LEAVE, SPECIAL_LEAVE};

        for (String type : types) {
            try {
                Optional<LeaveTypeEntity> leaveTypeOp = leaveTypeRepository.findByName(type);
                if (!leaveTypeOp.isPresent()) {
                    LeaveTypeEntity leaveType = new LeaveTypeEntity();
                    leaveType.setName(type);
                    leaveType.setPublicId(UUID.randomUUID().toString());
                    leaveType = leaveTypeRepository.save(leaveType);
                    leaveTypes.put(type, leaveType);
                } else {
                    leaveTypes.put(type, leaveTypeOp.get());
                }
            } catch (Exception e) {
                System.err.println("Error processing leave type " + type + ": " + e.getMessage());
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

        if ("F".equals(employee.getGender())) {
            saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);
        }
    }

    private void allocateLeavesForOneToTwoYears(EmployeeEntity employee, Map<String, LeaveTypeEntity> leaveTypes) {
        try {
            LocalDate joinDate = employee.getJoin_date().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            Month joinMonth = joinDate.getMonth();
            int annualLeaves;

            if (joinMonth.getValue() <= Month.MARCH.getValue()) {
                annualLeaves = 14;
            } else if (joinMonth.getValue() <= Month.JUNE.getValue()) {
                annualLeaves = 10;
            } else if (joinMonth.getValue() <= Month.AUGUST.getValue()) {
                annualLeaves = 7;
            } else {
                annualLeaves = 4;
            }

            saveLeaveAllocation(employee, leaveTypes.get(ANNUAL_LEAVE), annualLeaves);
            saveLeaveAllocation(employee, leaveTypes.get(MEDICAL_LEAVE), 14);
            saveLeaveAllocation(employee, leaveTypes.get(CASUAL_LEAVE), 7);
            saveLeaveAllocation(employee, leaveTypes.get(DUTY_LEAVE), -1);
            saveLeaveAllocation(employee, leaveTypes.get(SPECIAL_LEAVE), -1);
            saveLeaveAllocation(employee, leaveTypes.get(SHORT_LEAVE), 2);

            if ("F".equals(employee.getGender())) {
                saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);
            }
        } catch (Exception e) {
            System.err.println("Error allocating leaves for 1-2 year employee: " + e.getMessage());
        }
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
        saveLeaveAllocation(employee, leaveTypes.get(SHORT_LEAVE), 2);

        if ("F".equals(employee.getGender())) {
            saveLeaveAllocation(employee, leaveTypes.get(MATERNITY_LEAVE), 180);
        }
    }

    private void saveLeaveAllocation(EmployeeEntity employee, LeaveTypeEntity leaveType, int leaves) {
        try {
            if (employee == null || leaveType == null) {
                System.err.println("Error: Invalid employee or leave type");
                return;
            }

            // Save total leaves
            UserLeaveTypeTotalEntity totalEntity = totalRepository.findByEmployeeAndLeaveType(employee, leaveType);
            if (totalEntity == null) {
                totalEntity = new UserLeaveTypeTotalEntity();
                totalEntity.setEmployee(employee);
                totalEntity.setLeaveType(leaveType);
                totalEntity.setPublicId(UUID.randomUUID().toString());
            }
            totalEntity.setTotalLeaves(leaves);
            totalRepository.save(totalEntity);

            // Save remaining leaves
            UserLeaveTypeRemainingEntity remainingEntity = remainingRepository.findByEmployeeAndLeaveType(employee, leaveType);
            if (remainingEntity == null) {
                remainingEntity = new UserLeaveTypeRemainingEntity();
                remainingEntity.setEmployee(employee);
                remainingEntity.setLeaveType(leaveType);
                remainingEntity.setPublicId(UUID.randomUUID().toString());
            }
            remainingEntity.setRemainingLeaves(leaves);
            remainingRepository.save(remainingEntity);
        } catch (Exception e) {
            System.err.println("Error saving leave allocation for employee " +
                    (employee != null ? employee.getEmployeeId() : "null") +
                    ": " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void monthlyLeaveUpdate() {
        try {
            List<EmployeeEntity> allEmployees = (List<EmployeeEntity>) employeeRepository.findAll();
            if (allEmployees == null) return;

            LocalDate currentDate = LocalDate.now();
            for (EmployeeEntity employee : allEmployees) {
                try {
                    if (employee == null || employee.getJoin_date() == null) continue;

                    LocalDate joinDate = employee.getJoin_date().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();

                    long previousMonthYears = ChronoUnit.YEARS.between(joinDate, currentDate.minusMonths(1));
                    long currentYears = ChronoUnit.YEARS.between(joinDate, currentDate);

                    if (currentYears > previousMonthYears) {
                        allocateLeaves(employee);
                    }
                } catch (Exception e) {
                    System.err.println("Error in monthly update for employee " +
                            (employee != null ? employee.getEmployeeId() : "null") +
                            ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in monthly leave update job: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 0 ? * 2#1")
    @Transactional
    public void monthlyShortLeaveUpdate() {
        try {
            employeeRepository.findAll().forEach(employee -> {
                try {
                    if (employee == null) return;

                    UserLeaveTypeRemainingEntity remainingShortLeaves =
                            serviceEvent.getUserLeaveTypeRemaining(SHORT_LEAVE, employee.getEmployeeId());

                    if (remainingShortLeaves != null && remainingShortLeaves.getRemainingLeaves() < 1) {
                        Optional<LeaveTypeEntity> leaveTypeOp = leaveTypeRepository.findByName(SHORT_LEAVE);
                        leaveTypeOp.ifPresent(leaveType -> saveLeaveAllocation(employee, leaveType, 2));
                    }
                } catch (Exception e) {
                    System.err.println("Error in short leave update for employee " +
                            (employee != null ? employee.getEmployeeId() : "null") +
                            ": " + e.getMessage());
                }
            });
        } catch (Exception e) {
            System.err.println("Error in short leave update job: " + e.getMessage());
        }
    }
}