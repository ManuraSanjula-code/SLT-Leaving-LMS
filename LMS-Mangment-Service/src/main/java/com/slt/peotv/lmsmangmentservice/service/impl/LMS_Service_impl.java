package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.ComponetAdminsEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
import com.slt.peotv.lmsmangmentservice.entity.Enum.PayStatus;
import com.slt.peotv.lmsmangmentservice.entity.Enum.RequestStatus;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.exceptions.LMSServiceException_AllReadyExits;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.req.*;
import com.slt.peotv.lmsmangmentservice.model.res.DashBoardRes;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import com.slt.peotv.lmsmangmentservice.utils.service.LMSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;

@Service
public class LMS_Service_impl implements LMS_Service {
    @Autowired
    private LMSUtils lmsUtils;
    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private LeaveRepo leaveRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepo;
    @Autowired
    private MovementsRepo movementsRepo;
    @Autowired
    private NoPayRepo noPayRepo;
    @Autowired
    private ComponetAdminsRepo componetAdminsRepo;
    @Autowired
    private Utils utils;
    @Autowired
    private UserLeaveTypeRemainingRepo leaveTypeRemaiRepo;
    @Autowired
    private UserLeaveTypeTotalRepo leaveTypeTotRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepository;
    @Autowired
    private AccessLogRepo accessLogRepo;
    @Autowired
    private InOutRepo inOutRepo;
    @Autowired
    private Helper helper;
    @Autowired
    private Check_Service check_Service;
    @Autowired
    private NoPayReasonRepo noPayReasonRepo;

    @Override
    public List<InOutDTO> getAllInOuts(String id, boolean swap) {
        return List.of();
    }

