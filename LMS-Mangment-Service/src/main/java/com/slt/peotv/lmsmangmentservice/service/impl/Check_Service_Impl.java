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

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
    private static final int MAX_RETRY_ATTEMPTS = 3;

    public static Map<String, UserRest> createUserMap(List<UserRest> users) {

        final List<UserRest> usersCopy = new ArrayList<>(users);

        List<UserRest> filteredAndSortedUsers = usersCopy.stream().filter(user -> user.getHighestRolePriority() != 1)
                .sorted(Comparator.comparing(UserRest::getHighestRolePriority, Comparator.reverseOrder()))
                .toList();


        Map<String, UserRest> userMap = new ConcurrentHashMap<>();

        for (UserRest user : filteredAndSortedUsers) {
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
        EmployeeEntity employeeEntity = helper.getEmployeeById(userId);
        List<InOutDTO> allInOut = inOutRepo.findByEmployeeIdAndDate(employeeEntity.getSltId(), date)
                .stream()
                .map(lMSUtils::inOutDTO).toList();

        Map<String, InOutDTO> result = new HashMap<>();

        allInOut.stream()
                .filter(InOutDTO::isMorning)
                .min(Comparator.comparing(InOutDTO::getPunchTypeTime))
                .ifPresent(dto -> result.put("morning", dto));

        allInOut.stream()
                .filter(InOutDTO::isEvening)
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

    @Override
    public void requestMovement(MovementReq req, HttpServletRequest request, Authentication authentication) {
        try {
            EmployeeEntity employee = helper.getEmployeeById(req.getEmployeeId());

            String name = authentication.getName();
            if (name == null || name.trim().isEmpty())
                throw new RuntimeException("Failed to process movement request");

            if (!employee.getPublicId().equals(req.getUserId()) || !name.equals(req.getUserId()))
                throw new RuntimeException("Failed to process movement request");

            Optional<MovementsEntity> reqDate = movementsRepo.findAllByEmployeeAndHappenDate(employee, stripTimeFromDate(req.getHappenDate()));

            if (reqDate.isPresent())
                throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            /*if (!req.validateMovementReq()) {
                return;
            }*/

            String token = "Bearer " + extractJwtTokenFromCookie(request);
            if (token == null || token.trim().isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            final List<UserRest> admins = userClient.getEmployeeAdmins(req.getUserId(), token);
            /*if (admins == null || admins.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());*/

            Map<String, UserRest> userMap = createUserMap(admins);
            if (userMap == null || userMap.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            final String movementId = utils.generateId(10);

            MovementsEntity movementsEntity = mapToEntity(req, employee, movementId);

            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndDateAndIsActiveTrue(
                    employee, movementsEntity.getHappenDate());

            if (attendanceEntity.isEmpty())
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

            AttendanceEntity attendance = attendanceEntity.get();

            if (!Boolean.TRUE.equals(attendance.getHasIssues()) || Boolean.TRUE.equals(attendance.getIsResolved())) {
                throw new RuntimeException("Failed to process movement request");
            }

            movementsEntity.setAttendance(attendance);
            List<ComponetAdminsEntity> adminEntities = new ArrayList<>();

            userMap.entrySet().forEach(entry -> {
                UserRest value = entry.getValue();
                ComponetAdminsEntity admin = createAdminEntity(value, movementId);
                adminEntities.add(admin);
            });

            movementsEntity.setAdmins(adminEntities);

            lmsService.createMovements(movementsEntity);

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

    private ComponetAdminsEntity createAdminEntity(UserRest user_, String movementId) {
        EmployeeEntity user = helper.getEmployeeById(user_.getEmployeeId());

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
                .happenDate(stripTimeFromDate(movementReq.getHappenDate()))
                .logTime(movementReq.getLogTime() == null ? new Date() : movementReq.getLogTime())
                .inTime(movementReq.getInTime() == null ? "00:00:00" : movementReq.getInTime())
                .outTime(movementReq.getInTime() == null ? "00:00:00" : movementReq.getOutTime()) // Fixed this line
                .build();
    }

    private void logError(String message, Exception e) {
        System.err.println(message + ": " + e.getMessage());
        e.printStackTrace();
    }

    private Time parseToSqlTime(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return null;
        }
        LocalTime localTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
        return Time.valueOf(localTime);
    }

    public void approvedMove(MovementsEntity movement, String userId) {
        if (movement.getRequestStatus().equals(RequestStatus.REJECTED) || movement.getRequestStatus().equals(RequestStatus.APPROVED) || movement.getRequestStatus().equals(RequestStatus.CANCELLED))
            return;
        AttendanceEntity attendance = movement.getAttendance();
        if (attendance == null) {
            return;
        }
        if (movement.getAdmins() == null || movement.getAdmins().isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

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
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        if (currentAdmin.getIsAccepted()) throw new IllegalArgumentException("You are already accepted");

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

            switch (movement.getMovementType()) {
                case HOME_TO_OFFICE:
                    attendance.setArrivalTime(parseToSqlTime(movement.getInTime()));
                    break;
                case OFFICE_TO_HOME:
                    attendance.setLeftTime(parseToSqlTime(movement.getOutTime()));
                    break;
                default:
                    attendance.setArrivalTime(parseToSqlTime(movement.getInTime()));
                    attendance.setLeftTime(parseToSqlTime(movement.getOutTime()));
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
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

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
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        ComponetAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getEmployee().getPublicId()) ||
                                userId.equals(admin.getEmployee().getEmployeeId()) ||
                                userId.equals(admin.getEmployee().getSltId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        if (currentAdmin.getIsAccepted()) throw new IllegalArgumentException("You are already accepted");

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

    public Map<String, InOutEntity> findEarliestMorningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Map.of();
        }

        return entities.stream().filter(entity -> entity.getEmployeeId() != null && entity.getPunchTime() != null)
                .collect(Collectors.groupingBy(InOutEntity::getEmployeeId,
                        Collectors.minBy(Comparator.comparing(InOutEntity::getPunchTime))))
                .entrySet().stream().filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get()));
    }

    public Map<String, InOutEntity> findEarliestEveningPunchByEmployee(List<InOutEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Map.of();
        }

        return entities.stream().filter(entity -> entity.getEmployeeId() != null && entity.getPunchTime() != null)
                .collect(Collectors.groupingBy(InOutEntity::getEmployeeId,
                        Collectors.minBy(Comparator.comparing(InOutEntity::getPunchTime))))
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

        for (String employeeId : morningMap.keySet()) {
            if (eveningMap.containsKey(employeeId)) {
                InOutEntity morningPunch = morningMap.get(employeeId);
                InOutEntity eveningPunch = eveningMap.get(employeeId);

                employeesWithBothPunches.put(employeeId, new InOutEntity[]{morningPunch, eveningPunch});
            }
        }

        return employeesWithBothPunches;
    }

    public Map<String, InOutEntity> findEmployeesWithOnlyMorningPunches(Map<String, InOutEntity> morningMap,
                                                                        Map<String, InOutEntity> eveningMap) {

        Map<String, InOutEntity> employeesWithOnlyMorningPunches = new HashMap<>();

        for (String employeeId : morningMap.keySet()) {
            if (!eveningMap.containsKey(employeeId)) {
                InOutEntity morningPunch = morningMap.get(employeeId);
                employeesWithOnlyMorningPunches.put(employeeId, morningPunch);
            }
        }

        return employeesWithOnlyMorningPunches;
    }

    private synchronized void handleHolidays() throws IOException, InterruptedException {
        List<EmployeeEntity> all = (List<EmployeeEntity>) employeeRepo.findAll();
        boolean todayGovHoliday = HolidayChecker.isTodayGovHoliday();
        if (todayGovHoliday) {
            all.parallelStream().forEach(employee -> {
                AttendanceEntity attendance = new AttendanceEntity();
                attendance.setEmployee(employee);
                attendance.setPublicId(utils.generateId(10));
                attendance.setIsHoliday(true);
                attendance.setDate(helper.getDateWithoutTime());

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
        LocalDateTime yesterdayBefore830 = LocalDate.now().minusDays(1).atTime(8, 30);
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
        List<InOutEntity> employeesArrivedBefore830 = inOutRepo.findByDateAndPunchTypeTimeBefore(yesterdayDate,
                sqlTime830);
        List<InOutEntity> employeesLeftAfter5 = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
                timeEveStart, timeEveEnd);
        List<InOutEntity> employeesLeftAfter5_ = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
                timeEveStart_, timeEveEnd);
        List<InOutEntity> employeesArrivedAfter900 = inOutRepo.findByDateAndPunchTypeTimeAfter(yesterdayDate,
                sqlTime900);
        List<InOutEntity> employeesArrivedBetween830And900 = inOutRepo.findByDateAndPunchTypeTimeBetween(yesterdayDate,
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
                reportAttendance(morningPunch, eveningPunch, true, false, false, false, false, false, false, false,
                        false, true, false, false, null);
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
                reportAttendance(morningPunch, false,false, true, false, false, false, false, false, false, false, true,
                        false, false, null);
            });
        }

        Map<String, InOutEntity> employeesWithOnlyMorningPunches_ = findEmployeesWithOnlyMorningPunches(
                earliestEveningPunchByEmployee, earliestMorningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithOnlyMorningPunches_.entrySet()) {
            String employeeId = entry.getKey();
            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                InOutEntity morningPunch = entry.getValue();
                reportAttendance(morningPunch, true,false, true, false, false, false, false, false, false, false, true,
                        false, false, null);
            });
        }

        /// =====================================================================================
        /// Late Arrive at 9.00 Am but do the late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLate = findEmployeesWithBothPunches(lateArrivals,
                earliestEveningPunchByEmployee_);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLate.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue()[0];
            InOutEntity eveningPunch = entry.getValue()[1];

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {

                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, false, false, false, false,
                        true, false, false, null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 9.00 Am but do the did not late cover

        Map<String, InOutEntity> onlyMorningPunchesLate90 = findEmployeesWithOnlyMorningPunches(lateArrivals,
                earliestEveningPunchByEmployee_);

        for (Map.Entry<String, InOutEntity> entry : onlyMorningPunchesLate90.entrySet()) {

            String employeeId = entry.getKey();
            InOutEntity morningPunch = entry.getValue();

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, false,false, false, true, true, false, false, false, false, false, true,
                        false, false, null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 8.39 Am - 9.00.Am but did the late cover

        Map<String, InOutEntity[]> employeesWithBothPunchesLate830900 = findEmployeesWithBothPunches(
                arrivedBetween830And900, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity[]> entry : employeesWithBothPunchesLate830900.entrySet()) {
            String employeeId = entry.getKey();

            InOutEntity morningPunch = entry.getValue()[0];
            InOutEntity eveningPunch = entry.getValue()[1];

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, false, false, false, false,
                        true, false, false, null);
            });

        }

        /// =====================================================================================
        /// Late Arrive at 8.39 Am - 9.00.Am but did not the late cover

        Map<String, InOutEntity> employeesWithMorPunchesLate830900 = findEmployeesWithOnlyMorningPunches(
                arrivedBetween830And900, earliestEveningPunchByEmployee);

        for (Map.Entry<String, InOutEntity> entry : employeesWithMorPunchesLate830900.entrySet()) {
            InOutEntity morningPunch = entry.getValue();
            reportAttendance(morningPunch, false,false, false, true, true, false, false, false, false, false, true, false, false,
                    null);

        }

        /// ================================ employee who are absent
       /* List<String> absentEmployeesToday = getAbsentEmployeesToday();
        reportAbsent(absentEmployeesToday);*/

        getAbsentEmployeesToday().forEach(employee -> {
            List<UserLeaveTypeRemainingEntity> userLeaveCategoryRemaining = serviceEvent
                    .getUserLeaveTypeRemaining(employee);
            boolean nopay = userLeaveCategoryRemaining.stream().allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
            reportAttendance(employee, false, false, false, false, false, false, false, false, false, true, nopay, true, helper.getYesterdayDate());
        });
    }

    public List<String> getAbsentEmployeesToday() {
        List<EmployeeEntity> allEmployees = (List<EmployeeEntity>) employeeRepo.findAll();

        List<InOutEntity> todayRecords = inOutRepo.findByDate(helper.getYesterdayDate());
        Set<String> presentEmployeeIds = todayRecords.stream().map(InOutEntity::getEmployeeId)
                .collect(Collectors.toSet());

        return allEmployees.stream().map(EmployeeEntity::getSltId)
                .filter(presentEmployeeIds::contains)
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

        handleAttendanceTypeAndIssues(attendance, fullday, half_day, unAuthorized, unSuccessful, absent, inout.getEmployeeId());

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = attendanceRepo.save(attendance);

        inout.setAttendance(savedAttendance);
        inOutRepo.save(inout);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(inout.getPunchTime()));

        if (unSuccessful)
            helper.handleLateAndUnsuccessful(employee.getEmployeeId(), savedAttendance);

        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
    }

    @Override
    public void reportAttendance(InOutEntity moa, InOutEntity eve, Boolean fullday, Boolean unAuthorized,
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

        handleAttendanceTypeAndIssues(attendance, fullday, half_day, unAuthorized, unSuccessful, absent, employee.getEmployeeId());

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());
        updateInOutRelationships(moa, eve, savedAttendance);

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.removeTimeFromDate(moa.getPunchTime()));

        if (unSuccessful)
            helper.handleLateAndUnsuccessful(employee.getEmployeeId(), savedAttendance);
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

        Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndFromDate(employee, helper.getYesterdayDate());
        if(leave.isPresent()) return;

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

        handleAttendanceTypeAndIssues(attendance, fullday, half_day, unAuthorized, unSuccessful, absent, employeeID);

        handleLeaveStatus(attendance, leaveSuccess, leaveReq, isFullLeave);

        if (nopay) {
            attendance.setPayStatus(PayStatus.NO_PAY);
        }

        AttendanceEntity savedAttendance = attendanceRepo.save(attendance);
        logger.info("Attendance saved successfully for employee: {}", employee.getEmployeeId());

        if (nopay)
            saveNoPayEntity(employee, savedAttendance, createNoPayRequest(half_day, unSuccessful, unAuthorized, late, late_cover, absent), helper.getYesterdayDate());

        if (unSuccessful)
            helper.handleLateAndUnsuccessful(employeeID, savedAttendance);
    }

    private AttendanceEntity createBaseAttendance(EmployeeEntity employee, Date date) {
        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployee(employee);
        attendance.setDate(helper.getYesterdayDate());
        attendance.setEtlRunTime(new Date());
        attendance.setUpdatedDate(new Date());
        return attendance;
    }

    private void handleAttendanceTypeAndIssues(AttendanceEntity attendance, Boolean fullday, Boolean half_day,
                                               Boolean unAuthorized, Boolean unSuccessful, Boolean absent, String employeeId) {

        if (half_day) {
            attendance.setAttendanceType(AttendanceType.HALF_DAY);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setHasIssues(true);
        }

        if (fullday) {
            attendance.setAttendanceType(AttendanceType.FULL_DAY);
            attendance.setDueDateForUA(null);
            attendance.setHasIssues(false);
        }

        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIsUnauthorized(true);
            attendance.setHasIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO SWIPE ERROR. PLEASE RESOLVE BEFORE THE DUE DATE.");
        }

        if (unSuccessful) {
            attendance.setHasIssues(true);
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO LATE ATTENDANCE. PLEASE RESOLVE BEFORE THE DUE DATE.");
        }

        if (absent) {
            attendance.setAttendanceType(AttendanceType.ABSENT);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setHasIssues(true);
            attendance.setIssueDescription("GOING ABSENT DUE TO NO SYSTEM RECORDS FOUND. PLEASE RESOLVE BEFORE THE DUE DATE.");
        }
    }

    private void handleLeaveStatus(AttendanceEntity attendance, Boolean leaveSuccess, Boolean leaveReq, Boolean isFullLeave) {
        if (leaveSuccess) {
            attendance.setLeaveStatus(LeaveStatus.LEAVE_APPROVED);
        }

        if (leaveReq) {
            attendance.setLeaveStatus(LeaveStatus.LEAVE_REQUESTED);
        }

        if (isFullLeave) {
            attendance.setLeaveStatus(LeaveStatus.FULL_LEAVE);
        }
    }

    private void updateInOutRelationships(InOutEntity moa, InOutEntity eve, AttendanceEntity savedAttendance) {
        try {
            // Update morning entity
            if (moa.getId() != null) {
                Optional<InOutEntity> moaEntity = inOutRepo.findById(moa.getId());
                if (moaEntity.isPresent()) {
                    InOutEntity managedMoa = moaEntity.get();
                    managedMoa.setAttendance(savedAttendance);
                    inOutRepo.save(managedMoa);
                }
            }

            // Update evening entity
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

            EmployeeEntity employee = helper.getEmployeeById(employee_id);

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
                            allMatch, true, null);

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
                reportAttendance(employee_id, false, false, false, false, false, false, false, false, false, true, allMatch, true, null);
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

        EmployeeEntity employee = helper.getEmployeeById(emp);

        return LeaveEntity.builder().publicId(id)
                .employee(employee).submitDate(helper.removeTimeFromDate(new Date()))
                .fromDate(stripTimeFromDate(leaveReq.getFromDate())).toDate(stripTimeFromDate(leaveReq.getToDate()))
                .happenDate(stripTimeFromDate(leaveReq.getHappenDate())).leaveType(type)
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

        if (leaveRepo.findByEmployeeAndFromDate(employee, helper.removeTimeFromDate(new Date())).isPresent()) {
            throw new IllegalArgumentException((ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage()));
        }

        String name = authentication.getName();
        String employeeId = employee.getEmployeeId();

        if (name == null || name.isEmpty() || employeeId == null || employeeId.isEmpty())
            throw new IllegalArgumentException("Failed to make leave request");

        if (!req.getUserId().equals(name))
            throw new IllegalArgumentException("Failed to make leave movement request");

        List<UserLeaveTypeRemainingEntity> userLeaveTypeRemaining = serviceEvent.getUserLeaveTypeRemaining(employeeId);
        if (userLeaveTypeRemaining.isEmpty()) throw new IllegalArgumentException("Failed to make leave request");

        boolean noLeavesRemaining = userLeaveTypeRemaining.stream()
                .allMatch(leaveType -> leaveType.getRemainingLeaves() < 1);
        if (noLeavesRemaining) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        final String leaveId = utils.generateId(10);
        LeaveEntity leaveEntity = transformToEntity(req, employee.getSltId(), leaveId, leaveTypeRepository);
        leaveEntity.setRequestStatus(RequestStatus.PENDING_APPROVAL);

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {

            Optional<LeaveEntity> leave = leaveRepo.findByEmployeeAndHappenDate(employee, stripTimeFromDate(req.getHappenDate()));
            if (leave.isPresent())
                throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDateAndIsActiveTrue(
                    employee, leaveEntity.getHappenDate());

            if (attendanceEntityOp.isEmpty()) {
                throw new IllegalArgumentException("Failed to process leave request: No attendance record found");
            }
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            if (attendanceEntity.getIsResolved() || !attendanceEntity.getHasIssues()) return;

            leaveEntity.setAttendance(attendanceEntityOp.get());
        }
        if (req.getIsManualRequest()) {

            String token = "Bearer " + extractJwtTokenFromCookie(request);
            if (token == null || token.isEmpty()) throw new IllegalArgumentException("Failed to process leave request");

            final List<UserRest> admins;
            synchronized (this) {
                admins = userClient.getEmployeeAdmins(req.getUserId(), token);
            }
            /*if (admins.isEmpty() || admins == null)
                throw new NoSuchElementException(ErrorMessages.ADMIN_NO_RECORD_FOUND.getErrorMessage());*/

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

        if (req.getComponentBehavior() == ComponentBehavior.UNAUTHORIZED ||
                req.getComponentBehavior() == ComponentBehavior.ABSENT ||
                req.getComponentBehavior() == ComponentBehavior.UNSUCCESSFUL) {
            processUnauthorizedLeave(leaveEntity, employeeId);
        }
    }

    // Helper method to process unauthorized leave
    private void processUnauthorizedLeave(LeaveEntity leaveEntity, String employeeId) {
        Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeAndDate(
                leaveEntity.getEmployee(), leaveEntity.getHappenDate());
        if (attendanceEntityOp.isPresent()) {
            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            attendanceEntity.setIsResolved(true);
            attendanceEntity.setHasIssues(false);
            attendanceEntity.setResolve(ResolveType.VIA_LEAVE);

            leaveEntity.setRequestStatus(RequestStatus.APPROVED);
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
    @Transactional
    @Retryable(value = {DataAccessException.class},
            maxAttempts = MAX_RETRY_ATTEMPTS,
            backoff = @Backoff(delay = 1000))
    public void getAllTheInOutRecordsFromSLT() {
        final String methodName = "getAllTheInOutRecordsFromSLT";
        final String url = "jdbc:mysql://localhost:3306/attendance";
        final String username = "root";
        final String password = "User@123";

        String sql = "SELECT EmployeeID, LogDate, LogTime, TerminalID, `InOut`, `read`, processed, etl_run_time " +
                "FROM accesslog_archive " +
                "WHERE LogDate = DATE_FORMAT(DATE_SUB(CURRENT_DATE(), INTERVAL 1 DAY), '%d/%m/%Y')";

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
            while (resultSet.next()) {
                try {
                    AccessLogEntity accessLog = buildAccessLogEntity(resultSet);
                    accessLogEntities.add(accessLog);
                    recordCount++;
                } catch (SQLException e) {
                    logErrorWithStackTrace("Error processing record #" + (recordCount + 1), e, methodName);
                } catch (Exception e) {
                    logErrorWithStackTrace("Unexpected error processing record #" + (recordCount + 1), e, methodName);
                }
            }

            processRetrievedRecords(accessLogEntities, methodName);

        } catch (SQLException e) {
            logErrorWithStackTrace("Database connection failed", e, methodName);
        } catch (Exception e) {
            logErrorWithStackTrace("Unexpected error in " + methodName, e, methodName);
        } finally {
            closeDatabaseResources(connection, statement, resultSet, methodName);
        }
    }

    private AccessLogEntity buildAccessLogEntity(ResultSet resultSet) throws SQLException {
        try {
            return AccessLogEntity.builder()
                    .employeeId(resultSet.getString("EmployeeID"))
                    .logDate(resultSet.getString("LogDate"))
                    .logTime(resultSet.getString("LogTime"))
                    .terminalId(resultSet.getString("TerminalID"))
                    .inOut(resultSet.getString("InOut"))
                    .readStatus(resultSet.getString("read"))
                    .processed(resultSet.getInt("processed"))
                    .etlRunTime(resultSet.getTimestamp("etl_run_time"))
                    .build();
        } catch (SQLException e) {
            logger.error("Error building AccessLogEntity from ResultSet");
            throw e;
        }
    }

    private void processRetrievedRecords(List<AccessLogEntity> records, String methodName) {
        if (records.isEmpty()) {
            logger.info("{}: No records found for processing", methodName);
            return;
        }

        try {
            logger.info("{}: Attempting to save {} records", methodName, records.size());
            accessLogRepo.saveAll(records);
            logger.info("{}: Successfully saved {} records", methodName, records.size());
        } catch (DataIntegrityViolationException e) {
            logErrorWithStackTrace("Duplicate records detected. Attempting individual saves", e, methodName);
            saveRecordsIndividually(records, methodName);
        } catch (Exception e) {
            logErrorWithStackTrace("Failed to save records", e, methodName);
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
