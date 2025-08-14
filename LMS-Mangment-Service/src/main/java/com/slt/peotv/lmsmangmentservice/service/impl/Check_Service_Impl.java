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
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.HolidayChecker;
import com.slt.peotv.lmsmangmentservice.utils.service.LMSUtils;
import feign.FeignException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.dao.DataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class Check_Service_Impl implements Check_Service {

    private static final Logger logger = LoggerFactory.getLogger(Check_Service_Impl.class);
    private static final int ID_LENGTH = 10;
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
    private LMSUtils lMSUtils;
    @Autowired
    private NoPayReasonRepo noPayReasonRepo;
    @Autowired
    private HolidayRepository holidayRepo;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private final Object adminFetchLockForM = new Object();
    private final Object adminFetchLockForL = new Object();

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
                .toList();

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
            if (movementsOpt.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
            MovementsEntity movementsEntity = movementsOpt.get();

            if (!movementsEntity.getRequestStatus().equals(RequestStatus.APPROVED)) {
                movementsEntity.setRequestStatus(RequestStatus.REJECTED);
                movementsRepo.save(movementsEntity);
            }

        } else {
            Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
            if (leaveEntityOpt.isEmpty())
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
        return accessLogRepo.findByLogDate(date).stream().map(lMSUtils::toRest).toList();
    }

    @Override
    public List<AccessLogRest> getAllAccessLogs() {
        return accessLogRepo.findAll().stream().map(lMSUtils::toRest).toList();
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
            throw new NoSuchElementException("Failed to save no-pay record", e);
        }
    }

    @Override
    public Page<InOutDTO> getAllInOut(String employeeID, int pageNumber, int pageSize) {
        EmployeeEntity employee = helper.getEmployeeById(employeeID);
        Pageable pageableRequest = PageRequest.of(pageNumber, pageSize);
        Page<InOutEntity> entityPage = inOutRepo.findByEmployeeId(employee.getSltId(), pageableRequest);
        return entityPage.map(lMSUtils::inOutDTO);
    }

    @Override
    public List<InOutDTO> getAllInOut(String employeeID, Date date) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(employeeID);
        return inOutRepo.findByEmployeeIdAndPunchTime(employeeEntity.getSltId(), date)
                .stream().map(lMSUtils::inOutDTO).toList();

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
                    .map(lMSUtils::inOutDTO)
                    .filter(Objects::nonNull)
                    .toList();
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
        return records.stream().map(lMSUtils::inOutDTO).toList();
    }

    @Override
    public List<InOutDTO> getEarliestInOutByDate(String userId, Date date) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(userId);
        List<InOutEntity> records = inOutRepo.findByEmployeeIdAndDate(employeeEntity.getSltId(), date);
        return records.stream().map(lMSUtils::inOutDTO).toList();
    }

    public List<UserRest> fetchAdminsWithResilience(String userId, String token) {
        try {
            return userClient.getEmployeeAdmins(userId, token);
        }
        catch (FeignException.ServiceUnavailable ex) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "User service temporarily down"
            );
        }
        catch (CallNotPermittedException ex) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "User service overloaded (circuit breaker open)"
            );
        }
        catch (FeignException ex) {
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

            if (reqDate.isPresent() && reqDate.get().getRequestStatus() != null &&
                    reqDate.get().getRequestStatus() != RequestStatus.CANCELLED &&
                    reqDate.get().getRequestStatus() != RequestStatus.EXPIRED &&
                    reqDate.get().getRequestStatus() != RequestStatus.REJECTED)
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

            final String movementId = utils.generateId(10);

            MovementsEntity movementsEntity = mapToEntity(req, employee, movementId);

            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndArrivalDateAndIsActiveTrue(
                    employee, movementsEntity.getHappenDate());

            if (attendanceEntity.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            AttendanceEntity attendance = attendanceEntity.get();

            if (!attendance.getIsUnauthorized() || attendance.getIsUnSuccessful())
                throw new IllegalArgumentException(" This Attendance not unauthorized");

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

        } catch (IllegalArgumentException e){
            throw new IllegalArgumentException(e.getMessage());
        }
        catch (NoSuchElementException e){
            throw new NoSuchElementException(e.getMessage());
        }
        catch (Exception e) {
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
                .outTime(movementReq.getInTime())
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
                .toList();
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
            attendance.setAttendanceType(AttendanceType.FULL_DAY);
            attendance.setIsUnauthorized(false);
            attendance.setIssueDescription("none :: Movement approved");

            switch (movement.getMovementType()) {
                case HOME_TO_OFFICE:
                    if (movement.getInTime() != null) {
                        attendance.setArrivalTime(movement.getInTime());
                    }
                    if(attendance.getArrivalTime().equals(attendance.getLeftTime())){
                        attendance.setArrivalTime(null);
                        logger.warn("Two time are equal Arrival time: {} Movement In time {}", attendance.getArrivalTime(), movement.getInTime());
                    }
                    break;
                case OFFICE_TO_HOME:
                    if (movement.getOutTime() != null) {
                        attendance.setLeftTime((movement.getOutTime()));
                    }
                    if(attendance.getLeftTime().equals(attendance.getArrivalTime())){
                        attendance.setLeftTime(null);
                        logger.warn("Two time are equal Left time: {} Movement Out time {}", attendance.getLeftTime(), movement.getOutTime());
                    }
                    break;
                case FULLDAY, REMOTEWORK:
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

            if(attendance.getLeaveStatus() != null && attendance.getLeaveStatus().equals(LeaveStatus.FULL_LEAVE)) {
                attendance.setLeaveStatus(null);
            }

            attendance.setArrivalTimeRaw(movement.getInTimeRaw());
            attendance.setLeftTimeRaw(movement.getOutTimeRaw());

            AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
            movementsRepo.save(movement);

            // Link InOut records with the attendance
            updateAttendanceWithInOutRecords(movement, savedAttendance);
        }
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
            if (earliestInRecord.isEmpty() || !earliestInRecord.get().getId().equals(outRecord.getId())) {
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
        if (!leave.getIsManualRequest() && attendance == null) {
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
                .toList();

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
                .toList();

        overdueEntities_filter.forEach(entity -> {
            if (entity.getIsResolved())
                return;

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
        List<EmployeeEntity> allEmployees = (ArrayList<EmployeeEntity>)employeeRepo.findAll();
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
        if(holidayRepo.existsByHolidayDate(yesterday)){
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
                analysis.attendanceType,
                analysis.isAuthorized,
                analysis.isLate,
                analysis.isUnsuccessful,
                analysis.issueDescription
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
        AttendanceType attendanceType = AttendanceType.FULL_DAY;
        boolean isAuthorized = true;
        boolean isLate = false;
        boolean isUnsuccessful = false;
        StringBuilder issueDescription = new StringBuilder();

        // Define time thresholds
        LocalTime standardStart = LocalTime.of(8, 30);
        LocalTime lateThreshold = LocalTime.of(10, 0);
        LocalTime halfDayThreshold = LocalTime.of(12, 0);
        LocalTime fullLeaveThreshold = LocalTime.of(13, 0);
        LocalTime standardEnd = LocalTime.of(17, 30);

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
            return new AttendanceAnalysis(AttendanceType.FULL_DAY, false, isLate, false,
                    "Missing OUT punch - UNAUTHORIZED");
        }

        // Case 3: Only OUT punch, no IN punch (Unauthorized)
        if (firstIn == null && lastOut != null) {
            return new AttendanceAnalysis(AttendanceType.FULL_DAY, false, false, false,
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
            // Arrived after 12:00 PM - Half Day
            attendanceType = AttendanceType.HALF_DAY;
            isLate = true;
            issueDescription.append("Arrived after 12:00 - HALF DAY. ");

        } else if (arrivalTime.isAfter(lateThreshold)) {
            // Arrived after 10:00 AM - Check if compensated by late departure
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

        } else if (arrivalTime.isAfter(standardStart)) {
            // Arrived between 8:30 and 10:00 - Late but not unsuccessful
            isLate = true;
            issueDescription.append("Minor late arrival. ");
        }

        // Check departure time for early leaving
        if (departureTime.isBefore(LocalTime.of(14, 0)) && attendanceType == AttendanceType.FULL_DAY) {
            // Left before 2:00 PM - likely half day
            attendanceType = AttendanceType.HALF_DAY;
            issueDescription.append("Early departure - HALF DAY. ");
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

            // Handle complete IN/OUT pairs
            InOutEntity firstIn = pattern.getFirstInPunch();
            InOutEntity lastOut = pattern.getLastOutPunch();

            boolean isFullDay = pattern.getAttendanceType() == AttendanceType.FULL_DAY;
            boolean isHalfDay = pattern.getAttendanceType() == AttendanceType.HALF_DAY;
            boolean isAbsent = pattern.getAttendanceType() == AttendanceType.ABSENT;


            if (isAbsent) {
                reportAttendance(employee.getEmployeeId(), false, false, true, pattern.isUnsuccessful(),
                        pattern.isLate(), false, false, false, false, false, true, false, true, date);
            } else {
                reportAttendance(firstIn, lastOut, false, isFullDay, !pattern.isAuthorized(),
                        pattern.isUnsuccessful(), pattern.isLate(), !pattern.isUnsuccessful() && pattern.isLate(), isHalfDay, false, false,
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
        if (employeeEntity.isEmpty()) {
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
        attendance.setArrivalDate(helper.removeTimeFromDate(inout.getPunchTime()));
        if(swap)
            attendance.setLeftTime(inout.getPunchTypeTime());
        else
            attendance.setArrivalTime(inout.getPunchTypeTime());
        attendance.setIsActive(active);
        attendance.setIsLate(late);
        attendance.setIsLateCovered(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnauthorized(unAuthorized);

        handleAttendanceTypeAndIssues(
                swap ? null : inout,
                swap ? inout : null,
                attendance,
                swap,
                false,
                false,
                true,
                false,
                false,
                employee.getEmployeeId()
        );
        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);


        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if(!attendance.isArrivalOnWeekend())
            savedAttendance  = attendanceRepo.save(attendance);

        inout.setAttendance(savedAttendance);
        inOutRepo.save(inout);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(inout.getPunchTime()));

        if ((unSuccessful) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
            helper.handleLateAndUnsuccessful(employee.getEmployeeId(), savedAttendance, swap);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
    }

    @Override
    public void reportAttendance(InOutEntity moa, InOutEntity eve, Date date){
        if (moa.getEmployeeId() == null || eve.getEmployeeId() == null) {
            logger.warn("One or both InOut entities have null employee ID. Cannot proceed with attendance reporting.");
            return;
        }

        if (!moa.getEmployeeId().equals(eve.getEmployeeId())) {
            logger.warn("Employee IDs do not match between morning ({}) and evening ({}) records.",
                    moa.getEmployeeId(), eve.getEmployeeId());
            return;
        }

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(moa.getEmployeeId());
        if (employeeEntity.isEmpty()) {
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

        if(eve == null) return;
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

        AttendanceEntity attendance = createBaseAttendance(employee, date);
        attendance.setTerminalId(moa.getTerminalId() + " - " + eve.getTerminalId());
        attendance.setArrivalDate(helper.removeTimeFromDate(moa.getPunchTime()));
        attendance.setArrivalTime(moa.getPunchTypeTime());
        attendance.setLeftTime(eve.getPunchTypeTime());

        try {
            LocalTime arrivalTime = moa.getPunchTypeTime().toLocalTime();
            LocalTime departureTime = eve.getPunchTypeTime().toLocalTime();

            // Company policy time thresholds
            LocalTime standardArrival = LocalTime.of(8, 30);     // Expected arrival
            LocalTime lateThreshold = LocalTime.of(10, 0);       // Late but acceptable
            LocalTime veryLateThreshold = LocalTime.of(12, 0);   // Very late - half day
            LocalTime criticalThreshold = LocalTime.of(13, 0);   // Critical - full leave
            LocalTime standardDeparture = LocalTime.of(17, 0);   // Expected departure
            LocalTime noonTime = LocalTime.of(12, 0);            // Noon reference
            LocalTime earlyAfternoon = LocalTime.of(14, 0);      // Early afternoon

            // Calculate work duration
            Duration workDuration = Duration.between(arrivalTime, departureTime);
            long workHours = workDuration.toHours();
            long workMinutes = workDuration.toMinutes();

            logger.info("Employee: {} | Arrival: {} | Departure: {} | Work Duration: {} hours {} minutes",
                    employee.getEmployeeId(), arrivalTime, departureTime, workHours, workMinutes % 60);

            // COMPREHENSIVE ATTENDANCE LOGIC - All possible scenarios covered

            // SCENARIO 1: VERY EARLY DEPARTURE (Before noon) - Regardless of arrival time
            if (departureTime.isBefore(noonTime)) {
                if (workHours >= 4) {
                    // Worked at least 4 hours before noon - acceptable half day
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setIssueDescription(String.format("HALF DAY - LEFT AT %s AFTER WORKING %d HOURS",
                            departureTime.toString(), workHours));
                    attendance.setHasIssues(false);
                } else {
                    // Less than 4 hours - unauthorized early leave
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("UNAUTHORIZED EARLY LEAVE - LEFT AT %s AFTER ONLY %d HOURS",
                            departureTime.toString(), workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsUnauthorized(true);
                }
                attendance.setIsLate(arrivalTime.isAfter(standardArrival));

                // SCENARIO 2: AFTERNOON DEPARTURE (Noon to 2 PM) - Check if it's half day worthy
            } else if (departureTime.isBefore(earlyAfternoon)) {
                if (arrivalTime.isBefore(standardArrival) && workHours >= 4) {
                    // Came early, worked at least 4 hours - legitimate half day
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setIssueDescription(String.format("HALF DAY - WORKED %d HOURS (ARRIVED EARLY)", workHours));
                    attendance.setHasIssues(false);
                    attendance.setIsLate(false);
                } else if (arrivalTime.isBefore(lateThreshold) && workHours >= 4) {
                    // Came on time, worked at least 4 hours - acceptable half day
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setIssueDescription(String.format("HALF DAY - WORKED %d HOURS", workHours));
                    attendance.setHasIssues(false);
                    attendance.setIsLate(arrivalTime.isAfter(standardArrival));
                } else {
                    // Late arrival or insufficient hours
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("INSUFFICIENT WORK - ARRIVED %s, LEFT %s (%d HOURS)",
                            arrivalTime.toString(), departureTime.toString(), workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                }

                // SCENARIO 3: CRITICAL LATE ARRIVAL (After 1 PM) - Regardless of departure
            } else if (arrivalTime.isAfter(criticalThreshold)) {
                if (workHours >= 4 && departureTime.isAfter(standardDeparture)) {
                    // Very late but worked overtime to compensate
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("VERY LATE ARRIVAL (%s) BUT WORKED OVERTIME UNTIL %s",
                            arrivalTime.toString(), departureTime.toString()));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                } else {
                    // Very late arrival with insufficient compensation
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("CRITICAL LATE ARRIVAL - CAME AT %s, LEFT AT %s",
                            arrivalTime.toString(), departureTime.toString()));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                    attendance.setIsUnauthorized(true);
                }

                // SCENARIO 4: VERY LATE ARRIVAL (After noon but before 1 PM)
            } else if (arrivalTime.isAfter(veryLateThreshold)) {
                if (departureTime.isAfter(standardDeparture) && workHours >= 4) {
                    // Late but worked full hours with overtime
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setIssueDescription(String.format("LATE ARRIVAL (%s) COMPENSATED WITH OVERTIME UNTIL %s",
                            arrivalTime.toString(), departureTime.toString()));
                    attendance.setHasIssues(false);
                    attendance.setIsLate(true);
                } else {
                    // Late arrival without adequate compensation
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("LATE ARRIVAL - CAME AT %s, INSUFFICIENT HOURS (%d)",
                            arrivalTime.toString(), workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                }

                // SCENARIO 5: LATE BUT ACCEPTABLE (8:30 AM to 10:00 AM)
            } else if (arrivalTime.isAfter(standardArrival) && arrivalTime.isBefore(lateThreshold)) {
                if (departureTime.isAfter(standardDeparture)) {
                    // Late arrival but normal/overtime departure
                    long lateMinutes = Duration.between(standardArrival, arrivalTime).toMinutes();
                    Duration overtimeNeeded = Duration.ofMinutes(lateMinutes);
                    LocalTime requiredDeparture = standardDeparture.plus(overtimeNeeded);

                    if (departureTime.isAfter(requiredDeparture) || departureTime.equals(requiredDeparture)) {
                        // Fully compensated with overtime
                        attendance.setAttendanceType(AttendanceType.FULL_DAY);
                        attendance.setLeaveStatus(null);
                        attendance.setIssueDescription(String.format("LATE ARRIVAL COMPENSATED - WORKED UNTIL %s",
                                departureTime.toString()));
                        attendance.setHasIssues(false);
                    } else {
                        // Partially compensated
                        attendance.setAttendanceType(AttendanceType.FULL_DAY);
                        attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                        attendance.setIssueDescription(String.format("LATE ARRIVAL PARTIALLY COMPENSATED - NEED TO WORK UNTIL %s",
                                requiredDeparture.toString()));
                        attendance.setHasIssues(true);
                    }
                    attendance.setIsLate(true);
                } else {
                    // Late arrival and early departure
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("LATE ARRIVAL (%s) AND EARLY DEPARTURE (%s)",
                            arrivalTime.toString(), departureTime.toString()));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                }

                // SCENARIO 6: MODERATELY LATE (10:00 AM to noon)
            } else if (arrivalTime.isAfter(lateThreshold)) {
                if (departureTime.isAfter(standardDeparture) && workHours >= 6) {
                    // Moderately late but worked adequate hours
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setIssueDescription(String.format("MODERATELY LATE ARRIVAL - WORKED %d HOURS WITH OVERTIME", workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                } else {
                    // Moderately late with insufficient hours
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("MODERATELY LATE - INSUFFICIENT WORK HOURS (%d)", workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(true);
                }

                // SCENARIO 7: ON TIME OR EARLY ARRIVAL
            } else {
                if (departureTime.isAfter(standardDeparture) || departureTime.equals(standardDeparture)) {
                    // Perfect attendance
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setDueDateForUA(null);
                    if (arrivalTime.isBefore(LocalTime.of(8, 0))) {
                        attendance.setIssueDescription(String.format("EXCELLENT ATTENDANCE - EARLY ARRIVAL (%s)", arrivalTime.toString()));
                    } else {
                        attendance.setIssueDescription(null);
                    }
                    attendance.setHasIssues(false);
                    attendance.setIsLate(false);
                    attendance.setIsUnauthorized(false);
                    attendance.setIsUnSuccessful(false);
                } else if (departureTime.isAfter(noonTime) && workHours >= 6) {
                    // On time arrival but early departure with adequate hours
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setIssueDescription(String.format("EARLY DEPARTURE - LEFT AT %s AFTER %d HOURS",
                            departureTime.toString(), workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(false);
                } else if (departureTime.isAfter(noonTime) && workHours >= 4) {
                    // On time arrival, early departure - half day
                    attendance.setAttendanceType(AttendanceType.HALF_DAY);
                    attendance.setLeaveStatus(null);
                    attendance.setIssueDescription(String.format("HALF DAY - WORKED %d HOURS", workHours));
                    attendance.setHasIssues(false);
                    attendance.setIsLate(false);
                } else {
                    // On time arrival but very early departure
                    attendance.setAttendanceType(AttendanceType.FULL_DAY);
                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("VERY EARLY DEPARTURE - ONLY WORKED %d HOURS", workHours));
                    attendance.setHasIssues(true);
                    attendance.setIsLate(false);
                    attendance.setIsUnauthorized(true);
                }
            }

            // Set default values for fields not explicitly set
            if (attendance.getIsUnauthorized() == null) {
                attendance.setIsUnauthorized(false);
            }
            if (attendance.getIsUnSuccessful() == null) {
                attendance.setIsUnSuccessful(attendance.getHasIssues());
            }

            logger.info("Final: Employee: {} | Arrival: {} | Departure: {} | Type: {} | Leave: {} | Late: {} | Issues: {} | Description: {}",
                    employee.getEmployeeId(), arrivalTime, departureTime,
                    attendance.getAttendanceType(), attendance.getLeaveStatus(),
                    attendance.getIsLate(), attendance.getHasIssues(), attendance.getIssueDescription());

        } catch (Exception e) {
            logger.error("Error handling attendance type and issues for employee: {}", moa.getEmployeeId(), e);
            // Set default values in case of error
            attendance.setHasIssues(true);
            attendance.setIssueDescription("ERROR IN ATTENDANCE PROCESSING: " + e.getMessage());
            attendance.setIsUnauthorized(true);
            attendance.setDueDateForUA(helper.getDueDate());
        }



        AttendanceEntity savedAttendance = null;
        if ((!attendance.isArrivalOnWeekend()) && (!attendance.getArrivalTime().equals(attendance.getLeftTime()))) {
            savedAttendance = attendanceRepo.save(attendance);
            logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
        } else {
            logger.info("Attendance not saved - weekend or same arrival/departure time for employee: {}", employee.getEmployeeId());
        }

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave);

        if (savedAttendance != null) {
            updateInOutRelationships(moa, eve, savedAttendance);
        }
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
        if (employeeEntity.isEmpty()) {
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
        if(eve == null) return;
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

        AttendanceEntity attendance = createBaseAttendance(employee, date);
        attendance.setTerminalId(moa.getTerminalId() + " - " + eve.getTerminalId());
        attendance.setArrivalDate(helper.removeTimeFromDate(moa.getPunchTime()));
        attendance.setArrivalTime(moa.getPunchTypeTime());
        attendance.setLeftTime(eve.getPunchTypeTime());
        attendance.setIsActive(active);
        attendance.setIsLate(late);
        attendance.setIsLateCovered(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnauthorized(unAuthorized);

        handleAttendanceTypeAndIssues(moa, eve, attendance,
                false, true, false, false, false, false, employee.getEmployeeId());

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if( (!attendance.isArrivalOnWeekend()) && (!attendance.getArrivalTime().equals(attendance.getLeftTime())))
            savedAttendance = attendanceRepo.save(attendance);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
        updateInOutRelationships(moa, eve, savedAttendance);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(moa.getPunchTime()));

        if ((unSuccessful) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
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
        EmployeeEntity employee = employeeRepo.findByEmployeeId(employeeID)
                .or(() -> employeeRepo.findBySltId(employeeID))
                .or(() -> employeeRepo.findByPublicId(employeeID))
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

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if(!attendance.isArrivalOnWeekend())
            savedAttendance = attendanceRepo.save(attendance);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.getYesterdayDate());

        if ((unSuccessful) && ((savedAttendance.getAttendanceType() != null) && (!savedAttendance.getAttendanceType().equals(AttendanceType.HALF_DAY))) && (unAuthorized == false))
            helper.handleLateAndUnsuccessful(employeeID, savedAttendance, swap);
    }

    public void handleLeave(List<LeaveEntity> leave) {
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
                leaveEntity.setNotUsed(true);
                leaveEntity.setRequestStatus(RequestStatus.CANCELLED);
                leaveEntity.setDescription("CAME TO WORK EVEN THOUGH TODAY YOU MAKE A LEAVE BUT YOU CAME TO WORK IN FORM OF A HALF DAY");
                leaveRepo.save(leaveEntity);
            } catch (Exception e) {
                logger.error("Error processing leave entity: {}", leaveEntity.getId(), e);
            }
        }
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

    private void handleAttendanceTypeAndIssues(InOutEntity inPunch, InOutEntity outPunch, AttendanceEntity attendance,
                                               Boolean swap, Boolean fullday, Boolean half_day,
                                               Boolean unAuthorized, Boolean unSuccessful, Boolean absent, String employeeId) {

        if (attendance == null) {
            logger.error("Null attendance provided to handleAttendanceTypeAndIssues");
            return;
        }

        try {
            // Handle special flags first (highest priority)
            if (Boolean.TRUE.equals(absent)) {
                attendance.setAttendanceType(AttendanceType.ABSENT);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setHasIssues(true);
                attendance.setIsUnauthorized(false);
                attendance.setIsUnSuccessful(false);
                attendance.setIsLate(false);
                attendance.setLeaveStatus(null);
                attendance.setIssueDescription("ABSENT - NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE DUE DATE.");
                logger.info("Employee {} marked as ABSENT", employeeId);
                return;
            }

            if (Boolean.TRUE.equals(unAuthorized)) {
                // Unauthorized means missing IN or OUT punch or sequence error
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setIsUnSuccessful(false);

                if (Boolean.TRUE.equals(swap)) {
                    attendance.setIssueDescription("UNAUTHORIZED - WRONG PUNCH SEQUENCE (OUT BEFORE IN). RESOLVE BEFORE DUE DATE.");
                } else if (inPunch == null && outPunch != null) {
                    attendance.setIssueDescription("UNAUTHORIZED - MISSING IN PUNCH, ONLY OUT PUNCH FOUND. RESOLVE BEFORE DUE DATE.");
                } else if (inPunch != null && outPunch == null) {
                    attendance.setIssueDescription("UNAUTHORIZED - MISSING OUT PUNCH, ONLY IN PUNCH FOUND. RESOLVE BEFORE DUE DATE.");
                } else {
                    attendance.setIssueDescription("UNAUTHORIZED - PUNCH DATA ERROR. RESOLVE BEFORE DUE DATE.");
                }
                logger.warn("Employee {} marked as UNAUTHORIZED: {}", employeeId, attendance.getIssueDescription());
                return;
            }

            // For valid punch scenarios, we need both IN and OUT or explicit flags
            if (inPunch == null || inPunch.getPunchTypeTime() == null) {
                logger.warn("No valid IN punch for employee: {}", employeeId);
                // Treat as unauthorized if not explicitly handled above
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setIssueDescription("MISSING OR INVALID IN PUNCH DATA");
                return;
            }

            LocalTime arrivalTime = inPunch.getPunchTypeTime().toLocalTime();
            LocalTime departureTime = null;
            Duration workDuration = null;
            long workHours = 0;
            boolean hasValidOutPunch = (outPunch != null && outPunch.getPunchTypeTime() != null);

            if (hasValidOutPunch) {
                departureTime = outPunch.getPunchTypeTime().toLocalTime();
                workDuration = Duration.between(arrivalTime, departureTime);
                workHours = workDuration.toHours();
            }

            // Company policy time thresholds
            LocalTime standardArrival = LocalTime.of(8, 30);
            LocalTime lateThreshold = LocalTime.of(10, 0);
            LocalTime veryLateThreshold = LocalTime.of(12, 0);
            LocalTime criticalThreshold = LocalTime.of(13, 0);
            LocalTime standardDeparture = LocalTime.of(17, 0);
            LocalTime noonTime = LocalTime.of(12, 0);

            logger.debug("Processing employee {}: Arrival={}, Departure={}, WorkHours={}, Flags: fullday={}, half_day={}, unSuccessful={}",
                    employeeId, arrivalTime, departureTime, workHours, fullday, half_day, unSuccessful);

            // EXPLICIT FLAG HANDLING - Override time-based logic when flags are set
            if (Boolean.TRUE.equals(half_day)) {
                // Explicitly marked as half day
                attendance.setAttendanceType(AttendanceType.HALF_DAY);
                attendance.setLeaveStatus(null);
                attendance.setIsLate(arrivalTime.isAfter(standardArrival));

                if (hasValidOutPunch && workHours >= 4) {
                    attendance.setHasIssues(false);
                    attendance.setIssueDescription(String.format("APPROVED HALF DAY - WORKED %d HOURS", workHours));
                    attendance.setDueDateForUA(null);
                    attendance.setIsUnauthorized(true);
                } else if (hasValidOutPunch) {
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("HALF DAY BUT INSUFFICIENT HOURS (%d) - VERIFY ATTENDANCE", workHours));
                    attendance.setIsUnauthorized(true);
                } else {
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription("HALF DAY APPROVED BUT NO OUT PUNCH - VERIFY ATTENDANCE");
                    attendance.setIsUnauthorized(true);
                }

            } else if (Boolean.TRUE.equals(fullday)) {
                // Explicitly marked as full day
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                attendance.setIsLate(arrivalTime.isAfter(standardArrival));

                if (hasValidOutPunch && workHours >= 8) {
                    // Full day with adequate hours
                    attendance.setLeaveStatus(null);
                    attendance.setHasIssues(false);
                    attendance.setDueDateForUA(null);
                    if (arrivalTime.isAfter(standardArrival)) {
                        attendance.setIssueDescription(String.format("FULL DAY COMPLETED DESPITE LATE ARRIVAL (%s)", arrivalTime.toString()));
                    } else {
                        attendance.setIssueDescription(null);
                    }
                } else if (hasValidOutPunch && workHours >= 6) {
                    // Full day but short hours
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("FULL DAY BUT ONLY WORKED %d HOURS - SHORT LEAVE APPLIED", workHours));
                    attendance.setIsUnauthorized(true);
                } else if (hasValidOutPunch) {
                    // Full day but very short hours
                    attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription(String.format("FULL DAY MARKED BUT ONLY WORKED %d HOURS - FULL LEAVE", workHours));
                    attendance.setIsUnauthorized(true);
                } else {
                    // Full day but no out punch
                    attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                    attendance.setHasIssues(true);
                    attendance.setIssueDescription("FULL DAY MARKED BUT NO OUT PUNCH - VERIFY DEPARTURE TIME");
                    attendance.setIsUnauthorized(true);
                }

            } else {
                // NO EXPLICIT FLAGS - Use time-based logic

                if (!hasValidOutPunch) {
                    // Only IN punch, no OUT punch - Unauthorized
                    attendance.setHasIssues(true);
                    attendance.setIsUnauthorized(true);
                    attendance.setIsLate(arrivalTime.isAfter(standardArrival));
                    attendance.setDueDateForUA(helper.getDueDate());
                    attendance.setIssueDescription(String.format("MISSING OUT PUNCH - ARRIVED AT %s BUT NO DEPARTURE RECORD", arrivalTime.toString()));

                } else {
                    // Both IN and OUT punches available - Apply comprehensive logic

                    if (arrivalTime.isAfter(criticalThreshold)) {
                        // Critical late arrival (after 1 PM)
                        if (workHours >= 4 && departureTime.isAfter(standardDeparture)) {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("CRITICAL LATE ARRIVAL (%s) WITH OVERTIME COMPENSATION", arrivalTime.toString()));
                            attendance.setHasIssues(true);
                        } else {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                            attendance.setIssueDescription(String.format("CRITICAL LATE ARRIVAL (%s) - INSUFFICIENT COMPENSATION", arrivalTime.toString()));
                            attendance.setHasIssues(true);
                            attendance.setIsUnauthorized(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(veryLateThreshold)) {
                        // Very late arrival (after noon)
                        if (workHours >= 4) {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setIssueDescription(String.format("VERY LATE ARRIVAL BUT WORKED %d HOURS - HALF DAY", workHours));
                            attendance.setHasIssues(false);
                        } else {
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("VERY LATE ARRIVAL - INSUFFICIENT HOURS (%d)", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsUnauthorized(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(lateThreshold)) {
                        // Moderate late arrival (after 10 AM)
                        if (workHours >= 6 && departureTime.isAfter(standardDeparture)) {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("LATE ARRIVAL PARTIALLY COMPENSATED - WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                        } else {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("MODERATE LATE ARRIVAL - INSUFFICIENT COMPENSATION", workHours));
                            attendance.setHasIssues(true);
                        }
                        attendance.setIsLate(true);

                    } else if (arrivalTime.isAfter(standardArrival)) {
                        // Minor late arrival (after 8:30 AM)
                        if (departureTime.isAfter(standardDeparture)) {
                            long lateMinutes = Duration.between(standardArrival, arrivalTime).toMinutes();
                            LocalTime requiredDeparture = standardDeparture.plusMinutes(lateMinutes);

                            if (departureTime.isAfter(requiredDeparture) || departureTime.equals(requiredDeparture)) {
                                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                                attendance.setLeaveStatus(null);
                                attendance.setIssueDescription("LATE ARRIVAL FULLY COMPENSATED WITH OVERTIME");
                                attendance.setHasIssues(false);
                                attendance.setDueDateForUA(null);
                            } else {
                                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                                attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                                attendance.setIssueDescription("LATE ARRIVAL PARTIALLY COMPENSATED");
                                attendance.setHasIssues(true);
                            }
                        } else {
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription("LATE ARRIVAL WITHOUT COMPENSATION");
                            attendance.setHasIssues(true);
                            attendance.setDueDateForUA(helper.getDueDate());
                        }
                        attendance.setIsLate(true);

                    } else {
                        // On-time or early arrival
                        if (departureTime.isAfter(standardDeparture) || departureTime.equals(standardDeparture)) {
                            // Perfect attendance
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setHasIssues(false);
                            attendance.setIsLate(false);
                            attendance.setDueDateForUA(null);
                            attendance.setIssueDescription(null);
                        } else if (workHours >= 6) {
                            // Early departure but adequate hours
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
                            attendance.setIssueDescription(String.format("EARLY DEPARTURE BUT WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsLate(false);
                        } else if (workHours >= 4) {
                            // Half day scenario
                            attendance.setAttendanceType(AttendanceType.HALF_DAY);
                            attendance.setLeaveStatus(null);
                            attendance.setIssueDescription(String.format("HALF DAY - WORKED %d HOURS", workHours));
                            attendance.setHasIssues(false);
                            attendance.setIsLate(false);
                        } else {
                            // Very early departure
                            attendance.setAttendanceType(AttendanceType.FULL_DAY);
                            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
                            attendance.setIssueDescription(String.format("VERY EARLY DEPARTURE - ONLY WORKED %d HOURS", workHours));
                            attendance.setHasIssues(true);
                            attendance.setIsLate(false);
                            attendance.setIsUnauthorized(true);
                            attendance.setDueDateForUA(helper.getDueDate());
                        }
                    }
                }
            }

            // Apply unsuccessful flag if explicitly set
            if (Boolean.TRUE.equals(unSuccessful)) {
                attendance.setIsUnSuccessful(true);
                attendance.setHasIssues(true);
                if (attendance.getIssueDescription() == null || attendance.getIssueDescription().isEmpty()) {
                    attendance.setIssueDescription("MARKED AS UNSUCCESSFUL DUE TO ATTENDANCE ISSUES");
                } else {
                    attendance.setIssueDescription(attendance.getIssueDescription() + " - UNSUCCESSFUL");
                }
            }

            // Set default values for unset fields
            if (attendance.getIsUnauthorized() == null) {
                attendance.setIsUnauthorized(false);
            }
            if (attendance.getIsUnSuccessful() == null) {
                attendance.setIsUnSuccessful(attendance.getHasIssues() != null ? attendance.getHasIssues() : false);
            }
            if (attendance.getIsLate() == null) {
                attendance.setIsLate(false);
            }

            logger.info("Employee {}: Final attendance - Type: {}, Leave: {}, Late: {}, Issues: {}, Unauthorized: {}, Description: {}",
                    employeeId, attendance.getAttendanceType(), attendance.getLeaveStatus(),
                    attendance.getIsLate(), attendance.getHasIssues(), attendance.getIsUnauthorized(),
                    attendance.getIssueDescription());

        } catch (Exception e) {
            logger.error("Error handling attendance type and issues for employee: {}", employeeId, e);
            // Set safe defaults in case of error
            attendance.setHasIssues(true);
            attendance.setIssueDescription("ERROR IN ATTENDANCE PROCESSING: " + e.getMessage());
            attendance.setIsUnauthorized(true);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setAttendanceType(AttendanceType.NONE);
            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
        }
    }

    private void handleLeaveStatus(AttendanceEntity attendance, Boolean leaveSuccess, Boolean leaveReq, Boolean isFullLeave) {
        if (attendance == null) {
            logger.error("Null attendance provided to handleLeaveStatus");
            return;
        }

        try {
            if (Boolean.TRUE.equals(isFullLeave)) {
                attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
            } else if (Boolean.TRUE.equals(leaveSuccess)) {
                attendance.setLeaveStatus(LeaveStatus.LEAVE_APPROVED);
            } else if (Boolean.TRUE.equals(leaveReq)) {
                attendance.setLeaveStatus(LeaveStatus.LEAVE_REQUESTED);
            }
        } catch (Exception e) {
            logger.error("Error handling leave status for attendance: {}", attendance.getId(), e);
        }
    }

    private void updateInOutRelationships(InOutEntity moa, InOutEntity eve, AttendanceEntity savedAttendance) {
        if (savedAttendance == null) {
            logger.error("Null attendance provided to updateInOutRelationships");
            return;
        }

        try {

            if (moa != null && moa.getId() != null) {
                Optional<InOutEntity> moaEntity = inOutRepo.findById(moa.getId());
                if (moaEntity.isPresent()) {
                    InOutEntity managedMoa = moaEntity.get();
                    managedMoa.setAttendance(savedAttendance);
                    inOutRepo.save(managedMoa);
                } else {
                    logger.warn("Morning inout entity not found for ID: {}", moa.getId());
                }
            }

            if (eve != null && eve.getId() != null) {
                Optional<InOutEntity> eveEntity = inOutRepo.findById(eve.getId());
                if (eveEntity.isPresent()) {
                    InOutEntity managedEve = eveEntity.get();
                    managedEve.setAttendance(savedAttendance);
                    inOutRepo.save(managedEve);
                } else {
                    logger.warn("Evening inout entity not found for ID: {}", eve.getId());
                }
            }

            logger.info("InOut relationships established for attendance: {}", savedAttendance.getId());

        } catch (Exception e) {
            logger.error("Error establishing InOut relationships for attendance: {}", savedAttendance.getId(), e);
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

        if (name == null || employeeId == null || name.isEmpty()  || employeeId.isEmpty())
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        if (!req.getUserId().equals(name))
            throw new IllegalArgumentException("Failed to make leave movement request");

        UserLeaveTypeRemainingEntity userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(req.getLeaveType(), employeeId);

        if ((userLeaveTypeRemaining == null || userLeaveTypeRemaining.getRemainingLeaves() == null || userLeaveTypeRemaining.getRemainingLeaves() <= 0 ) && (userLeaveTypeRemaining.getRemainingLeaves() != -1)) {
            throw new IllegalArgumentException("No remaining leaves available for this leave type");
        }
        final String leaveId = utils.generateId(10);
        LeaveEntity leaveEntity = transformToEntity(req, employee.getSltId(), leaveId, leaveTypeRepository);
        leaveEntity.setRequestStatus(RequestStatus.PENDING_APPROVAL);

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {

            Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndHappenDate(employee, helper.removeTimeFromDate(req.getHappenDate()));
            if (leave.isPresent())
                throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndArrivalDateAndIsActiveTrue(
                    employee, leaveEntity.getHappenDate());

            if (attendanceEntityOp.isEmpty()) {
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
        if (req.getIsManualRequest()) {

            String token = "Bearer " + extractJwtTokenFromCookie(request);
            if (token == null || token.isEmpty()) throw new IllegalArgumentException("AUTH TOKEN NOT FOUND");

            final List<UserRest> admins;
            synchronized (adminFetchLockForL) {
                admins = fetchAdminsWithResilience(req.getUserId(), token);
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
        }

        synchronized (this) {
            lmsService.saveLeave(leaveEntity);
        }

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {
            processUnauthorizedLeave(leaveEntity, employeeId);
        }
    }


    private void processUnauthorizedLeave(LeaveEntity leaveEntity, String employeeId) {
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
            throw new DataAccessException("Database operation failed", e) {};
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

    public static class NoPayRequest {
        private final boolean isHalfDay;
        private final boolean unAuthorized;
        private final boolean isUnsuccessful;
        private final boolean isLate;
        private final boolean isLateCover;
        private final boolean isAbsent;

        public NoPayRequest(boolean isHalfDay, boolean unAuthorized, boolean isUnsuccessful,
                            boolean isLate, boolean isLateCover, boolean isAbsent) {
            this.isHalfDay = isHalfDay;
            this.unAuthorized = unAuthorized;
            this.isUnsuccessful = isUnsuccessful;
            this.isLate = isLate;
            this.isLateCover = isLateCover;
            this.isAbsent = isAbsent;
        }

        public boolean isHalfDay() {
            return isHalfDay;
        }

        public boolean isUnAuthorized() {
            return unAuthorized;
        }

        public boolean isUnsuccessful() {
            return isUnsuccessful;
        }

        public boolean isLate() {
            return isLate;
        }

        public boolean isLateCover() {
            return isLateCover;
        }

        public boolean isAbsent() {
            return isAbsent;
        }
    }

    private static class AttendanceAnalysis {
        final AttendanceType attendanceType;
        final boolean isAuthorized;
        final boolean isLate;
        final boolean isUnsuccessful;
        final String issueDescription;

        public AttendanceAnalysis(AttendanceType attendanceType, boolean isAuthorized,
                                  boolean isLate, boolean isUnsuccessful, String issueDescription) {
            this.attendanceType = attendanceType;
            this.isAuthorized = isAuthorized;
            this.isLate = isLate;
            this.isUnsuccessful = isUnsuccessful;
            this.issueDescription = issueDescription;
        }
    }

    private static class EmployeePunchPattern {
        private final String employeeId;
        private final List<InOutEntity> allPunches;
        private final InOutEntity firstInPunch;
        private final InOutEntity lastOutPunch;
        private final AttendanceType attendanceType;
        private final boolean isAuthorized;
        private final boolean isLate;
        private final boolean isUnsuccessful;
        private final String issueDescription;

        public EmployeePunchPattern(String employeeId, List<InOutEntity> allPunches,
                                    InOutEntity firstInPunch, InOutEntity lastOutPunch,
                                    AttendanceType attendanceType, boolean isAuthorized,
                                    boolean isLate, boolean isUnsuccessful, String issueDescription) {
            this.employeeId = employeeId;
            this.allPunches = new ArrayList<>(allPunches);
            this.firstInPunch = firstInPunch;
            this.lastOutPunch = lastOutPunch;
            this.attendanceType = attendanceType;
            this.isAuthorized = isAuthorized;
            this.isLate = isLate;
            this.isUnsuccessful = isUnsuccessful;
            this.issueDescription = issueDescription;
        }

        public String getEmployeeId() { return employeeId; }
        public List<InOutEntity> getAllPunches() { return allPunches; }
        public InOutEntity getFirstInPunch() { return firstInPunch; }
        public InOutEntity getLastOutPunch() { return lastOutPunch; }
        public AttendanceType getAttendanceType() { return attendanceType; }
        public boolean isAuthorized() { return isAuthorized; }
        public boolean isLate() { return isLate; }
        public boolean isUnsuccessful() { return isUnsuccessful; }
        public String getIssueDescription() { return issueDescription; }



        public boolean hasValidInOutPair() {
            return firstInPunch != null && lastOutPunch != null &&
                    !firstInPunch.getPunchTypeTime().equals(lastOutPunch.getPunchTypeTime());
        }
    }

}