    @Override
    public Page<AttendanceDTO> getAllAttendance(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findAll(pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public void makeInAttendanceActive(String publicId) {
        AttendanceEntity attendanceEntity = attendanceRepo.findByPublicId(publicId).orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        attendanceEntity.setIsActive(false);
        attendanceRepo.save(attendanceEntity);
    }

    @Override
    public DashBoardRes getDashBoard(String userId) {
        Optional<EmployeeEntity> employee = helper.getEmployeeByIdV2(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();
        List<AttendanceEntity> attedance = attendanceRepo.findByEmployee(employeeEntity);

        EmployeeEntity emp = employee.get();

        List<UserLeaveTypeRemainingEntity> remain = leaveTypeRemaiRepo.findByEmployee(emp);

        int totalRemainingLeaves = remain.stream()
                .filter(leave -> leave.getRemainingLeaves() != null)
                .mapToInt(UserLeaveTypeRemainingEntity::getRemainingLeaves)
                .sum();
        int sum = leaveTypeTotRepo.findByEmployee(emp).stream()
                .filter(leave -> leave.getTotalLeaves() != null)
                .mapToInt(UserLeaveTypeTotalEntity::getTotalLeaves)
                .sum();
        int total = attedance.stream().map(AttendanceEntity::getIsFullDay).toList().size();
        String name = employee.get().getFirstName() + " " + employee.get().getLastName();

        Map<String, Integer> remainLeaveDistribution = leaveTypeRemaiRepo.findByEmployee(emp)
                .stream()
                .collect(Collectors.toMap(
                        leave -> leave.getLeaveType().getName(),
                        UserLeaveTypeRemainingEntity::getRemainingLeaves,
                        (existing, replacement) -> existing
                ));

        LocalDate yearStart = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();
        Date yearStartDate = Date.from(yearStart.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date todayEndDate = Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());

        List<AttendanceEntity> attendanceThisYear = attendanceRepo.findByEmployeeAndDateBetween(employee.get(), yearStartDate, todayEndDate);

        Map<String, Integer> monthlyAttendanceDistribution = attendanceThisYear.stream()
                .filter(attendance -> Boolean.TRUE.equals(attendance.getIsFullDay()))
                .filter(attendance -> Boolean.TRUE.equals(attendance.getIsActive()))
                .collect(Collectors.groupingBy(
                        attendance -> {
                            LocalDate date = attendance.getDate().toInstant()
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();
                            return date.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                        },
                        Collectors.collectingAndThen(
                                Collectors.counting(),
                                Math::toIntExact
                        )
                ));

        DashBoardRes dashBoardRes = new DashBoardRes();

        if (today.getDayOfWeek() == DayOfWeek.MONDAY){

            LocalDate lastFriday = today.minusDays(3);
            Date fridayDate = Date.from(lastFriday.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Optional<InOutEntity> earliestByEmployeeIdAndDateLast = inOutRepo.findEarliestByEmployeeIdAndDate(employeeEntity.getSltId(), fridayDate);
            Optional<InOutEntity> latestByEmployeeIdAndDateLast = inOutRepo.findLatestByEmployeeIdAndDate(employeeEntity.getSltId(), fridayDate);

            earliestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setLastPunch(inOutEntity.getPunchTypeTimeAsString()));
            latestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setLastPunch((dashBoardRes.getLastPunch().isEmpty() || dashBoardRes.getLastPunch() == null ? "": dashBoardRes.getLastPunch() + " - " + inOutEntity.getPunchTypeTimeAsString()).trim()));

        }else{

            Optional<InOutEntity> earliestByEmployeeIdAndDateLast = inOutRepo.findEarliestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.getYesterdayDate());
            Optional<InOutEntity> latestByEmployeeIdAndDateLast = inOutRepo.findLatestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.getYesterdayDate());

            earliestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setLastPunch(inOutEntity.getPunchTypeTimeAsString()));
            latestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setLastPunch((dashBoardRes.getLastPunch().isEmpty() || dashBoardRes.getLastPunch() == null ? "": dashBoardRes.getLastPunch() + " - " + inOutEntity.getPunchTypeTimeAsString()).trim()));
        }

        /* AccessLogEntity todayEarliestAccessLogsByEmployee = check_Service.getTodayEarliestAccessLogsByEmployee(employeeEntity.getSltId());
        AccessLogEntity todayLatestAccessLogsByEmployee = check_Service.getTodayLatestAccessLogsByEmployee(employeeEntity.getSltId()); */

        /* if(todayEarliestAccessLogsByEmployee != null)
            dashBoardRes.setNowPunch(todayEarliestAccessLogsByEmployee.getLogTime());

        if(todayLatestAccessLogsByEmployee != null)
            dashBoardRes.setNowPunch(dashBoardRes.getLastPunch() + " - " + todayLatestAccessLogsByEmployee.getLogTime());

        if(dashBoardRes.getNowPunch() == null) dashBoardRes.setNowPunch("NOT FOUND"); */

        Optional<InOutEntity> earliestByEmployeeIdAndDateLast = inOutRepo.findEarliestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.removeTimeFromDate(new Date()));
        Optional<InOutEntity> latestByEmployeeIdAndDateLast = inOutRepo.findLatestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.removeTimeFromDate(new Date()));

        earliestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setNowPunch(inOutEntity.getPunchTypeTimeAsString()));
        latestByEmployeeIdAndDateLast.ifPresent(inOutEntity -> dashBoardRes.setNowPunch((dashBoardRes.getNowPunch().isEmpty() || dashBoardRes.getNowPunch() == null ? "": dashBoardRes.getNowPunch() + " - " + inOutEntity.getPunchTypeTimeAsString()).trim()));

        if(dashBoardRes.getNowPunch() == null) dashBoardRes.setNowPunch("NOT FOUND");
        if(dashBoardRes.getLastPunch() == null) dashBoardRes.setLastPunch("NOT FOUND");

        dashBoardRes.setTotalAttendance(total);
        dashBoardRes.setRemainLeaveDistribution(remainLeaveDistribution);
        dashBoardRes.setMonthlyAttendanceDistribution(monthlyAttendanceDistribution);
        dashBoardRes.setName(name);
        dashBoardRes.setTotalLeave(sum);
        dashBoardRes.setLeaveBalance(totalRemainingLeaves);
        return dashBoardRes;
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByEmployee(helper.getEmployeeById(userId), pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUn(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrue(pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnA(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnauthorizedTrue(pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrueAndEmployee(helper.getEmployeeById(userId), pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnAByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnauthorizedTrueAndEmployee(helper.getEmployeeById(userId), pageable);
        return attendanceEntityPage.map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public List<AttendanceEntity> getAttendanceByUserId(String employeeId) {
        return attendanceRepo.findByEmployee(helper.getEmployeeById(employeeId));
    }

    @Override
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId) {
        return attendanceRepo.findByEmployee(helper.getEmployeeById(employeeId));
    }

    @Override
    public void createMovements(MovementsEntity entity) {
        entity.setUpdateDate(new Date());
        MovementsEntity movementsEntity = movementsRepo.save(entity);
    }

    @Override
    public void deleteAttendance(String publicId) {
        AttendanceEntity attendance = attendanceRepo.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found or cannot be deleted"));

        attendanceRepo.delete(attendance);
    }

    @Override
    public void deleteAttendanceV1(String publicId) {
        AttendanceEntity entity = attendanceRepo.findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("Attendance record not found or cannot be deleted"));
        entity.setIsActive(false);
        attendanceRepo.save(entity);
    }

    @Override
    public Page<MovementDTO> getAllMovementByUser(String employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAllByEmployee(helper.getEmployeeById(employeeId), pageable);
        return allByUser.map(lmsUtils::toMovementDTO);
    }

    @Override
    public Page<MovementDTO> getAllMovementByAdmin(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<EmployeeEntity> employee = helper.getEmployeeByIdV2(userId);
        if (employee.isEmpty()) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        return employee.map(employeeEntity -> {
            Page<ComponetAdminsEntity> coAdminsPage = componetAdminsRepo.findByEmployee(employeeEntity, pageable);
            List<MovementDTO> movementDTOs = coAdminsPage.getContent().stream()
                    .map(coAdminsEntity -> {
                        Optional<MovementsEntity> movement = movementsRepo.findByPublicId(coAdminsEntity.getComponetID());
                        return movement.map(lmsUtils::toMovementDTO).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new PageImpl<>(movementDTOs, pageable, coAdminsPage.getTotalElements());
        }).orElseGet(() -> new PageImpl<>(Collections.emptyList()));
    }

    @Override
    public Page<MovementDTO> getAllMovements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAll(pageable);
        return allByUser.map(lmsUtils::toMovementDTO);
    }

    @Override
    public MovementsEntity getMovement(String publicId) {
        return movementsRepo.findByPublicId(publicId).orElseThrow(
                () -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage())
        );
    }

    @Override
    public void updateMovement(MovementReq req, String publicId) {
        MovementsEntity movementsEntity = movementsRepo.findByPublicId(publicId)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        if (movementsEntity.getRequestStatus().equals(RequestStatus.CANCELLED) || movementsEntity.getRequestStatus().equals(RequestStatus.REJECTED)
                || movementsEntity.getRequestStatus().equals(RequestStatus.APPROVED)) return;

        EmployeeEntity employee = movementsEntity.getEmployee();

        if(req.getHappenDate() != null && employee != null){
            req.setHappenDate(helper.removeTimeFromDate(req.getHappenDate()));
            Optional<AttendanceEntity> attendanceEntity = attendanceRepo.findByEmployeeAndArrivalDateAndIsActiveTrue(
                    employee, req.getHappenDate());
            if(attendanceEntity.isPresent()){
                AttendanceEntity attendanceEntity_ = attendanceEntity.get();
                if(attendanceEntity_.getIsUnauthorized() && !attendanceEntity_.getIsResolved() && !attendanceEntity_.getIsUnSuccessful() && attendanceEntity_.getHasIssues()){
                    movementsEntity.setAttendance(attendanceEntity_);
                    movementsEntity.setHappenDate(req.getHappenDate());
                    if(req.getHappenDateRaw() != null)
                        movementsEntity.setHappenDateRaw(req.getHappenDateRaw());
                }
            }
        }

        if (req.getMovementType() != null)
            movementsEntity.setMovementType(req.getMovementType());
        if (req.getComment() != null)
            movementsEntity.setComment(req.getComment());
        if (req.getDestination() != null)
            movementsEntity.setDestination(req.getDestination());
        if (req.getCategory() != null)
            movementsEntity.setCategory(req.getCategory());
        if (req.getHappenDate() != null)
            movementsEntity.setHappenDate(req.getHappenDate());
        if (req.getLogTime() != null)
            movementsEntity.setLogTime(req.getLogTime());
        if (req.getInTime() != null)
            movementsEntity.setInTime(req.getInTime());
        if (req.getOutTime() != null)
            movementsEntity.setOutTime(req.getOutTime());
        if (req.getIsEdited() != null) {
            movementsEntity.setIsEdited(req.getIsEdited());
        }
        if (req.getRequestStatus() != null)
            movementsEntity.setRequestStatus(req.getRequestStatus());

        if(req.getInTimeRaw() != null)
            movementsEntity.setInTimeRaw(req.getInTimeRaw());
        if(req.getOutTimeRaw() != null)
            movementsEntity.setOutTimeRaw(req.getOutTimeRaw());    

        movementsEntity.setUpdateDate(new Date());
        movementsRepo.save(movementsEntity);
    }

    @Override
    public void deleteMovements(String publicId) {
        MovementsEntity movement = getMovement(publicId);
        if (movement == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        if (movement.getRequestStatus().equals(RequestStatus.CANCELLED) || movement.getRequestStatus().equals(RequestStatus.REJECTED)
                || movement.getRequestStatus().equals(RequestStatus.APPROVED)) return;

        movement.setRequestStatus(RequestStatus.CANCELLED);
        movementsRepo.save(movement);
    }

    @Override
    public void createNoPay(NoPayEntity entity) {
        noPayRepo.save(entity);
    }

    @Override
    public Page<NopayDTO> getAllNoPayByUser(String employeeId, int page, int size) {
        EmployeeEntity employeeEntity = helper.getEmployeeByIdV2(employeeId).orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate")
                .and(Sort.by(Sort.Direction.DESC, "submissionDate"));
        Pageable pageable = PageRequest.of(page, size, sort);        Page<NoPayEntity> byUser = noPayRepo.findByEmployee(employeeEntity, pageable);
        return byUser.map(lmsUtils::toNopayDTO);
    }

    @Override
    public Page<NopayDTO> getAllNoPays(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdDate")
                .and(Sort.by(Sort.Direction.DESC, "submissionDate"));
        Pageable pageable = PageRequest.of(page, size, sort);        return noPayRepo.findAll(pageable).map(lmsUtils::toNopayDTO);
    }

    @Override
    public NoPayEntity getNoPay(String publicId) {
        return noPayRepo.findByPublicId(publicId).orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
    }

    @Override
    public void deleteNoPay(String publicId) {
        NoPayEntity noPayEntity = noPayRepo.findByPublicId(publicId).orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        NoPayReasonEntity noPayReasonEntitiesByNoPay = noPayReasonRepo.findNoPayReasonEntitiesByNoPay(noPayEntity)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        AttendanceEntity attendance = noPayEntity.getAttendance();
        if(attendance != null) {
            attendance.setPayStatus(null);
            attendanceRepo.save(attendance);
        }
        noPayReasonRepo.delete(noPayReasonEntitiesByNoPay);
        noPayRepo.delete(noPayEntity);
    }

    @Override
    public void saveLeave(LeaveEntity entity) {
        entity.setUpdateDate(new Date());
        leaveRepo.save(entity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserId(String userId, int page, int size) {
        EmployeeEntity employeeEntity = helper.getEmployeeByIdV2(userId)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findByEmployee(employeeEntity, pageable);
        return leaveEntityPage.map(lmsUtils::toLeaveDTO);
    }

    @Override
    public Page<LeaveDTO> getAllLeaves(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findAll(pageable);
        return leaveEntityPage.map(lmsUtils::toLeaveDTO);

    }

    @Override
    public LeaveEntity getOneLeave(String publicId) {
        return leaveRepo.findByPublicId(publicId).orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
    }

    @Override
    public void deleteLeave(String publicId) {
        LeaveEntity leaveEntity = leaveRepo.findByPublicId(publicId).
                orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        if (leaveEntity.getRequestStatus().equals(RequestStatus.CANCELLED) || leaveEntity.getRequestStatus().equals(RequestStatus.REJECTED) ||
                leaveEntity.getRequestStatus().equals(RequestStatus.APPROVED)) return;

        leaveEntity.setRequestStatus(RequestStatus.CANCELLED);
        leaveRepo.save(leaveEntity);
    }

    @Override
    public void saveLeaveType(String name) {
        if (getLeaveType(name) != null)
            throw new LMSServiceException_AllReadyExits(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        LeaveTypeEntity leaveTypeEntity = new LeaveTypeEntity();
        leaveTypeEntity.setName(name);
        leaveTypeEntity.setPublicId(utils.generateId(10));
        leaveTypeRepo.save(leaveTypeEntity);
    }

    @Override
    public LeaveTypeEntity getLeaveType(String name) {
        if (name != null) {
            Optional<LeaveTypeEntity> result = leaveTypeRepo.findByName(name);
            if (result.isPresent()) {
                return result.get();
            }
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        return null;
    }

    @Override
    public void updateLeaveType(String old_name, String name) {
        LeaveTypeEntity leaveTypeEntity = leaveTypeRepo.findByName(old_name)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        leaveTypeEntity.setName(name);
        leaveTypeRepo.save(leaveTypeEntity);
    }

    @Override
    public void updateLeaveType(String old_name, String userId, int days) {
        LeaveTypeEntity leaveTypeEntity = leaveTypeRepo.findByName(old_name)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        EmployeeEntity employee = helper.getEmployeeById(userId);
        if (employee == null)
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        UserLeaveTypeRemainingEntity type = leaveTypeRemaiRepo.findByEmployeeAndLeaveType(employee, leaveTypeEntity);
        type.setRemainingLeaves(days);
        leaveTypeRemaiRepo.save(type);
    }

    @Override
    public void deleteLeaveType(String name) {
        LeaveTypeEntity leaveTypeEntity = leaveTypeRepo.findByName(name)
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        leaveTypeRepo.delete(leaveTypeEntity);
    }

    @Override
    public LeaveTypeTotDTO getTotalLeaves(String employeeId, String leaveTypeName) {
        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployee(helper.getEmployeeById(employeeId));

        for (UserLeaveTypeTotalEntity entity : totalEntities) {
            if (entity.getLeaveType().getName().equals(leaveTypeName)) {
                LeaveTypeTotDTO dto = new LeaveTypeTotDTO();
                dto.setName(leaveTypeName);
                dto.setRemainLeave(entity.getTotalLeaves());
                return dto;
            }
        }
        return new LeaveTypeTotDTO();
    }

    @Override
    public LeaveTypeRetDTO getRemainingLeaves(String employeeId, String leaveTypeName) {
        EmployeeEntity employee = helper.getEmployeeById(employeeId);
        if (employee == null)
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo.findByEmployee(employee);

        for (UserLeaveTypeRemainingEntity entity : remainingEntities) {
            if (entity.getLeaveType().getName().equals(leaveTypeName)) {
                LeaveTypeRetDTO dto = new LeaveTypeRetDTO();
                dto.setName(leaveTypeName);
                dto.setRemainLeave(entity.getRemainingLeaves());
                return dto;
            }
        }
        return new LeaveTypeRetDTO();
    }

    @Override
    public UserLeaveDetailsDTO getAllLeaveDetails(String userId) {
        EmployeeEntity employee = helper.getEmployeeById(userId);
        if (employee == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployee(employee);
        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo.findByEmployee(employee);

        if (totalEntities == null || totalEntities.isEmpty() || remainingEntities == null || remainingEntities.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Map<Long, Integer> remainingLeavesMap = new HashMap<>();
        for (UserLeaveTypeRemainingEntity entity : remainingEntities) {
            remainingLeavesMap.put(entity.getLeaveType().getId(), entity.getRemainingLeaves());
        }

        List<LeaveDetailDTO> leaveDetails = new ArrayList<>();

        for (UserLeaveTypeTotalEntity totalEntity : totalEntities) {
            LeaveTypeEntity leaveType = totalEntity.getLeaveType();
            LeaveDetailDTO detailDto = new LeaveDetailDTO();
            detailDto.setLeaveTypeName(leaveType.getName());
            detailDto.setTotalLeaves(totalEntity.getTotalLeaves());

            Integer remainingLeaves = remainingLeavesMap.getOrDefault(leaveType.getId(), 0);
            detailDto.setRemainingLeaves(remainingLeaves);

            leaveDetails.add(detailDto);
        }

        UserLeaveDetailsDTO userLeaveDetailsDto = new UserLeaveDetailsDTO();
        userLeaveDetailsDto.setEmployeeId(userId);
        userLeaveDetailsDto.setLeaveDetails(leaveDetails);

        return userLeaveDetailsDto;
    }

    @Override
    public void updateLeave(LeaveReq req, String leaveId) {
        Optional<LeaveEntity> byPublicId = leaveRepo.findByPublicId(leaveId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        LeaveEntity leaveEntity = byPublicId.get();

        if (leaveEntity.getRequestStatus().equals(RequestStatus.CANCELLED) || leaveEntity.getRequestStatus().equals(RequestStatus.REJECTED)
                || leaveEntity.getRequestStatus().equals(RequestStatus.APPROVED)) return;

        if (req.getLeaveType() != null && !req.getLeaveType().trim().isEmpty()) {
            LeaveTypeEntity type = leaveTypeRepository.findByName(req.getLeaveType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave type: " + req.getLeaveType()));
            leaveEntity.setLeaveType(type);
        }
        if (req.getFromDate() != null) {
            leaveEntity.setFromDate(lmsUtils.stripTimeFromDate(req.getFromDate()));
        }
        if (req.getToDate() != null) {
            leaveEntity.setToDate(lmsUtils.stripTimeFromDate(req.getToDate()));
        }
        if (req.getHappenDate() != null) {
            leaveEntity.setHappenDate(lmsUtils.stripTimeFromDate(req.getHappenDate()));
        }
        if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
            leaveEntity.setDescription(req.getDescription());
        }
        if (req.getNumOfDays() != null) {
            leaveEntity.setNumOfDays(req.getNumOfDays());
        }
        if (req.getIsManualRequest() != null) {
            leaveEntity.setIsManualRequest(req.getIsManualRequest());
        }
        if (req.getIsEdited() != null) {
            leaveEntity.setIsEdited(req.getIsEdited());
        }
        if (req.getNotUsed() != null) {
            leaveEntity.setNotUsed(req.getNotUsed());
        }
        if (req.getComponentBehavior() != null)
            leaveEntity.setComponentBehavior(req.getComponentBehavior());

        if (req.getRequestStatus() != null)
            leaveEntity.setRequestStatus(req.getRequestStatus());

        leaveEntity.setUpdateDate(new Date());
        leaveRepo.save(leaveEntity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(String userId, int page, int size) {
        if (userId == null || userId.trim().isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Pageable pageable = PageRequest.of(page, size);
        Optional<EmployeeEntity> em = helper.getEmployeeByIdV2(userId);
        if (em.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return em.map(employeeEntity -> componetAdminsRepo.findByEmployee(employeeEntity, pageable).map(componetAdmins -> {
            Optional<LeaveEntity> publicId = leaveRepo.findByPublicId(componetAdmins.getComponetID());
            return publicId.map(lmsUtils::toLeaveDTO).orElse(null);
        })).orElseGet(Page::empty);
    }

    @Override
    public AttendanceDTO createAttendance(AttendanceReq req) {
        AttendanceEntity attendanceEntity = lmsUtils.toAttendanceEntity(req);
        if(attendanceEntity == null) throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        EmployeeEntity employee_ = attendanceEntity.getEmployee();
        if(employee_ == null) throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        if (attendanceRepo.existsByEmployeeAndDate(employee_, helper.removeTimeFromDate(req.getDate()))) {
            return null;
        }

        if (attendanceRepo.existsByEmployeeAndArrivalDateAndArrivalTime(employee_, helper.removeTimeFromDate(req.getArrivalDate()), req.getArrivalTime())) {
            return null;
        } 
        if(!employee_.getRoaster() && attendanceEntity.isArrivalOnWeekend()) return null;
       
        
        AttendanceEntity saved = attendanceRepo.save(attendanceEntity);
        EmployeeEntity employee = saved.getEmployee();
        if (saved.getPayStatus() != null) {
            if (saved.getPayStatus().equals(PayStatus.NO_PAY)) {
                check_Service.saveNoPayEntity(employee, attendanceEntity, check_Service.createNoPayRequest(req.getIsHalfDay(), req.getIsUnSuccessful(), req.getIsUnAuthorized(),
                        req.getIsLate(), req.getLateCover(), req.getIsAbsent()), attendanceEntity.getArrivalDate() == null ? attendanceEntity.getDate() : req.getArrivalDate());
            }
        }
        if (saved.getIsUnSuccessful()) helper.handleLateAndUnsuccessful(employee.getEmployeeId(), attendanceEntity, true);
        return lmsUtils.toAttendanceDTO(saved);
    }

    @Override
    public AttendanceDTO updateAttendance(AttendanceReq req, String publicId) {
        Optional<AttendanceEntity> opt = attendanceRepo.findByPublicId(publicId);
        if (opt.isPresent()) {
            AttendanceEntity attendanceEntity = opt.get();

            PayStatus originalPayStatus = attendanceEntity.getPayStatus();
            Boolean originalIsUnSuccessful = attendanceEntity.getIsUnSuccessful();

            lmsUtils.updateAttendanceEntityFromReq(attendanceEntity, req);

            AttendanceEntity saved = attendanceRepo.save(attendanceEntity);
            EmployeeEntity employee = saved.getEmployee();

            if (saved.getPayStatus() != null) {
                if ((originalPayStatus == null || !originalPayStatus.equals(PayStatus.NO_PAY))
                        && saved.getPayStatus().equals(PayStatus.NO_PAY)) {

                    check_Service.saveNoPayEntity(employee, saved,
                            check_Service.createNoPayRequest(req.getIsHalfDay(), req.getIsUnSuccessful(),
                                    req.getIsUnAuthorized(), req.getIsLate(), req.getLateCover(), req.getIsAbsent()),
                            saved.getArrivalDate() == null ? saved.getDate() : req.getArrivalDate());
                }
            }

            if ((originalIsUnSuccessful == null || !originalIsUnSuccessful) && saved.getIsUnSuccessful()) {
                helper.handleLateAndUnsuccessful(employee.getEmployeeId(), saved, true);
            }

            return lmsUtils.toAttendanceDTO(saved);
        } else {
            return null;
        }
    }

    @Override
    public void createAccessLog(AccessLogReq req) {
        AccessLogEntity accessLogEntity = lmsUtils.toAccessLogEntity(req);
        accessLogEntity.setIsManual(true);
        accessLogEntity.setUpdatedDate(new Date());
        AccessLogEntity saved = accessLogRepo.save(accessLogEntity);
    }

    @Override
    public void createInout(InOutReq req) {

    }

    @Override
    public List<AccessLogEntity> getAccessLog(String employeeId, String date) {
        return List.of();
    }

    @Override
    public List<AttendanceDTO> getAttendance(String employeeId, String date) {
        return List.of();
    }

    @Override
    public Page<AttendanceDTO> getAllAbsent(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return attendanceRepo.findByAttendanceType(AttendanceType.ABSENT, pageable).map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAbsentByUser(int page, int size, String user) {
        EmployeeEntity employee = helper.getEmployeeById(user);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"));
        return attendanceRepo.findByEmployeeAndAttendanceType(employee, AttendanceType.ABSENT, pageable).map(lmsUtils::toAttendanceDTO);
    }

    @Override
    public Optional<InOutDTO> getEarliestInOut(String employeeID) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(employeeID);
        return inOutRepo.findEarliestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.getYesterdayDate())
                .map(lmsUtils::inOutDTO);
    }

    @Override
    public Optional<InOutDTO> getLatestInOut(String employeeID) {
        EmployeeEntity employeeEntity = helper.getEmployeeById(employeeID);
        return inOutRepo.findLatestByEmployeeIdAndDate(employeeEntity.getSltId(), helper.getYesterdayDate())
                .map(lmsUtils::inOutDTO);
    }
}
