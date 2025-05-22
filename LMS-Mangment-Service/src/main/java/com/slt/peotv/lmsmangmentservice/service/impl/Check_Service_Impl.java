package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.feign_client.UserClient;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.feign_client.model.UserRest;
import com.slt.peotv.lmsmangmentservice.model.AbsenteeReq;
import com.slt.peotv.lmsmangmentservice.model.req.BulkApprovedReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.dto.InOutDTO;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.service.ServiceEvent;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.HolidayChecker;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class Check_Service_Impl implements Check_Service {

    private final AttendanceRepo attendanceRepo;
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
    private AbsenteeRepo absenteeRepo;
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
    private MovementAdminsRepo movementAdminsRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepository;
    @Autowired
    private LeaveAdminsRepo leaveAdminsRepo;
    @Autowired
    private AccessLogRepo accessLogRepo;

    public Check_Service_Impl(AttendanceRepo attendanceRepo) {
        this.attendanceRepo = attendanceRepo;
    }

    @Override
    public synchronized void allApproved(BulkApprovedReq bulkApprovedReq, boolean swap) {
        // Using atomic operations for nested loops
        bulkApprovedReq.getApprovedEmployeesToday().parallelStream().forEach(emp -> {
            bulkApprovedReq.getApprovedIds().forEach(id -> {
                synchronized (this) {
                    if(swap) processMovement(id, emp);
                    else processLeave(emp, id);
                }
            });
        });
    }

    @Override
    public synchronized void allReject(BulkApprovedReq bulkApprovedReq, boolean swap) {
        bulkApprovedReq.getApprovedIds().parallelStream().forEach(id -> {
            if(swap) {
                synchronized (movementsRepo) {
                    Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
                    if (movementsOpt.isPresent()) {
                        MovementsEntity movementsEntity = movementsOpt.get();
                        movementsEntity.setIsReject(true);
                        movementsRepo.save(movementsEntity);
                    }
                }
            }
            else {
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
    public void reject(String id, String userId, boolean swap){
        if(swap){
            Optional<MovementsEntity> movementsOpt = movementsRepo.findByPublicId(id);
            MovementsEntity movementsEntity = movementsOpt.get();
            if(!movementsEntity.getUserId().equals(userId)) return;
            movementsEntity.setIsReject(true);
            movementsRepo.save(movementsEntity);
        }else{
            Optional<LeaveEntity> leaveEntityOpt = leaveRepo.findByPublicId(id);
            LeaveEntity leaveEntity = leaveEntityOpt.get();
            if(!leaveEntity.getUserId().equals(userId)) return;
            leaveEntity.setIsReject(true);
            leaveRepo.save(leaveEntity);
        }
    }

    @Override
    public List<AccessLogRest> getAllAccessLogsToday(String date) {
        return accessLogRepo.findByLogDate(date).stream().map(employee->{
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
        return accessLogRepo.findAll().stream().map(employee->{
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

    public static Map<String, UserRest> createUserMap(List<UserRest> users) {

        final List<UserRest> usersCopy = new ArrayList<>(users);

        List<UserRest> filteredAndSortedUsers = usersCopy.stream().filter(user -> user.getHighestRolePriority() != 1)
                .sorted(Comparator.comparing(UserRest::getHighestRolePriority, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Use ConcurrentHashMap for thread safety - note that we lose insertion order
        // If order is critical, consider using Collections.synchronizedMap(new
        // LinkedHashMap<>())
        Map<String, UserRest> userMap = new ConcurrentHashMap<>();

        // Populate the map with sltId as key, falling back to userId if sltId is null
        for (UserRest user : filteredAndSortedUsers) {
            String key = user.getSltId() != null ? user.getSltId() : user.getUserId();
            userMap.put(key, user);
        }

        // Return an unmodifiable view of the map to prevent external modification
        return Collections.unmodifiableMap(userMap);
    }

    public static String convertCode(String code) {
        if (code != null && code.startsWith("VC")) {
            return "A" + code.substring(2);
        }
        return code;
    }


    @Override
    public NoPayEntity saveNoPayEntity(String employeeId, AttendanceEntity attendanceEntity, Boolean isHalfDay,
                                       Boolean unSuccessful, Boolean isLate, Boolean isLateCover, Boolean isAbsent, Date accualDate) {
        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(employeeId);
        if (employeeEntity.isEmpty())
            return null;
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

        nopayEntity.setUserId(employeeEntity.get().getPublicId());
        nopayEntity.setEmployeeID(employeeId);
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

    public boolean validateMovementReq(MovementReq request) {
        // Validate required fields
        if (Objects.isNull(request.getEmployeeId()) || request.getEmployeeId().trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(request.getUserId()) || request.getUserId().trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(request.getMovementType())) {
            return false;
        }

        if (Objects.isNull(request.getDestination()) || request.getDestination().trim().isEmpty()) {
            return false;
        }

        if (Objects.isNull(request.getHappenDate())) {
            return false;
        }

        // All validations passed
        return true;
    }

    @Override
    public List<InOutDTO> getAllInOut(String employeeID, Date date){
        return inOutRepo.findByEmployeeIDAndPunchInMoa(employeeID, date)
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
        List<InOutDTO> allInOut = inOutRepo.findByEmployeeIDAndPunchInMoa(employeeEntity.getSltId(), date)
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

        List<InOutEntity> moaRecords = inOutRepo.findByEmployeeIDAndPunchInMoaBetween(employeeEntity.getSltId(), date, date2);
        List<InOutEntity> evRecords = inOutRepo.findByEmployeeIDAndPunchInEvBetween(employeeEntity.getSltId(), date, date2);

        List<InOutEntity> combinedRecords = new ArrayList<>(moaRecords);
        combinedRecords.addAll(evRecords);

        return combinedRecords.stream()
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
        List<InOutEntity> moaRecords = inOutRepo.findByEmployeeIDAndPunchInMoa(employeeEntity.getSltId(), date);
        List<InOutEntity> evRecords = inOutRepo.findByEmployeeIDAndPunchInEv(employeeEntity.getSltId(), date);

        List<InOutEntity> combinedRecords = new ArrayList<>(moaRecords);
        combinedRecords.addAll(evRecords);

        return combinedRecords.stream()
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
            Optional<MovementsEntity> reqDate = movementsRepo.findAllByEmployeeIdAndReqDate(req.getEmployeeId(), req.getHappenDate());

            if(reqDate.isPresent()) throw new IllegalArgumentException(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

            if (!validateMovementReq(req)) {
                return;
            }
            String name = authentication.getName();
            if (name == null || name.trim().isEmpty())
                throw new RuntimeException("Failed to process movement request");

            // Check if employee exists - using Optional pattern properly
            EmployeeEntity employeeEntity = employeeRepo.findBySltId(req.getEmployeeId())
                    .orElse(employeeRepo.findByPublicId(req.getEmployeeId())
                            .orElse(employeeRepo.findByEmployeeId(req.getEmployeeId()).orElse(null)));

            if (employeeEntity == null)
                throw new RuntimeException("Failed to process movement request");

            if (!employeeEntity.getPublicId().equals(req.getUserId()) || !name.equals(req.getUserId()))
                throw new RuntimeException("Failed to process movement request");

            // Token extraction
            String token = "Bearer " + extractJwtTokenFromCookie(request);

            // Fetch admins
            final List<UserRest> admins = userClient.getEmployeeAdmins(req.getUserId(), token);

            // Create thread-safe user map
            Map<String, UserRest> userMap = createUserMap(admins);

            // Generate movement ID once
            final String movementId = utils.generateId(10);
            // Initialize entities

            MovementsEntity movementsEntity = createMovementEntity(req, movementId, employeeEntity.getEmployeeId());
            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeIDAndArrivalDate(
                    convertCode(movementsEntity.getEmployeeId()), movementsEntity.getHappenDate());

            if (attendanceEntity.isEmpty())
                throw new RuntimeException("Failed to process movement request");

            AttendanceEntity attendance = attendanceEntity.get();
            // Return if attendance has no issues or if it has issues that are already resolved
            if (!Boolean.TRUE.equals(attendance.getIssues()) || Boolean.TRUE.equals(attendance.getResolve())) {
                return;
            }
            movementsEntity.setAttendance(attendance);

            // Create admin entities without saving them individually
            List<MovementAdminsEntity> adminEntities = new ArrayList<>();

            userMap.entrySet().forEach(entry -> {
                UserRest value = entry.getValue();
                MovementAdminsEntity admin = createMovementAdminEntity(value, movementId);
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

    private MovementAdminsEntity createMovementAdminEntity(UserRest user, String movementId) {
        MovementAdminsEntity entity = new MovementAdminsEntity();
        entity.setSltId(user.getSltId());
        entity.setUserId(user.getUserId());
        entity.setEmployeeId(user.getEmployeeId());
        entity.setHighestRolePriority(user.getHighestRolePriority());
        entity.setMovementId(movementId);
        entity.setIsAccepted(false);
        return entity;
    }

    private LeaveAdminsEntity createLeaveAdminEntity(UserRest user, String leaveId) {
        LeaveAdminsEntity entity = new LeaveAdminsEntity();
        entity.setSltId(user.getSltId());
        entity.setUserId(user.getUserId());
        entity.setEmployeeId(user.getEmployeeId());
        entity.setHighestRolePriority(user.getHighestRolePriority());
        entity.setLeaveId(leaveId);
        entity.setIsAccepted(false);
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


    private MovementsEntity createMovementEntity(MovementReq req, String movementId, String employeeId) {
        Date currentDate = new Date();

        return MovementsEntity.builder().publicId(movementId).employeeId(employeeId).userId(req.getUserId())
                .reqDate(helper.removeTimeFromDate(currentDate)).logTime(helper.removeTimeFromDate(currentDate))
                .comment(req.getComment()).isAbsent(req.getIsAbsent())
                .happenDate(stripTimeFromDate(req.getHappenDate()))
                .isUnSuccessfulAttdate(req.getIsUnSuccessfulAttdate()).isPending(true).resolve(false)
                .isHalfDay(req.getIsHalfDay()).isAccepted(false).destination(req.getDestination())
                .category(req.getCategory()).movementType(req.getMovementType()).isLate(req.getIsLate())
                .isLateCover(req.getIsLateCover()).build();
    }

    private MovementsEntity createMovementEntity(MovementReq req, String movementId, String employeeId, Date logTime, String intime, String outtime) {
        Date currentDate = new Date();

        return MovementsEntity.builder().publicId(movementId).employeeId(employeeId).userId(req.getUserId())
                .reqDate(helper.removeTimeFromDate(currentDate))
                .logTime(helper.removeTimeFromDate(logTime))
                .comment(req.getComment()).isAbsent(req.getIsAbsent())
                .happenDate(stripTimeFromDate(req.getHappenDate()))
                .isUnSuccessfulAttdate(req.getIsUnSuccessfulAttdate()).isPending(true).resolve(false)
                .isHalfDay(req.getIsHalfDay()).isAccepted(false).destination(req.getDestination())
                .category(req.getCategory()).movementType(req.getMovementType()).isLate(req.getIsLate())
                .inTime(intime).outTime(outtime)
                .isLateCover(req.getIsLateCover()).build();
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

        List<MovementAdminsEntity> admins_ = movement.getAdmins();
        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getUserId()) ||
                                userId.equals(admin.getSltId()) ||
                                userId.equals(admin.getEmployeeId())
                );
        if (!isAuthorizedAdmin) return;

        // Get admins sorted by priority (lowest priority first)
        List<MovementAdminsEntity> admins = movement.getAdmins().stream()
                .sorted(Comparator.comparingInt(MovementAdminsEntity::getHighestRolePriority))
                .collect(Collectors.toList());

        // Find the admin matching the current user
        MovementAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getUserId()) ||
                                userId.equals(admin.getSltId()) ||
                                userId.equals(admin.getEmployeeId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) return;

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
            movementAdminsRepo.save(currentAdmin);
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
        if (attendance == null) {
            return;
        }

        List<LeaveAdminsEntity> admins_ = leave.getAdmins();
        boolean isAuthorizedAdmin = admins_.stream()
                .anyMatch(admin ->
                        userId.equals(admin.getUserId()) ||
                                userId.equals(admin.getSltId()) ||
                                userId.equals(admin.getEmployeeId())
                );
        if (!isAuthorizedAdmin) return;

        // Get admins sorted by priority (lowest priority first)
        List<LeaveAdminsEntity> admins = leave.getAdmins().stream()
                .sorted(Comparator.comparingInt(LeaveAdminsEntity::getHighestRolePriority))
                .collect(Collectors.toList());

        // Find the admin matching the current user
        LeaveAdminsEntity currentAdmin = admins.stream()
                .filter(admin ->
                        userId.equals(admin.getUserId()) ||
                                userId.equals(admin.getSltId()) ||
                                userId.equals(admin.getEmployeeId()))
                .findFirst()
                .orElse(null);


        if (currentAdmin == null) return;

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
            leaveAdminsRepo.save(currentAdmin);
        }

        // Check if all admins have approved now
        boolean allApproved = admins.stream()
                .allMatch(admin -> admin.getApprovedDate() != null &&
                        Boolean.TRUE.equals(admin.getIsAccepted()));

        // If all admins have approved or there are no admins
        if (allApproved || admins.isEmpty()) {
            leave.setIsPending(false);
            leave.setIsAccepted(true);

            attendance.setResolve(true);
            attendance.setDueDateForUA(null);
            attendance.setIssues(false);

            // Save all changes
            attendanceRepo.save(attendance);
            leaveRepo.save(leave);
        }
    }

    public void approvedLeave(LeaveEntity leave) {
        AttendanceEntity attendance = leave.getAttendance();

        // Modified condition: return only if attendance is null AND none of the
        // specified conditions are true
        if (attendance == null && (leave.getUnSuccessful() && leave.getIsUnauthorized() && leave.getIsLate()
                && leave.getIsLateCover())) {
            return;
        }
        // Get admins sorted by priority (highest priority first)
        List<LeaveAdminsEntity> admins = leave.getAdmins().stream()
                .sorted(Comparator.comparingInt(LeaveAdminsEntity::getHighestRolePriority).reversed())
                .collect(Collectors.toList());

        // Process from lowest priority to highest
        boolean allApproved = true;
        for (int i = admins.size() - 1; i >= 0; i--) {
            LeaveAdminsEntity admin = admins.get(i);

            if (admin.getApprovedDate() == null) {
                // This admin hasn't approved yet
                admin.setApprovedDate(new Date());
                admin.setIsAccepted(true);
                leaveAdminsRepo.save(admin);
                allApproved = false;
                break; // Stop after the first unapproved admin is processed
            }
        }

        // If all admins have approved or we've processed the highest priority
        if (allApproved || admins.isEmpty()) {
            leave.setIsPending(false);
            leave.setIsAccepted(true);

            // Only update attendance if it's not null
            if (attendance != null) {
                attendance.setResolve(true);
                attendance.setDueDateForUA(null);
                attendance.setIssues(false);
                attendanceRepo.save(attendance);
            }

            // Save leave changes
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
                    .findByEmployeeIDAndIsManualRequest(entity.getEmployeeID(), true);
            allTheLeavesByEmployee = StreamSupport.stream(allTheLeavesByEmployee.spliterator(), false)
                    .filter(leave -> leave.getSubmitDate().equals(new Date())).collect(Collectors.toList());

            /// CHECK ARE THERE ANY MOVEMENTS
            List<MovementsEntity> allTheMovementsByEmployee = movementsRepo.findByIsPendingAndEmployeeId(true,
                    entity.getEmployeeID());
            allTheMovementsByEmployee = StreamSupport.stream(allTheMovementsByEmployee.spliterator(), false)
                    .filter(movement -> movement.getReqDate().equals(new Date())).collect(Collectors.toList());

            if (allTheLeavesByEmployee.isEmpty() || allTheMovementsByEmployee.isEmpty())
                saveNoPayEntity(entity.getEmployeeID(), null, false, false, false, false, true, entity.getDate());

            allTheLeavesByEmployee.forEach(leave -> {

                if (leave.getIsPending() && !leave.getIsAccepted()) {
                    saveNoPayEntity(entity.getEmployeeID(), null, false, false, false, false, true, entity.getDate());
                }
            });

            allTheMovementsByEmployee.forEach(movement -> {
                if (movement.getIsPending() && !movement.getIsAccepted()) {
                    saveNoPayEntity(entity.getEmployeeID(), null, false, false, false, false, true, entity.getDate());
                }
            });
        });

    }

    private boolean checkLateCoverage(Date date) {
        // Define standard workday boundaries
        LocalTime standardArrivalTime = LocalTime.of(8, 30); // 8:30 AM
        LocalTime standardDepartureTime = LocalTime.of(17, 0); // 5:00 PM

        // Get all attendance records for the given date
        List<AttendanceEntity> attendanceRecords = attendanceRepo.findByDate(date);

        for (AttendanceEntity record : attendanceRecords) {
            // Skip records without arrival or departure times
            if (record.getArrivalTime() == null || record.getLeftTime() == null) {
                continue;
            }

            // Convert SQL Time to LocalTime for calculations
            LocalTime actualArrivalTime = record.getArrivalTime().toLocalTime();
            LocalTime actualDepartureTime = record.getLeftTime().toLocalTime();

            if (actualArrivalTime.isAfter(standardArrivalTime)) {
                // Calculate minutes late
                long minutesLate = ChronoUnit.MINUTES.between(standardArrivalTime, actualArrivalTime);

                // Calculate minutes stayed beyond standard departure time
                long minutesExtra = 0;
                if (actualDepartureTime.isAfter(standardDepartureTime)) {
                    minutesExtra = ChronoUnit.MINUTES.between(standardDepartureTime, actualDepartureTime);
                }

                // If employee stayed extra time equal to or more than their late time, consider
                // it covered
                if (minutesExtra >= minutesLate) {
                    return true;
                }
            }
        }

        return false;
    }


    private List<InOutEntity> getMorningPunchOnlyRecords() {
        Date yesterdayDate = helper.getYesterdayDate();
        List<InOutEntity> result = new ArrayList<>();

        // Get all InOut records for yesterday
        List<InOutEntity> allRecords = inOutRepo.findByPunchInMoa(yesterdayDate);

        if (allRecords != null) {
            for (InOutEntity record : allRecords) {
                // Check if time morning exists but time evening is null or empty
                if (record.getTimeMoa() != null
                        && (record.getTimeEve() == null || record.getTimeEve().toString().isEmpty())) {
                    result.add(record);
                }
            }
        }

        return result;
    }


    private List<InOutEntity> getEveningPunchOnlyRecords() {
        Date yesterdayDate = helper.getYesterdayDate();
        List<InOutEntity> result = new ArrayList<>();

        // Get all InOut records for yesterday
        List<InOutEntity> allRecords = inOutRepo.findByPunchInEv(yesterdayDate);

        if (allRecords != null) {
            for (InOutEntity record : allRecords) {
                // Check if time evening exists but time morning is null or empty
                if (record.getTimeEve() != null
                        && (record.getTimeMoa() == null || record.getTimeMoa().toString().isEmpty())) {
                    result.add(record);
                }
            }
        }

        return result;
    }


    private List<InOutEntity> getHalfDayEmployees() {
        Date yesterdayDate = helper.getYesterdayDate();

        // Define half-day time boundaries
        Time halfDayStartTime = Time.valueOf("12:30:00"); // 12:30 PM
        Time workdayEndTime = Time.valueOf("17:00:00"); // 5:00 PM

        // Find employees who left between half-day time and end of workday
        List<InOutEntity> halfDayEmployees = inOutRepo.findByPunchInEvAndTimeEveBetween(yesterdayDate, halfDayStartTime,
                workdayEndTime);

        // Filter out employees who might be on half-day leave
        List<InOutEntity> result = new ArrayList<>();
        for (InOutEntity employee : halfDayEmployees) {
            // Check if employee has a valid punch-in record
            if (employee.getPunchInMoa() != null && employee.getTimeMoa() != null) {
                result.add(employee);
            }
        }

        return result;
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


    public Map<String, InOutEntity> processHalfDayEmployees(List<InOutEntity> employeesHalfDay) {
        return findEarliestEveningPunchByEmployee(employeesHalfDay);
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
        if(todayGovHoliday){
            // Use a thread-safe approach for processing employees
            all.parallelStream().forEach(employee -> {
                // Create a new attendance entity for each employee
                AttendanceEntity attendance = new AttendanceEntity();
                attendance.setEmployeeID(employee.getEmployeeId());
                attendance.setUserId(employee.getPublicId());
                attendance.setPublicId(utils.generateId(10));
                attendance.setIsHoliday(true);
                attendance.setDate(helper.getDateWithoutTime());

                // Ensure atomic save operation
                synchronized(attendanceRepo) {
                    attendanceRepo.save(attendance);
                }
            });
        }
    }

    @Override
    public void prerequisite() {
        try{
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
        Map<String, InOutEntity> halfDayEmployees = processHalfDayEmployees(employeesHalfDay);
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
                        false, true, false, null);
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
                        false, null);
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

            System.out.println("Employee Late (after 9:00)" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());
            System.out.println("  - Evening punch at: " + eveningPunch.getTimeEve());

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {

                reportAttendance(morningPunch, eveningPunch, true, false, false, true, true, false, false, true, false,
                        true, false, null);
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

            System.out.println("Employee Late (after 9:00)" + employeeId + ":");
            System.out.println("  - Morning punch at: " + morningPunch.getTimeMoa());

            employeeRepo.findBySltId(employeeId).ifPresent(employee -> {
                reportAttendance(morningPunch, false, true, true, true, false, false, false, false, false, true,
                        false, null);
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
                reportAttendance(morningPunch, eveningPunch, true, true, true, true, true, false, false, true, true,
                        true, false, null);
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

            reportAttendance(morningPunch, false, true, true, true, false, false, false, false, false, true, false,
                    null);

        }

        /// =====================================================================================
        /// employee who actually came as half-day

        System.out.println("\nEmployees who took half day:");

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

        }

        /// ================================ employee who are absent
        List<String> absentEmployeesToday = getAbsentEmployeesToday();
        reportAbsent(absentEmployeesToday);
    }

    /**
     * Process employees who have incomplete punch records (missing either morning
     * or evening punch)
     */
    private void processUnauthorizedEmployees(Set<InOutEntity> e80, Set<InOutEntity> e50) {
        HashSet<InOutEntity> morningPunchOnly = new HashSet<>(getMorningPunchOnlyRecords());
        HashSet<InOutEntity> eveningPunchOnly = new HashSet<>(getEveningPunchOnlyRecords());

        morningPunchOnly.removeAll(e80);
        morningPunchOnly.removeAll(e50);

        eveningPunchOnly.removeAll(e80);
        eveningPunchOnly.removeAll(e50);

        for (InOutEntity employee : morningPunchOnly) {
            reportAttendance(employee, false, true, false, false, false, false, false, false, true, true, false, null);
        }

        for (InOutEntity employee : eveningPunchOnly) {
            reportAttendance(employee, false, true, false, false, false, false, false, false, true, true, true, null);
        }
    }

    /**
     * Process employees who arrived late (between 8:30 and 9:00)
     */
    private void processLateEmployees(Set<InOutEntity> lateEmployees, Set<InOutEntity> stayedLateEmployees) {
        // Late employees who stayed until end of day
        Set<InOutEntity> lateButFullDay = new HashSet<>(lateEmployees);
        lateButFullDay.retainAll(stayedLateEmployees);

        // Late employees who left early
        Set<InOutEntity> lateAndLeftEarly = new HashSet<>(lateEmployees);
        lateAndLeftEarly.removeAll(stayedLateEmployees);

        // Process employees who were late but stayed for full day
        for (InOutEntity employee : lateButFullDay) {
            boolean coveredLateTime = checkLateCoverage(employee.getDate());
            if (coveredLateTime) {
                // Employee covered their late time
                reportAttendance(employee, true, false, false, true, true, false, false, true, true, true, false, null);
            } else {
                // Employee was late without covering time
                reportAttendance(employee, false, false, true, true, false, false, false, true, true, true, false,
                        null);
            }
        }

        // Process employees who were late and left early
        Set<InOutEntity> halfDayEmployees = new HashSet<>(getHalfDayEmployees());
        for (InOutEntity employee : lateAndLeftEarly) {
            boolean isHalfDay = halfDayEmployees.contains(employee);
            if (isHalfDay) {
                reportAttendance(employee, false, false, true, false, false, true, false, true, true, true, false,
                        null);
            } else {
                // Very late arrival and early departure - treat as absent
                reportAttendance(employee, false, true, false, false, false, false, true, false, true, true, true,
                        null);
            }
        }
    }

    /**
     * Process half-day employees (check for approved leaves or mark as
     * unauthorized)
     */
    private void processHalfDayEmployees(Set<InOutEntity> onTimeEmployees, Set<InOutEntity> lateEmployees,
                                         Set<InOutEntity> veryLateEmployees, Set<InOutEntity> halfDayEmployees) {

        // Filter out employees already processed in other categories
        Set<InOutEntity> unprocessedHalfDays = new HashSet<>(halfDayEmployees);
        unprocessedHalfDays.removeAll(onTimeEmployees);
        unprocessedHalfDays.removeAll(lateEmployees);
        unprocessedHalfDays.removeAll(veryLateEmployees);

        for (InOutEntity employee : unprocessedHalfDays) {
            // Check if employee has approved leave for this half-day
            List<LeaveEntity> approvedLeaves = leaveRepo.findApprovedLeavesByEmployeeIDAndFromDateAndToDate(
                    employee.getEmployeeID(), helper.getYesterdayDate(), helper.getYesterdayDate());

            if (!approvedLeaves.isEmpty()) {
                // Process employee with approved leave
                processEmployeeWithApprovedLeave(employee, approvedLeaves.get(0));
            } else {
                // Process employee without approved leave
                processEmployeeWithoutApprovedLeave(employee);
            }
        }
    }

    /**
     * Process employee with approved leave for half-day
     */
    private void processEmployeeWithApprovedLeave(InOutEntity employee, LeaveEntity leave) {
        if (attendanceRepo.existsByEmployeeIDAndDate(employee.getEmployeeID(), helper.getYesterdayDate()))
            return;
        if (leave.getIsAccepted() && leave.getToDate().equals(helper.getYesterdayDate())) {

            // Mark leave as used
            leave.setDescription("Absent - Leave Used");
            leave.setNotUsed(false);

            // Deduct from leave balance
            UserLeaveTypeRemainingEntity leaveBalance = getUserLeaveTypeRemaining(leave.getLeaveType().getName(),
                    employee.getEmployeeID());

            if (leaveBalance.getRemainingLeaves() > 1) {
                leaveBalance.setRemainingLeaves(leaveBalance.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(leaveBalance);
            }

            leaveRepo.save(leave);
            reportAttendance(employee, false, false, false, false, false, leave.getIsHalfDay(), leave.getIsFullDay(),
                    true, true, true, false, null);
        } else {
            // Leave not approved but employee absent
            processUnapprovedLeaveForAbsentee(employee, leave);
        }
    }

    /**
     * Process employee without approved leave for half-day
     */
    private void processEmployeeWithoutApprovedLeave(InOutEntity employee) {
        if (attendanceRepo.existsByEmployeeIDAndDate(employee.getEmployeeID(), helper.getYesterdayDate()))
            return;
        List<UserLeaveTypeRemainingEntity> leaveBalances = serviceEvent
                .getUserLeaveTypeRemaining(employee.getEmployeeID());
        boolean noLeavesRemaining = leaveBalances.stream().allMatch(balance -> balance.getRemainingLeaves() < 1);

        helper.handleAbsenteeReq(employee.getEmployeeID(), true, true);
        reportAttendance(employee, false, true, false, false, false, true, true, false, false, true, noLeavesRemaining,
                null);
    }

    /**
     * Process employee with unapproved leave who is absent
     */
    private void processUnapprovedLeaveForAbsentee(InOutEntity employee, LeaveEntity leave) {
        if (attendanceRepo.existsByEmployeeIDAndDate(employee.getEmployeeID(), helper.getYesterdayDate()))
            return;
        List<UserLeaveTypeRemainingEntity> leaveBalances = serviceEvent
                .getUserLeaveTypeRemaining(employee.getEmployeeID());
        boolean noLeavesRemaining = leaveBalances.stream().allMatch(balance -> balance.getRemainingLeaves() < 1);

        if (noLeavesRemaining) {
            // No remaining leaves, mark as no-pay
            leave.setIsPending(false);
            leave.setDescription("EMPLOYEE IS ABSENT ALSO HE/SHE NO LEAVES SO GOING NO PAY");

            reportAttendance(employee, false, true, false, false, false, true, true, false, true, true, true,
                    leave.getHappenDate());

            helper.handleAbsenteeReq(employee.getEmployeeID(), true, true);
        } else {
            // There are leaves available
            leave.setIsPending(true);
            leave.setDescription(
                    "EMPLOYEE IS ABSENT ALSO HE/SHE MAKE REQUEST TO LEAVE NOT APPROVED HENCE THIS LEAVE STILL PENDING");

            reportAttendance(employee, false, true, false, false, false, true, true, false, true, true, false, null);

            helper.handleAbsenteeReq(employee.getEmployeeID(), true, true);
        }
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

    public InOutEntity findEarliestEntity(List<InOutEntity> entities) {
        List<InOutEntity> copiedEntities = entities != null ? List.copyOf(entities) : Collections.emptyList();

        if (copiedEntities.isEmpty()) {
            return null;
        }

        synchronized (copiedEntities) {
            InOutEntity earliest = copiedEntities.get(0);
            long earliestMillis = getCombinedMillis(earliest);

            for (int i = 1; i < copiedEntities.size(); i++) {
                InOutEntity current = copiedEntities.get(i);
                long currentMillis = getCombinedMillis(current);

                if (currentMillis < earliestMillis) {
                    earliest = current;
                    earliestMillis = currentMillis;
                }
            }

            return earliest;
        }
    }

    private long getCombinedMillis(InOutEntity entity) {
        Objects.requireNonNull(entity, "Entity cannot be null");

        if (entity.getPunchInEv() == null || entity.getTimeEve() == null) {
            return Long.MAX_VALUE;
        }

        return entity.getPunchInEv().getTime() + entity.getTimeEve().getTime();
    }


    @Override
    public void reportAttendance(InOutEntity inout, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                 Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                 Boolean leaveReq, Boolean active, Boolean nopay, Date date) {

        if (inout.getEmployeeID() == null)
            return;

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(inout.getEmployeeID());
        if (employeeEntity.isEmpty())
            return;

        if (attendanceRepo.existsByEmployeeIDAndDate(inout.getEmployeeID(), helper.getYesterdayDate()))
            return;

        LocalTime eveStart = LocalTime.of(17, 0); // 5:00 PM
        LocalTime eveEnd = LocalTime.of(23, 0); // 11:00 PM
        Time timeEveStart = Time.valueOf(eveStart);
        Time timeEveEnd = Time.valueOf(eveEnd);

        Date yesterdayDateV2 = helper.getYesterdayDate();
        Set<InOutEntity> employeesLeftAfter5 = new HashSet<>(
                inOutRepo.findByPunchInEvAndTimeEveBetween(yesterdayDateV2, timeEveStart, timeEveEnd));

        InOutEntity earliestEntityInEve = findEarliestEntity(employeesLeftAfter5.stream().toList());

        AttendanceEntity attendance = new AttendanceEntity();
        attendance.setTerminalID(inout.getTerminalID());
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(inout.getEmployeeID());
        attendance.setDate(inout.getDate() == null ? helper.getYesterdayDate() : inout.getDate());

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

        attendance.setUserId(employeeEntity.get().getPublicId());

        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + "AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        } else if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(inout.getEmployeeID(), attendance);
            attendance.setDueDateForUA(helper.getDueDate()); /// Get all the un-successful attendance if date goes make
            /// it no pay
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);
        if (nopay)
            saveNoPayEntity(inout.getEmployeeID(), attendance, false, true, false, false, false,
                    date != null ? date : helper.getYesterdayDate());

        attendanceRepo.save(attendance);

    }

    @Override
    public void reportAttendance(InOutEntity moa, InOutEntity eve, Boolean fullday, Boolean unAuthorized,
                                 Boolean unSuccessful, Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave,
                                 Boolean leaveSuccess, Boolean leaveReq, Boolean active, Boolean nopay, Date date) {
        if (moa.getEmployeeID() == null)
            return;
        if (eve.getEmployeeID() == null)
            return;
        if (!eve.getEmployeeID().equals(moa.getEmployeeID()))
            return;

        if (attendanceRepo.existsByEmployeeIDAndDate(eve.getEmployeeID(), helper.getYesterdayDate()))
            return;

        Optional<EmployeeEntity> employeeEntity = employeeRepo.findBySltId(moa.getEmployeeID());
        if (employeeEntity.isEmpty())
            return;

        AttendanceEntity attendance = new AttendanceEntity();
        if(moa.getTerminalID().equals(eve.getTerminalID())) {
            attendance.setTerminalID(moa.getTerminalID());
        }
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(moa.getEmployeeID());
        attendance.setDate(moa.getDate() == null ? helper.getYesterdayDate() : moa.getDate());

        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);

        attendance.setArrivalDate(moa.getPunchInMoa());
        attendance.setArrivalTime(moa.getTimeMoa());
        attendance.setLeftTime(eve.getTimeEve());

        attendance.setUserId(employeeEntity.get().getPublicId());

        if (unAuthorized) {
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + "AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        } else if (unSuccessful) {
            attendance.setIssues(true);
            helper.handleLateAndUnsuccessful(moa.getEmployeeID(), attendance);
            attendance.setDueDateForUA(helper.getDueDate()); /// Get all the un-successful attendance if date goes make
            /// it no pay
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }
        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);
        if (nopay)
            saveNoPayEntity(moa.getEmployeeID(), attendance, false, true, false, false, false,
                    date != null ? date : helper.getYesterdayDate());

        attendanceRepo.save(attendance);

    }

    @Override
    public <T> void reportAttendance(Object obj, Boolean fullday, Boolean unAuthorized, Boolean unSuccessful,
                                     Boolean late, Boolean late_cover, Boolean half_day, Boolean isFullLeave, Boolean leaveSuccess,
                                     Boolean leaveReq, Boolean active, Boolean nopay, Date date) {
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
        if (attendanceRepo.existsByEmployeeIDAndDate(
                (inOutEntity != null) ? inOutEntity.getEmployeeID() : attendanceEntity.getEmployeeID(),
                helper.getYesterdayDate()))
            return;

        Optional<EmployeeEntity> employeeEntity = employeeRepo
                .findBySltId((inOutEntity != null) ? inOutEntity.getEmployeeID() : attendanceEntity.getEmployeeID());

        if (employeeEntity.isEmpty())
            return;

        // Dynamically fetch UserEntity based on the type of obj
        String userByEmployeeId = (inOutEntity != null) ? inOutEntity.getEmployeeID()
                : attendanceEntity.getEmployeeID();

        if (userByEmployeeId == null)
            return;

        if (attendanceRepo.existsByEmployeeIDAndDate(userByEmployeeId, helper.getYesterdayDate()))
            return;

        AttendanceEntity attendance = new AttendanceEntity();
        if(inOutEntity != null) {
            attendance.setTerminalID(inOutEntity.getTerminalID());
        }
        attendance.setPublicId(utils.generateId(10));
        attendance.setEmployeeID(userByEmployeeId);
        attendance.setDate((inOutEntity != null) ? inOutEntity.getDate() : attendanceEntity.getDate());

        attendance.setIsLate(late);
        attendance.setLateCover(late_cover);
        attendance.setIsUnSuccessful(unSuccessful);
        attendance.setIsUnAuthorized(unAuthorized);
        attendance.setIsFullDay(fullday);
        attendance.setIsHalfDay(half_day);
        attendance.setUserId(employeeEntity.get().getPublicId());

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
            attendance.setIssueDescription("GOING UNAUTHORIZED DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");

        } else if (unSuccessful) {
            helper.handleLateAndUnsuccessful(userByEmployeeId, attendance);
            attendance.setDueDateForUA(helper.getDueDate());
            attendance.setIssues(true);
            attendance.setIssueDescription("GOING UNSUCCESSFUL DUE TO THE  "
                    + (half_day ? "HALF DAY " : "UNKNOWN REASON PLEASE CHECK ATTENDANCE")
                    + " AND BEFORE PASS THE DUE DATE PLEASE RESOLVE IT");
        }

        attendance.setLeaveSuccess(leaveSuccess);
        attendance.setLeaveReq(leaveReq);
        attendance.setActive(active);
        attendance.setNopay(nopay);
        attendance.setIsFullLeave(isFullLeave);
        if (nopay)
            saveNoPayEntity(userByEmployeeId, attendance, false, true, false, false, false,
                    date != null ? date : helper.getYesterdayDate());

        attendanceRepo.save(attendance);
    }


    @Override
    public void reportAbsent(List<String> absentEmployeesToday) {

        absentEmployeesToday.forEach(employee -> {
            List<UserLeaveTypeRemainingEntity> userLeaveCategoryRemaining = serviceEvent
                    .getUserLeaveTypeRemaining(employee);

            /// CHECKING IF EMPLOYEE MIGHT PUT A LEAVE BEFORE SHE/HE ABSENT (FULL-DAY) --
            /// EMPLOYEE DO
            List<LeaveEntity> byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual = leaveRepo
                    .findByEmployeeIDAndFromDateLessThanEqualAndToDateGreaterThanEqual(employee, new Date(),
                            new Date());

            if (!byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.isEmpty()) { /// IF PASSES WHICH MEANS EMPLOYEE
            /// DO MAKE LEAVE

                byUserAndFromDateLessThanEqualAndToDateGreaterThanEqual.forEach(leaveEntity -> {

                    /// DOUBLE CHECK LEAVE DATE MATCH CURRENT DATE AND WHETHER LEAVE APPROVED OR NOT
                    if (leaveEntity.getIsAccepted() && leaveEntity.getToDate().equals(helper.getYesterdayDate())) {

                        leaveEntity.setDescription("Absent - Leave Used");
                        leaveEntity.setNotUsed(false); /// WHICH MEANS EMPLOYEE USE THE LEAVE

                        /// CUT OF ONE OF THE LEAVES
                        UserLeaveTypeRemainingEntity userLeaveTypeRemainingEntity = getUserLeaveTypeRemaining(
                                leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
                        if (userLeaveTypeRemainingEntity.getRemainingLeaves() > 1) {
                            userLeaveTypeRemainingEntity
                                    .setRemainingLeaves(userLeaveTypeRemainingEntity.getRemainingLeaves() - 1);
                            userLeaveTypeRemainingRepo.save(userLeaveTypeRemainingEntity);
                        }

                        leaveRepo.save(leaveEntity);

                    } else {
                        boolean allMatch = userLeaveCategoryRemaining.stream()
                                .allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
                        helper.handleAbsenteeReq(employee, true, true);
                        reportAttendance(employee, false, true, false, false, false, true, true, false, false, true,
                                allMatch, null);

                    }

                });
            } else {
                boolean allMatch = userLeaveCategoryRemaining.stream()
                        .allMatch(userLeaveTypeRemaining -> userLeaveTypeRemaining.getRemainingLeaves() < 1);
                helper.handleAbsenteeReq(employee, true, true);
                reportAttendance(employee, false, true, false, false, false, false, false, false, false, true, allMatch,
                        null);
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
        absenteeEntity.setAudited(0);
        absenteeEntity.setIsNoPay(0);
        absenteeRepo.save(absenteeEntity);
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

        // Create and populate the entity using builder
        return LeaveEntity.builder().publicId(id) // Generate a unique ID
                .employeeID(emp).userId(leaveReq.getUserId()).submitDate(helper.removeTimeFromDate(new Date())) // Current date
                .fromDate(stripTimeFromDate(leaveReq.getFromDate())).toDate(stripTimeFromDate(leaveReq.getToDate()))
                .happenDate(stripTimeFromDate(leaveReq.getHappenDate())).leaveType(type)
                .numOfDays(leaveReq.getNumOfDays()).description(leaveReq.getDescription())
                .isHalfDay(leaveReq.getIsHalfDay() != null ? leaveReq.getIsHalfDay() : false)
                .isFullDay(leaveReq.getIsFullDay() != null ? leaveReq.getIsFullDay() : true) // Use value from request
                .isManualRequest(leaveReq.getIsManualRequest() != null ? leaveReq.getIsManualRequest() : false)
                .isUnauthorized(leaveReq.getIsUnauthorized() != null ? leaveReq.getIsUnauthorized() : false) // Add
                // missing
                // field
                .isAbsent(leaveReq.getIsAbsent() != null ? leaveReq.getIsAbsent() : false) // Add missing field
                .isLateCover(leaveReq.getIsLateCover() != null ? leaveReq.getIsLateCover() : false) // Add new field
                .isLate(leaveReq.getIsLate() != null ? leaveReq.getIsLate() : false) // Add new field
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
        Optional<EmployeeEntity> optional = employeeRepo.findByPublicId(userId);
        if (optional.isEmpty()) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        if(leaveRepo.findByEmployeeIDAndSubmitDate(userId, helper.removeTimeFromDate(new Date())).isPresent()) {
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
        LeaveEntity leaveEntity = transformToEntity(req, employee.getEmployeeId(), leaveId, leaveTypeRepository);

        if (req.getIsHalfDay() || req.getIsUnauthorized() || req.getIsAbsent() || req.getUnSuccessful()
                || req.getIsLate() || req.getIsLateCover()) {

            Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeIDAndArrivalDate(
                    convertCode(leaveEntity.getEmployeeID()), leaveEntity.getHappenDate());

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

            final List<LeaveAdminsEntity> adminEntities = Collections.synchronizedList(new ArrayList<>());

            synchronized (leaveAdminsRepo) { // Use the correct repository for synchronization
                for (Map.Entry<String, UserRest> entry : userMap.entrySet()) {
                    UserRest value = entry.getValue();
                    LeaveAdminsEntity admin = createLeaveAdminEntity(value, leaveId);
                    LeaveAdminsEntity savedAdmin = leaveAdminsRepo.save(admin);
                    adminEntities.add(savedAdmin);
                }
            }

            leaveEntity.setAdmins(adminEntities);
        }

        List<AbsenteeEntity> absenteeEntities = null;
        if (Boolean.TRUE.equals(req.getAbsent())) {
            absenteeEntities = absenteeRepo.findByEmployeeID(employeeId);

            if (absenteeEntities != null && !absenteeEntities.isEmpty() && req.getHappenDate() != null) {
                absenteeEntities = absenteeEntities.stream().filter(
                                absentee -> absentee.getDate() != null && absentee.getDate().equals(req.getHappenDate()))
                        .collect(Collectors.toList());
            }
        }
        synchronized (this) {
            lmsService.saveLeave(leaveEntity);
        }

        if (Boolean.TRUE.equals(req.getUnauthorized()) || Boolean.TRUE.equals(req.getIsHalfDay())
                || Boolean.TRUE.equals(req.getUnSuccessful()) || Boolean.TRUE.equals(req.getIsLateCover())
                || Boolean.TRUE.equals(req.getIsLate())) {
            processUnauthorizedLeave(leaveEntity);
        }

        if (absenteeEntities != null && !absenteeEntities.isEmpty()) {
            processAbsenteeRecords(absenteeEntities, employeeId, leaveEntity);
        }
    }

    // Helper method to process unauthorized leave
    private void processUnauthorizedLeave(LeaveEntity leaveEntity) {
        Optional<AttendanceEntity> attendanceEntityOp = attendanceRepo.findByEmployeeIDAndArrivalDate(
                convertCode(leaveEntity.getEmployeeID()), leaveEntity.getHappenDate());
        if (attendanceEntityOp.isPresent()) {

            AttendanceEntity attendanceEntity = attendanceEntityOp.get();
            attendanceEntity.setResolve(true);
            attendanceEntity.setIssues(false);
            attendanceEntity.setViaLeave(true);

            leaveEntity.setIsAccepted(true);
            leaveRepo.save(leaveEntity);
            attendanceRepo.save(attendanceEntity);

            UserLeaveTypeRemainingEntity userLeaveTypeRemaining = getUserLeaveTypeRemaining(
                    leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());


            if (userLeaveTypeRemaining != null && userLeaveTypeRemaining.getRemainingLeaves() > 0) {
                userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
            }
        } else {
            throw new IllegalArgumentException("Failed to process leave request: No attendance record found");
        }
    }

    private void processAbsenteeRecords(List<AbsenteeEntity> absenteeEntities, String employeeId,
                                        LeaveEntity leaveEntity) {
        for (AbsenteeEntity absenteeEntity : absenteeEntities) {

            absenteeRepo.save(absenteeEntity);

            // Update leave balance
            UserLeaveTypeRemainingEntity userLeaveTypeRemaining = getUserLeaveTypeRemaining(
                    leaveEntity.getLeaveType().getName(), leaveEntity.getEmployeeID());
            if (userLeaveTypeRemaining != null && userLeaveTypeRemaining.getRemainingLeaves() > 0) {
                userLeaveTypeRemaining.setRemainingLeaves(userLeaveTypeRemaining.getRemainingLeaves() - 1);
                userLeaveTypeRemainingRepo.save(userLeaveTypeRemaining);
            }

            // Update attendance record
            if (absenteeEntity.getDate() != null) {
                Optional<AttendanceEntity> byUserAndDate = attendanceRepo.findByEmployeeIDAndDate(employeeId,
                        absenteeEntity.getDate());
                if (byUserAndDate.isPresent()) {
                    AttendanceEntity attendanceEntity = byUserAndDate.get();
                    attendanceEntity.setResolve(true);
                    attendanceEntity.setIssues(false);
                    attendanceEntity.setViaLeave(true);
                    attendanceRepo.save(attendanceEntity);
                }
            }
        }
    }


    @Override
    public void getAllTheInOutRecordsFromSLT() {
        /// First get the all the data and using employee id query the our local
        /// database
    }

    @Override
    public void processLeave(String leaveId) {
        Optional<LeaveEntity> leaveEntityOp = leaveRepo.findByPublicId(leaveId);
        if (leaveEntityOp.isPresent()) {
            LeaveEntity leaveEntity = leaveEntityOp.get();
            approvedLeave(leaveEntity);
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
