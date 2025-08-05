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
            allInOut = inOutRepo.findByEmployeeIdAndDate(employeeEntity.getSltId(), date)
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
                .filter(InOutDTO::isMorning)
                .filter(dto -> dto.getPunchTypeTime() != null)
                .min(Comparator.comparing(InOutDTO::getPunchTypeTime))
                .ifPresent(dto -> result.put("morning", dto));

        allInOut.stream()
                .filter(Objects::nonNull)
                .filter(InOutDTO::isEvening)
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

            if (reqDate.isPresent())
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

            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndDateAndIsActiveTrue(
                    employee, movementsEntity.getHappenDate());

            if (attendanceEntity.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            AttendanceEntity attendance = attendanceEntity.get();

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
                .inTime(movementReq.getInTime() == null ? "00:00:00" : movementReq.getInTime())
                .outTime(movementReq.getInTime() == null ? "00:00:00" : movementReq.getOutTime()) // Fixed this line
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

        if (movement.getRequestStatus().equals(RequestStatus.REJECTED) || movement.getRequestStatus().equals(RequestStatus.APPROVED) || movement.getRequestStatus().equals(RequestStatus.CANCELLED))
            return;

        AttendanceEntity attendance = movement.getAttendance();
        if (attendance == null) {
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

            switch (movement.getMovementType()) {
                case HOME_TO_OFFICE:
                    if (movement.getInTime() != null) {
                        attendance.setArrivalTime(helper.parseToSqlTime(movement.getInTime()));
                    }
                    break;
                case OFFICE_TO_HOME:
                    if (movement.getOutTime() != null) {
                        attendance.setLeftTime(helper.parseToSqlTime(movement.getOutTime()));
                    }
                    break;
                default:
                    if (movement.getInTime() != null) {
                        attendance.setArrivalTime(helper.parseToSqlTime(movement.getInTime()));
                    }
                    if (movement.getOutTime() != null) {
                        attendance.setLeftTime(helper.parseToSqlTime(movement.getOutTime()));
                    }
            }


            attendanceRepo.save(attendance);
            movementsRepo.save(movement);
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

    private String getSafeEmployeeId(InOutEntity entity) {
        return (entity != null && entity.getEmployeeId() != null) ? entity.getEmployeeId().trim() : null;
    }

    public Map<String, InOutEntity> findEarliestMorningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyMap();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .filter(entity -> {
                    String empId = getSafeEmployeeId(entity);
                    return empId != null && !empId.isEmpty() && entity.getPunchTime() != null;
                })
                .collect(Collectors.groupingBy(
                        InOutEntity::getEmployeeId,
                        Collectors.minBy(Comparator.comparing(InOutEntity::getPunchTime))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(),
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }

    public Map<String, InOutEntity> findEarliestEveningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyMap();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .filter(entity -> {
                    String empId = getSafeEmployeeId(entity);
                    return empId != null && !empId.isEmpty() && entity.getPunchTime() != null;
                })
                .collect(Collectors.groupingBy(
                        InOutEntity::getEmployeeId,
                        Collectors.maxBy(Comparator.comparing(InOutEntity::getPunchTime))
                ))
                .entrySet().stream()
                .filter(entry -> entry.getKey() != null && entry.getValue().isPresent())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get(),
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }

    public Map<String, InOutEntity> processLateArrivals(List<InOutEntity> employeesArrivedAfter900) {
        return (employeesArrivedAfter900 == null || employeesArrivedAfter900.isEmpty()) ?
                Collections.emptyMap() :
                findEarliestMorningPunchByEmployee(employeesArrivedAfter900);
    }

    public Map<String, InOutEntity> processEmployeesArrivedBetween830And900(
            List<InOutEntity> employeesArrivedBetween830And900) {
        return (employeesArrivedBetween830And900 == null || employeesArrivedBetween830And900.isEmpty()) ?
                Collections.emptyMap() :
                findEarliestMorningPunchByEmployee(employeesArrivedBetween830And900);
    }

    public Map<String, InOutEntity[]> findEmployeesWithBothPunches(
            Map<String, InOutEntity> morningMap, Map<String, InOutEntity> eveningMap) {

        if (morningMap == null || eveningMap == null || morningMap.isEmpty() || eveningMap.isEmpty()) {
            return Collections.emptyMap();
        }

        return morningMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> {
                    InOutEntity eveningPunch = eveningMap.get(entry.getKey());
                    return eveningPunch != null;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new InOutEntity[]{entry.getValue(), eveningMap.get(entry.getKey())},
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
    }

    public Map<String, InOutEntity> findEmployeesWithOnlyOnePunches(
            Map<String, InOutEntity> morningMap, Map<String, InOutEntity> eveningMap) {

        if (morningMap == null || morningMap.isEmpty()) {
            return Collections.emptyMap();
        }

        final Map<String, InOutEntity> safeEveningMap = eveningMap != null ? eveningMap : Collections.emptyMap();

        return morningMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty())
                .filter(entry -> entry.getValue() != null)
                .filter(entry -> !safeEveningMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));
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

    private boolean checkHalfDay(Time morningPunch) {
        if (morningPunch == null) {
            return false;
        }
        Time halfDayThreshold = new Time(12, 30, 0);
        return morningPunch.after(halfDayThreshold);
    }

    @Override
    public void prerequisite() {

        LocalDate yesterday = LocalDate.now().minusDays(1);
        if(holidayRepo.existsByHolidayDate(yesterday)){
            handleHolidays();
        }

        LocalDateTime yesterdayBefore830 = LocalDate.now().minusDays(1).atTime(8, 30);
        
        LocalTime eveStart = LocalTime.of(17, 0);
        LocalTime eveStart_ = LocalTime.of(17, 30);

        LocalTime eveEnd = LocalTime.of(23, 59);

        Time sqlTime830 = Time.valueOf(yesterdayBefore830.toLocalTime());

        Time timeEveStart = Time.valueOf(eveStart);
        Time timeEveStart_ = Time.valueOf(eveStart_);

        Time timeEveEnd = Time.valueOf(eveEnd);


        Date yesterdayDate = helper.getYesterdayDate();

        List<InOutEntity> employeesArrivedBefore830 = inOutRepo.findByDateAndPunchTypeTimeBefore(yesterdayDate,
                sqlTime830);
        List<InOutEntity> employeesArrivedAfter830 = inOutRepo.findByDateAndPunchTypeTimeAfter(yesterdayDate,
                sqlTime830);

        /// ************************************************************************************************

        List<InOutEntity> employeesLeftAfter5 = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
                timeEveStart, timeEveEnd);
        List<InOutEntity> employeesLeftAfter530 = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
                timeEveStart_, timeEveEnd);

        List<InOutEntity> employeesLeftAfter50_30 = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
                timeEveStart, Time.valueOf(LocalTime.of(17, 29)));


        /// ************************************************************************************************

        Map<String, InOutEntity> earliestMorningPunchByEmployee = findEarliestMorningPunchByEmployee(
                employeesArrivedBefore830); /// PUNCH BEFORE 8.30.am
        Map<String, InOutEntity> earliestMorningPunchByEmployee_ = findEarliestMorningPunchByEmployee(
                employeesArrivedAfter830); /// PUNCH AFTER 8.30.am

        Map<String, InOutEntity> earliestEveningPunchByEmployee = findEarliestEveningPunchByEmployee(
                employeesLeftAfter5); /// PUNCH BETWEEN 5.00pm - 23.59pm
        Map<String, InOutEntity> earliestEveningPunchByEmployee_ = findEarliestEveningPunchByEmployee(
                employeesLeftAfter530); /// PUNCH BETWEEN 5.30pm - 23.59pm

        Map<String, InOutEntity> earliestEveningPunchByEmployee_5_30 = findEarliestEveningPunchByEmployee(
                employeesLeftAfter50_30); /// PUNCH BETWEEN 5.00pm - 5.30pm

        /// =====================================================================================
        /// =====================================================================================
        /// =====================================================================================
        /// FULL DAY

        Map<String, InOutEntity[]> employeesWithBothPunches = findEmployeesWithBothPunches(
                earliestMorningPunchByEmployee, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunches.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue()[0];
                InOutEntity eveningPunch = entry.getValue()[1];
                reportAttendance(morningPunch, eveningPunch, false, true, false, false, false, false, false, false, false,
                        false, true, false, false, yesterdayDate);
            });
        }

        /// ***************************************************************************************************************

        /// ==============================================================================================
        /// ==============================================================================================
        /// ==============================================================================================

        /// Late Arrive 8.30 (AFTER) Am but do the did not late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLateNot = findEmployeesWithBothPunches(
                earliestMorningPunchByEmployee_, earliestEveningPunchByEmployee_5_30);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLateNot.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue()[0];
                InOutEntity eveningPunch = entry.getValue()[1];
                boolean punchTimeAfter1730 = !eveningPunch.isPunchTimeAfter1730();
                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, punchTimeAfter1730, false, false, false,
                        false, true, false, false, yesterdayDate);

            });
        }

        /// =====================================================================================
        /// =====================================================================================
        /// =====================================================================================

        /// Late Arrive 8.30 Am (AFTER) but do the late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLate = findEmployeesWithBothPunches(earliestMorningPunchByEmployee_,
                earliestEveningPunchByEmployee_);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLate.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue()[0];
            InOutEntity eveningPunch = entry.getValue()[1];

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {

                reportAttendance(morningPunch, eveningPunch, false,true, false, false, true, true, false, false, false, false,
                        true, false, false, yesterdayDate);
            });

        }


        /*Map<String, InOutEntity> onlyMorningPunchesLate90 = findEmployeesWithOnlyOnePunches(earliestMorningPunchByEmployee_,
                earliestEveningPunchByEmployee_);

        for (Map.Entry<String, InOutEntity> entry : onlyMorningPunchesLate90.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue();

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, false,false, false, true, true, false, false, false, false, false, true,
                        false, false, yesterdayDate);
            });

        }*/

        /// ***************************************************************************************************************



        /// =====================================================================================
        /// =====================================================================================
        /// =====================================================================================
        /// Un-Authorized

        /// ================= Un-Authorized -- 8.30 === > BEFORE

        Map<String, InOutEntity> employeesWithOnlyMorningPunches = findEmployeesWithOnlyOnePunches(
                earliestMorningPunchByEmployee, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnlyMorningPunches.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                reportAttendance(morningPunch, false,false, true, false, false, false, false, false, false, false, true,
                        false, false, yesterdayDate);
            });
        }

        Map<String, InOutEntity> employeesWithOnlyMorningPunches_ = findEmployeesWithOnlyOnePunches(
                earliestEveningPunchByEmployee, earliestMorningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnlyMorningPunches_.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                reportAttendance(morningPunch, true,false, true, false, false, false, false, false, false, false, true,
                        false, false, yesterdayDate);
            });
        }

        /// ================= Un-Authorized -- 8.30 === > AFTER

        Map<String, InOutEntity> employeesWithOnly830Punches = findEmployeesWithOnlyOnePunches(
                earliestMorningPunchByEmployee_, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnly830Punches.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                reportAttendance(morningPunch, false,false, true, false, false, false, false, false, false, false, true,
                        false, false, yesterdayDate);
            });
        }

        Map<String, InOutEntity> employeesWithOnly830Punches_ = findEmployeesWithOnlyOnePunches(
                earliestEveningPunchByEmployee, earliestMorningPunchByEmployee_);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnly830Punches_.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                reportAttendance(morningPunch, true,false, true, false, false, false, false, false, false, false, true,
                        false, false, yesterdayDate);
            });
        }
        /// ***************************************************************************************************************

        /// ================================ ABSENT
        getAbsentEmployeesToday().forEach(employee -> {
            reportAttendance(employee,false,false, false, false, false, false, false, false, false, false, true, false, true, yesterdayDate);
        });
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

        handleAttendanceTypeAndIssues(inout,attendance, fullday, half_day, unAuthorized, unSuccessful, absent, employee.getEmployeeId());

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if(!attendance.isArrivalOnWeekend())
            savedAttendance  = attendanceRepo.save(attendance);

        inout.setAttendance(savedAttendance);
        inOutRepo.save(inout);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(inout.getPunchTime()));

        if (unSuccessful)
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

        handleAttendanceTypeAndIssues(moa, attendance, fullday, half_day, unAuthorized, unSuccessful, absent, employee.getEmployeeId());

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        List<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, helper.getYesterdayDate(), helper.getYesterdayDate());
        if (!leave.isEmpty()) handleLeave(leave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = null;
        if( (!attendance.isArrivalOnWeekend()) || (!attendance.getArrivalTime().equals(attendance.getLeftTime())))
            savedAttendance = attendanceRepo.save(attendance);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
        updateInOutRelationships(moa, eve, savedAttendance);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(moa.getPunchTime()));

        if (unSuccessful)
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

        if (unSuccessful)
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
            attendance.setPublicId(Objects.requireNonNull(utils.generateId(10), "Generated ID cannot be null"));
            attendance.setEmployee(employee);
            attendance.setDate(Objects.requireNonNull(helper.getYesterdayDate(), "Yesterday date cannot be null"));
            attendance.setArrivalDate(date);
            attendance.setEtlRunTime(new Date());
            attendance.setUpdatedDate(new Date());
            return attendance;
        } catch (NullPointerException e) {
            logger.error("Null value encountered while creating base attendance", e);
            throw new IllegalStateException("Failed to create attendance due to null values", e);
        }
    }

    private void handleAttendanceTypeAndIssues(InOutEntity inout, AttendanceEntity attendance, Boolean fullday, Boolean half_day,
                                               Boolean unAuthorized, Boolean unSuccessful, Boolean absent, String employeeId) {

        if (attendance == null) {
            logger.error("Null attendance provided to handleAttendanceTypeAndIssues");
            return;
        }

        if (inout == null || inout.getPunchTypeTime() == null) {
            logger.warn("Null inout or punch time for employee: {}", employeeId);
            return;
        }

        try {
            LocalTime punchTime = inout.getPunchTypeTime().toLocalTime();
            LocalTime eightThirtyAM = LocalTime.of(8, 30);
            LocalTime nineAM = LocalTime.of(9, 0);
            LocalTime twelvePM = LocalTime.of(12, 0);
            LocalTime thirteenPM = LocalTime.of(13, 0);

            if (punchTime.isAfter(thirteenPM)) {
                attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
            }

            if (Boolean.TRUE.equals(half_day) || punchTime.isAfter(twelvePM)) {
                attendance.setAttendanceType(AttendanceType.HALF_DAY);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setHasIssues(true);
            }

            if((Boolean.TRUE.equals(unSuccessful) && !Boolean.TRUE.equals(unAuthorized)) && punchTime.isAfter(nineAM)){
                attendance.setLeaveStatus(LeaveStatus.SHORT_LEAVE);
            }

            if (Boolean.TRUE.equals(fullday) && punchTime.isBefore(eightThirtyAM)) {
                attendance.setAttendanceType(AttendanceType.FULL_DAY);
                attendance.setDueDateForUA(null);
                attendance.setHasIssues(false);
            }

            if (Boolean.TRUE.equals(unAuthorized)) {
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setIsUnauthorized(true);
                attendance.setHasIssues(true);
                attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
            }

            if (Boolean.TRUE.equals(unSuccessful)) {
                attendance.setHasIssues(true);
                attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO LATE ATTENDANCE. PLEASE RESOLVE BEFORE THE DUE DATE.");
            }

            if (Boolean.TRUE.equals(absent)) {
                attendance.setAttendanceType(AttendanceType.ABSENT);
                attendance.setDueDateForUA(helper.getDueDate());
                attendance.setHasIssues(true);
                attendance.setIssueDescription("GOING ABSENT DUE TO NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE THE DUE DATE.");
            }
        } catch (Exception e) {
            logger.error("Error handling attendance type and issues for employee: {}", employeeId, e);
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

            // Update evening entity
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

    public boolean isDatePassed(Date toDate) {
        LocalDate targetDate = toDate.toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        LocalDate today = LocalDate.now();

        return targetDate.isBefore(today);
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
        /*if (!req.validateLeaveReq())
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());*/

        EmployeeEntity employee = helper.getEmployeeById(userId);

        /* if (leaveRepo.findByEmployeeAndFromDate(employee, helper.removeTimeFromDate(new Date())).isPresent()) {
            throw new IllegalArgumentException((ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()));
        } */

        List<LeaveEntity> leave_ = leaveRepo.findByEmployeeAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, req.getFromDate(), req.getToDate());

        if (!leave_.isEmpty()){
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

        /* List<UserLeaveTypeRemainingEntity> userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(employeeId);
        if (userLeaveTypeRemaining.isEmpty()) throw new IllegalArgumentException("Failed to make leave request");

        boolean noLeavesRemaining = userLeaveTypeRemaining.stream()
                .allMatch(leaveType -> leaveType.getRemainingLeaves() < 1);
        if (noLeavesRemaining) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        */

        final String leaveId = utils.generateId(10);
        LeaveEntity leaveEntity = transformToEntity(req, employee.getSltId(), leaveId, leaveTypeRepository);
        leaveEntity.setRequestStatus(RequestStatus.PENDING_APPROVAL);

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {

            Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndHappenDate(employee, helper.removeTimeFromDate(req.getHappenDate()));
            if (leave.isPresent())
                throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDateAndIsActiveTrue(
                    employee, leaveEntity.getHappenDate());

            if (attendanceEntityOp.isEmpty()) {
                throw new IllegalArgumentException("No ATTENDANCE RECORD FOUND");
            }
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            if (attendanceEntity.getIsResolved() || !attendanceEntity.getHasIssues()) return;

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

            // Process attendance
            attendanceEntity.setIsResolved(true);
            attendanceEntity.setHasIssues(false);
            attendanceEntity.setResolve(ResolveType.VIA_LEAVE);
            attendanceRepo.save(attendanceEntity);

            // Process leave
            leaveEntity.setRequestStatus(RequestStatus.APPROVED);
            leaveRepo.save(leaveEntity);

            // Process leave balance
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

            // Skip records with null essential fields
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

}