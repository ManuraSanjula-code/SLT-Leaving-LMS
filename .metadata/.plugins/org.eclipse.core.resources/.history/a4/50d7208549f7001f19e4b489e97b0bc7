package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.UserLeaveTypeRemaining;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.model.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.types.MovementType;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class Check_Service_Impl implements Check_Service {

    @Autowired
    private static AttendanceRepo attendanceRepo;
    @Autowired
    private static Utils utils;
    @Autowired
    private static NoPayRepo noPayRepo;
    private final ModelMapper modelMapper = new ModelMapper();
    @Autowired
    private LMS_Service lmsService;
    @Autowired
    private InOutRepo inOutRepo;
    @Autowired
    private MovementsRepo movementsRepo;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private AbsenteeRepo absenteeRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepo;
    @Autowired
    private Helper helper;
    @Autowired
    private LeaveRepo leaveRepo;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;


    @Override
    public NoPayEntity saveNoPayEntity(String employeeId, AttendanceEntity attendanceEntity, Boolean isHalfDay, Boolean unSuccessful, Boolean isLate, Boolean isLateCover, Boolean isAbsent,
                                       Date accualDate) {
        if (attendanceEntity == null) {
            attendanceEntity = new AttendanceEntity();

            attendanceEntity.setPublicId(utils.generateId(10));
            attendanceEntity.setDate(helper.getYesterdayDate());
            attendanceEntity.setIsHalfDay(isHalfDay);
            attendanceEntity.setIsUnSuccessful(unSuccessful);
            attendanceEntity.setLateCover(isLate);
            attendanceEntity.setLateCover(isLateCover);
            attendanceEntity.setIsAbsent(isAbsent);
            attendanceEntity.setEmployeeID(employeeId);

            attendanceRepo.save(attendanceEntity);

        }
        NoPayEntity nopayEntity = new NoPayEntity();

        nopayEntity.setEmployeeID(employeeId);
        nopayEntity.setPublicId(utils.generateId(10));
        nopayEntity.setAcctualDate(accualDate == null ? new Date() : accualDate);
        nopayEntity.setSubmissionDate(new Date());
        nopayEntity.setSubmissionDate(new Date());

        nopayEntity.setIsHalfDay(isHalfDay);
        nopayEntity.setUnSuccessful(unSuccessful);
        nopayEntity.setIsLate(isLate);
        nopayEntity.setIsLateCover(isLateCover);
        nopayEntity.setIsAbsent(isAbsent);

        nopayEntity.setHappenDate(accualDate);

        StringBuilder description = new StringBuilder();

        if (isAbsent) description.append("Absent on ").append(accualDate).append(". ");
        if (isHalfDay) description.append("Half-day on ").append(accualDate).append(". ");
        if (unSuccessful) description.append("Unsuccessful attendance on ").append(accualDate).append(". ");
        if (isLate) description.append("Late on ").append(accualDate).append(". ");
        if (isLateCover) description.append("Late cover on ").append(accualDate).append(". ");

        String finalDescription = description.toString().trim();
        nopayEntity.setComment(finalDescription);
        nopayEntity.setAttendance(attendanceEntity);

        attendanceEntity.setIsNoPay(true);
        attendanceRepo.save(attendanceEntity);

        nopayEntity = noPayRepo.save(nopayEntity);

        return nopayEntity;
    }

    @Override
    public void requestMovement(MovementReq req, Date dueDate) {

        UserLeaveTypeRemaining casual = getUserLeaveTypeRemaining("CASUAL", req.getEmployeeId());
        UserLeaveTypeRemaining annual = getUserLeaveTypeRemaining("ANNUAL", req.getEmployeeId());
        UserLeaveTypeRemaining sick = getUserLeaveTypeRemaining("SICK", req.getEmployeeId());
        UserLeaveTypeRemaining special = getUserLeaveTypeRemaining("SPECIAL", req.getEmployeeId());
        UserLeaveTypeRemaining duty = getUserLeaveTypeRemaining("DUTY", req.getEmployeeId());
        UserLeaveTypeRemaining maternityLeave = getUserLeaveTypeRemaining("MATERNITY_LEAVE", req.getEmployeeId());

        switch (req.getCategory()) {
            case "CASUAL" -> {
                if (casual.getRemainingLeaves() < 1) return;
            }
            case "ANNUAL" -> {
                if (annual.getRemainingLeaves() < 1) return;
            }
            case "SICK" -> {
                if (sick.getRemainingLeaves() < 1) return;
            }
            case "SPECIAL" -> {
                if (special.getRemainingLeaves() < 1) return;
            }
            case "DUTY" -> {
                if (duty.getRemainingLeaves() < 1) return;
            }
            case "MATERNITY LEAVE" -> {
                if (maternityLeave.getRemainingLeaves() < 1) return;
            }
            default -> {
                throw new IllegalArgumentException("Invalid leave type: " + req.getCategory());
            }
        }

        MovementsEntity movementsEntity = modelMapper.map(req, MovementsEntity.class);
        movementsEntity.setPublicId(utils.generateId(10));
        movementsEntity.setEmployeeID(req.getEmployeeId());
        movementsEntity.setReqDate(new Date());
        movementsEntity.setLogTime(new Date());

        movementsEntity.setIsHalfDay(req.getHalfDay());
        movementsEntity.setIsAbsent(req.getAbsent());
        movementsEntity.setIsLate(req.getLate());
        movementsEntity.setIsLateCover(req.getLateCover());
        movementsEntity.setHappenDate(req.getHappenDate());

        movementsEntity.setIsUnSuccessfulAttdate(req.getIsUnSuccessfulAttdate());
        movementsEntity.setHappenDate(req.getHappenDate());
        movementsEntity.setIsUnSuccessfulAttdate(req.getUnSuccessfulAttdate());
        movementsEntity.setIsHalfDay(req.getHalfDay());

        //movementsEntity.setDueDate(dueDate);

        movementsEntity.setIsHalfDay(req.getHalfDay());
        movementsEntity.setIsLateCover(req.getLateCover());
        movementsEntity.setIsPending(false);
        movementsEntity.setIsAccepted(false);
        movementsEntity.setIsExpired(false);
        movementsEntity.setIsLateCover(req.getLateCover());

        lmsService.createMovements(movementsEntity);

    }

    public void approvedMove(MovementsEntity entity) {
        MovementType movementType = entity.getMovementType();

        /// When Adding a due date make sure put extra 1 month 2 weeks
        List<UserLeaveTypeRemaining> userLeaveTypeRemainingRepo = serviceEvent.getUserLeaveTypeRemaining(entity.getEmployeeId());

        List<UserLeaveTypeRemaining> filteredList = userLeaveTypeRemainingRepo.stream()
                .filter(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1)
                .collect(Collectors.toList());

        boolean allMatch = userLeaveTypeRemainingRepo.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
        if (allMatch) return;


        Date movementDate = entity.getHappenDate();
        List<MovementsEntity> byHappenDate = movementsRepo.findByHappenDate(movementDate);

        Optional<AttendanceEntity> attendance = attendanceRepo.findByEmployeeIDAndDate(entity.getEmployeeId(), entity.getHappenDate());
        Optional<AbsenteeEntity> absentee = absenteeRepo.findByEmployeeIDAndDate((entity.getEmployeeId()), entity.getHappenDate());
        if (attendance.isPresent()) {

            AttendanceEntity attendanceEntity = attendance.get();
            attendanceEntity.setResolve(true);
            attendanceRepo.save(attendanceEntity);

            if (absentee.isPresent()) {
                AbsenteeEntity absenteeEntity = absentee.get();
                absenteeEntity.setIsArchived(true);
                absenteeEntity.setComment("EMPLOYEE RESOLVE HIS/HER " + (absenteeEntity.getIsHalfDay() ? "HALF DAY" : absenteeEntity.getIsAbsent() ? "ABSENT" : "ISSUE WITH HIS/HER ATTENDANCE"));
                absenteeRepo.save(absenteeEntity);
            }
        }
    }

    @Override
    public void processMovementBySup(String superId, String moveId) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY
    }

    @Override
    public void processMovementByHOD(String hodId, String moveId) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

    }

    @Override
    public void processMovementParticularUserBySup(String superId, String userId) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

    }

    @Override
    public void processMovementParticularUserByHOD(String hodId, String userId) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

    }

    @Override
    public void processMovementParticularIdsBySup(String superId, List<String> ids) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

    }

    @Override
    public void processMovementParticularIdsByHOD(String hodId, List<String> ids) {
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

    }

    @Override
    public void main() {

        List<AttendanceEntity> attendanceEntities = attendanceRepo.findByDueDateForUA(new Date());
        List<AttendanceEntity> overdueEntities_filter = StreamSupport.stream(attendanceEntities.spliterator(), false)
                .filter(entity -> Boolean.TRUE.equals(entity.getIsUnAuthorized()) || Boolean.TRUE.equals(entity.getIsUnSuccessful()))
                .collect(Collectors.toList());

        overdueEntities_filter.forEach(entity -> {

            /// CHECK ARE THERE ANY LEAVE REQ
            List<LeaveEntity> allTheLeavesByEmployee = leaveRepo.findByEmployeeIDAndIsManualRequest(entity.getEmployeeID(), true);
            allTheLeavesByEmployee = StreamSupport.stream(allTheLeavesByEmployee.spliterator(), false)
                    .filter(leave -> leave.getSubmitDate().equals(new Date()))
                    .collect(Collectors.toList());

            /// CHECK ARE THERE ANY MOVEMENTS
            List<MovementsEntity> allTheMovementsByEmployee = movementsRepo.findByIsPendingAndEmployeeID(true, entity.getEmployeeID());
            allTheMovementsByEmployee = StreamSupport.stream(allTheMovementsByEmployee.spliterator(), false)
                    .filter(movement -> movement.getReqDate().equals(new Date()))
                    .collect(Collectors.toList());

            if(allTheLeavesByEmployee.isEmpty() || allTheMovementsByEmployee.isEmpty())
                saveNoPayEntity(entity.getEmployeeID(), null, false,
                        false, false, false,
                        true, entity.getDate());

            allTheLeavesByEmployee.forEach(leave -> {

                if(leave.getIsPending() && !leave.getIsAccepted()){
                    saveNoPayEntity(entity.getEmployeeID(), null, false,
                            false, false, false,
                            true, entity.getDate());
                }
            });

            allTheMovementsByEmployee.forEach(movement -> {
                if(movement.getIsPending() && !movement.getIsAccepted()){
                    saveNoPayEntity(entity.getEmployeeID(), null, false,
                            false, false, false,
                            true, entity.getDate());
                }
            });
        });

        prerequisite();

    }

    public List<InOutEntity> getMorningPunchOnlyRecords() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return inOutRepo.findByIsMoaningTrueAndIsEveningFalseAndPunchInMoa(yesterdayDate);
    }

    public List<InOutEntity> getEveningPunchOnlyRecords() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        Date yesterdayDate = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return inOutRepo.findByIsMoaningFalseAndIsEveningTrueAndPunchInEv(yesterdayDate);
    }

    public boolean checkLateCoverage(Date date) {
        Time lateArrivalThreshold = Time.valueOf("08:30:00"); // Define the late arrival threshold
        Time coverageEndTime = Time.valueOf("17:30:00"); // Define the end of the regular working hours
        Time coverageStartTime = Time.valueOf("17:00:00"); // Define the start time for coverage check

        // Retrieve employees who arrived after the late arrival threshold
        List<InOutEntity> lateEmployees = inOutRepo.findByDateAndTimeMoaAfter(date, lateArrivalThreshold);

        for (InOutEntity employee : lateEmployees) {
            // Calculate the late arrival time in minutes
            long lateMinutes = (employee.getTimeMoa().getTime() - lateArrivalThreshold.getTime()) / (60 * 1000);

            // If the employee arrived more than 30 minutes late
            if (lateMinutes > 30) {
                // Check if their leaving time (timeEve) falls within the coverage start time and end time
                if (employee.getTimeEve().after(coverageStartTime) && employee.getTimeEve().before(coverageEndTime)) {
                    // Calculate coverage time based on their leaving time
                    long coverageMinutes = (employee.getTimeEve().getTime() - coverageStartTime.getTime()) / (60 * 1000);
                    if (coverageMinutes < lateMinutes) {
                        return false; // Did not cover their required late time
                    }
                } else {
                    return false; // Did not cover their required late time
                }
            }
        }
        return true; // All employees covered their required late time
    }

    @Override
    public void prerequisite() {
        LocalDateTime yesterdayBefore830 = LocalDate.now().minusDays(1).atTime(8, 29);
        Time sqlTime = Time.valueOf(yesterdayBefore830.toLocalTime());

        LocalTime eveAfter = LocalTime.of(17, 0);  // 5:00 PM
        LocalTime eveBefore = LocalTime.of(23, 59); // 11:59 PM
        Time timeEveAfter = Time.valueOf(eveAfter);
        Time timeEveBefore = Time.valueOf(eveBefore);

        /// Employees coming before 8.30 am
        Set<InOutEntity> employeesArrivedBefore830 = new HashSet<>(inOutRepo.findByDateAndTimeMoa(helper.getYesterdayDate(), sqlTime));

        /// Employees leave the office between 5.00 - 5.30 pm
        Set<InOutEntity> employeesLeftBetween500And530 = new HashSet<>(inOutRepo.findByDateAndTimeEveBetween(helper.getYesterdayDate(), timeEveAfter, timeEveBefore));

        /// Employees Half-Day
        LocalTime after12_30PM = LocalTime.of(12, 30);
        Time timeAfter12_30PM = Time.valueOf(after12_30PM);
        Set<InOutEntity> inOutEntities_EmployeesHalfDay = new HashSet<>(inOutRepo.findByDateAndTimeMoaAfter(helper.getYesterdayDate(), timeAfter12_30PM));

        /// Employees who Covered LateTime - 1

        /*Time lateArrivalTime = Time.valueOf("08:31:00"); // Define late arrival time
        Time coverStartTime = Time.valueOf("17:30:00"); // Start time for coverage check
        Time coverEndTime = Time.valueOf("23:59:59"); // End time for coverage check

        Set<InOutEntity> inOutEntities_EmployeesWhoCoveredLateTime = new HashSet<>(
                inOutRepo.findByDateAndTimeMoaAfterAndTimeEveBetween(getYesterdayDate(), lateArrivalTime, coverStartTime, coverEndTime));

        /// Employees Who Did Not Cover LateTime
        Set<InOutEntity> inOutEntities_EmployeesWhoDidNotCoverLateTime = new HashSet<>(
                inOutRepo.findByDateAndTimeMoaAfter830AndNotCoveredLateArrival(getYesterdayDate()));*/

        /// Employees Who came between 8.30 - 9.00 am
        Time startTime = Time.valueOf("08:30:00");
        Time endTime = Time.valueOf("09:00:00");
        Set<InOutEntity> employeesArrivedBetween830And900 = new HashSet<>(inOutRepo.findByDateAndTimeMoaBetween(helper.getYesterdayDate(), startTime, endTime));


        /// Cover -- FULL DAY ✅
        /// Cover -- UnAuthorized ✅ (swipe error)
        /// Cover -- UnSuccessFull ✅ (Late and Late work do not cover)
        /// Cover -- LATE ✅ (Late and Late work do cover so it will consider as full day)
        /// Cover -- HALF-DAY ✅

        /// Report employee who has full day attendance and who has swipe error ***************************************** --- START

        if (employeesArrivedBefore830.equals(employeesLeftBetween500And530)) {
            /// On Time Employees and full day
            Set<InOutEntity> commonEmployees = new HashSet<>(employeesArrivedBefore830);
            commonEmployees.retainAll(employeesLeftBetween500And530);

            for (InOutEntity commonEmployee : commonEmployees)
                reportAttendance(commonEmployee, true, false, false, false, false, false);

        } else {
            /// UnAuthorized employees (employees who forgot to swipe the card)

            HashSet<InOutEntity> inOutEntities_MorningPunch = new HashSet<>(getMorningPunchOnlyRecords());
            HashSet<InOutEntity> inOutEntities_EveningPunch = new HashSet<>(getEveningPunchOnlyRecords());

            for (InOutEntity employee : inOutEntities_MorningPunch)
                reportAttendance(employee, false, true, false, false, false, false);

            for (InOutEntity employee : inOutEntities_EveningPunch)
                reportAttendance(employee, false, true, false, false, false, false);

            NoPayEntity noPayEntity = new NoPayEntity();
            /// GOING NO-PAY
        }


        /// Report employee who has full day attendance and who has swipe error ***************************************** --- END

        /// Reporting Late employees  ********************************************************* --- START
        employeesArrivedBetween830And900.forEach(entity -> {

            /*inOutEntities_EmployeesWhoDidNotCoverLateTime.forEach(dnclt -> {
                reportAttendance(dnclt, false, false, true, true, false, false);
            });

            inOutEntities_EmployeesWhoCoveredLateTime.forEach(clt -> {
                reportAttendance(clt, true, false, false, true, true, false);
            });*/

            if (!checkLateCoverage(entity.getDate()))
                reportAttendance(entity, false, false, true, true, false, false);
            else
                reportAttendance(entity, true, false, false, true, true, false);

        });
        /// Reporting Late employees  ********************************************************* --- END


        /// Reporting Half Days  ********************************************************* --- START
        inOutEntities_EmployeesHalfDay.forEach(entity -> {

            /// CHECKING IF EMPLOYEE MIGHT PUT A LEAVE BEFORE SHE/HE ABSENT (HALF-DAY) -- EMPLOYEE DO
            List<LeaveEntity> byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual = leaveRepo.findApprovedLeavesByEmployeeIDAndFromDateAndToDate(entity.getEmployeeID(), new Date(), new Date());

            if (!byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.isEmpty()) {
                byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.forEach(leaveEntity -> {

                    /// DOUBLE CHECK LEAVE DATE MATCH CURRENT DATE AND WHETHER LEAVE APPROVED OR NOT
                    if (leaveEntity.getIsHODApproved() && leaveEntity.getIsSupervisedApproved() && leaveEntity.getToDate().equals(helper.getYesterdayDate())) {

                        if (leaveEntity.getIsHalfDay()) {
                            /// GETTING ONLY HALF-DAYS
                            /// Employee absent || Employee make a leave before she/he absent
                            /// AND SHE/HE NOW USED THE LEAVE
                            /// cut of the leave because leave actually been used and mark as used

                            leaveEntity.setDescription("Absent - Leave Used");
                            leaveEntity.setNotUsed(false); /// WHICH MEANS EMPLOYEE USE THE LEAVE

                            /// CUT OF ONE OF THE LEAVES
                            UserLeaveTypeRemaining userLeaveTypeRemaining = getUserLeaveTypeRemaining(leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
                            if (userLeaveTypeRemaining.getRemainingLeaves() < 1) {
                                userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                                userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
                            }

                            leaveRepo.save(leaveEntity);

                        }

                    } else {

                        /// LEAVE NOT APPROVED BUT EMPLOYEE ABSENT
                        /// CHECK ARE THERE ANY REMAINING LEAVES -- IF YES -> OKAY || IF NO -> NO_PAY
                        List<UserLeaveTypeRemaining> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(entity.getEmployeeID());
                        boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);

                        if (allMatch) { /// NO REMAINING LEAVES
                        /// GOING NO PAY -- SET DESCRIPTION IN NO-PAY
                            leaveEntity.setIsPending(true);
                            leaveEntity.setDescription("EMPLOYEE IS ABSENT ALSO HE/SHE MAKE REQUEST TO LEAVE NOT APPROVED HENCE THIS LEAVE STILL PENDING");
                            saveNoPayEntity(leaveEntity.getEmployeeID(), null, false, true, false, false, false, leaveEntity.getHappenDate());

                        } else {
                            /// THERE ARE LEAVES

                            helper.handleAbsenteeReqHalf(leaveEntity.getEmployeeID());
                        }

                    }
                });
            } else {

                /// CHECKING IF EMPLOYEE MIGHT PUT A LEAVE BEFORE SHE/HE ABSENT (HALF-DAY) --- EMPLOYEE DO NOT
                /// CHECK ARE THERE ANY REMAINING LEAVES -- IF YES -> OKAY || IF NO -> NO_PAY

                List<UserLeaveTypeRemaining> userLeaveCategoryRemaining = serviceEvent.getUserLeaveTypeRemaining(entity.getEmployeeID());
                boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);


                /// GOING NO PAY -- SET DESCRIPTION IN NO-PAY
                if (allMatch)
                    saveNoPayEntity(entity.getEmployeeID(), null, false, true, false, false, false, helper.getYesterdayDate());

                /// THERE ARE LEAVES
                helper.handleAbsenteeReqHalf(entity.getEmployeeID());
            }

        });
        /// Reporting Half Days  ********************************************************* --- END

        List<String> absentEmployeesToday = getAbsentEmployeesToday_();
        reportAbsent(absentEmployeesToday);
    }

    public List<String> getAbsentEmployeesToday_() {
        List<InOutEntity> todayRecords = inOutRepo.findByDate(helper.getYesterdayDate());

        // Synchronized list to handle multiple threads safely
        List<String> absentEmployeesToday = Collections.synchronizedList(new ArrayList<>());

        Set<String> presentUserIds = todayRecords.stream()
                .map(InOutEntity::getEmployeeID)
                .collect(Collectors.toSet());

        /// GET ALL THE EMPLOYEES FROM THE SERVER

        return absentEmployeesToday;
    }


    public List<String> getAbsentEmployeesToday() {
        // Fetch today's InOut records
        List<InOutEntity> todayRecords = inOutRepo.findByDate(helper.getYesterdayDate());

        // Use a thread-safe set to store present employee IDs
        Set<String> presentEmployeeIds = ConcurrentHashMap.newKeySet();

        // Collect user IDs from today's records (thread-safe)
        todayRecords.parallelStream().forEach(inOut -> {
            presentEmployeeIds.add(inOut.getEmployeeID());
        });

        // Get all users and filter out those who are absent
        List<String> absentEmployeesToday = Collections.synchronizedList(new ArrayList<>());
        /// GET ALL THE EMPLOYEES FROM THE SERVER

        return absentEmployeesToday;
    }

    @Override
    public void reportAttendance(InOutEntity inout, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day) {

        if (inout.getEmployeeID() == null) return;

        if (attendanceRepo.existsByEmployeeIDAndDate(inout.getEmployeeID(), helper.getYesterdayDate())) return;

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(inout.getEmployeeID());
        attendance.setDate(inout.getDate());

        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);

        attendance.setArrivalDate(inout.getPunchInMoa());
        attendance.setArrivalTime(inout.getTimeMoa());
        attendance.setLeftTime(inout.getTimeEve());

        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  " + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE") + "AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        } else if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(inout.getEmployeeID(), attendance);
            attendance.setDueDateForUA(helper.getDueDate()); /// Get all the un-successful attendance if date goes make it no pay
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  " +
                    (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE") +
                    " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendanceRepo.save(attendance);

    }

    /*
      An employee punches their card when they arrive, and this punch is stored in our database. They punch again at 5:30 PM.
      If the employee arrives on time, our system checks whether there is an attendance record for that employee on the current day.
      If an entry exists, we save the latest morning punch-in as well as the latest evening punch-out.
      We then retrieve both the earliest morning and evening records to calculate the total working hours,
      determining whether the employee has worked a full day or a half day.
      It is possible for an employee to have multiple punch-ins and punch-outs in both the morning and evening.
      In such cases, we take the earliest punch-in from the morning and the earliest punch-out from the evening to calculate attendance.

      If an employee has only a morning punch-in without an evening punch-out, it is considered an unsuccessful (or unauthorized) attendance.
      If an employee punches in later than the designated time, it is marked as late.
      Additionally, if the employee arrives more than 30 minutes late, it is considered a short leave.
      If the employee repeats the same behavior the next day, it is again marked as short leave.
      However, if the same pattern continues on the third day, it is considered a half-day absence.

      And employee need to cover is late work (imagine he came within 30 min waiting period) if employee do not cover it is considered a short leave.
      If the employee repeats the same behavior the next day, it is again marked as short leave.
      However, if the same pattern continues on the third day, it is considered a half-day absence.

      if employee new (means he/she start working not more than 1 year) which means no leaves
      if employee work at 2 years which means there are leaves depending on when he/she end the their 1st year
      if employee work at 3 year he/she what ever she want until all leaves are gone
    * */

    @Override
    /// CHECK IT PLEASE ⚠️ DATA IS MISSING OR NOT
    public <T> void reportAttendance(Object obj, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day) {
        InOutEntity inOutEntity = null;
        AttendanceEntity attendanceEntity = null;

        if (obj instanceof InOutEntity) {
            inOutEntity = (InOutEntity) obj;
        } else if (obj instanceof AttendanceEntity) {
            attendanceEntity = (AttendanceEntity) obj;
        } else {
            System.out.println("Unknown Class");
            return;
        }

        // Dynamically fetch UserEntity based on the type of obj
        String userByEmployeeId = (inOutEntity != null) ?
                inOutEntity.getEmployeeID() :
                attendanceEntity.getEmployeeID();

        if (userByEmployeeId == null) return;

        if (attendanceRepo.existsByEmployeeIDAndDate(userByEmployeeId, helper.getYesterdayDate())) return;

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(userByEmployeeId);
        attendance.setDate((inOutEntity != null) ? inOutEntity.getDate() : attendanceEntity.getDate());

        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);

        if (inOutEntity != null) {
            attendance.setArrivalDate(inOutEntity.getPunchInMoa());
            attendance.setArrivalTime(inOutEntity.getTimeMoa());
            attendance.setLeftTime(inOutEntity.getTimeEve());
        } else {
            attendance.setArrivalDate(attendanceEntity.getArrivalDate());
            attendance.setArrivalTime(attendanceEntity.getArrivalTime());
            attendance.setLeftTime(attendanceEntity.getLeftTime());
        }

        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  " +
                    (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE") +
                    " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        } else if (unSuccessful) {
            helper.handleLateAndUnsuccessful(userByEmployeeId, attendance);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  " +
                    (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE") +
                    " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendanceRepo.save(attendance);
    }

    public void saveLeave(String employeeId, Date happenDate) {
        LeaveEntity leaveEntity = new LeaveEntity();
        leaveEntity.setPublicId(utils.generateId(10));
        leaveEntity.setEmployeeID(employeeId);

        leaveEntity.setSubmitDate(new Date());
        leaveEntity.setFromDate(new Date());

        leaveEntity.setIsHODApproved(false);
        leaveEntity.setIsSupervisedApproved(false);
        leaveEntity.setHappenDate(happenDate);

        leaveRepo.save(leaveEntity);
    }

    @Override
    /// Day absents
    public void reportAbsent(List<String> absentEmployeesToday) {

        absentEmployeesToday.forEach(employee -> {

            /// CHECKING IF EMPLOYEE MIGHT PUT A LEAVE BEFORE SHE/HE ABSENT (FULL-DAY) -- EMPLOYEE DO
            List<LeaveEntity> byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual = leaveRepo.findByEmployeeIDAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, new Date(), new Date());

            if (!byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.isEmpty()) { /// IF PASSES WHICH MEANS EMPLOYEE DO MAKE LEAVE

                byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.forEach(leaveEntity -> {

                    /// DOUBLE CHECK LEAVE DATE MATCH CURRENT DATE AND WHETHER LEAVE APPROVED OR NOT
                    if (leaveEntity.getIsHODApproved() && leaveEntity.getIsSupervisedApproved() && leaveEntity.getToDate().equals(helper.getYesterdayDate())) {

                        leaveEntity.setDescription("Absent - Leave Used");
                        leaveEntity.setNotUsed(false); /// WHICH MEANS EMPLOYEE USE THE LEAVE

                        /// CUT OF ONE OF THE LEAVES
                        UserLeaveTypeRemaining userLeaveTypeRemaining = getUserLeaveTypeRemaining(leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
                        if (userLeaveTypeRemaining.getRemainingLeaves() < 1) {
                            userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
                        }

                        leaveRepo.save(leaveEntity);

                    } else {

                        helper.handleAbsenteeReqFull(employee, leaveEntity);
                        reportAttendance(employee, false, true, false, false, false, false);

                    }

                });
            } else {

                helper.handleAbsenteeReqFull(employee);
                reportAttendance(employee, false, true, false, false, false, false);

            }

        });
    }

    /// Absent Req for unSuccessful, Short_Leave, LateCover, Late
    public void reportAbsent(AbsenteeReq req) {
        if (req.getEmployeeId() == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        AbsenteeEntity absenteeEntity = new AbsenteeEntity();
        absenteeEntity.setPublicId(utils.generateId(10));
        absenteeEntity.setEmployeeID(req.getEmployeeId());
        absenteeEntity.setDate(new Date());
        absenteeEntity.setIsHODApproved(false);
        absenteeEntity.setIsSupervisedApproved(false);
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);

        absenteeEntity.setIsAbsent(req.getAbsent() != null ? req.getAbsent() : false);
        absenteeEntity.setIsLate(req.getLate() != null ? req.getLate() : false);
        absenteeEntity.setIsLateCover(req.getLateCover() != null ? req.getLateCover() : false);
        absenteeEntity.setIsUnSuccessfulAttdate(req.getUnSuccessfulAttdate() != null ? req.getUnSuccessfulAttdate() : false);
        absenteeEntity.setIsHalfDay(req.getHalfDay() != null ? req.getHalfDay() : false);
        absenteeEntity.setIsArchived(req.getArchived() != null ? req.getArchived() : false);
        absenteeEntity.setComment(req.getComment() != null ? req.getComment() : "");

        absenteeEntity.setIsPending(false);
        absenteeEntity.setIsAccepted(false);


        absenteeRepo.save(absenteeEntity);

    }

    private UserLeaveTypeRemaining getUserLeaveTypeRemaining(String name, String employeeId) {
        return serviceEvent.getUserLeaveTypeRemaining(name, employeeId);
    }

    @Override
    public void requestALeave(LeaveReq req, String userId, String employeeId) { ///  Leave Request user - userId
        String u = (userId != null && !userId.isEmpty()) ? userId : employeeId;


        List<AbsenteeEntity> byUser = absenteeRepo.findByEmployeeID(u);

        byUser = byUser.stream()
                .filter(absentee -> absentee.getDate().equals(req.getHappenDate()))
                .collect(Collectors.toList());

        if (u != null) {

            LeaveCategoryEntity leaveCategory = lmsService.getLeaveCategory(req.getLeaveCategory());
            LeaveTypeEntity leaveType = lmsService.getLeaveType(req.getLeaveType());


            /// Check ae there any leaves

            List<UserLeaveTypeRemaining> userLeaveTypeRemainingRepo_ = serviceEvent.getUserLeaveTypeRemaining(u);

            List<UserLeaveTypeRemaining> filteredList = userLeaveTypeRemainingRepo_.stream()
                    .filter(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1)
                    .collect(Collectors.toList());

            boolean allMatch = userLeaveTypeRemainingRepo_.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);

            if (!allMatch) {

                LeaveEntity leaveEntity = new LeaveEntity();
                leaveEntity.setPublicId(utils.generateId(10));
                leaveEntity.setEmployeeID(u);
                leaveEntity.setSubmitDate(new Date());

                leaveEntity.setIsNoPay(0);

                leaveEntity.setFromDate(req.getFromDate());
                leaveEntity.setToDate(req.getToDate());

                leaveEntity.setLeaveCategory(leaveCategory);
                leaveEntity.setLeaveType(leaveType);

                leaveEntity.setIsSupervisedApproved(false);
                leaveEntity.setIsHODApproved(false);
                leaveEntity.setIsHalfDay(req.getHalfDay());
                leaveEntity.setNumOfDays(req.getNumOfDays());
                leaveEntity.setDescription(req.getDescription());

                leaveEntity.setUnSuccessful(false);
                leaveEntity.setIsLate(false);
                leaveEntity.setIsLateCover(false);
                leaveEntity.setIsShort_Leave(false);
                leaveEntity.setIsAccepted(false);
                leaveEntity.setIsPending(false);
                leaveEntity.setNotUsed(false);
                leaveEntity.setIsManualRequest(req.getManualRequest());
                lmsService.saveLeave(leaveEntity);

                UserLeaveTypeRemaining userLeaveTypeRemaining = getUserLeaveTypeRemaining(leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
                if (userLeaveTypeRemaining.getRemainingLeaves() < 1) {
                    userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                    userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
                }
                byUser.forEach(absenteeEntity -> {
                    absenteeEntity.setIsArchived(true);
                    absenteeEntity.setComment("EMPLOYEE RESOLVE HIS/HER " + (absenteeEntity.getIsHalfDay() ? "HALF DAY" : absenteeEntity.getIsAbsent() ? "ABSENT" : "ISSUE WITH HIS/HER ATTENDANCE"));
                    absenteeRepo.save(absenteeEntity);

                    Optional<AttendanceEntity> byUserAndDate = attendanceRepo.findByEmployeeIDAndDate(u, absenteeEntity.getDate());
                    if (byUserAndDate.isPresent()) {
                        AttendanceEntity attendanceEntity = byUserAndDate.get();
                        attendanceEntity.setResolve(true);
                        attendanceRepo.save(attendanceEntity);
                    }
                });

            } else {
            }
        } else {
        }

    }

    public void approvedLeaveBySup(LeaveEntity entity) {

        UserLeaveTypeRemaining userLeaveTypeRemaining = getUserLeaveTypeRemaining(entity.getLeaveType().getName(), entity.getEmployeeID());
        if (userLeaveTypeRemaining.getRemainingLeaves() < 1) {
            userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
            userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
        }
        lmsService.saveLeave(entity);
    }

    public void approvedLeaveByHOD(LeaveEntity entity) {
        UserLeaveTypeRemaining userLeaveTypeRemaining = getUserLeaveTypeRemaining(entity.getLeaveType().getName(), entity.getEmployeeID());
        if (userLeaveTypeRemaining.getRemainingLeaves() < 1) {
            userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
            userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
        }
        lmsService.saveLeave(entity);
    }

    @Override
    public void processLeaveBySup(String superId, String leaveId) {
        LeaveEntity entity = lmsService.getOneLeave(leaveId);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY
        approvedLeaveBySup(entity);

    }

    @Override
    public void processLeaveByHOD(String hodId, String leaveId) {
        /// HOD by using his/her id and get particular leave accept it using leaveId

        LeaveEntity entity = lmsService.getOneLeave(leaveId);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY
        approvedLeaveBySup(entity);
    }

    List<LeaveEntity> getAllLeavesByPubicId(String userId) {
        return lmsService.getAllLeaveByUserByPubicId(userId);
    }

    public List<LeaveEntity> getAllLeavesByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of(); // Return an empty list if no IDs are provided
        }

        // Using a thread-safe collection to avoid ConcurrentModificationException
        CopyOnWriteArrayList<LeaveEntity> result = new CopyOnWriteArrayList<>();

        ids.parallelStream()
                .map(lmsService::getOneLeave) // Assuming getOneLeave() fetches LeaveEntity by ID
                .filter(leave -> leave != null) // Ignore null results
                .forEach(result::add);

        return result;
    }

    @Override
    public void processLeaveParticularUserBySup(String superId, String userId) {
        /// Supervisor by using his/her id and get particular leave accept it using userId
        List<LeaveEntity> allLeaveByUserByPubicId = getAllLeavesByPubicId(userId);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY
        allLeaveByUserByPubicId.forEach(this::approvedLeaveBySup);


    }

    @Override
    public void processLeaveParticularUserByHOD(String hodId, String userId) {
        /// HOD by using his/her id and get particular leave accept §it using userId
        List<LeaveEntity> allLeaveByUserByPubicId = getAllLeavesByPubicId(userId);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY
        allLeaveByUserByPubicId.forEach(this::approvedLeaveByHOD);

    }

    @Override
    public void processLeaveParticularIdsBySup(String superId, List<String> ids) {
        /// Supervisor by using his/her id and get particular leave accept it using List of Ids
        List<LeaveEntity> allLeaveByIds = getAllLeavesByIds(ids);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

        allLeaveByIds.forEach(this::approvedLeaveBySup);
    }

    @Override
    public void processLeaveParticularIdsByHOD(String hodId, List<String> ids) {
        /// HOD by using his/her id and get particular leave accept it using List of Ids
        List<LeaveEntity> allLeaveByIds = getAllLeavesByIds(ids);
        /// SEND A REQ A SERVER CHECK EMPLOYEE HAVE THAT ROLE OR NOT IF DO OKAY

        allLeaveByIds.forEach(this::approvedLeaveByHOD);
    }

    @Override
    public void getAllTheInOutRecordsFromSLT() {
        /// First get the all the data and using employee id query the our local database
    }
}
