package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.*;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import com.slt.peotv.lmsmangmentservice.model.req.*;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.Main_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.LMSMapper;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class Main_Service_Impl implements Main_Service {

    private static final Logger logger = LoggerFactory.getLogger(Main_Service_Impl.class);
    private static final int ID_LENGTH = 10;
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final Object adminFetchLockForM = new Object();
    private final Object adminFetchLockForL = new Object();
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
    private LMSMapper lMSMapper;
    @Autowired
    private NoPayReasonRepo noPayReasonRepo;
    @Autowired
    private HolidayRepository holidayRepo;

    public static Map<String, UserRest> createUserMap(List<UserRest> users) {
        if (users == null) {
            return Collections.emptyMap();
        }

        final List<UserRest> usersCopy = new ArrayList<>(users);

        List<UserRest> filteredAndSortedUsers = usersCopy.stream()
                .filter(Objects::nonNull)
                .filter(user -> user.getHighestRolePriority() != 1)
                .sorted(Comparator.comparing(
                        UserRest::getHighestRolePriority,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .collect(Collectors.toList());

        Map<String, UserRest> userMap = new ConcurrentHashMap<>();

        for (UserRest user : filteredAndSortedUsers) {
            if (user.getUserId() == null) {
                continue;
            }
            String key = user.getSltId() != null ? user.getSltId() : user.getUserId();
            userMap.put(key, user);
        }

        return Collections.unmodifiableMap(userMap);
    }

    @Override
    public synchronized void allApproved(BulkApprovedReq bulkApprovedReq, boolean swap) {
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
                        if (!movementsEntity.getRequestStatus().equals(RequestStatus.APPROVED)) {
                            movementsEntity.setRequestStatus(RequestStatus.REJECTED);
                            movementsRepo.save(movementsEntity);
                        }
                    }
                }
            } else {
                synchronized (leaveRepo) {
                    Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
                    if (leaveEntityOpt.isPresent()) {
                        LeaveEntity leaveEntity = leaveEntityOpt.get();
                        if (!leaveEntity.getRequestStatus().equals(RequestStatus.APPROVED)) {
                            leaveEntity.setRequestStatus(RequestStatus.REJECTED);
                            leaveRepo.save(leaveEntity);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void reject(String id, String userId, boolean swap) {
        if (swap) {
            Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
            if (!movementsOpt.isPresent())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            MovementsEntity movementsEntity = movementsOpt.get();

            if (!movementsEntity.getRequestStatus().equals(RequestStatus.APPROVED)) {
                movementsEntity.setRequestStatus(RequestStatus.REJECTED);
                movementsRepo.save(movementsEntity);
            }

        } else {
            Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
            if (!leaveEntityOpt.isPresent())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            LeaveEntity leaveEntity = leaveEntityOpt.get();

            if (!leaveEntity.getRequestStatus().equals(RequestStatus.APPROVED)) {
                leaveEntity.setRequestStatus(RequestStatus.REJECTED);
                leaveRepo.save(leaveEntity);
            }

        }
    }

    @Override
    public List<AccessLogRest> getAllAccessLogsToday(String date) {
        return accessLogRepo.findByLogDate(date).stream().map(lMSMapper::toRest).collect(Collectors.toList());
    }

    @Override
    public List<AccessLogRest> getAllAccessLogs() {
        return accessLogRepo.findAll().stream().map(lMSMapper::toRest).collect(Collectors.toList());
    }

    @Override
    public NoPayEntity saveNoPayEntity(EmployeeEntity employee, AttendanceEntity attendanceEntity,
                                       NoPayRequest request, Date actualDate) {

        if (employee == null || request == null) {
            throw new IllegalArgumentException("Employee and NoPayRequest cannot be null");
        }

        if (employee.getRoaster()) return null;

        AttendanceEntity attendance = attendanceEntity;
        if (attendance == null) {
            attendance = AttendanceEntity.builder()
                    .publicId(utils.generateId(ID_LENGTH))
                    .date(actualDate != null ? actualDate : helper.getYesterdayDate())
                    .employee(employee)
                    .updatedDate(new Date())
                    .isUnSuccessful(request.isUnsuccessful())
                    .isLateCovered(request.isLateCover())
                    .isLate(request.isLate())
                    .isUnauthorized(request.isUnAuthorized())
                    .attendanceType(request.isAbsent() ? AttendanceType.ABSENT :
                            request.isHalfDay() ? AttendanceType.HALF_DAY : AttendanceType.FULL_DAY)
                    .payStatus(PayStatus.NO_PAY)
                    .build();

            if (!attendanceRepo.existsByEmployeeAndDate(employee, attendance.getDate()))
                attendance = attendanceRepo.save(attendance);

        } else {
            attendance.setPayStatus(PayStatus.NO_PAY);
            attendance.setUpdatedDate(new Date());
            if (attendance.getAttendanceType() == null) {
                attendance.setAttendanceType(request.isAbsent() ? AttendanceType.ABSENT :
                        request.isHalfDay() ? AttendanceType.HALF_DAY : AttendanceType.FULL_DAY);
            }
            attendanceRepo.save(attendance);
        }

        NoPayReason reason = request.isAbsent() ? NoPayReason.ABSENT :
                request.isHalfDay() ? NoPayReason.HALF_DAY :
                        request.isUnsuccessful() ? NoPayReason.UNSUCCESSFUL :
                                request.isLate() ? NoPayReason.LATE :
                                        request.isLateCover() ? NoPayReason.LATE_NOT_COVERED : null;

        if (reason == null) {
            throw new IllegalStateException("No valid no-pay reason found");
        }

        List<String> reasons = new ArrayList<>();
        if (request.isAbsent()) reasons.add("Absent");
        if (request.isHalfDay()) reasons.add("Half Day");
        if (request.isUnsuccessful()) reasons.add("Unsuccessful");
        if (request.isLate()) reasons.add("Late");
        if (request.isLateCover()) reasons.add("Late Not Covered");

        String comment = "No Pay for employee: " + employee.getEmployeeId() +
                " - Reasons: " + String.join(", ", reasons);

        try {
            NoPayEntity noPayEntity = NoPayEntity.builder()
                    .publicId(utils.generateId(ID_LENGTH))
                    .employee(employee)
                    .attendance(attendance)
                    .submissionDate(helper.getDateWithoutTime())
                    .date(actualDate != null ? actualDate : new Date())
                    .comment(comment)
                    .build();

            NoPayEntity savedEntity = noPayRepo.save(noPayEntity);

            NoPayReasonEntity noPayReason = NoPayReasonEntity.builder()
                    .reason(reason)
                    .noPay(savedEntity)
                    .build();
            noPayReasonRepo.save(noPayReason);

            logger.info("No-pay record created for employee: {} with reason: {}",
                    employee.getEmployeeId(), reason);

            return savedEntity;
        } catch (Exception e) {
            logger.error("Failed to save no-pay record for employee: {}", employee.getEmployeeId(), e);
            throw new NoSuchElementException("Failed to save no-pay record");
        }
    }

    @Override
    public Page<InOutDTO> getAllInOut(String employeeID, int pageNumber, int pageSize) {
        EmployeeEntity employee = helper.getEmployeeById(employeeID);
        Pageable pageableRequest = PageRequest.of(pageNumber, pageSize);
        Page<InOutEntity> entityPage = inOutRepo.findByEmployeeId(employee.getSltId(), pageableRequest);
        return entityPage.map(lMSMapper::inOutDTO);
    }

    @Override
    public List<InOutDTO> getAllInOut(String employeeID, Date date) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(employeeID);
        return inOutRepo.findByEmployeeIdAndPunchTime(employeeEntity.getSltId(), date)
                .stream().map(lMSMapper::inOutDTO).collect(Collectors.toList());

    }

    @Override
    public Map<String, InOutDTO> getEarliestInOut(String userId, Date date) {
        if (userId == null || date == null) {
            return Collections.emptyMap();
        }

        EmployeeEntity employeeEntity = helper.getEmployeeById(userId);
        if (employeeEntity == null || employeeEntity.getSltId() == null) {
            return Collections.emptyMap();
        }

        List<InOutDTO> allInOut = Collections.emptyList();
        try {
            allInOut = inOutRepo.findByEmployeeIdAndPunchTime(employeeEntity.getSltId(), date)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(lMSMapper::inOutDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyMap();
        }

        Map<String, InOutDTO> result = new HashMap<>();

        allInOut.stream()
                .filter(Objects::nonNull)
                .filter(dto -> dto.getInOutValue() != null && dto.getInOutValue() == 1)
                .filter(dto -> dto.getPunchTypeTime() != null)
                .min(Comparator.comparing(InOutDTO::getPunchTypeTime))
                .ifPresent(dto -> result.put("morning", dto));

        allInOut.stream()
                .filter(Objects::nonNull)
                .filter(dto -> dto.getInOutValue() != null && dto.getInOutValue() == 0)
                .filter(dto -> dto.getPunchTypeTime() != null)
                .max(Comparator.comparing(InOutDTO::getPunchTypeTime))
                .ifPresent(dto -> result.put("evening", dto));

        return result;
    }

    @Override
    public List<InOutDTO> getEarliestInOutBetweenDate(String userId, Date date, Date date2) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(userId);
        List<InOutEntity> records = inOutRepo.findByEmployeeIdAndDateBetween(employeeEntity.getSltId(), date, date2);
        return records.stream().map(lMSMapper::inOutDTO).collect(Collectors.toList());
    }

    @Override
    public List<InOutDTO> getEarliestInOutByDate(String userId, Date date) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(userId);
        List<InOutEntity> records = inOutRepo.findByEmployeeIdAndDate(employeeEntity.getSltId(), date);
        return records.stream().map(lMSMapper::inOutDTO).collect(Collectors.toList());
    }

    public List<UserRest> fetchAdminsWithResilience(String userId, String token) {
        try {
            return userClient.getEmployeeAdmins(userId, token);
        } catch (FeignException.ServiceUnavailable ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service temporarily down"
            );
        } catch (CallNotPermittedException ex) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "User service overloaded (circuit breaker open)"
            );
        } catch (FeignException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to fetch admins: " + ex.getMessage()
            );
        }
    }

    @Override
    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication) {
        try {
            EmployeeEntity employee = helper.getEmployeeById(req.getEmployeeId());

            String name = authentication.getName();
            if (name == null || name.trim().isEmpty())
                throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

            if (!employee.getPublicId().equals(req.getUserId()) || !name.equals(req.getUserId()))
                throw new IllegalArgumentException("Record with provided id is in-correct");

            Optional<MovementsEntity> reqDate = movementsRepo.findAllByEmployeeAndHappenDate(employee, helper.removeTimeFromDate(req.getHappenDate()));

            if (reqDate.isPresent() && reqDate.get().getRequestStatus() != null && (reqDate.get().getRequestStatus() == RequestStatus.EXPIRED || reqDate.get().getRequestStatus() == RequestStatus.REJECTED || reqDate.get().getRequestStatus() == RequestStatus.APPROVED))
                throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            String token = "Bearer " + extractJwtTokenFromCookie(request);
            if (token == null || token.trim().isEmpty())
                throw new NoSuchElementException("AUTH TOKEN NOT FOUND");

            final List<UserRest> admins;
            synchronized (adminFetchLockForM) {
                admins = fetchAdminsWithResilience(req.getUserId(), token);
            }
            if (admins == null || admins.isEmpty())
                throw new NoSuchElementException("No ADMINS FOUND ");

            Map<String, UserRest> userMap = createUserMap(admins);
            if (userMap == null || userMap.isEmpty())
                throw new NoSuchElementException("NO ADMINS FOUND");

            final String movementId = "MV-" + utils.generateId(10);

            MovementsEntity movementsEntity = mapToEntity(req, employee, movementId);

            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndArrivalDateAndIsActiveTrue(
                    employee, movementsEntity.getHappenDate());

            if (!attendanceEntity.isPresent())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            AttendanceEntity attendance = attendanceEntity.get();

            if (attendance.getAttendanceType() != null &&
                    (attendance.getAttendanceType() == AttendanceType.FULL_DAY || attendance.getAttendanceType() == AttendanceType.HALF_DAY))
                throw new IllegalArgumentException(" This Attendance not unauthorized");


            if (!Boolean.TRUE.equals(attendance.getHasIssues()) || Boolean.TRUE.equals(attendance.getIsResolved())) {
                throw new IllegalArgumentException("Attendance has no issues OR Attendance is Resolved ");
            }

            movementsEntity.setAttendance(attendance);
            List<ComponetAdminsEntity> adminEntities = new ArrayList<>();

            if (userMap != null) {
                userMap.entrySet().stream()
                        .filter(entry -> entry != null && entry.getKey() != null && entry.getValue() != null)
                        .forEach(entry -> {
                            try {
                                UserRest value = entry.getValue();
                                if (value != null && movementId != null) {
                                    ComponetAdminsEntity admin = createAdminEntity(value, movementId);
                                    if (admin != null && adminEntities != null) {
                                        adminEntities.add(admin);
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Failed to process admin entry with key: {}", entry.getKey(), e);
                            }
                        });
            } else {
                logger.warn("User map is null - no admins to process");
            }

            movementsEntity.setAdmins(adminEntities);

            lmsService.createMovements(movementsEntity);

        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
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
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    private ComponetAdminsEntity createAdminEntity(UserRest user, String movementId) {
        if (user == null || movementId == null) {
            throw new IllegalArgumentException("UserRest and movementId parameters cannot be null");
        }

        if (user.getEmployeeId() == null || user.getHighestRolePriority() == null) {
            throw new IllegalArgumentException("UserRest must have non-null employeeId and highestRolePriority");
        }

        EmployeeEntity emp = helper.getEmployeeById(user.getEmployeeId());
        if (user == null) {
            throw new IllegalStateException("No employee found for ID: " + user.getEmployeeId());
        }

        ComponetAdminsEntity entity = new ComponetAdminsEntity();
        entity.setEmployee(emp);
        entity.setHighestRolePriority(user.getHighestRolePriority());
        entity.setComponetID(movementId);
        entity.setIsAccepted(false);

        if (user.getProfilePic() != null) {
            entity.setProfilePic(user.getProfilePic());
        } else {
            entity.setProfilePic("");
        }

        return entity;
    }

    public MovementsEntity mapToEntity(MovementReq movementReq, EmployeeEntity employee, String movementId) {
        if (movementReq == null) {
            return null;
        }

        return MovementsEntity.builder()
                .publicId(movementId)
                .reqDate(new Date())
                .requestStatus(RequestStatus.PENDING_APPROVAL)
                .updateDate(new Date())
                .createDate(new Date())
                .employee(employee)
                .movementType(movementReq.getMovementType())
                .comment(movementReq.getComment())
                .destination(movementReq.getDestination())
                .category(movementReq.getCategory())
                .happenDate(helper.removeTimeFromDate(movementReq.getHappenDate()))
                .logTime(movementReq.getLogTime() == null ? new Date() : movementReq.getLogTime())
                .inTime(movementReq.getInTime())
                .outTime(movementReq.getOutTime())
                .inTimeRaw(movementReq.getInTimeRaw())
                .outTimeRaw(movementReq.getOutTimeRaw())
                .happenDateRaw(movementReq.getHappenDateRaw())
                .build();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private void validateLowerPriorityApprovals(List<ComponetAdminsEntity> admins, ComponetAdminsEntity currentAdmin) {
        int currentAdminIndex = admins.indexOf(currentAdmin);
        for (int i = 0; i < currentAdminIndex; i++) {
            ComponetAdminsEntity admin = admins.get(i);
            if (admin.getApprovedDate() == null || !Boolean.TRUE.equals(admin.getIsAccepted())) {
                logger.warn("Lower priority admin {} has not approved", admin.getEmployee().getEmployeeId());
                throw new IllegalArgumentException("Lower priority admins have not approved");
            }
        }
    }

    public void approvedMove(MovementsEntity movement, String userId) {

        if (movement.getRequestStatus().equals(RequestStatus.REJECTED) ||
                movement.getRequestStatus().equals(RequestStatus.APPROVED) ||
                movement.getRequestStatus().equals(RequestStatus.CANCELLED))
            return;

        AttendanceEntity attendance = movement.getAttendance();
        if (attendance == null || attendance.getIsResolved()) {
            return;
        }
        if (movement.getAdmins() == null || movement.getAdmins().isEmpty())
            throw new NoSuchElementException("NO ADMINS FOUND");

        List<ComponetAdminsEntity> admins_ = movement.getAdmins();
        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId())
                );
        if (!isAuthorizedAdmin) throw new NoSuchElementException(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());

        List<ComponetAdminsEntity> admins = admins_.stream()
                .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());
        if (admins == null || admins.isEmpty())
            throw new NoSuchElementException("NO ADMINS FOUND");

        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);

        if (currentAdmin == null) throw new NoSuchElementException("ADMIN NOT FOUND");
        validateLowerPriorityApprovals(admins, currentAdmin);
        if (currentAdmin.getIsAccepted()) throw new IllegalArgumentException("ALREADY ACCEPTED");

        int currentAdminIndex = admins.indexOf(currentAdmin);

        boolean allLowerPriorityApproved = true;
        for (int i = 0; i < currentAdminIndex; i++) {
            if (admins.get(i).getApprovedDate() == null ||
                    !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                allLowerPriorityApproved = false;
                break;
            }
        }

        if (!allLowerPriorityApproved) {
            return;
        }

        if (currentAdmin.getApprovedDate() == null) {
            currentAdmin.setApprovedDate(new Date());
            currentAdmin.setIsAccepted(true);
            componetAdminsRepo.save(currentAdmin);
        }

        boolean allApproved = admins.stream()
                .allMatch(admin -> admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));

        if (allApproved || admins.isEmpty()) {
            movement.setRequestStatus(RequestStatus.APPROVED);

            attendance.setIsResolved(true);
            attendance.setDueDateForUA(null);
            attendance.setHasIssues(false);
            attendance.setResolve(ResolveType.VIA_MOVEMENT);
            attendance.setIsUnauthorized(false);
            attendance.setIssueDescription("none :: Movement approved");

            if (attendance.getLeaveStatus() != null && attendance.getLeaveStatus().equals(LeaveStatus.FULL_LEAVE)) {
                attendance.setLeaveStatus(null);
            }
            recalculateAttendanceFromApprovedMovement(attendance, movement);

            attendance.setArrivalTimeRaw(movement.getInTimeRaw());
            attendance.setLeftTimeRaw(movement.getOutTimeRaw());

            AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
            movementsRepo.save(movement);

            // Link InOut records with the attendance
            updateAttendanceWithInOutRecords(movement, savedAttendance);

            if ((savedAttendance.getIsUnSuccessful()) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (attendance.getIsUnauthorized() == false))
                helper.handleLateAndUnsuccessful(movement.getEmployee().getSltId(), savedAttendance, true);
        }
    }

    @Override
    public void recalculateAttendanceFromApprovedMovement(AttendanceEntity attendance, MovementsEntity movement) {
        try {
            String employeeId = movement.getEmployee().getSltId();

            InOutEntity inPunch = createInOutFromMovement(movement, true);
            InOutEntity outPunch = createInOutFromMovement(movement, false);

            updateAttendanceTimesFromMovement(attendance, movement);

            utils.handleAttendanceTypeAndIssues(
                    inPunch,
                    outPunch,
                    attendance,
                    null,
                    null,
                    null,
                    false,
                    null,
                    null,
                    employeeId
            );

            logger.info("Movement recalculation completed for {}: Type={}, Late={}, Issues={}",
                    employeeId, attendance.getAttendanceType(), attendance.getIsLate(), attendance.getHasIssues());

        } catch (Exception e) {
            logger.error("Error recalculating attendance for movement {}: {}", movement.getPublicId(), e.getMessage(), e);
            setFallbackAttendanceValues(attendance, movement);
        }
    }

    private void updateAttendanceTimesFromMovement(AttendanceEntity attendance, MovementsEntity movement) {
        switch (movement.getMovementType()) {
            case HOME_TO_OFFICE:
                if (movement.getInTime() != null) {
                    attendance.setArrivalTime(movement.getInTime());
                }
                if (attendance.getArrivalTime().equals(attendance.getLeftTime())) {
                    attendance.setArrivalTime(null);
                    logger.warn("Two time are equal Arrival time: {} Movement In time {}", attendance.getArrivalTime(), movement.getInTime());
                }
                break;
            case OFFICE_TO_HOME:
                if (movement.getOutTime() != null) {
                    attendance.setLeftTime((movement.getOutTime()));
                }
                if (attendance.getLeftTime().equals(attendance.getArrivalTime())) {
                    attendance.setLeftTime(null);
                    logger.warn("Two time are equal Left time: {} Movement Out time {}", attendance.getLeftTime(), movement.getOutTime());
                }
                break;
            case FULLDAY:
            case REMOTEWORK:
                if (movement.getInTime() != null) {
                    attendance.setArrivalTime(movement.getInTime());
                }
                if (movement.getOutTime() != null) {
                    attendance.setLeftTime(movement.getOutTime());
                }
                if (Objects.equals(attendance.getArrivalTime(), attendance.getLeftTime())) {
                    logger.warn("Both times are equal - Arrival time and Left time: {} : {}",
                            attendance.getArrivalTime(), attendance.getLeftTime());
                    attendance.setArrivalTime(null);
                    attendance.setLeftTime(null);
                }
                break;
            default:
                logger.warn("Unknown movement type: {}", movement.getMovementType());
        }
    }

    private InOutEntity createInOutFromMovement(MovementsEntity movement, boolean isInPunch) {
        if ((isInPunch && movement.getInTime() == null) || (!isInPunch && movement.getOutTime() == null)) {
            return null;
        }

        InOutEntity inOut = new InOutEntity();

        try {
            if (isInPunch && movement.getInTime() != null) {
                inOut.setPunchTime(movement.getHappenDate());
                inOut.setPunchTypeTime(movement.getInTime());
                inOut.setInOutValue(1); // IN punch
            } else if (!isInPunch && movement.getOutTime() != null) {
                inOut.setPunchTime(movement.getHappenDate());
                inOut.setPunchTypeTime(movement.getOutTime());
                inOut.setInOutValue(0); // OUT punch
            }

            inOut.setEmployeeId(movement.getEmployee().getSltId());
            return inOut;
        } catch (Exception e) {
            logger.warn("Error creating InOut entity from movement: {}", e.getMessage());
            return null;
        }
    }


    private void setFallbackAttendanceValues(AttendanceEntity attendance, MovementsEntity movement) {
        // Set safe fallback values in case of calculation error
        switch (movement.getMovementType()) {
            case FULLDAY:
            case REMOTEWORK:
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                break;
            default:
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
        }

        attendance.setHasIssues(false);
        attendance.setIsUnauthorized(false);
        attendance.setDueDateForUA(null);
        attendance.setLeaveStatus(null);
        attendance.setIssueDescription("Movement approved with calculation fallback");
    }


    private void updateAttendanceWithInOutRecords(MovementsEntity movement, AttendanceEntity attendance) {
        try {
            String employeeId = movement.getEmployee().getSltId();
            Date happenDate = movement.getHappenDate();

            if (employeeId == null || happenDate == null) {
                logger.warn("Missing employeeId or happenDate for movement: {}", movement.getId());
                return;
            }

            List<InOutEntity> inOutRecords = inOutRepo.findByEmployeeIdAndPunchTime(employeeId, happenDate);

            if (inOutRecords.isEmpty()) {
                logger.info("No InOut records found for employee {} on date {}", employeeId, happenDate);
                return;
            }

            switch (movement.getMovementType()) {
                case HOME_TO_OFFICE:
                    linkInRecordsOnly(inOutRecords, attendance, movement);
                    break;
                case OFFICE_TO_HOME:
                    linkOutRecordsOnly(inOutRecords, attendance, movement);
                    break;
                case FULLDAY:
                case REMOTEWORK:
                    linkAllInOutRecords(inOutRecords, attendance, movement);
                    break;
                default:
                    logger.warn("Unknown movement type: {}", movement.getMovementType());
            }

            logger.info("Successfully linked InOut records with attendance for movement: {}", movement.getId());

        } catch (Exception e) {
            logger.error("Error linking InOut records with attendance for movement: {}", movement.getId(), e);
        }
    }


    private void linkInRecordsOnly(List<InOutEntity> inOutRecords, AttendanceEntity attendance, MovementsEntity movement) {
        Optional<InOutEntity> earliestInRecord = inOutRecords.stream()
                .filter(record -> record.getInOutValue() != null && record.getInOutValue() == 1)
                .min(Comparator.comparing(InOutEntity::getPunchTime));

        if (earliestInRecord.isPresent()) {
            InOutEntity inRecord = earliestInRecord.get();
            inRecord.setAttendance(attendance);
            inOutRepo.save(inRecord);
            logger.info("Linked IN record {} with attendance {}", inRecord.getId(), attendance.getId());
        } else {
            logger.warn("No IN records found for HOME_TO_OFFICE movement: {}", movement.getId());
        }
    }


    private void linkOutRecordsOnly(List<InOutEntity> inOutRecords, AttendanceEntity attendance, MovementsEntity movement) {
        Optional<InOutEntity> latestOutRecord = inOutRecords.stream()
                .filter(record -> record.getInOutValue() != null && record.getInOutValue() == 0)
                .max(Comparator.comparing(InOutEntity::getPunchTime));

        if (latestOutRecord.isPresent()) {
            InOutEntity outRecord = latestOutRecord.get();
            outRecord.setAttendance(attendance);
            inOutRepo.save(outRecord);
            logger.info("Linked OUT record {} with attendance {}", outRecord.getId(), attendance.getId());
        } else {
            logger.warn("No OUT records found for OFFICE_TO_HOME movement: {}", movement.getId());
        }
    }


    private void linkAllInOutRecords(List<InOutEntity> inOutRecords, AttendanceEntity attendance, MovementsEntity movement) {
        Optional<InOutEntity> earliestInRecord = inOutRecords.stream()
                .filter(record -> record.getInOutValue() != null && record.getInOutValue() == 1)
                .min(Comparator.comparing(InOutEntity::getPunchTime));

        Optional<InOutEntity> latestOutRecord = inOutRecords.stream()
                .filter(record -> record.getInOutValue() != null && record.getInOutValue() == 0)
                .max(Comparator.comparing(InOutEntity::getPunchTime));

        int linkedCount = 0;

        if (earliestInRecord.isPresent()) {
            InOutEntity inRecord = earliestInRecord.get();
            inRecord.setAttendance(attendance);
            inOutRepo.save(inRecord);
            linkedCount++;
            logger.info("Linked IN record {} with attendance {}", inRecord.getId(), attendance.getId());
        }

        if (latestOutRecord.isPresent()) {
            InOutEntity outRecord = latestOutRecord.get();
            if (!earliestInRecord.isPresent() || !earliestInRecord.get().getId().equals(outRecord.getId())) {
                outRecord.setAttendance(attendance);
                inOutRepo.save(outRecord);
                linkedCount++;
                logger.info("Linked OUT record {} with attendance {}", outRecord.getId(), attendance.getId());
            }
        }

        if (linkedCount == 0) {
            logger.warn("No suitable InOut records found for FULLDAY/REMOTEWORK movement: {}", movement.getId());
        }
    }

    @Override
    public NoPayRequest createNoPayRequest(Boolean isHalfDay, Boolean unSuccessful, Boolean unAuthorized, Boolean isLate, Boolean isLateCover, Boolean isAbsent) {
        return new NoPayRequest(isHalfDay, unAuthorized, unSuccessful, isLate, isLateCover, isAbsent);
    }

    public void approvedLeave(LeaveEntity leave, String userId) {

        if (leave.getRequestStatus().equals(RequestStatus.REJECTED) || leave.getRequestStatus().equals(RequestStatus.APPROVED) || leave.getRequestStatus().equals(RequestStatus.CANCELLED))
            return;

        AttendanceEntity attendance = leave.getAttendance();
        if (leave.getIsManualRequest() && attendance == null) {
            throw new IllegalArgumentException(ErrorMessages.COULD_NOT_UPDATE_RECORD.getErrorMessage());
        }

        List<ComponetAdminsEntity> admins_ = leave.getAdmins();
        if (admins_ == null || admins_.isEmpty())
            throw new NoSuchElementException("NO ADMINS FOUND");

        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId())
                );
        if (!isAuthorizedAdmin) throw new NoSuchElementException(ErrorMessages.AUTHENTICATION_FAILED.getErrorMessage());

        List<ComponetAdminsEntity> admins = admins_.stream()
                .sorted(Comparator.comparingInt(ComponetAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());

        if (admins == null || admins.isEmpty())
            throw new NoSuchElementException("NO ADMINS FOUND");

        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) throw new NoSuchElementException("ADMIN NOT FOUND");
        validateLowerPriorityApprovals(admins, currentAdmin);
        if (currentAdmin.getIsAccepted()) throw new IllegalArgumentException("ALREADY ACCEPTED");

        int currentAdminIndex = admins.indexOf(currentAdmin);

        boolean allLowerPriorityApproved = true;
        for (int i = 0; i < currentAdminIndex; i++) {
            if (admins.get(i).getApprovedDate() == null ||
                    !Boolean.TRUE.equals(admins.get(i).getIsAccepted())) {
                allLowerPriorityApproved = false;
                break;
            }
        }

        if (!allLowerPriorityApproved) {
            return;
        }

        if (currentAdmin.getApprovedDate() == null) {
            currentAdmin.setApprovedDate(new Date());
            currentAdmin.setIsAccepted(true);
            componetAdminsRepo.save(currentAdmin);
        }

        boolean allApproved = admins.stream()
                .allMatch(admin -> admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));

        if (allApproved || admins.isEmpty()) {
            if (((leave.getComponentBehavior() != null) & (leave.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                    leave.getComponentBehavior() == ComponentBehavior.ABSENT ||
                    leave.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL))) {
                EmployeeEntity employee = attendance.getEmployee();
                if (employee != null)
                    processUnauthorizedLeave(leave, attendance.getEmployee().getSltId());
            }

            leave.setRequestStatus(RequestStatus.APPROVED);
            leaveRepo.save(leave);
        }


    }

    @Override
    public void main() {

        prerequisite();

        List<AttendanceEntity> attendanceEntities = attendanceRepo.findByDueDateForUA(helper.getDateWithoutTime());
        List<AttendanceEntity> overdueEntities_filter = attendanceEntities.stream()
                .filter(entity -> Boolean.TRUE.equals(entity.getIsUnauthorized())
                        || Boolean.TRUE.equals(entity.getIsUnSuccessful()) || entity.getAttendanceType().equals(AttendanceType.ABSENT))
                .collect(Collectors.toList());

        overdueEntities_filter.forEach(entity -> {
            if (entity == null) return;

            if (entity.getIsResolved())
                return;

            EmployeeEntity employee = entity.getEmployee();
            if (employee == null) return;
            if (Objects.isNull(employee.getRoaster())) {
                employee.setRoaster(false);
            }
            if (employee.getRoaster()) return;

            Optional<MovementsEntity> movement = movementsRepo.findAllByEmployeeAndHappenDate(entity.getEmployee(), entity.getArrivalDate());
            Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDate(entity.getEmployee(), entity.getArrivalDate());

            if (leave.isPresent() || movement.isPresent())
                return;

            entity.setPayStatus(PayStatus.NO_PAY);
            entity.setResolve(ResolveType.EXPIRED);

            AttendanceEntity attendanceEntity = attendanceRepo.save(entity);

            saveNoPayEntity(entity.getEmployee(), attendanceEntity,
                    createNoPayRequest(entity.getAttendanceType().equals(AttendanceType.HALF_DAY), entity.getIsUnSuccessful(), entity.getIsUnauthorized(), entity.getIsLate(), entity.getIsLateCovered(), entity.getAttendanceType().equals(AttendanceType.ABSENT))
                    , entity.getDate());
        });

    }

    private void handleHolidays() {
        List<EmployeeEntity> allEmployees = (ArrayList<EmployeeEntity>) employeeRepo.findAll();
        ConcurrentLinkedQueue<AttendanceEntity> attendances = new ConcurrentLinkedQueue<>();

        allEmployees.parallelStream().forEach(employee -> {
            Date yesterdayDate = helper.getYesterdayDate();
            if (!attendanceRepo.existsByEmployeeAndDate(employee, yesterdayDate) && !employee.getRoaster()) {
                AttendanceEntity attendance = new AttendanceEntity();
                attendance.setEmployee(employee);
                attendance.setPublicId(utils.generateId(10));
                attendance.setIsHoliday(true);
                attendance.setDate(yesterdayDate);
                attendance.setArrivalDate(yesterdayDate);
                attendances.add(attendance);
            }
        });

        attendanceRepo.saveAll(attendances);
    }


    @Override
    public void prerequisite() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        if (holidayRepo.existsByHolidayDate(yesterday)) {
            handleHolidays();
        }

        Date yesterdayDate = helper.getYesterdayDate();

        // Get ALL punches for the day - we'll analyze the complete pattern
        List<InOutEntity> allDayPunches = inOutRepo.findByDate(yesterdayDate);

        // Process each employee's complete punch pattern
        Map<String, EmployeePunchPattern> employeePunchPatterns = analyzeEmployeePunchPatterns(allDayPunches);

        // Process attendance based on complete patterns
        for (Map.Entry<String, EmployeePunchPattern> entry : employeePunchPatterns.entrySet()) {
            String employeeId = entry.getKey();
            EmployeePunchPattern pattern = entry.getValue();

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                if (employee.getRoaster() != null && employee.getRoaster()) {
                    logger.info("Employee {} is on roaster. Skipping attendance reporting.", employee.getEmployeeId());
                    return;
                }

                if (attendanceRepo.existsByEmployeeAndDate(employee, yesterdayDate)) {
                    logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                            employee.getEmployeeId(), yesterdayDate);
                    return;
                }

                processEmployeeAttendance(pattern, employee, yesterdayDate);
            });
        }

        // Handle absent employees
        getAbsentEmployeesToday().forEach(employeeId -> {
            reportAttendance(employeeId, false, false, false, false, false, false, false, false, false, false, true, false, true, yesterdayDate);
        });
    }


    private Map<String, EmployeePunchPattern> analyzeEmployeePunchPatterns(List<InOutEntity> allDayPunches) {
        Map<String, EmployeePunchPattern> patterns = new HashMap<>();

        // Group punches by employee
        Map<String, List<InOutEntity>> punchesByEmployee = allDayPunches.stream()
                .filter(Objects::nonNull)
                .filter(punch -> punch.getEmployeeId() != null && punch.getPunchTypeTime() != null)
                .collect(Collectors.groupingBy(InOutEntity::getEmployeeId));

        // Analyze each employee's pattern
        for (Map.Entry<String, List<InOutEntity>> entry : punchesByEmployee.entrySet()) {
            String employeeId = entry.getKey();
            List<InOutEntity> punches = entry.getValue();

            EmployeePunchPattern pattern = analyzeIndividualEmployeePattern(employeeId, punches);
            if (pattern != null) {
                patterns.put(employeeId, pattern);
            }
        }

        return patterns;
    }

    private EmployeePunchPattern analyzeIndividualEmployeePattern(String employeeId, List<InOutEntity> punches) {
        if (punches == null || punches.isEmpty()) {
            return null;
        }

        // Sort punches by time to analyze sequence
        List<InOutEntity> sortedPunches = punches.stream()
                .sorted(Comparator.comparing(InOutEntity::getPunchTypeTime))
                .collect(Collectors.toList());

        // Find first IN and last OUT punches
        InOutEntity firstInPunch = findFirstValidInPunch(sortedPunches);
        InOutEntity lastOutPunch = findLastValidOutPunch(sortedPunches);

        // Analyze the pattern and determine attendance details
        AttendanceAnalysis analysis = analyzeAttendancePattern(firstInPunch, lastOutPunch, sortedPunches);

        return new EmployeePunchPattern(
                employeeId,
                sortedPunches,
                firstInPunch,
                lastOutPunch,
                analysis.getAttendanceType(),
                analysis.isAuthorized(),
                analysis.isLate(),
                analysis.isUnsuccessful(),
                analysis.getIssueDescription()
        );
    }

    private InOutEntity findFirstValidInPunch(List<InOutEntity> sortedPunches) {
        return sortedPunches.stream()
                .filter(punch -> punch.getInOutValue() == 1) // IN punch
                .findFirst()
                .orElse(null);
    }

    private InOutEntity findLastValidOutPunch(List<InOutEntity> sortedPunches) {
        // Get all OUT punches and find the absolute last one
        List<InOutEntity> outPunches = sortedPunches.stream()
                .filter(punch -> punch.getInOutValue() == 0) // OUT punch
                .collect(Collectors.toList());

        if (outPunches.isEmpty()) {
            return null;
        }

        // Check if there are any IN punches after the last OUT punch
        InOutEntity lastOut = outPunches.get(outPunches.size() - 1);

        // Verify no IN punches exist after this OUT punch
        boolean hasSubsequentInPunch = sortedPunches.stream()
                .filter(punch -> punch.getInOutValue() == 1)
                .anyMatch(inPunch -> inPunch.getPunchTypeTime().after(lastOut.getPunchTypeTime()));

        if (hasSubsequentInPunch) {
            return sortedPunches.stream()
                    .filter(punch -> punch.getInOutValue() == 0)
                    .filter(punch -> punch.getPunchTypeTime().after(lastOut.getPunchTypeTime()))
                    .max(Comparator.comparing(InOutEntity::getPunchTypeTime))
                    .orElse(lastOut);
        }

        return lastOut;
    }


    private AttendanceAnalysis analyzeAttendancePattern(InOutEntity firstIn, InOutEntity lastOut,
                                                        List<InOutEntity> allPunches) {

        // Default values
        AttendanceType attendanceType = AttendanceType.NONE;
        boolean isAuthorized = true;
        boolean isLate = false;
        boolean isUnsuccessful = false;
        StringBuilder issueDescription = new StringBuilder();

        // Define time thresholds
        LocalTime standardStart = LocalTime.of(8, 30);
        LocalTime standardStartWaier = LocalTime.of(9, 0);
        LocalTime lateThreshold = LocalTime.of(10, 0);
        LocalTime halfDayThreshold = LocalTime.of(12, 30);
        LocalTime fullLeaveThreshold = LocalTime.of(13, 0);
        LocalTime standardEnd = LocalTime.of(17, 00);

        // Case 1: No punches or incomplete pattern
        if (firstIn == null && lastOut == null) {
            return new AttendanceAnalysis(AttendanceType.ABSENT, false, false, false,
                    "No punch records found - ABSENT");
        }

        // Case 2: Only IN punch, no OUT punch (Unauthorized)
        if (firstIn != null && lastOut == null) {
            LocalTime arrivalTime = firstIn.getPunchTypeTime().toLocalTime();
            isAuthorized = false;
            isLate = arrivalTime.isAfter(standardStart);
            return new AttendanceAnalysis(AttendanceType.NONE, false, isLate, false,
                    "Missing OUT punch - UNAUTHORIZED");
        }

        // Case 3: Only OUT punch, no IN punch (Unauthorized)
        if (firstIn == null && lastOut != null) {
            return new AttendanceAnalysis(AttendanceType.NONE, false, false, false,
                    "Missing IN punch - UNAUTHORIZED");
        }

        // Case 4: Both IN and OUT punches exist - analyze timing
        LocalTime arrivalTime = firstIn.getPunchTypeTime().toLocalTime();
        LocalTime departureTime = lastOut.getPunchTypeTime().toLocalTime();

        // Check arrival time
        if (arrivalTime.isAfter(fullLeaveThreshold)) {
            // Arrived after 1:00 PM - Full Leave
            attendanceType = AttendanceType.ABSENT;
            isLate = true;
            isUnsuccessful = true;
            issueDescription.append("Arrived after 13:00 - FULL LEAVE required. ");

        } else if (arrivalTime.isAfter(halfDayThreshold)) {
            // Case 1: Arrived after 12:30 PM - Always half day regardless of departure
            attendanceType = AttendanceType.HALF_DAY;
            isLate = true;
            issueDescription.append("Arrived after 12:30 PM - HALF DAY. ");

        } else if (arrivalTime.isAfter(lateThreshold) && departureTime.isBefore(fullLeaveThreshold)) {
            // Case 2: Arrived after 10:00 AM AND left before 13:00 PM - Half day
            attendanceType = AttendanceType.HALF_DAY;
            isLate = true;
            issueDescription.append("Arrived after 10:00 AM and left before 13:00 PM - HALF DAY. ");

        } else if (arrivalTime.isBefore(standardStart) && departureTime.isBefore(halfDayThreshold)) {
            // Case 3: Early arrival but left before 12:30 PM - Half day
            attendanceType = AttendanceType.HALF_DAY;
            issueDescription.append("Left before 12:30 PM - HALF DAY. ");

        } else if (arrivalTime.isBefore(lateThreshold) && departureTime.isBefore(halfDayThreshold)) {
            // Case 4: Normal arrival but left too early - Half day
            attendanceType = AttendanceType.HALF_DAY;
            issueDescription.append("Left before 12:30 PM - HALF DAY. ");

        } else if (calculateWorkingHours(arrivalTime, departureTime) < 4.0) {
            // Case 5: Worked less than 4 hours - Half day
            attendanceType = AttendanceType.HALF_DAY;
            issueDescription.append("Worked less than 4 hours - HALF DAY. ");

        } else if (arrivalTime.isAfter(standardStartWaier)) {
            // Arrived after 8:30 AM LATE
            isLate = true;
            int morningGapMinutes = (int) Duration.between(standardStart, arrivalTime).toMinutes();
            int extraEveningMinutes = departureTime.isAfter(standardEnd) ?
                    (int) Duration.between(standardEnd, departureTime).toMinutes() : 0;

            if (extraEveningMinutes < morningGapMinutes) {
                isUnsuccessful = true;
                issueDescription.append("Late arrival not compensated by late departure. ");
            } else {
                issueDescription.append("Late arrival compensated by extended hours. ");
            }
        }


        // Final validation - check total work duration
        long totalWorkMinutes = Duration.between(arrivalTime, departureTime).toMinutes();
        if (totalWorkMinutes < 240) { // Less than 4 hours
            attendanceType = AttendanceType.HALF_DAY;
            issueDescription.append("Total work time less than 4 hours. ");
        }

        return new AttendanceAnalysis(attendanceType, isAuthorized, isLate, isUnsuccessful,
                issueDescription.toString().trim());
    }

    private double calculateWorkingHours(LocalTime arrival, LocalTime departure) {
        Duration duration = Duration.between(arrival, departure);
        return duration.toMinutes() / 60.0;
    }

    private void processEmployeeAttendance(EmployeePunchPattern pattern, EmployeeEntity employee, Date date) {
        try {
            if (!pattern.hasValidInOutPair()) {
                // Handle single punch scenarios
                if (pattern.getFirstInPunch() != null) {
                    reportAttendance(pattern.getFirstInPunch(), false, false, true, false,
                            pattern.isLate(), false, false, false, false, false, true, false, false, date);
                } else if (pattern.getLastOutPunch() != null) {
                    reportAttendance(pattern.getLastOutPunch(), true, false, true, false,
                            false, false, false, false, false, false, true, false, false, date);
                } else {
                    // No valid punches - absent
                    reportAttendance(employee.getEmployeeId(), false, false, false, false, false, false, false,
                            false, false, false, true, true, true, date);
                }
                return;
            }

            LocalTime standardStart = LocalTime.of(8, 30);
            LocalTime standardEnd = LocalTime.of(17, 0);

            // Handle complete IN/OUT pairs
            InOutEntity firstIn = pattern.getFirstInPunch();
            InOutEntity lastOut = pattern.getLastOutPunch();

            LocalTime localTimeIn = firstIn.getPunchTypeTime().toLocalTime();
            LocalTime localTimeOut = lastOut.getPunchTypeTime().toLocalTime();

            boolean isFullDay = localTimeIn.isBefore(standardStart) && localTimeOut.isAfter(standardEnd);
            boolean isHalfDay = pattern.getAttendanceType() == AttendanceType.HALF_DAY;
            boolean isAbsent = pattern.getAttendanceType() == AttendanceType.ABSENT;

            if (isAbsent) {
                reportAttendance(employee.getEmployeeId(), false, false, true, pattern.isUnsuccessful(),
                        pattern.isLate(), false, false, false, false, false, true, false, true, date);
            } else {
                reportAttendance(firstIn, lastOut, false, isFullDay, !pattern.isAuthorized(),
                        pattern.isUnsuccessful(), pattern.isLate(), false, isHalfDay, false, false,
                        false, true, false, false, date);
            }

            logger.info("Processed attendance for employee {}: {} - First IN: {}, Last OUT: {}, Issues: {}",
                    employee.getEmployeeId(), pattern.getAttendanceType(),
                    firstIn.getPunchTypeTime(), lastOut.getPunchTypeTime(), pattern.getIssueDescription());

        } catch (Exception e) {
            logger.error("Error processing attendance for employee: {}", employee.getEmployeeId(), e);
        }
    }

    public List<String> getAbsentEmployeesToday() {
        // Get all employees
        List<EmployeeEntity> allEmployees = (List<EmployeeEntity>) employeeRepo.findAll();

        // Get yesterday's records
        List<InOutEntity> todayRecords = inOutRepo.findByDate(helper.getYesterdayDate());

        // Extract present employee IDs (using the correct field name from InOutEntity)
        Set<String> presentEmployeeIds = todayRecords.stream()
                .map(InOutEntity::getEmployeeId)
                .collect(Collectors.toSet());

        // Return employees whose IDs are NOT in presentEmployeeIds
        return allEmployees.stream()
                .filter(employee -> !employee.getRoaster())
                .map(EmployeeEntity::getSltId)
                .filter(sltId -> !presentEmployeeIds.contains(sltId))
                .collect(Collectors.toList());
    }


    @Override
    public void reportAttendance(InOutEntity inout, Boolean swap, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                 Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                 Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent, Date date) {

        if (inout.getEmployeeId() == null) {
            logger.warn("InOut entity has null employee ID. Cannot proceed with attendance reporting.");
            return;
        }

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(inout.getEmployeeId());
        if (!employeeEntity.isPresent()) {
            logger.warn("Employee not found for ID: {}", inout.getEmployeeId());
            return;
        }

        EmployeeEntity employee = employeeEntity.get();
        if (Objects.isNull(employee.getRoaster())) {
            employee.setRoaster(false);
        }

        if (employee.getRoaster()) {
            logger.info("Employee {} is on roaster. Skipping attendance reporting.", employee.getEmployeeId());
            return;
        }

        if (attendanceRepo.existsByEmployeeAndDate(employee, helper.getYesterdayDate())) {
            logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                    employee.getEmployeeId(), helper.getYesterdayDate());
            return;
        }

        if (attendanceRepo.existsByEmployeeAndArrivalDateAndArrivalTime(employee, inout.getPunchTime(), inout.getPunchTypeTime())) {
            logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                    employee.getEmployeeId(), helper.getYesterdayDate());
            return;
        }

        AttendanceEntity attendance = createBaseAttendance(employee, date);
        attendance.setTerminalId(inout.getTerminalId());

        Date arrivalDate = (inout.getPunchTime() != null)
                ? helper.removeTimeFromDate(inout.getPunchTime())
                : (date != null ? date : helper.getYesterdayDate());

        attendance.setArrivalDate(helper.removeTimeFromDate(arrivalDate));
        if (swap)
            attendance.setLeftTime(inout.getPunchTypeTime());
        else
            attendance.setArrivalTime(inout.getPunchTypeTime());
        attendance.setIsActive(active);
        attendance.setIsLate(late);
        attendance.setIsLateCovered(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnauthorized(unAuthorized);

        utils.handleAttendanceTypeAndIssues(
                swap ? null : inout,
                swap ? inout : null,
                attendance,
                swap,
                fullday, half_day, unAuthorized, unSuccessful, absent,
                employee.getEmployeeId()
        );
        utils.handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);


        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if (!attendance.isArrivalOnWeekend())
            savedAttendance = attendanceRepo.save(attendance);

        inout.setAttendance(savedAttendance);
        inOutRepo.save(inout);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave, attendance);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(inout.getPunchTime()));

        if ((unSuccessful || savedAttendance.getIsUnSuccessful()) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
            helper.handleLateAndUnsuccessful(employee.getEmployeeId(), savedAttendance, swap);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
    }

    @Override
    public void reportAttendance(InOutEntity moa, InOutEntity eve, Boolean swap, Boolean fullday, Boolean unAuthorized,
                                 Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave,
                                 Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent, Date date) {

        if (moa.getEmployeeId() == null || eve.getEmployeeId() == null) {
            logger.warn("One or both InOut entities have null employee ID. Cannot proceed with attendance reporting.");
            return;
        }

        if (!eve.getEmployeeId().equals(moa.getEmployeeId())) {
            logger.warn("Employee IDs do not match between morning ({}) and evening ({}) records.",
                    moa.getEmployeeId(), eve.getEmployeeId());
            return;
        }

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(moa.getEmployeeId());
        if (!employeeEntity.isPresent()) {
            logger.warn("Employee not found for ID: {}", moa.getEmployeeId());
            return;
        }

        EmployeeEntity employee = employeeEntity.get();
        if (Objects.isNull(employee.getRoaster())) {
            employee.setRoaster(false);
        }

        if (employee.getRoaster()) {
            logger.info("Employee {} is on roaster. Skipping attendance reporting.", employee.getEmployeeId());
            return;
        }
        if (eve == null) return;
        if (moa.getPunchTypeTime().equals(eve.getPunchTypeTime())) return;

        if (attendanceRepo.existsByEmployeeAndDate(employee, helper.getYesterdayDate())) {
            logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                    employee.getEmployeeId(), helper.getYesterdayDate());
            return;
        }

        if (attendanceRepo.existsByEmployeeAndArrivalDateAndArrivalTime(employee, moa.getPunchTime(), moa.getPunchTypeTime())) {
            logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                    employee.getEmployeeId(), helper.getYesterdayDate());
            return;
        }

        LocalTime standardStart = LocalTime.of(8, 30);
        LocalTime lateThreshold = LocalTime.of(10, 0);
        LocalTime halfDayThreshold = LocalTime.of(12, 30);
        LocalTime fullLeaveThreshold = LocalTime.of(13, 0);
        LocalTime standardEnd = LocalTime.of(17, 30);

        AttendanceEntity attendance = createBaseAttendance(employee, date);
        attendance.setTerminalId(moa.getTerminalId() + " - " + eve.getTerminalId());
        Date arrivalDate = (moa.getPunchTime() != null)
                ? helper.removeTimeFromDate(moa.getPunchTime())
                : (date != null ? date : helper.getYesterdayDate());

        attendance.setArrivalDate(helper.removeTimeFromDate(arrivalDate));
        attendance.setArrivalTime(moa.getPunchTypeTime());
        attendance.setLeftTime(eve.getPunchTypeTime());
        attendance.setIsActive(active);
        attendance.setIsLate(late);
        attendance.setIsLateCovered(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnauthorized(unAuthorized);

        utils.handleAttendanceTypeAndIssues(moa, eve, attendance,
                false, fullday, half_day, unAuthorized, unSuccessful, absent, employee.getEmployeeId());

        utils.handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        LocalTime arrivalTime = moa.getPunchTypeTime().toLocalTime();
        LocalTime departureTime = eve.getPunchTypeTime().toLocalTime();

        if (!((departureTime.isAfter(halfDayThreshold)) ||
                (arrivalTime.isAfter(standardStart) && departureTime.isAfter(halfDayThreshold)) ||
                (arrivalTime.isAfter(lateThreshold) && departureTime.isAfter(fullLeaveThreshold)) ||
                departureTime.isAfter(LocalTime.of(13, 0)) && departureTime.isAfter(LocalTime.of(17, 0)))) {
            attendance.setIsUnauthorized(true);
            attendance.setAttendanceType(AttendanceType.NONE);
        }

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if ((!attendance.isArrivalOnWeekend()) && (!attendance.getArrivalTime().equals(attendance.getLeftTime())))
            savedAttendance = attendanceRepo.save(attendance);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave, attendance);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
        utils.updateInOutRelationships(moa, eve, savedAttendance);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(moa.getPunchTime()));

        if ((unSuccessful || savedAttendance.getIsUnSuccessful()) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
            helper.handleLateAndUnsuccessful(employee.getEmployeeId(), savedAttendance, swap);
    }

    @Override
    public void reportAttendance(String employeeID, Boolean swap, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                 Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                 Boolean leaveReq, Boolean active, Boolean nopay, Boolean absent, Date date) {

        if (employeeID == null || employeeID.isEmpty()) {
            logger.warn("Employee ID is null or empty. Cannot proceed with attendance reporting.");
            return;
        }
        EmployeeEntity employee = Stream.of(
                        employeeRepo.findByEmployeeId(employeeID),
                        employeeRepo.findBySltId(employeeID),
                        employeeRepo.findByPublicId(employeeID)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!swap && !leave.isEmpty()) return;


        if (attendanceRepo.existsByEmployeeAndDate(employee, helper.getYesterdayDate())) {
            logger.info("Attendance already exists for employee: {} on date: {}. Skipping.",
                    employee.getEmployeeId(), helper.getYesterdayDate());
            return;
        }
        if (Objects.isNull(employee.getRoaster())) {
            employee.setRoaster(false);
        }

        if (employee.getRoaster()) {
            logger.info("Employee {} is on roaster. Skipping attendance reporting.", employee.getEmployeeId());
            return;
        }

        AttendanceEntity attendance = createBaseAttendance(employee, date);
        attendance.setTerminalId("NONE");
        attendance.setIsActive(active);
        attendance.setIsLate(late);
        attendance.setIsLateCovered(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnauthorized(unAuthorized);
        attendance.setHasIssues(true);
        attendance.setAttendanceType(AttendanceType.ABSENT);

        utils.handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if (!attendance.isArrivalOnWeekend())
            savedAttendance = attendanceRepo.save(attendance);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.getYesterdayDate());

        if ((unSuccessful) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
            helper.handleLateAndUnsuccessful(employeeID, savedAttendance, swap);
    }

    public void handleLeave(List<LeaveEntity> leave, AttendanceEntity attendance) {
        if (leave == null || leave.isEmpty()) {
            logger.warn("Null or empty leave list provided to handleLeave");
            return;
        }

        for (LeaveEntity leaveEntity : leave) {
            if (leaveEntity == null) {
                logger.warn("Null leave entity encountered in handleLeave");
                continue;
            }

            try {
                processLeaveEntity(leaveEntity, attendance);
            } catch (Exception e) {
                logger.error("Error processing leave entity with ID: {}",
                        leaveEntity.getId(), e);
            }
        }
    }

    private void processLeaveEntity(LeaveEntity leaveEntity, AttendanceEntity attendance) {
        boolean isHalfDayLeave = isHalfDayLeave(leaveEntity);

        boolean isNonHalfDayAttendance = isNonHalfDayAttendance(attendance);

        if (isHalfDayLeave && isNonHalfDayAttendance) {
            markLeaveAsUsed(leaveEntity);
        } else {
            markLeaveAsCancelled(leaveEntity);
        }

        leaveRepo.save(leaveEntity);
    }

    private boolean isHalfDayLeave(LeaveEntity leaveEntity) {
        return leaveEntity.getComponentBehavior() != null &&
                leaveEntity.getComponentBehavior().equals(ComponentBehavior.HALF_DAY);
    }

    private boolean isNonHalfDayAttendance(AttendanceEntity attendance) {
        if (attendance == null || attendance.getAttendanceType() == null) {
            return true;
        }

        return !attendance.getAttendanceType().equals(AttendanceType.HALF_DAY);
    }

    private void markLeaveAsUsed(LeaveEntity leaveEntity) {
        leaveEntity.setNotUsed(false);
        leaveEntity.setRequestStatus(RequestStatus.SUBMITTED);
        leaveEntity.setDescription("Half day leave applied - attendance recorded as full day");

        logger.info("Leave ID {} marked as used - half day leave with non-half-day attendance",
                leaveEntity.getId());
    }

    private void markLeaveAsCancelled(LeaveEntity leaveEntity) {
        leaveEntity.setNotUsed(true);
        leaveEntity.setRequestStatus(RequestStatus.CANCELLED);
        leaveEntity.setDescription("Leave cancelled - employee attendance does not require leave usage");

        logger.info("Leave ID {} cancelled - attendance matches leave request or employee worked full day",
                leaveEntity.getId());
    }

    private AttendanceEntity createBaseAttendance(EmployeeEntity employee, Date date) {
        if (employee == null || date == null) {
            logger.error("Null employee or date provided to createBaseAttendance");
            throw new IllegalArgumentException("Employee and date cannot be null");
        }

        try {
            AttendanceEntity attendance = new AttendanceEntity();
            attendance.setAttendanceType(AttendanceType.NONE);
            attendance.setPublicId(Objects.requireNonNull(utils.generateId(10), "Generated ID cannot be null"));
            attendance.setEmployee(employee);
            attendance.setDate(Objects.requireNonNull(helper.getYesterdayDate(), "Yesterday date cannot be null"));
            attendance.setArrivalDate(date);
            attendance.setEtlRunTime(new Date());
            attendance.setUpdatedDate(new Date());
            attendance.setCreatedDate(new Date());

            return attendance;
        } catch (NullPointerException e) {
            logger.error("Null value encountered while creating base attendance", e);
            throw new IllegalStateException("Failed to create attendance due to null values", e);
        }
    }


    @Override
    public void reportAbsent(List<String> absentEmployeesToday) {

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

        EmployeeEntity employee = helper.getEmployeeById(emp);

        return LeaveEntity.builder().publicId(id)
                .employee(employee).submitDate(helper.removeTimeFromDate(new Date()))
                .fromDate(helper.removeTimeFromDate(leaveReq.getFromDate())).toDate(helper.removeTimeFromDate(leaveReq.getToDate()))
                .happenDate(helper.removeTimeFromDate(leaveReq.getHappenDate())).leaveType(type)
                .numOfDays(leaveReq.getNumOfDays()).description(leaveReq.getDescription())
                .isManualRequest(leaveReq.getIsManualRequest() != null ? leaveReq.getIsManualRequest() : false)
                .componentBehavior(leaveReq.getComponentBehavior())
                .build();
    }

    @Override
    public void requestALeave(LeaveReq req, String userId, Authentication authentication, HttpServletRequest request) {
        EmployeeEntity employee = helper.getEmployeeById(userId);

        if (leaveRepo.findByEmployeeAndFromDate(employee, helper.removeTimeFromDate(new Date())).isPresent() && req.getIsManualRequest()) {
            throw new IllegalArgumentException((ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()));
        }


        String name = authentication.getName();
        String employeeId = employee.getEmployeeId();

        if (name == null || employeeId == null || name.isEmpty() || employeeId.isEmpty())
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        if (!employee.getPublicId().equals(name))
            throw new IllegalArgumentException("Failed to make leave movement request");

        UserLeaveTypeRemainingEntity userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(req.getLeaveType(), employeeId);

        if ((userLeaveTypeRemaining == null || userLeaveTypeRemaining.getRemainingLeaves() == null || userLeaveTypeRemaining.getRemainingLeaves() <= 0) && (userLeaveTypeRemaining.getRemainingLeaves() != -1)) {
            throw new IllegalArgumentException("No remaining leaves available for this leave type");
        }
        Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndHappenDate(employee, helper.removeTimeFromDate(req.getHappenDate()));
        if (leave.isPresent() && leave.get().getRequestStatus() != null &&
                (leave.get().getRequestStatus() == RequestStatus.EXPIRED || leave.get().getRequestStatus() == RequestStatus.REJECTED || leave.get().getRequestStatus() == RequestStatus.APPROVED))
            throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        final String leaveId = "LV-" + utils.generateId(10);
        LeaveEntity leaveEntity = transformToEntity(req, employee.getSltId(), leaveId, leaveTypeRepository);
        leaveEntity.setRequestStatus(RequestStatus.PENDING_APPROVAL);

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndArrivalDateAndIsActiveTrue(
                    employee, leaveEntity.getHappenDate());

            if (!attendanceEntityOp.isPresent()) {
                throw new IllegalArgumentException("No ATTENDANCE RECORD FOUND");
            }
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            if (attendanceEntity.getIsResolved() || !attendanceEntity.getHasIssues())
                throw new IllegalArgumentException("To this attendance you can not make leave");

            if (attendanceEntity.getAttendanceType() != null &&
                    (attendanceEntity.getAttendanceType() == AttendanceType.FULL_DAY || attendanceEntity.getAttendanceType() == AttendanceType.HALF_DAY))
                throw new IllegalArgumentException(" This Attendance not unauthorized");

            if ((req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED) && (!attendanceEntity.getIsUnauthorized())
                    || (req.getComponentBehavior() == ComponentBehavior.ABSENT) && (!attendanceEntity.getAttendanceType().equals(AttendanceType.ABSENT))
                    || (req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) && (!attendanceEntity.getIsUnSuccessful()))

                throw new IllegalArgumentException("To this attendance you can not make leave");

            leaveEntity.setAttendance(attendanceEntityOp.get());
        }
        String token = "Bearer " + extractJwtTokenFromCookie(request);
        if (token == null || token.isEmpty()) throw new IllegalArgumentException("AUTH TOKEN NOT FOUND");

        final List<UserRest> admins;
        synchronized (adminFetchLockForL) {
            admins = fetchAdminsWithResilience(employee.getPublicId(), token);
        }
        if (admins.isEmpty() || admins == null)
            throw new NoSuchElementException("NO ADMINS FOUND");

        Map<String, UserRest> userMap = createUserMap(admins);

        if (userMap == null || userMap.isEmpty())
            throw new NoSuchElementException("NO ADMINS FOUND");

        final List<ComponetAdminsEntity> adminEntities = Collections.synchronizedList(new ArrayList<>());

        synchronized (componetAdminsRepo) {
            if (userMap != null) {
                for (Map.Entry<String, UserRest> entry : userMap.entrySet()) {
                    if (entry != null && entry.getValue() != null) {
                        UserRest value = entry.getValue();
                        if (leaveId != null) {
                            ComponetAdminsEntity admin = createAdminEntity(value, leaveId);
                            if (admin != null) {
                                ComponetAdminsEntity savedAdmin = componetAdminsRepo != null ?
                                        componetAdminsRepo.save(admin) : null;
                                if (savedAdmin != null) {
                                    if (adminEntities != null) {
                                        adminEntities.add(savedAdmin);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        leaveEntity.setAdmins(adminEntities);

        synchronized (this) {
            try {
                lmsService.saveLeave(leaveEntity);
            } catch (DataIntegrityViolationException e) {
                if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("publicId")) {
                    final String newLeaveId = "LV-" + utils.generateId(10);
                    leaveEntity.setPublicId(newLeaveId);

                    for (ComponetAdminsEntity admin : leaveEntity.getAdmins()) {
                        admin.setComponetID(newLeaveId);
                    }
                    lmsService.saveLeave(leaveEntity);
                } else {
                    logger.error("Error saving leave record for employee: {}", employeeId, e);
                    throw e;
                }
            }
        }
    }


    @Override
    public void processUnauthorizedLeave(LeaveEntity leaveEntity, String employeeId) {
        if (leaveEntity == null) {
            throw new IllegalArgumentException("LeaveEntity cannot be null");
        }
        if (employeeId == null || employeeId.trim().isEmpty()) {
            throw new IllegalArgumentException("EmployeeId cannot be null or empty");
        }

        if (leaveEntity.getEmployee() == null) {
            throw new IllegalArgumentException("LeaveEntity employee cannot be null");
        }
        if (leaveEntity.getHappenDate() == null) {
            throw new IllegalArgumentException("LeaveEntity happenDate cannot be null");
        }
        if (leaveEntity.getLeaveType() == null || leaveEntity.getLeaveType().getName() == null) {
            throw new IllegalArgumentException("LeaveType and its name cannot be null");
        }

        if (attendanceRepo == null) {
            throw new IllegalStateException("Attendance repository is not initialized");
        }
        if (leaveRepo == null) {
            throw new IllegalStateException("Leave repository is not initialized");
        }
        if (userLeaveTypeRemainingRepo == null) {
            throw new IllegalStateException("UserLeaveTypeRemaining repository is not initialized");
        }

        Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDate(
                leaveEntity.getEmployee(), leaveEntity.getHappenDate());

        if (attendanceEntityOp.isPresent()) {
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();

            attendanceEntity.setIsResolved(true);
            attendanceEntity.setHasIssues(false);
            attendanceEntity.setResolve(ResolveType.VIA_LEAVE);
            attendanceRepo.save(attendanceEntity);

            leaveEntity.setRequestStatus(RequestStatus.APPROVED);
            leaveRepo.save(leaveEntity);

            UserLeaveTypeRemainingEntity userLeaveTypeRemaining = getUserLeaveTypeRemaining(
                    leaveEntity.getLeaveType().getName(), employeeId);

            if (userLeaveTypeRemaining != null) {
                if (userLeaveTypeRemaining.getRemainingLeaves() == null) {
                    throw new IllegalStateException("Remaining leaves cannot be null");
                }
                if (userLeaveTypeRemaining.getRemainingLeaves() > 0) {
                    userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                    userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
                }
            }
        } else {
            throw new IllegalArgumentException("Failed to process leave request: No attendance record found");
        }
    }

    @Override
    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void getAllTheInOutRecordsFromSLT_YES() {
        processRecordsForDate("getAllTheInOutRecordsFromSLT_YES", "DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY)");
    }

    @Override
    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void getAllTheInOutRecordsFromSLT_TOD() {
        processRecordsForDate("getAllTheInOutRecordsFromSLT_TOD", "CURRENT_DATE()");
    }

    private void processRecordsForDate(String methodName, String dateFunction) {
        if (accessLogRepo == null) {
            logger.error("{}: AccessLogRepository is not initialized", methodName);
            return;
        }

        final String url = "jdbc:mysql://localhost:3306/attendance";
        final String username = "root";
        final String password = "User@123";

        String sql = "SELECT EmployeeID, LogDate, LogTime, TerminalID, `InOut`, `read`, processed, etl_run_time " +
                "FROM accesslog_archive " +
                "WHERE LogDate = DATE_FORMAT(" + dateFunction + ", '%d/%m/%Y')";

        List<AccessLogEntity> accessLogEntities = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            logger.info("{}: Attempting to connect to database", methodName);
            connection = DriverManager.getConnection(url, username, password);
            statement = connection.prepareStatement(sql);
            resultSet = statement.executeQuery();

            logger.info("{}: Database connection established successfully", methodName);

            int recordCount = 0;
            int skippedCount = 0;
            while (resultSet != null && resultSet.next()) {
                try {
                    AccessLogEntity accessLog = buildAccessLogEntity(resultSet, methodName);
                    if (accessLog != null && isValidAccessLog(accessLog)) {
                        accessLogEntities.add(accessLog);
                        recordCount++;
                    } else {
                        skippedCount++;
                    }
                } catch (SQLException e) {
                    logErrorWithStackTrace("Error processing record #" + (recordCount + skippedCount + 1), e, methodName);
                } catch (Exception e) {
                    logErrorWithStackTrace("Unexpected error processing record #" + (recordCount + skippedCount + 1), e, methodName);
                }
            }

            if (skippedCount > 0) {
                logger.warn("{}: Skipped {} invalid records", methodName, skippedCount);
            }

            processRetrievedRecords(accessLogEntities, methodName);

        } catch (SQLException e) {
            logErrorWithStackTrace("Database connection failed", e, methodName);
            throw new DataAccessException("Database operation failed", e) {
            };
        } catch (Exception e) {
            logErrorWithStackTrace("Unexpected error in " + methodName, e, methodName);
            throw new RuntimeException("Unexpected error", e);
        } finally {
            closeDatabaseResources(connection, statement, resultSet, methodName);
        }
    }

    private AccessLogEntity buildAccessLogEntity(ResultSet resultSet, String methodName) throws SQLException {
        if (resultSet == null) {
            return null;
        }

        try {
            String employeeId = resultSet.getString("EmployeeID");
            String logDate = resultSet.getString("LogDate");
            String logTime = resultSet.getString("LogTime");
            String terminalId = resultSet.getString("TerminalID");
            String inOut = resultSet.getString("InOut");
            String readStatus = resultSet.getString("read");
            int processed = resultSet.getInt("processed");
            Timestamp etlRunTime = resultSet.getTimestamp("etl_run_time");

            if (employeeId == null || logDate == null || logTime == null || terminalId == null || inOut == null) {
                logger.debug("Skipping record with null essential fields");
                return null;
            }

            return AccessLogEntity.builder()
                    .employeeId(employeeId)
                    .logDate(logDate)
                    .logTime(logTime)
                    .terminalId(terminalId)
                    .inOut(inOut)
                    .readStatus(readStatus)
                    .processed(processed)
                    .etlRunTime(etlRunTime)
                    .build();
        } catch (SQLException e) {
            logger.error("{}: Error building AccessLogEntity from ResultSet", methodName);
            throw e;
        }
    }

    private boolean isValidAccessLog(AccessLogEntity accessLog) {
        if (accessLog == null) {
            return false;
        }

        if (accessLog.getLogDate() != null && !accessLog.getLogDate().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate.parse(accessLog.getLogDate().trim(), formatter);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid date format: '{}'", accessLog.getLogDate());
                return false;
            }
        }

        if (accessLog.getLogTime() != null && !accessLog.getLogTime().isEmpty()) {
            try {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                LocalTime.parse(accessLog.getLogTime().trim(), timeFormatter);
            } catch (DateTimeParseException e) {
                logger.warn("Invalid time format: '{}' (length: {})",
                        accessLog.getLogTime(), accessLog.getLogTime().length());
                return false;
            }
        }

        return true;
    }

    private void processRetrievedRecords(List<AccessLogEntity> records, String methodName) {
        if (records == null || records.isEmpty()) {
            logger.info("{}: No valid records found for processing", methodName);
            return;
        }

        try {
            List<AccessLogEntity> uniqueRecords = records.stream()
                    .filter(record -> record != null
                            && record.getEmployeeId() != null
                            && record.getLogDate() != null
                            && record.getLogTime() != null
                            && record.getTerminalId() != null
                            && !accessLogRepo.existsByEmployeeIdAndLogDateAndLogTimeAndTerminalId(
                            record.getEmployeeId(),
                            record.getLogDate(),
                            record.getLogTime(),
                            record.getTerminalId()))
                    .collect(Collectors.toList());

            logger.info("{}: Attempting to save {} records ({} unique)",
                    methodName, records.size(), uniqueRecords.size());

            if (!uniqueRecords.isEmpty()) {
                accessLogRepo.saveAll(uniqueRecords);
                logger.info("{}: Successfully saved {} records", methodName, uniqueRecords.size());
            } else {
                logger.info("{}: No new unique records to save", methodName);
            }

        } catch (DataIntegrityViolationException e) {
            logger.warn("{}: Duplicate records detected. Attempting individual saves", methodName);
            saveRecordsIndividually(records, methodName);
        } catch (Exception e) {
            logErrorWithStackTrace("Failed to save records", e, methodName);
            throw e;
        }
    }


    private void saveRecordsIndividually(List<AccessLogEntity> records, String methodName) {
        int successCount = 0;
        int errorCount = 0;
        int duplicateCount = 0;

        for (AccessLogEntity record : records) {
            try {
                if (!accessLogRepo.existsByEmployeeIdAndLogDateAndLogTimeAndTerminalId(
                        record.getEmployeeId(),
                        record.getLogDate(),
                        record.getLogTime(),
                        record.getTerminalId())) {

                    accessLogRepo.save(record);
                    successCount++;
                } else {
                    duplicateCount++;
                    logger.debug("{}: Duplicate record skipped - Employee: {}, Date: {}, Time: {}",
                            methodName, record.getEmployeeId(), record.getLogDate(), record.getLogTime());
                }
            } catch (Exception e) {
                errorCount++;
                logErrorWithStackTrace(String.format("Failed to save record for employee %s",
                        record.getEmployeeId()), e, methodName);
            }
        }

        logger.info("{}: Individual save results - Success: {}, Duplicates: {}, Errors: {}",
                methodName, successCount, duplicateCount, errorCount);
    }

    private void closeDatabaseResources(Connection connection, PreparedStatement statement,
                                        ResultSet resultSet, String methodName) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException e) {
            logErrorWithStackTrace("Error closing ResultSet", e, methodName);
        }

        try {
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            logErrorWithStackTrace("Error closing PreparedStatement", e, methodName);
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logErrorWithStackTrace("Error closing Connection", e, methodName);
        }
    }

    private void logErrorWithStackTrace(String message, Throwable e, String methodName) {
        logger.error("{}: {} - Error: {}", methodName, message, e.getMessage());
        logger.error("Stack trace:", e);  // This will log the full stack trace

        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            logger.error("SQL State: {}, Error Code: {}", sqlEx.getSQLState(), sqlEx.getErrorCode());
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