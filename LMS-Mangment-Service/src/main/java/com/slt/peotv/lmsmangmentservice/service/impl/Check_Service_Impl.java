package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.AttendanceUtils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.HolidayChecker;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class Check_Service_Impl implements Check_Service {

    private static final Logger logger = LoggerFactory.getLogger(Check_Service_Impl.class);
    @Autowired
    private NoPayRepo noPayRepo;
    @Autowired
    private Utils utils;
    @Autowired
    private LMS_Service lmsService;
    @Autowired
    private InOutRepo inOutRepo;
    @Autowired
    private MovementsRepo movementsRepo;
    @Autowired
    private ServiceEvent serviceEvent;
    @Autowired
    private Helper helper;
    @Autowired
    private LeaveRepo leaveRepo;
    @Autowired
    private UserLeaveTypeRemainingRepo userLeaveTypeRemainingRepo;
    @Autowired
    private UserClient userClient;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private ComponetAdminsRepo componetAdminsRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepository;
    @Autowired
    private AccessLogRepo accessLogRepo;
    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private AttendanceUtils attendanceUtils;

    public static Map<String, UserRest> createUserMap(List<UserRest> users) {

        final List<UserRest> usersCopy = new ArrayList<>(users);

        List<UserRest> filteredAndSortedUsers = usersCopy.stream().filter(user -> user.getHighestRolePriority() != 1)
                .sorted(Comparator.comparing(UserRest::getHighestRolePriority, Comparator.reverseOrder()))
                .collect(Collectors.toList());


        Map<String, UserRest> userMap = new ConcurrentHashMap<>();

        for (UserRest user : filteredAndSortedUsers) {
            String key = user.getSltId() != null ? user.getSltId() : user.getUserId();
            userMap.put(key, user);
        }

        return Collections.unmodifiableMap(userMap);
    }

    @Override
    public synchronized void allApproved(BulkApprovedReq bulkApprovedReq, boolean swap) {
        // Using atomic operations for nested loops
        bulkApprovedReq.getApprovedEmployeesToday().parallelStream().forEach(emp -> {
            bulkApprovedReq.getApprovedIds().forEach(id -> {
                synchronized (this) {
                    if (swap) processMovement(id, emp);
                    else processLeave(emp, id);
                }
            });
        });
    }

    @Override
    public synchronized void allReject(BulkApprovedReq bulkApprovedReq, boolean swap) {
        bulkApprovedReq.getApprovedIds().parallelStream().forEach(id -> {
            if (swap) {
                synchronized (movementsRepo) {
                    Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
                    if (movementsOpt.isPresent()) {
                        MovementsEntity movementsEntity = movementsOpt.get();
                        movementsEntity.setIsReject(true);
                        movementsRepo.save(movementsEntity);
                    }
                }
            } else {
                synchronized (leaveRepo) {
                    Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
                    if (leaveEntityOpt.isPresent()) {
                        LeaveEntity leaveEntity = leaveEntityOpt.get();
                        leaveEntity.setIsReject(true);
                        leaveRepo.save(leaveEntity);
                    }
                }
            }
        });
    }

    @Override
    public void reject(String id, String userId, boolean swap) {
        if (swap) {
            Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
            MovementsEntity movementsEntity = movementsOpt.get();
            movementsEntity.setIsReject(true);
            movementsEntity.setIsPending(false);
            movementsRepo.save(movementsEntity);
        } else {
            Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
            LeaveEntity leaveEntity = leaveEntityOpt.get();
            leaveEntity.setIsPending(false);
            leaveEntity.setIsReject(true);
            leaveRepo.save(leaveEntity);
        }
    }

    @Override
    public List<AccessLogRest> getAllAccessLogsToday(String date) {
        return accessLogRepo.findByLogDate(date).stream().map(employee -> {
            AccessLogRest rest = new AccessLogRest();
            rest.setEmployeeID(employee.getEmployeeID());
            rest.setId(employee.getId());
            rest.setLogDate(employee.getLogDate());
            rest.setLogTime(employee.getLogTime());
            rest.setTerminalID(employee.getTerminalID());
            rest.setInOut(employee.getInOut());
            rest.setReadStatus(employee.getReadStatus());
            rest.setProcessed(employee.getProcessed());
            rest.setEtlRunTime(employee.getEtlRunTime());
            return rest;
        }).toList();
    }

    @Override
    public List<AccessLogRest> getAllAccessLogs() {
        return accessLogRepo.findAll().stream().map(employee -> {
            AccessLogRest rest = new AccessLogRest();
            rest.setId(employee.getId());
            rest.setLogDate(employee.getLogDate());
            rest.setLogTime(employee.getLogTime());
            rest.setTerminalID(employee.getTerminalID());
            rest.setInOut(employee.getInOut());
            rest.setReadStatus(employee.getReadStatus());
            rest.setProcessed(employee.getProcessed());
            rest.setEtlRunTime(employee.getEtlRunTime());
            return rest;
        }).toList();
    }

    @Override
    public NoPayEntity saveNoPayEntity(EmployeeEntity employee, InOutEntity inOut, AttendanceEntity attendanceEntity, Boolean isHalfDay,
                                       Boolean unSuccessful, Boolean isLate, Boolean isLateCover, Boolean isAbsent, Date accualDate) {


        if (attendanceEntity == null) {
            attendanceEntity = new AttendanceEntity();

            attendanceEntity.setPublicId(utils.generateId(10));
            attendanceEntity.setDate(helper.getYesterdayDate());
            attendanceEntity.setIsHalfDay(isHalfDay);
            attendanceEntity.setIsUnSuccessful(unSuccessful);
            attendanceEntity.setLateCover(isLate);
            attendanceEntity.setLateCover(isLateCover);
            attendanceEntity.setIsAbsent(isAbsent);
            attendanceEntity.setEmployee(employee);
            attendanceEntity.setUpdateDate(new Date());

            if (inOut != null) attendanceEntity.setInOuts(List.of(inOut));

            if (!attendanceUtils.isDuplicateAttendanceByHash(attendanceEntity)) {
                attendanceRepo.save(attendanceEntity);
                logger.info("Attendance saved successfully for employee: {}", attendanceEntity.getEmployee().getEmployeeId());
            } else {
                logger.warn("Duplicate attendance detected for employee: {} on date: {}. Record not saved.",
                        attendanceEntity.getEmployee().getEmployeeId(), attendanceEntity.getDate());
            }

        }
        NoPayEntity nopayEntity = new NoPayEntity();
        nopayEntity.setAttendance(attendanceEntity);
        nopayEntity.setEmployee(employee);
        nopayEntity.setPublicId(utils.generateId(10));
        nopayEntity.setAcctualDate(accualDate == null ? new Date() : accualDate);
        nopayEntity.setSubmissionDate(helper.getDateWithoutTime());

        nopayEntity.setIsHalfDay(isHalfDay);
        nopayEntity.setUnSuccessful(unSuccessful);
        nopayEntity.setIsLate(isLate);
        nopayEntity.setIsLateCover(isLateCover);
        nopayEntity.setIsAbsent(isAbsent);

        nopayEntity.setHappenDate(accualDate);

        StringBuilder description = new StringBuilder();

        if (isAbsent)
            description.append("Absent on ").append(accualDate).append(". ");
        if (isHalfDay)
            description.append("Half-day on ").append(accualDate).append(". ");
        if (unSuccessful)
            description.append("Unsuccessful attendance on ").append(accualDate).append(". ");
        if (isLate)
            description.append("Late on ").append(accualDate).append(". ");
        if (isLateCover)
            description.append("Late cover on ").append(accualDate).append(". ");

        String finalDescription = description.toString().trim();
        nopayEntity.setComment(finalDescription);
        nopayEntity.setAttendance(attendanceEntity);

        attendanceEntity.setIsNoPay(true);
        attendanceRepo.save(attendanceEntity);

        nopayEntity = noPayRepo.save(nopayEntity);

        return nopayEntity;
    }

    @Override
    public Page<InOutDTO> getAllInOut(String employeeID, int pageNumber, int pageSize) {
        Pageable pageableRequest = PageRequest.of(pageNumber, pageSize);
        Page<InOutEntity> entityPage = inOutRepo.findByEmployeeID(employeeID, pageableRequest);

        return entityPage.map(inOutEntity -> {
            InOutDTO inOutDTO = new InOutDTO();
            inOutDTO.setEmployeeID(inOutEntity.getEmployeeID());
            inOutDTO.setDate(inOutEntity.getDate());
            inOutDTO.setPunchInMoa(inOutEntity.getPunchInMoa());
            inOutDTO.setPunchInEv(inOutEntity.getPunchInEv());
            inOutDTO.setTimeMoa(inOutEntity.getTimeMoa());
            inOutDTO.setTimeEve(inOutEntity.getTimeEve());
            inOutDTO.setInOut(inOutEntity.getInOut());
            inOutDTO.setMoaning(inOutEntity.getIsMoaning());
            inOutDTO.setEvening(inOutEntity.getIsEvening());
            inOutDTO.setPast(inOutEntity.getIsPast());
            inOutDTO.setTerminalID(inOutEntity.getTerminalID());
            return inOutDTO;
        });
    }

    @Override
    public List<InOutDTO> getAllInOut(String employeeID, Date date) {
        EmployeeEntity user = employeeRepo.findByPublicId(employeeID).orElseThrow(() -> new NoSuchElementException("User not found"));
        return inOutRepo.findByEmployeeIDAndPunchInMoa(user.getSltId(), date)
                .stream()
                .map(inOutEntity -> {
                    InOutDTO inOutDTO = new InOutDTO();
                    inOutDTO.setEmployeeID(inOutEntity.getEmployeeID());
                    inOutDTO.setDate(inOutEntity.getDate());
                    inOutDTO.setPunchInMoa(inOutEntity.getPunchInMoa());
                    inOutDTO.setPunchInEv(inOutEntity.getPunchInEv());
                    inOutDTO.setTimeMoa(inOutEntity.getTimeMoa());
                    inOutDTO.setTimeEve(inOutEntity.getTimeEve());
                    inOutDTO.setInOut(inOutEntity.getInOut());
                    inOutDTO.setMoaning(inOutEntity.getIsMoaning());
                    inOutDTO.setEvening(inOutEntity.getIsEvening());
                    inOutDTO.setPast(inOutEntity.getIsPast());
                    inOutDTO.setTerminalID(inOutEntity.getTerminalID());
                    return inOutDTO;
                }).toList();
    }

    @Override
    public Map<String, InOutDTO> getEarliestInOut(String userId, Date date) {
        EmployeeEntity employeeEntity = employeeRepo.findByPublicId(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        List<InOutDTO> allInOut = inOutRepo.findByEmployeeIDAndDate(employeeEntity.getSltId(), date)
                .stream()
                .map(inOutEntity -> {
                    InOutDTO inOutDTO = new InOutDTO();
                    inOutDTO.setEmployeeID(inOutEntity.getEmployeeID());
                    inOutDTO.setDate(inOutEntity.getDate());
                    inOutDTO.setPunchInMoa(inOutEntity.getPunchInMoa());
                    inOutDTO.setPunchInEv(inOutEntity.getPunchInEv());
                    inOutDTO.setTimeMoa(inOutEntity.getTimeMoa());
                    inOutDTO.setTimeEve(inOutEntity.getTimeEve());
                    inOutDTO.setInOut(inOutEntity.getInOut());
                    inOutDTO.setMoaning(inOutEntity.getIsMoaning());
                    inOutDTO.setEvening(inOutEntity.getIsEvening());
                    inOutDTO.setPast(inOutEntity.getIsPast());
                    inOutDTO.setTerminalID(inOutEntity.getTerminalID());
                    return inOutDTO;
                }).toList();

        Map<String, InOutDTO> result = new HashMap<>();

        allInOut.stream()
                .filter(InOutDTO::getMoaning)
                .min(Comparator.comparing(InOutDTO::getTimeMoa))
                .ifPresent(dto -> result.put("morning", dto));

        allInOut.stream()
                .filter(InOutDTO::getEvening)
                .min(Comparator.comparing(InOutDTO::getTimeEve))
                .ifPresent(dto -> result.put("evening", dto));

        return result;
    }

    @Override
    public List<InOutDTO> getEarliestInOutBetweenDate(String userId, Date date, Date date2) {
        EmployeeEntity employeeEntity = employeeRepo.findByPublicId(userId).orElseThrow(() -> new NoSuchElementException("User not found"));

        List<InOutEntity> records = inOutRepo.findByEmployeeIDAndDateBetween(employeeEntity.getSltId(), date, date2);
        return records.stream()
                .map(inOutEntity -> {
                    InOutDTO inOutDTO = new InOutDTO();
                    inOutDTO.setEmployeeID(inOutEntity.getEmployeeID());
                    inOutDTO.setDate(inOutEntity.getDate());
                    inOutDTO.setPunchInMoa(inOutEntity.getPunchInMoa());
                    inOutDTO.setPunchInEv(inOutEntity.getPunchInEv());
                    inOutDTO.setTimeMoa(inOutEntity.getTimeMoa());
                    inOutDTO.setTimeEve(inOutEntity.getTimeEve());
                    inOutDTO.setInOut(inOutEntity.getInOut());
                    inOutDTO.setMoaning(inOutEntity.getIsMoaning());
                    inOutDTO.setEvening(inOutEntity.getIsEvening());
                    inOutDTO.setPast(inOutEntity.getIsPast());
                    inOutDTO.setTerminalID(inOutEntity.getTerminalID());
                    return inOutDTO;
                }).toList();
    }

    @Override
    public List<InOutDTO> getEarliestInOutByDate(String userId, Date date) {
        EmployeeEntity employeeEntity = employeeRepo.findByPublicId(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        List<InOutEntity> records = inOutRepo.findByEmployeeIDAndDate(employeeEntity.getSltId(), date);

        return records.stream()
                .map(inOutEntity -> {
                    InOutDTO inOutDTO = new InOutDTO();
                    inOutDTO.setEmployeeID(inOutEntity.getEmployeeID());
                    inOutDTO.setDate(inOutEntity.getDate());
                    inOutDTO.setPunchInMoa(inOutEntity.getPunchInMoa());
                    inOutDTO.setPunchInEv(inOutEntity.getPunchInEv());
                    inOutDTO.setTimeMoa(inOutEntity.getTimeMoa());
                    inOutDTO.setTimeEve(inOutEntity.getTimeEve());
                    inOutDTO.setInOut(inOutEntity.getInOut());
                    inOutDTO.setMoaning(inOutEntity.getIsMoaning());
                    inOutDTO.setEvening(inOutEntity.getIsEvening());
                    inOutDTO.setPast(inOutEntity.getIsPast());
                    inOutDTO.setTerminalID(inOutEntity.getTerminalID());
                    return inOutDTO;
                }).toList();
    }

    @Override
    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication) {
        try {
            EmployeeEntity employee = employeeRepo.findByEmployeeId(req.getEmployeeId())
                    .or(() -> employeeRepo.findBySltId(req.getEmployeeId()))
                    .or(() -> employeeRepo.findByPublicId(req.getEmployeeId()))
                    .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

            if (employee == null)
                throw new RuntimeException("Failed to process movement request");

            String name = authentication.getName();
            if (name == null || name.trim().isEmpty())
                throw new RuntimeException("Failed to process movement request");

            if (!employee.getPublicId().equals(req.getUserId()) || !name.equals(req.getUserId()))
                throw new RuntimeException("Failed to process movement request");

            Optional<MovementsEntity> reqDate = movementsRepo.findAllByEmployeeAndReqDate(employee, req.getHappenDate());

            if (reqDate.isPresent())
                throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

            if (!req.validateMovementReq()) {
                return;
            }

            // Token extraction
            String token = "Bearer " + extractJwtTokenFromCookie(request);

            // Fetch admins
            final List<UserRest> admins = userClient.getEmployeeAdmins(req.getUserId(), token);

            // Create thread-safe user map
            Map<String, UserRest> userMap = createUserMap(admins);

            // Generate movement ID once
            final String movementId = utils.generateId(10);
            // Initialize entities

            MovementsEntity movementsEntity = mapToEntity(req,employee, movementId);

            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndDate(
                    employee, movementsEntity.getHappenDate());

            if (attendanceEntity.isEmpty())
                throw new RuntimeException("Failed to process movement request");

            AttendanceEntity attendance = attendanceEntity.get();
            // Return if attendance has no issues or if it has issues that are already resolved
            if (!Boolean.TRUE.equals(attendance.getIssues()) || Boolean.TRUE.equals(attendance.getResolve())) {
                return;
            }

            movementsEntity.setAttendance(attendance);
            List<ComponetAdminsEntity> adminEntities = new ArrayList<>();

            userMap.entrySet().forEach(entry -> {
                UserRest value = entry.getValue();
                ComponetAdminsEntity admin = createAdminEntity(value, movementId);
                adminEntities.add(admin);
            });

            // Set admins list
            movementsEntity.setAdmins(adminEntities);

            // Save movement entity with all its admins in a single transaction
            lmsService.createMovements(movementsEntity);

        } catch (Exception e) {
            // Proper error handling
            logError("Error in requestMovement", e);
            throw new RuntimeException("Failed to process movement request", e);
        }
    }

    @Override
    public void processMovement(String moveId, String userId) {
        Optional<MovementsEntity> movementsEntity = movementsRepo.findByPublicId(moveId);
        if (movementsEntity.isPresent()) {
            MovementsEntity movementEntity = movementsEntity.get();
            approvedMove(movementEntity, userId);
        }
    }

    private ComponetAdminsEntity createAdminEntity(UserRest user_, String movementId) {
        Optional<EmployeeEntity> employee = employeeRepo.findByEmployeeId(user_.getEmployeeId());
        if (employee.isEmpty()) return null;
        EmployeeEntity user = employee.get();
        ComponetAdminsEntity entity = new ComponetAdminsEntity();
        entity.setEmployee(user);
        entity.setHighestRolePriority(user_.getHighestRolePriority());
        entity.setComponetID(movementId);
        entity.setIsAccepted(false);
        entity.setProfilePic(user.getProfilePic());
        return entity;
    }

    private Date stripTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateWithTime);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public MovementsEntity mapToEntity(MovementReq movementReq,EmployeeEntity employee, String movementId) {
        if (movementReq == null) {
            return null;
        }

        return MovementsEntity.builder()
                .publicId(movementId)
                .reqDate(new Date())
                .isPending(true)
                .employee(employee)
                .movementType(movementReq.getMovementType())
                .comment(movementReq.getComment())
                .destination(movementReq.getDestination())
                .category(movementReq.getCategory())
                .happenDate(stripTimeFromDate(movementReq.getHappenDate()))
                .isAbsent(movementReq.getIsAbsent())
                .isUnSuccessfulAttdate(movementReq.getIsUnSuccessfulAttdate())
                .isHalfDay(movementReq.getIsHalfDay())
                .unAuthorized(movementReq.getUnAuthorized())
                .isLate(movementReq.getIsLate() != null ? movementReq.getIsLate() : false)
                .isLateCover(movementReq.getIsLateCover() != null ? movementReq.getIsLateCover() : false)
                .logTime(movementReq.getLogTime() == null ? new Date() : movementReq.getLogTime())
                .inTime(movementReq.getIntime() == null ? "00:00:00" : movementReq.getIntime())
                .outTime(movementReq.getOuttime() == null ? "00:00:00" : movementReq.getOuttime()) // Fixed this line
                .build();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    public void approvedMove(MovementsEntity movement, String userId) {
        AttendanceEntity attendance = movement.getAttendance();
        if (attendance == null) {
            return;
        }

        List<ComponetAdminsEntity> admins_ = movement.getAdmins();
        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId())
                );
        if (!isAuthorizedAdmin) return;

        // Get admins sorted by priority (lowest priority first)
        List<ComponetAdminsEntity> admins = movement.getAdmins().stream()
                .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());

        // Find the admin matching the current user
        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) return;
        if (currentAdmin.getIsAccepted()) return;

        // Get the index of the current admin in the sorted list
        int currentAdminIndex = admins.indexOf(currentAdmin);

        // Check if all lower priority admins have approved
        boolean allLowerPriorityApproved = true;
        for (int i = 0; i < currentAdminIndex; i++) {
            if (admins.get(i).getApprovedDate() == null ||
                    !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                allLowerPriorityApproved = false;
                break;
            }
        }

        // If not all lower priority admins have approved, don't allow this approval
        if (!allLowerPriorityApproved) {
            return;
        }

        // Process the current admin's approval
        if (currentAdmin.getApprovedDate() == null) {
            currentAdmin.setApprovedDate(new Date());
            currentAdmin.setIsAccepted(true);
            componetAdminsRepo.save(currentAdmin);
        }

        // Check if all admins have approved now
        boolean allApproved = admins.stream()
                .allMatch(admin -> admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));

        // If all admins have approved or there are no admins
        if (allApproved || admins.isEmpty()) {
            movement.setIsPending(false);
            movement.setIsAccepted(true);

            attendance.setResolve(true);
            attendance.setDueDateForUA(null);
            attendance.setIssues(false);

            // Save all changes
            attendanceRepo.save(attendance);
            movementsRepo.save(movement);
        }
    }

    public void approvedLeave(LeaveEntity leave, String userId) {
        AttendanceEntity attendance = leave.getAttendance();
        if (!leave.getIsManualRequest() && attendance == null) {
            return;
        }
        List<ComponetAdminsEntity> admins_ = leave.getAdmins();
        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId())
                );
        if (!isAuthorizedAdmin) return;

        // Get admins sorted by priority (lowest priority first)
        List<ComponetAdminsEntity> admins = leave.getAdmins().stream()
                .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());

        // Find the admin matching the current user
        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) return;

        if (currentAdmin.getIsAccepted()) return;

        // Get the index of the current admin in the sorted list
        int currentAdminIndex = admins.indexOf(currentAdmin);

        // Check if all lower priority admins have approved
        boolean allLowerPriorityApproved = true;
        for (int i = 0; i < currentAdminIndex; i++) {
            if (admins.get(i).getApprovedDate() == null ||
                    !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                allLowerPriorityApproved = false;
                break;
            }
        }

        // If not all lower priority admins have approved, don't allow this approval
        if (!allLowerPriorityApproved) {
            return;
        }

        // Process the current admin's approval
        if (currentAdmin.getApprovedDate() == null) {
            currentAdmin.setApprovedDate(new Date());
            currentAdmin.setIsAccepted(true);
            componetAdminsRepo.save(currentAdmin);
        }

        // Check if all admins have approved now
        boolean allApproved = admins.stream()
                .allMatch(admin -> admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));

        // If all admins have approved or there are no admins
        if (allApproved || admins.isEmpty()) {
            leave.setIsPending(false);
            leave.setIsAccepted(true);

            if (attendance != null) {
                attendance.setResolve(true);
                attendance.setDueDateForUA(null);
                attendance.setIssues(false);
                attendanceRepo.save(attendance);
            }
            leaveRepo.save(leave);
        }
    }

    @Override
    public void main() {

        prerequisite();

        List<AttendanceEntity> attendanceEntities = attendanceRepo.findByDueDateForUA(helper.getDateWithoutTime());
        List<AttendanceEntity> overdueEntities_filter = StreamSupport.stream(attendanceEntities.spliterator(), false)
                .filter(entity -> Boolean.TRUE.equals(entity.getIsUnAuthorized())
                        || Boolean.TRUE.equals(entity.getIsUnSuccessful()))
                .collect(Collectors.toList());

        overdueEntities_filter.forEach(entity -> {

            if (entity.getResolve())
                return;

            /// CHECK ARE THERE ANY LEAVE REQ
            List<LeaveEntity> allTheLeavesByEmployee = leaveRepo
                    .findByEmployeeAndIsManualRequest(entity.getEmployee(), true);
            allTheLeavesByEmployee = StreamSupport.stream(allTheLeavesByEmployee.spliterator(), false)
                    .filter(leave -> leave.getSubmitDate().equals(new Date())).collect(Collectors.toList());

            /// CHECK ARE THERE ANY MOVEMENTS
            List<MovementsEntity> allTheMovementsByEmployee = movementsRepo.findByIsPendingAndEmployee(true,
                    entity.getEmployee());

            allTheMovementsByEmployee = StreamSupport.stream(allTheMovementsByEmployee.spliterator(), false)
                    .filter(movement -> movement.getReqDate().equals(new Date())).collect(Collectors.toList());

            if (allTheLeavesByEmployee.isEmpty() || allTheMovementsByEmployee.isEmpty())
                saveNoPayEntity(entity.getEmployee(), null, entity, false, false, false, false, true, entity.getDate());

            allTheLeavesByEmployee.forEach(leave -> {

                if (leave.getIsPending() && !leave.getIsAccepted()) {
                    saveNoPayEntity(entity.getEmployee(), null, entity, false, false, false, false, true, entity.getDate());
                }
            });

            allTheMovementsByEmployee.forEach(movement -> {
                if (movement.getIsPending() && !movement.getIsAccepted()) {
                    saveNoPayEntity(entity.getEmployee(), null, entity, false, false, false, false, true, entity.getDate());
                }
            });
        });

    }

    public Map<String, InOutEntity> findEarliestMorningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Map.of();
        }

        return entities.stream().filter(entity -> entity.getEmployeeID() != null && entity.getTimeMoa() != null)
                .collect(Collectors.groupingBy(InOutEntity::getEmployeeID,
                        Collectors.minBy(Comparator.comparing(InOutEntity::getTimeMoa))))
                .entrySet().stream().filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }


    public Map<String, InOutEntity> findEarliestEveningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Map.of();
        }

        return entities.stream().filter(entity -> entity.getEmployeeID() != null && entity.getTimeEve() != null)
                .collect(Collectors.groupingBy(InOutEntity::getEmployeeID,
                        Collectors.minBy(Comparator.comparing(InOutEntity::getTimeEve))))
                .entrySet().stream().filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }


    public Map<String, InOutEntity> processLateArrivals(List<InOutEntity> employeesArrivedAfter900) {
        return findEarliestMorningPunchByEmployee(employeesArrivedAfter900);
    }

    public Map<String, InOutEntity> processEmployeesArrivedBetween830And900(
            List<InOutEntity> employeesArrivedBetween830And900) {
        return findEarliestMorningPunchByEmployee(employeesArrivedBetween830And900);
    }


    public Map<String, InOutEntity[]> findEmployeesWithBothPunches(Map<String, InOutEntity> morningMap,
                                                                   Map<String, InOutEntity> eveningMap) {

        Map<String, InOutEntity[]> employeesWithBothPunches = new HashMap<>();

        // Find common employees (keys) that exist in both maps
        for (String employeeId : morningMap.keySet()) {
            if (eveningMap.containsKey(employeeId)) {
                // This employee has both morning and evening punches
                InOutEntity morningPunch = morningMap.get(employeeId);
                InOutEntity eveningPunch = eveningMap.get(employeeId);

                // Store both records in an array
                employeesWithBothPunches.put(employeeId, new InOutEntity[]{morningPunch, eveningPunch});
            }
        }

        return employeesWithBothPunches;
    }


    public Map<String, InOutEntity> findEmployeesWithOnlyMorningPunches(Map<String, InOutEntity> morningMap,
                                                                        Map<String, InOutEntity> eveningMap) {

        Map<String, InOutEntity> employeesWithOnlyMorningPunches = new HashMap<>();

        // Find employees (keys) that exist in morning map but not in evening map
        for (String employeeId : morningMap.keySet()) {
            if (!eveningMap.containsKey(employeeId)) {
                // This employee has only morning punch but no evening punch
                InOutEntity morningPunch = morningMap.get(employeeId);

                // Store the morning record
                employeesWithOnlyMorningPunches.put(employeeId, morningPunch);
            }
        }

        return employeesWithOnlyMorningPunches;
    }

    private synchronized void handleHolidays() throws IOException, InterruptedException {
        List<EmployeeEntity> all = (List<EmployeeEntity>) employeeRepo.findAll();
        boolean todayGovHoliday = HolidayChecker.isTodayGovHoliday();
        if (todayGovHoliday) {
            // Use a thread-safe approach for processing employees
            all.parallelStream().forEach(employee -> {
                // Create a new attendance entity for each employee
                AttendanceEntity attendance = new AttendanceEntity();
                attendance.setEmployee(employee);
                attendance.setPublicId(utils.generateId(10));
                attendance.setIsHoliday(true);
                attendance.setDate(helper.getDateWithoutTime());

                // Ensure atomic save operation
                synchronized (attendanceRepo) {
                    attendanceRepo.save(attendance);
                }
            });
        }
    }

    private boolean checkHalfDay(Time morningPunch) {
        if (morningPunch == null) {
            return false;
        }
        Time halfDayThreshold = new Time(12, 30, 0);
        return morningPunch.after(halfDayThreshold);
    }

    @Override
    public void prerequisite() {
        try {
            handleHolidays();
        } catch (Exception e) {

        }
        LocalDateTime yesterdayBefore830 = LocalDate.now().minusDays(1).atTime(8, 29);
        Time sqlTime830 = Time.valueOf(yesterdayBefore830.toLocalTime());

        LocalTime eveStart = LocalTime.of(17, 0); // 5:00 PM
        LocalTime eveEnd = LocalTime.of(23, 0); // 11:00 PM
        Time timeEveStart = Time.valueOf(eveStart);
        Time timeEveEnd = Time.valueOf(eveEnd);

        LocalTime eveStart_ = LocalTime.of(17, 30); // 5:00 PM
        Time timeEveStart_ = Time.valueOf(eveStart_);

        LocalDateTime yesterdayAfter900 = LocalDate.now().minusDays(1).atTime(9, 0);
        Time sqlTime900 = Time.valueOf(yesterdayAfter900.toLocalTime());

        LocalTime halfDayTime = LocalTime.of(12, 30); // 12:30 PM
        Time timeHalfDay = Time.valueOf(halfDayTime);

        Time startTimeLate = Time.valueOf("08:30:00");
        Time endTimeLate = Time.valueOf("09:00:00");

        Date yesterdayDate = helper.getYesterdayDate();

        // Get employee attendance sets
        List<InOutEntity> employeesArrivedBefore830 = inOutRepo.findByPunchInMoaAndTimeMoaBefore(yesterdayDate,
                sqlTime830);

        List<InOutEntity> employeesLeftAfter5 = inOutRepo.findByPunchInEvAndTimeEveBetween(yesterdayDate,
                timeEveStart, timeEveEnd);
        List<InOutEntity> employeesLeftAfter5_ = inOutRepo.findByPunchInEvAndTimeEveBetween(yesterdayDate,
                timeEveStart_, timeEveEnd);

        List<InOutEntity> employeesArrivedAfter900 = inOutRepo.findByPunchInMoaAndTimeMoaAfter(yesterdayDate,
                sqlTime900);

        List<InOutEntity> employeesHalfDay = inOutRepo.findByPunchInEvAndTimeEveAfter(yesterdayDate, timeHalfDay);

        List<InOutEntity> employeesArrivedBetween830And900 = inOutRepo.findByPunchInMoaAndTimeMoaBetween(yesterdayDate,
                startTimeLate, endTimeLate);

        Map<String, InOutEntity> earliestMorningPunchByEmployee = findEarliestMorningPunchByEmployee(
                employeesArrivedBefore830);
        Map<String, InOutEntity> earliestEveningPunchByEmployee = findEarliestEveningPunchByEmployee(
                employeesLeftAfter5);
        Map<String, InOutEntity> earliestEveningPunchByEmployee_ = findEarliestEveningPunchByEmployee(
                employeesLeftAfter5_);

        Map<String, InOutEntity> lateArrivals = processLateArrivals(employeesArrivedAfter900);
        /*
        Map<String, InOutEntity> halfDayEmployees = processHalfDayEmployees(employeesHalfDay);
*/
        Map<String, InOutEntity> arrivedBetween830And900 = processEmployeesArrivedBetween830And900(
                employeesArrivedBetween830And900);

        /// =====================================================================================
        /// FULL DAY

        Map<String, InOutEntity[]> employeesWithBothPunches = findEmployeesWithBothPunches(
                earliestMorningPunchByEmployee, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunches.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue()[0];
                InOutEntity eveningPunch = entry.getValue()[1];

                System.out.println("Employee " + employeeId + ":");
                System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());
                System.out.println("  - Evening punch at: " + eveningPunch.getTimeEve());

                reportAttendance(morningPunch, eveningPunch, true, false, false, false, false, false, false, false,
                        false, true, false,false ,null);
            });
        }

        /// =====================================================================================
        /// Un-Authorized

        Map<String, InOutEntity> employeesWithOnlyMorningPunches = findEmployeesWithOnlyMorningPunches(
                earliestMorningPunchByEmployee, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnlyMorningPunches.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                System.out.println("Employee " + employeeId + ":");
                System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());

                reportAttendance(morningPunch, false, true, false, false, false, false, false, false, false, true,
                        false, false,null);
            });
        }

        /// =====================================================================================
        /// Late Arrive at 9.00 Am but do the late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLate = findEmployeesWithBothPunches(lateArrivals,
                earliestEveningPunchByEmployee_);

        System.out.println("Employees who arrived late (after 9:00):");
        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLate.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue()[0];
            InOutEntity eveningPunch = entry.getValue()[1];

            Time timeMoa = morningPunch.getTimeMoa();
            if (checkHalfDay(timeMoa))
                reportAttendance(morningPunch, eveningPunch, false, false, false, true, false, true, false, false, false,
                        true, false, true,null);

            if(timeMoa == null){
                Time timeEve = morningPunch.getTimeEve();
                if(timeEve != null)
                    reportAttendance(morningPunch, false, false, false, true, false, true, false, false, false, true,
                            false,true ,null);
            }
            System.out.println("Employee Late (after 9:00)" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());
            System.out.println("  - Evening punch at: " + eveningPunch.getTimeEve());

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {

                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, false, false, false, false,
                        true, false, false,null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 9.00 Am but do the did not late cover

        Map<String, InOutEntity> onlyMorningPunchesLate90 = findEmployeesWithOnlyMorningPunches(lateArrivals,
                earliestEveningPunchByEmployee_);

        System.out.println("Employees who arrived late (after 9:00):");
        for (Map.Entry<String, InOutEntity> entry : onlyMorningPunchesLate90.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue();

            Time timeMoa = morningPunch.getTimeMoa();
            if (checkHalfDay(timeMoa))
                reportAttendance(morningPunch, false, false, false, true, false, true, false, false, false, true,
                        false,true ,null);

            if(timeMoa == null){
                Time timeEve = morningPunch.getTimeEve();
                if(timeEve != null)
                    reportAttendance(morningPunch, false, false, false, true, false, true, false, false, false, true,
                            false,true ,null);
            }
            System.out.println("Employee Late (after 9:00)" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, false, false, true, true, false, false, false, false, false, true,
                        false,false ,null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 8.39 Am - 9.00.Am but did the late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLate830900 = findEmployeesWithBothPunches(
                arrivedBetween830And900, earliestEveningPunchByEmployee);

        System.out.println("\nEmployees who arrived between 8:30 and 9:00:");
        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLate830900.entrySet()) {
            String employeeId = entry.getKey();

            InOutEntity morningPunch = entry.getValue()[0];
            InOutEntity eveningPunch = entry.getValue()[1];

            System.out.println("Employees who arrived between 8:30 and 9:00" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());
            System.out.println("  - Evening punch at: " + eveningPunch.getTimeEve());

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, false, false, false, false,
                        true, false,false, null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 8.39 Am - 9.00.Am but did not the late cover

        Map<String, InOutEntity> employeesWithMorPunchesLate830900 = findEmployeesWithOnlyMorningPunches(
                arrivedBetween830And900, earliestEveningPunchByEmployee);

        System.out.println("\nEmployees who arrived between 8:30 and 9:00:");
        for (Map.Entry<String, InOutEntity> entry : employeesWithMorPunchesLate830900.entrySet()) {
            String employeeId = entry.getKey();

            InOutEntity morningPunch = entry.getValue();

            System.out.println("Employees who arrived between 8:30 and 9:00" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());

            reportAttendance(morningPunch, false, false, true, true, false, false, false, false, false, true, false,false,
                    null);

        }

        /// =====================================================================================
        /// employee who actually came as half-day

        /*System.out.println("\nEmployees who took half day:");

        for (Map.Entry<String, InOutEntity> entry : halfDayEmployees.entrySet()) {
            String employeeId = entry.getKey();

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {

                InOutEntity record = entry.getValue();
                System.out.println(" Half day - Employee " + employeeId + " left at " + record.getTimeEve());

                List<LeaveEntity> approvedLeaves = leaveRepo.findApprovedLeavesByEmployeeIDAndFromDateAndToDate(
                        record.getEmployeeID(), helper.getYesterdayDate(), helper.getYesterdayDate());

                if (!approvedLeaves.isEmpty()) {
                    // Process employee with approved leave
                    processEmployeeWithApprovedLeave(record, approvedLeaves.get(0));
                } else {
                    // Process employee without approved leave
                    processEmployeeWithoutApprovedLeave(record);
                }

            });

        }*/

        /// ================================ employee who are absent
        List<String> absentEmployeesToday = getAbsentEmployeesToday();
        reportAbsent(absentEmployeesToday);
    }


    public List<String> getAbsentEmployeesToday() {
        List<EmployeeEntity> allEmployees = (List<EmployeeEntity>) employeeRepo.findAll();

        List<InOutEntity> todayRecords = inOutRepo.findByDate(helper.getYesterdayDate());
        Set<String> presentEmployeeIds = todayRecords.stream().map(InOutEntity::getEmployeeID)
                .collect(Collectors.toSet());

        return allEmployees.stream().map(EmployeeEntity::getSltId)
                .filter(presentEmployeeIds::contains)
                .collect(Collectors.toList());
    }

    @Override
    public void reportAttendance(InOutEntity inout, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                 Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                 Boolean leaveReq, Boolean active, Boolean nopay,Boolean absent, Date date) {

        if (inout.getEmployeeID() == null)
            return;

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(inout.getEmployeeID());
        if (employeeEntity.isEmpty())
            return;

        EmployeeEntity employee = employeeEntity.get();
        if (Objects.isNull(employee.getRoaster())) employee.setRoaster(false);
        if (employee.getRoaster())
            return;

        LocalTime eveStart = LocalTime.of(17, 0); // 5:00 PM
        LocalTime eveEnd = LocalTime.of(23, 0); // 11:00 PM
        Time timeEveStart = Time.valueOf(eveStart);
        Time timeEveEnd = Time.valueOf(eveEnd);

        Date yesterdayDateV2 = helper.getYesterdayDate();
        Set<InOutEntity> employeesLeftAfter5 = new HashSet<>(
                inOutRepo.findByPunchInEvAndTimeEveBetween(yesterdayDateV2, timeEveStart, timeEveEnd));

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setTerminalID(inout.getTerminalID());
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployee(employee);
        attendance.setDate(helper.getYesterdayDate());
        attendance.setEtl_run_time(new Date());
        attendance.setIsAbsent(absent);

        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);

        attendance.setArrivalDate(helper.removeTimeFromDate(inout.getPunchInMoa()));
        attendance.setArrivalTime(inout.getTimeMoa());

        if (half_day)
            attendance.setLeftTime(inout.getTimeEve());


        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (" SWIPE ERROR ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(inout.getEmployeeID(), attendance);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (" LATE ATTENDANCE ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if(absent){
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING ABSENT DUE TO THE  "
                    + (" THE FOUD FOUND SYSTEM RECORDS ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);
        attendance.setUpdateDate(new Date());

        // FIXED: Properly set up the bidirectional relationship
        // Save attendance first to get the ID
        if (!attendanceUtils.isDuplicateAttendanceByHash(attendance)) {
            AttendanceEntity savedAttendance = attendanceRepo.save(attendance);

            // Now set the relationship properly
            inout.setAttendance(savedAttendance);
            inOutRepo.save(inout); // Save the inout with the attendance reference

            // Update the attendance with the inout list
            List<InOutEntity> inOuts = new ArrayList<>();
            inOuts.add(inout);
            savedAttendance.setInOuts(inOuts);

            if (nopay)
                saveNoPayEntity(attendance.getEmployee(), inout, savedAttendance, false, true, false, false, false,
                        date != null ? date : helper.getYesterdayDate());

            logger.info("Attendance saved successfully for employee: {}", savedAttendance.getEmployee().getEmployeeId());
        } else {
            logger.warn("Duplicate attendance detected for employee: {} on date: {}. Record not saved.",
                    attendance.getEmployee().getEmployeeId(), attendance.getDate());
        }
    }

    @Override
    public void reportAttendance(InOutEntity moa, InOutEntity eve, Boolean fullday, Boolean unAuthorized,
                                 Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave,
                                 Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent, Date date) {
        // Validation checks
        if (moa.getEmployeeID() == null || eve.getEmployeeID() == null)
            return;
        if (!eve.getEmployeeID().equals(moa.getEmployeeID()))
            return;

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(moa.getEmployeeID());
        if (employeeEntity.isEmpty())
            return;

        EmployeeEntity employee = employeeEntity.get();
        if (Objects.isNull(employee.getRoaster())) employee.setRoaster(false);
        if (employee.getRoaster())
            return;

        // Create attendance entity (without InOut relationships initially)
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setTerminalID(moa.getTerminalID() + " - " + eve.getTerminalID());
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployee(employee);
        attendance.setDate(helper.getYesterdayDate());
        attendance.setEtl_run_time(new Date());
        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);
        attendance.setIsAbsent(absent);

        attendance.setArrivalDate(moa.getPunchInMoa());
        attendance.setArrivalTime(moa.getTimeMoa());
        attendance.setLeftTime(eve.getTimeEve());
        attendance.setUpdateDate(new Date());

        // Handle unauthorized/unsuccessful cases
        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (" SWIPE ERROR ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(moa.getEmployeeID(), attendance);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (" LATE ATTENDANCE ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if(absent){
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING ABSENT DUE TO THE  "
                    + (" THE FOUD FOUND SYSTEM RECORDS ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);

        // Check for duplicates before saving
        if (attendanceUtils.isDuplicateAttendanceByHash(attendance)) {
            logger.warn("Duplicate attendance detected for employee: {} on date: {}. Record not saved.",
                    attendance.getEmployee().getEmployeeId(), attendance.getDate());
            return;
        }

        // STEP 1: Save attendance first (without InOut relationships)
        AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
        logger.info("Attendance saved successfully for employee: {}", savedAttendance.getEmployee().getEmployeeId());

        // STEP 2: Establish relationships with existing InOut entities
        try {
            // Update moa entity to reference the attendance
            if (moa.getId() != null) {
                Optional<InOutEntity> moaEntity = inOutRepo.findById(moa.getId());
                if (moaEntity.isPresent()) {
                    InOutEntity managedMoa = moaEntity.get();
                    managedMoa.setAttendance(savedAttendance);
                    inOutRepo.save(managedMoa);
                }
            }

            // Update eve entity to reference the attendance
            if (eve.getId() != null) {
                Optional<InOutEntity> eveEntity = inOutRepo.findById(eve.getId());
                if (eveEntity.isPresent()) {
                    InOutEntity managedEve = eveEntity.get();
                    managedEve.setAttendance(savedAttendance);
                    inOutRepo.save(managedEve);
                }
            }

            logger.info("InOut relationships established for attendance: {}", savedAttendance.getId());

        } catch (Exception e) {
            logger.error("Error establishing InOut relationships for attendance: {}", savedAttendance.getId(), e);
        }

        if (nopay) {
            saveNoPayEntity(attendance.getEmployee(), moa, savedAttendance, false, true, false, false, false,
                    date != null ? date : helper.getYesterdayDate());
        }
    }

    @Override
    public void reportAttendance(String employeeID, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                 Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                 Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent, Date date) {

        if (employeeID == null || employeeID.isEmpty()) {
            logger.warn("Employee ID is null or empty. Cannot proceed with attendance reporting.");
            return;
        }

        EmployeeEntity employee = employeeRepo.findByEmployeeId(employeeID)
                .or(() -> employeeRepo.findBySltId(employeeID))
                .or(() -> employeeRepo.findByPublicId(employeeID))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        if (employee == null)
            throw new RuntimeException("Failed to process movement request");

        if (attendanceRepo.existsByEmployeeAndDate(employee, helper.getYesterdayDate())) {
            return;
        }

        if (employee.getRoaster()) {
            return;
        }

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setEmployee(employee);
        attendance.setIsAbsent(absent);
        attendance.setPublicId(utils.generateId(10));
        attendance.setDate(helper.getYesterdayDate());
        attendance.setEtl_run_time(new Date());
        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);
        attendance.setUpdateDate(new Date());
        attendance.setTerminalID("NONE");
        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (" SWIPE ERROR ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(employeeID, attendance);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (" LATE ATTENDANCE ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        if(absent){
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING ABSENT DUE TO THE  "
                    + (" THE FOUD FOUND SYSTEM RECORDS ")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);

        if (attendanceUtils.isDuplicateAttendanceByHash(attendance)) {
            logger.warn("Duplicate attendance detected for employee: {} on date: {}. Record not saved.",
                    attendance.getEmployee().getEmployeeId(), attendance.getDate());
            return;
        }

        AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
        logger.info("Attendance saved successfully for employee: {}", savedAttendance.getEmployee().getEmployeeId());

        if (nopay) {
            saveNoPayEntity(employee, null, savedAttendance, false, true, false, false, false,
                    date != null ? date : helper.getYesterdayDate());
        }
    }

    public boolean isDatePassed(Date toDate) {
        LocalDate targetDate = toDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();

        return targetDate.isBefore(today);
    }

    @Override
    public void reportAbsent(List<String> absentEmployeesToday) {

        absentEmployeesToday.forEach(employee_id -> {
            List<UserLeaveTypeRemainingEntity> userLeaveCategoryRemaining = serviceEvent
                    .getUserLeaveTypeRemaining(employee_id);

            EmployeeEntity employee = employeeRepo.findByEmployeeId(employee_id)
                    .or(() -> employeeRepo.findBySltId(employee_id))
                    .or(() -> employeeRepo.findByPublicId(employee_id))
                    .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

            /// CHECKING IF EMPLOYEE MIGHT PUT A LEAVE BEFORE SHE/HE ABSENT (FULL-DAY) --
            /// EMPLOYEE DO
            List<LeaveEntity> byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual = leaveRepo
                    .findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, new Date(),
                            new Date());

            if (!byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.isEmpty()) { /// IF PASSES WHICH MEANS EMPLOYEE DO MAKE LEAVE
                byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.forEach(leaveEntity -> {

                    ///  CHECK TO DATE PASS OR NOT
                    if (!isDatePassed(leaveEntity.getFromDate())) {
                        leaveEntity.setDescription("Absent - Leave Used");
                        leaveEntity.setNotUsed(false); /// WHICH MEANS EMPLOYEE USE THE LEAVE
                        leaveRepo.save(leaveEntity);
                    }

                    boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
                    reportAttendance(employee_id, false, false, false, false, false, false, true, true, true, true,
                            allMatch,true ,null);

                    /// CUT OF ONE OF THE LEAVES
                    /*UserLeaveTypeRemainingEntity userLeaveTypeRemainingEntity = getUserLeaveTypeRemaining(
                            leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
                    if (userLeaveTypeRemainingEntity.getRemainingLeaves() > 1) {
                        userLeaveTypeRemainingEntity
                                .setRemainingLeaves(userLeaveTypeRemainingEntity.getRemainingLeaves() - 1);
                        userLeaveTypeRemainingRepo.save(userLeaveTypeRemainingEntity);
                    }*/
                });
            } else {
                boolean allMatch = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
                reportAttendance(employee_id, false, false, false, false, false, false, false, false, false, true, allMatch,true, null);
            }

        });
    }


    private UserLeaveTypeRemainingEntity getUserLeaveTypeRemaining(String name, String employeeId) {
        return serviceEvent.getUserLeaveTypeRemaining(name, employeeId);
    }

    private String extractJwtTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public LeaveEntity transformToEntity(LeaveReq leaveReq, String emp, String id, LeaveTypeRepo leaveTypeRepository) {
        LeaveTypeEntity type = leaveTypeRepository.findByName(leaveReq.getLeaveType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type: " + leaveReq.getLeaveType()));

        EmployeeEntity employee = employeeRepo.findByEmployeeId(emp)
                .or(() -> employeeRepo.findBySltId(emp))
                .or(() -> employeeRepo.findByPublicId(emp))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        // Create and populate the entity using builder
        return LeaveEntity.builder().publicId(id) // Generate a unique ID
                .employee(employee).submitDate(helper.removeTimeFromDate(new Date())) // Current date
                .fromDate(stripTimeFromDate(leaveReq.getFromDate())).toDate(stripTimeFromDate(leaveReq.getToDate()))
                .happenDate(stripTimeFromDate(leaveReq.getHappenDate())).leaveType(type)
                .numOfDays(leaveReq.getNumOfDays()).description(leaveReq.getDescription())
                .isHalfDay(leaveReq.getHalfDay() != null ? leaveReq.getHalfDay() : false)
                .isFullDay(leaveReq.getFullDay() != null ? leaveReq.getFullDay() : true) // Use value from request
                .isManualRequest(leaveReq.getManualRequest() != null ? leaveReq.getManualRequest() : false)
                .isUnauthorized(leaveReq.getUnauthorized() != null ? leaveReq.getUnauthorized() : false) // Add
                // missing
                // field
                .isAbsent(leaveReq.getAbsent() != null ? leaveReq.getAbsent() : false) // Add missing field
                .isLateCover(leaveReq.getLateCover() != null ? leaveReq.getLateCover() : false) // Add new field
                .isLate(leaveReq.getLate() != null ? leaveReq.getLate() : false) // Add new field
                // Set appropriate status flags based on request
                .isPending(true) // Default to pending status for new requests
                .isAccepted(false).isCanceled(false).notUsed(false)
                .unSuccessful(leaveReq.getUnSuccessful() != null ? leaveReq.getUnSuccessful() : false) // Use proper
                // getter
                .isNoPay(0) // Default to not no-pay leave
                .build();
    }

    @Override
    public void requestALeave(LeaveReq req, String userId, Authentication authentication, HttpServletRequest request) {

        /*if (!req.validateLeaveReq())
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());*/

        Optional<EmployeeEntity> optional = employeeRepo.findByPublicId(userId);
        if (optional.isEmpty()) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        EmployeeEntity employeeEntity = optional.get();
        if (leaveRepo.findByEmployeeAndSubmitDate(employeeEntity, helper.removeTimeFromDate(new Date())).isPresent()) {
            throw new IllegalArgumentException((ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()));
        }
        String name = authentication.getName();
        EmployeeEntity employee = optional.get();
        String employeeId = employee.getEmployeeId();

        if (name == null || name.isEmpty() || employeeId == null || employeeId.isEmpty())
            throw new IllegalArgumentException("Failed to make leave request");

        if (!req.getUserId().equals(name))
            throw new IllegalArgumentException("Failed to make leave movement request");

        List<UserLeaveTypeRemainingEntity> userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(employeeId);

        boolean noLeavesRemaining = userLeaveTypeRemaining.stream()
                .allMatch(leaveType -> leaveType.getRemainingLeaves() < 1);
        if (noLeavesRemaining) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        final String leaveId = utils.generateId(10);
        LeaveEntity leaveEntity = transformToEntity(req, employee.getSltId(), leaveId, leaveTypeRepository);

        if (req.getHalfDay() || req.getUnauthorized() || req.getAbsent() || req.getUnSuccessful()
                || req.getLate() || req.getLateCover()) {

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDate(
                    employee, leaveEntity.getHappenDate());

            if (attendanceEntityOp.isEmpty()) {
                throw new IllegalArgumentException("Failed to process leave request: No attendance record found");
            }
            leaveEntity.setIsPending(false);
            leaveEntity.setAttendance(attendanceEntityOp.get());
        }
        if (req.getManualRequest()) {

            String token = "Bearer " + extractJwtTokenFromCookie(request);
            final List<UserRest> admins;
            synchronized (this) { // Consider using a dedicated lock object
                admins = userClient.getEmployeeAdmins(req.getUserId(), token);
            }
            Map<String, UserRest> userMap = createUserMap(admins);

            final List<ComponetAdminsEntity> adminEntities = Collections.synchronizedList(new ArrayList<>());

            synchronized (componetAdminsRepo) { // Use the correct repository for synchronization
                for (Map.Entry<String, UserRest> entry : userMap.entrySet()) {
                    UserRest value = entry.getValue();
                    ComponetAdminsEntity admin = createAdminEntity(value, leaveId);
                    ComponetAdminsEntity savedAdmin = componetAdminsRepo.save(admin);
                    adminEntities.add(savedAdmin);
                }
            }

            leaveEntity.setAdmins(adminEntities);
        }

        synchronized (this) {
            lmsService.saveLeave(leaveEntity);
        }

        if (Boolean.TRUE.equals(req.getUnauthorized()) || Boolean.TRUE.equals(req.getHalfDay())
                || Boolean.TRUE.equals(req.getUnSuccessful()) || Boolean.TRUE.equals(req.getLateCover())
                || Boolean.TRUE.equals(req.getLate()) || Boolean.TRUE.equals(req.getAbsent())) {
            processUnauthorizedLeave(leaveEntity, employeeId);
        }
    }

    // Helper method to process unauthorized leave
    private void processUnauthorizedLeave(LeaveEntity leaveEntity, String employeeId) {
        Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDate(
                leaveEntity.getEmployee(), leaveEntity.getHappenDate());
        if (attendanceEntityOp.isPresent()) {
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            attendanceEntity.setResolve(true);
            attendanceEntity.setIssues(false);
            attendanceEntity.setViaLeave(true);

            leaveEntity.setIsAccepted(true);
            leaveRepo.save(leaveEntity);
            attendanceRepo.save(attendanceEntity);

            UserLeaveTypeRemainingEntity userLeaveTypeRemaining = getUserLeaveTypeRemaining(
                    leaveEntity.getLeaveType().getName(), employeeId);

            if (userLeaveTypeRemaining != null && userLeaveTypeRemaining.getRemainingLeaves() > 0) {
                userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
            }
        } else {
            throw new IllegalArgumentException("Failed to process leave request: No attendance record found");
        }
    }

    @Override
    public void getAllTheInOutRecordsFromSLT() {
        String url = "jdbc:mysql://192.168.3.238:3306/attendance";
        String username = "appuser";
        String password = "asdfghjkl";

        String sql = "SELECT EmployeeID, LogDate, LogTime, TerminalID, InOut, `read`, processed, etl_run_time FROM accesslog_final WHERE LogDate = DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-%d')";

        List<AccessLogEntity> accessLogEntities = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(url, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            System.out.println("Connected to MySQL database successfully!");

            while (resultSet.next()) {
                // Create AccessLogEntity from database record
                AccessLogEntity accessLog = AccessLogEntity.builder()
                        .employeeID(resultSet.getString("EmployeeID"))
                        .logDate(resultSet.getString("LogDate"))
                        .logTime(resultSet.getString("LogTime"))
                        .terminalID(resultSet.getString("TerminalID"))
                        .inOut(resultSet.getString("InOut"))
                        .readStatus(resultSet.getString("read"))
                        .processed(resultSet.getInt("processed"))
                        .etlRunTime(resultSet.getTimestamp("etl_run_time"))
                        .build();

                accessLogEntities.add(accessLog);
            }
            accessLogRepo.saveAll(accessLogEntities);
            System.out.println("Retrieved " + accessLogEntities.size() + " records from SLT database");

        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
            // You might want to throw a custom exception here
            throw new RuntimeException("Failed to retrieve records from SLT database", e);
        }
    }

    @Override
    public void processLeave(String leaveId, String userId) {
        Optional<LeaveEntity> leaveEntityOp = leaveRepo.findByPublicId(leaveId);
        if (leaveEntityOp.isPresent()) {
            LeaveEntity leaveEntity = leaveEntityOp.get();
            approvedLeave(leaveEntity, userId);
        }

    }
}
