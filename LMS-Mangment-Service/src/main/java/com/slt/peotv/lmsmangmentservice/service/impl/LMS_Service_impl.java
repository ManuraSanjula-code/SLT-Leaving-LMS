package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveTra;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementTra;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.exceptions.LMSServiceException_AllReadyExits;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.req.AttendanceReq;
import com.slt.peotv.lmsmangmentservice.model.req.LeaveReq;
import com.slt.peotv.lmsmangmentservice.model.req.MovementReq;
import com.slt.peotv.lmsmangmentservice.model.res.DashBoardRes;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import com.slt.peotv.lmsmangmentservice.utils.service.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LMS_Service_impl implements LMS_Service {
    @Autowired
    private AbsenteeRepo absenteeRepo;
    @Autowired
    private AttendanceRepo attendanceRepo;
    @Autowired
    private AttendanceTypeRepo attendanceTypeRepo;
    @Autowired
    private LeaveCategoryRepo leaveCategoryRepo;
    @Autowired
    private LeaveRepo leaveRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepo;
    @Autowired
    private MovementsRepo movementsRepo;
    @Autowired
    private NoPayRepo noPayRepo;
    @Autowired
    private MovementAdminsRepo movementAdminsRepo;
    @Autowired
    private Utils utils;
    @Autowired
    private UserLeaveTypeRemainingRepo leaveTypeRemaiRepo;
    @Autowired
    private UserLeaveTypeTotalRepo leaveTypeTotRepo;
    @Autowired
    private EmployeeRepo employeeRepo;
    @Autowired
    private LeaveAdminsRepo leaveAdminsRepo;
    @Autowired
    private LeaveTypeRepo leaveTypeRepository;
    @Autowired
    private Helper helper;
    @Override
    public List<AbsenteeEntity> getAllAbsentee() {
        return absenteeRepo.findAll();
    }

    @Override
    public AbsenteeEntity getOneAbsentee(String publicId, String employeeId) {
        Optional<AbsenteeEntity> byPublicId = absenteeRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) {
            return byPublicId.get();
        } else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void saveAbsentee(String employeeId, Boolean isHalfDay, Boolean swipeErr) {
        AbsenteeEntity absentee = new AbsenteeEntity();
        absentee.setEmployeeID(employeeId);
        absenteeRepo.save(absentee);
    }

    @Override
    public void deleteAbsentee(String publicId) {
        Optional<AbsenteeEntity> byPublicId = absenteeRepo.findByPublicId(publicId);
        if (byPublicId.isPresent())
            absenteeRepo.delete(byPublicId.get());
        else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public Page<AttendanceDTO> getAllAttendance(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findAll(pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setUserId(attendanceEntity.getUserId());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());
            return attendanceDTO;
        });
    }

    @Override
    public void makeInAttendanceActive(String publicId) {
        Optional<AttendanceEntity> entity = attendanceRepo.findByPublicId(publicId);
        entity.ifPresent(attendanceEntity -> {
            attendanceEntity.setActive(false);
            attendanceRepo.save(attendanceEntity);
        });
    }

    @Override
    public DashBoardRes getDashBoard(String userId) {

        List<AttendanceEntity> attedance = attendanceRepo.findByUserId(userId);
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);

        EmployeeEntity emp = employee.get();
        if(employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        List<UserLeaveTypeRemainingEntity> remain = leaveTypeRemaiRepo.findByEmployeeID(emp.getEmployeeId());


        if(remain.isEmpty() || attedance.isEmpty()) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        int totalRemainingLeaves = remain.stream()
                .filter(leave -> leave.getRemainingLeaves() != null) // Handle null values
                .mapToInt(UserLeaveTypeRemainingEntity::getRemainingLeaves)
                .sum();
        int totL = 215;
        int total = attedance.stream().map(AttendanceEntity::getIsFullDay).toList().size();
        String name = employee.get().getFirstName() + " " + employee.get().getLastName();

        Map<String, Integer> remainLeaveDistribution = leaveTypeRemaiRepo.findByEmployeeID(employee.get().getEmployeeId())
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

        List<AttendanceEntity> attendanceThisYear = attendanceRepo.findByEmployeeIDAndDateBetween(employee.get().getSltId(), yearStartDate, todayEndDate);


        Map<String, Integer> monthlyAttendanceDistribution = attendanceThisYear.stream()
                .filter(attendance -> Boolean.TRUE.equals(attendance.getIsFullDay())) // Only full day attendance
                .collect(Collectors.groupingBy(
                        attendance -> {
                            LocalDate date = attendance.getDate().toInstant() // Using 'date' field instead of 'arrivalDate'
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

        dashBoardRes.setTotalAttendance(total);
        dashBoardRes.setRemainLeaveDistribution(remainLeaveDistribution);
        dashBoardRes.setMonthlyAttendanceDistribution(monthlyAttendanceDistribution);
        dashBoardRes.setName(name);
        dashBoardRes.setTotalLeave(totL);
        dashBoardRes.setLeaveBalance(totalRemainingLeaves);
        return dashBoardRes;
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByUserId(userId, pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        });
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUn(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrue(pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        });
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnA(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnAuthorizedTrue(pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        });
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrueAndUserId(userId,
                pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        });
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnAByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnAuthorizedTrueAndUserId(userId,
                pageable);

        return attendanceEntityPage.map(attendanceEntity -> {
            AttendanceDTO attendanceDTO = new AttendanceDTO();

            attendanceDTO.setId(attendanceEntity.getId());
            attendanceDTO.setPublicId(attendanceEntity.getPublicId());
            attendanceDTO.setDate(attendanceEntity.getDate());
            attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
            attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
            attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
            attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
            attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
            attendanceDTO.setLate(attendanceEntity.getIsLate());
            attendanceDTO.setLateCover(attendanceEntity.getLateCover());
            attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
            attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
            attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
            attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
            attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
            attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
            attendanceDTO.setIssues(attendanceEntity.getIssues());
            attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
            attendanceDTO.setResolve(attendanceEntity.getResolve());
            attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
            attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
            attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
            attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
            attendanceDTO.setActive(attendanceEntity.getActive());
            attendanceDTO.setNopay(attendanceEntity.getNopay());
            attendanceDTO.setManual(attendanceEntity.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        });
    }

    @Override
    public List<AttendanceEntity> getAttendanceByUserId(String employeeId) {
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return attendanceRepo.findByEmployeeID(employeeId);
    }

    @Override
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId) {
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return attendanceRepo.findByEmployeeID(employeeId);
    }

    @Override
    public Page<AbsenteeDto> getAllAbsentee(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AbsenteeEntity> absenteeEntityPage = absenteeRepo.findAll(pageable);

        return absenteeEntityPage.map(this::convertToDto);
    }

    // Helper method to convert AbsenteeEntity to AbsenteeDto
    private AbsenteeDto convertToDto(AbsenteeEntity entity) {
        AbsenteeDto dto = new AbsenteeDto();
        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setDate(entity.getDate());
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setUserId(entity.getUserId());
        dto.setAudited(entity.getAudited());
        dto.setIsNoPay(entity.getIsNoPay());
        return dto;
    }

    @Override
    public Page<AbsenteeDto> getAllAbsenteeByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AbsenteeEntity> absenteeEntityPage = absenteeRepo.findByUserId(userId, pageable);
        return absenteeEntityPage.map(this::convertToDto);
    }

    @Override
    public void createMovements(MovementsEntity entity) {
        MovementsEntity movementsEntity = movementsRepo.save(entity);
        System.out.println("MovementsEntity: " + movementsEntity);
    }

    @Override
    public void deleteAttendance(String publicId) {
        Optional<AttendanceEntity> entity = attendanceRepo.findByPublicId(publicId);
        entity.ifPresent(attendanceEntity -> {
            if(attendanceEntity.getIsManual())
                attendanceRepo.delete(attendanceEntity);
            else
                throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        });
    }

    @Override
    public void deleteAttendanceV1(String publicId) {
        Optional<AttendanceEntity> entity = attendanceRepo.findByPublicId(publicId);
        entity.ifPresent(attendanceEntity -> {
            attendanceEntity.setActive(false);
            attendanceRepo.save(attendanceEntity);
        });
    }

    @Override
    public Page<MovementDTO> getAllMovementByUser(String employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAllByUserId(employeeId, pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return allByUser.map(movementEntity -> {
            List<MovementTra> movementAdminsDtoList = new ArrayList<>();

            MovementDTO movementDTO = new MovementDTO();

            movementEntity.getAdmins().forEach(movementAdminsEntity -> {
                Optional<EmployeeEntity> opE = employeeRepo.findBySltId(movementAdminsEntity.getSltId());
                if(opE.isEmpty()) return;
                EmployeeEntity employeeEntity = opE.get();

                MovementTra movementAdmins = new MovementTra();
                movementAdmins.setId(movementAdminsEntity.getId());
                movementAdmins.setMovementId(movementAdminsEntity.getMovementId());
                movementAdmins.setUserId(movementAdminsEntity.getUserId());
                movementAdmins.setSltId(movementAdminsEntity.getSltId());
                movementAdmins.setEmployeeId(movementAdminsEntity.getEmployeeId());
                movementAdmins.setApprovedDate(movementAdminsEntity.getApprovedDate());
                movementAdmins.setHighestRolePriority(movementAdminsEntity.getHighestRolePriority());
                movementAdmins.setAccepted(movementAdminsEntity.getIsAccepted());

                movementAdmins.setEmail(employeeEntity.getEmail());
                movementAdmins.setFirstName(employeeEntity.getFirstName());
                movementAdmins.setLastName(employeeEntity.getLastName());

                movementAdminsDtoList.add(movementAdmins);
            });
            movementDTO.setAdminsTra(movementAdminsDtoList);

            movementDTO.setId(movementEntity.getId());
            movementDTO.setPublicId(movementEntity.getPublicId());
            movementDTO.setUserId(movementEntity.getEmployeeId());
            movementDTO.setInTime(movementEntity.getInTime());
            movementDTO.setOutTime(movementEntity.getOutTime());
            movementDTO.setComment(movementEntity.getComment());
            movementDTO.setLogTime(movementEntity.getLogTime());

            movementDTO.setCategory(movementEntity.getCategory());
            movementDTO.setDestination(movementEntity.getDestination());
            movementDTO.setEmployeeId(movementEntity.getEmployeeId());
            movementDTO.setReqDate(movementEntity.getReqDate());

            movementDTO.setMovementType(movementEntity.getMovementType());
            movementDTO.setAttSync(movementEntity.getAttSync());
            movementDTO.setHappenDate(movementEntity.getHappenDate());
            movementDTO.setPending(movementEntity.getIsPending());
            movementDTO.setAccepted(movementEntity.getIsAccepted());
            movementDTO.setHalfDay(movementEntity.getIsHalfDay());
            movementDTO.setAbsent(movementEntity.getIsAbsent());
            movementDTO.setUnSuccessfulAttdate(movementEntity.getIsUnSuccessfulAttdate());
            movementDTO.setUnAuthorized(movementEntity.getUnAuthorized());
            movementDTO.setReject(movementEntity.getIsReject());

            if (movementEntity.getAttendance() != null) {
                movementDTO.setAttendance(movementEntity.getAttendance().getPublicId());
            }

            return movementDTO;
        });
    }

    @Override
    public Page<MovementDTO> getAllMovementByAdmin(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movementAdminsRepo.findByUserId(userId, pageable).map(movementAdminsEntity -> {
            Optional<MovementsEntity> publicId = movementsRepo.findByPublicId(movementAdminsEntity.getMovementId());

            if (publicId.isPresent()) {
                MovementDTO movementDTO = new MovementDTO();
                MovementsEntity movementEntity = publicId.get();
                Optional<EmployeeEntity> opE = employeeRepo.findBySltId(movementAdminsEntity.getSltId());
                List<MovementTra> movementAdminsDtoList = new ArrayList<>();

                if (opE.isPresent()) {
                    EmployeeEntity employeeEntity = opE.get();

                    MovementTra movementAdmins = new MovementTra();
                    movementAdmins.setId(movementAdminsEntity.getId());
                    movementAdmins.setMovementId(movementAdminsEntity.getMovementId());
                    movementAdmins.setUserId(movementAdminsEntity.getUserId());
                    movementAdmins.setSltId(movementAdminsEntity.getSltId());
                    movementAdmins.setEmployeeId(movementAdminsEntity.getEmployeeId());
                    movementAdmins.setApprovedDate(movementAdminsEntity.getApprovedDate());
                    movementAdmins.setHighestRolePriority(movementAdminsEntity.getHighestRolePriority());
                    movementAdmins.setAccepted(movementAdminsEntity.getIsAccepted());

                    movementAdmins.setEmail(employeeEntity.getEmail());
                    movementAdmins.setFirstName(employeeEntity.getFirstName());
                    movementAdmins.setLastName(employeeEntity.getLastName());

                    movementAdminsDtoList.add(movementAdmins);
                }

                movementDTO.setAdminsTra(movementAdminsDtoList);
                movementDTO.setId(movementEntity.getId());
                movementDTO.setPublicId(movementEntity.getPublicId());
                movementDTO.setUserId(movementEntity.getEmployeeId());
                movementDTO.setInTime(movementEntity.getInTime());
                movementDTO.setOutTime(movementEntity.getOutTime());
                movementDTO.setComment(movementEntity.getComment());
                movementDTO.setLogTime(movementEntity.getLogTime());

                movementDTO.setCategory(movementEntity.getCategory());
                movementDTO.setDestination(movementEntity.getDestination());
                movementDTO.setEmployeeId(movementEntity.getEmployeeId());
                movementDTO.setReqDate(movementEntity.getReqDate());

                movementDTO.setMovementType(movementEntity.getMovementType());
                movementDTO.setAttSync(movementEntity.getAttSync());
                movementDTO.setHappenDate(movementEntity.getHappenDate());
                movementDTO.setPending(movementEntity.getIsPending());
                movementDTO.setAccepted(movementEntity.getIsAccepted());
                movementDTO.setHalfDay(movementEntity.getIsHalfDay());
                movementDTO.setAbsent(movementEntity.getIsAbsent());
                movementDTO.setUnSuccessfulAttdate(movementEntity.getIsUnSuccessfulAttdate());
                movementDTO.setUnAuthorized(movementEntity.getUnAuthorized());
                movementDTO.setReject(movementEntity.getIsReject());

                return movementDTO;
            } else
                return null;
        });
    }

    @Override
    public Page<MovementDTO> getAllMovements(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAll(pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return allByUser.map(movementEntity -> {
            List<MovementTra> movementAdminsDtoList = new ArrayList<>();

            MovementDTO movementDTO = new MovementDTO();

            movementEntity.getAdmins().forEach(movementAdminsEntity -> {
                Optional<EmployeeEntity> opE = employeeRepo.findBySltId(movementAdminsEntity.getSltId());
                if(opE.isEmpty()) return;
                EmployeeEntity employeeEntity = opE.get();

                MovementTra movementAdmins = new MovementTra();
                movementAdmins.setId(movementAdminsEntity.getId());
                movementAdmins.setMovementId(movementAdminsEntity.getMovementId());
                movementAdmins.setUserId(movementAdminsEntity.getUserId());
                movementAdmins.setSltId(movementAdminsEntity.getSltId());
                movementAdmins.setEmployeeId(movementAdminsEntity.getEmployeeId());
                movementAdmins.setApprovedDate(movementAdminsEntity.getApprovedDate());
                movementAdmins.setHighestRolePriority(movementAdminsEntity.getHighestRolePriority());
                movementAdmins.setAccepted(movementAdminsEntity.getIsAccepted());

                movementAdmins.setEmail(employeeEntity.getEmail());
                movementAdmins.setFirstName(employeeEntity.getFirstName());
                movementAdmins.setLastName(employeeEntity.getLastName());

                movementAdminsDtoList.add(movementAdmins);
            });

            movementDTO.setAdminsTra(movementAdminsDtoList);
            movementDTO.setId(movementEntity.getId());
            movementDTO.setPublicId(movementEntity.getPublicId());
            movementDTO.setUserId(movementEntity.getEmployeeId());
            movementDTO.setInTime(movementEntity.getInTime());
            movementDTO.setOutTime(movementEntity.getOutTime());
            movementDTO.setComment(movementEntity.getComment());
            movementDTO.setLogTime(movementEntity.getLogTime());

            movementDTO.setCategory(movementEntity.getCategory());
            movementDTO.setDestination(movementEntity.getDestination());
            movementDTO.setEmployeeId(movementEntity.getEmployeeId());
            movementDTO.setReqDate(movementEntity.getReqDate());

            movementDTO.setMovementType(movementEntity.getMovementType());
            movementDTO.setAttSync(movementEntity.getAttSync());
            movementDTO.setHappenDate(movementEntity.getHappenDate());
            movementDTO.setPending(movementEntity.getIsPending());
            movementDTO.setAccepted(movementEntity.getIsAccepted());
            movementDTO.setHalfDay(movementEntity.getIsHalfDay());
            movementDTO.setAbsent(movementEntity.getIsAbsent());
            movementDTO.setUnSuccessfulAttdate(movementEntity.getIsUnSuccessfulAttdate());
            movementDTO.setUnAuthorized(movementEntity.getUnAuthorized());
            movementDTO.setReject(movementEntity.getIsReject());


            // Handle the attendance relationship - we need to convert it to a string
            if (movementEntity.getAttendance() != null) {
                movementDTO.setAttendance(movementEntity.getAttendance().getPublicId());
            }

            return movementDTO;
        });
    }

    @Override
    public MovementsEntity getMovement(String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isPresent())
            return byPublicId.get();
        else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void updateMovement(MovementsEntity entity, String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        MovementsEntity movementsEntity = byPublicId.get();
        // Update fields if not null
        if (entity.getInTime() != null)
            movementsEntity.setInTime(entity.getInTime());
        if (entity.getOutTime() != null)
            movementsEntity.setOutTime(entity.getOutTime());
        if (entity.getComment() != null)
            movementsEntity.setComment(entity.getComment());
        if (entity.getLogTime() != null)
            movementsEntity.setLogTime(entity.getLogTime());

        if (entity.getCategory() != null)
            movementsEntity.setCategory(entity.getCategory());
        if (entity.getDestination() != null)
            movementsEntity.setDestination(entity.getDestination());
        if (entity.getEmployeeId() != null)
            movementsEntity.setEmployeeId(entity.getEmployeeId());
        if (entity.getReqDate() != null)
            movementsEntity.setReqDate(entity.getReqDate());

        if (entity.getAttSync() != null)
            movementsEntity.setAttSync(entity.getAttSync());
        if (entity.getHappenDate() != null)
            movementsEntity.setHappenDate(entity.getHappenDate());
        if (entity.getMovementType() != null)
            movementsEntity.setMovementType(entity.getMovementType());

        // Save updated entity
        movementsRepo.save(movementsEntity);
    }

    @Override
    public void updateMovement(MovementReq req, String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        MovementsEntity movementsEntity = byPublicId.get();
        // Update fields if not null

        if (req.getComment() != null)
            movementsEntity.setComment(req.getComment());


        if (req.getCategory() != null)
            movementsEntity.setCategory(req.getCategory());

        if (req.getDestination() != null)
            movementsEntity.setDestination(req.getDestination());

        if (req.getEmployeeId() != null)
            movementsEntity.setEmployeeId(req.getEmployeeId());

        if (req.getHappenDate() != null)
            movementsEntity.setHappenDate(req.getHappenDate());

        if (req.getMovementType() != null)
            movementsEntity.setMovementType(req.getMovementType());

        // Save updated entity
        movementsRepo.save(movementsEntity);
    }

    @Override
    public void deleteMovements(String publicId) {
        MovementsEntity movement = getMovement(publicId);
        if (movement == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        movementsRepo.delete(movement);
    }

    @Override
    public void createNoPay(NoPayEntity entity) {
        noPayRepo.save(entity);
    }

    @Override
    public Page<NopayDTO> getAllNoPayByUser(String employeeId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoPayEntity> byUser = noPayRepo.findByUserId(employeeId, pageable);

        if (byUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return byUser.map(noPayEntity -> {
            NopayDTO nopayDTO = new NopayDTO();
            nopayDTO.setId(noPayEntity.getId());
            nopayDTO.setPublicId(noPayEntity.publicId);
            nopayDTO.setEmployeeID(noPayEntity.getEmployeeID());
            nopayDTO.setSubmissionDate(noPayEntity.getSubmissionDate());
            nopayDTO.setAcctualDate(noPayEntity.getAcctualDate());
            nopayDTO.setHalfDay(noPayEntity.getIsHalfDay());
            nopayDTO.setUnSuccessful(noPayEntity.getUnSuccessful());
            nopayDTO.setLate(noPayEntity.getIsLate());
            nopayDTO.setLateCover(noPayEntity.getIsLateCover());
            nopayDTO.setAbsent(noPayEntity.getIsAbsent());
            nopayDTO.setComment(noPayEntity.getComment());
            nopayDTO.setHappenDate(noPayEntity.getHappenDate());

            // Handle the attendance relationship - assuming you want the attendance ID or
            // some identifier
            if (noPayEntity.getAttendance() != null) {
                nopayDTO.setAttendance(noPayEntity.getAttendance().getPublicId());
            }

            return nopayDTO;
        });
    }

    @Override
    public Page<NopayDTO> getAllNoPays(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoPayEntity> byUser = noPayRepo.findAll(pageable);

        if (byUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return byUser.map(noPayEntity -> {
            NopayDTO nopayDTO = new NopayDTO();
            nopayDTO.setId(noPayEntity.getId());
            nopayDTO.setPublicId(noPayEntity.publicId);
            nopayDTO.setEmployeeID(noPayEntity.getEmployeeID());
            nopayDTO.setSubmissionDate(noPayEntity.getSubmissionDate());
            nopayDTO.setAcctualDate(noPayEntity.getAcctualDate());
            nopayDTO.setHalfDay(noPayEntity.getIsHalfDay());
            nopayDTO.setUnSuccessful(noPayEntity.getUnSuccessful());
            nopayDTO.setLate(noPayEntity.getIsLate());
            nopayDTO.setLateCover(noPayEntity.getIsLateCover());
            nopayDTO.setAbsent(noPayEntity.getIsAbsent());
            nopayDTO.setComment(noPayEntity.getComment());
            nopayDTO.setHappenDate(noPayEntity.getHappenDate());

            // Handle the attendance relationship - assuming you want the attendance ID or
            // some identifier
            if (noPayEntity.getAttendance() != null) {
                nopayDTO.setAttendance(noPayEntity.getAttendance().getPublicId());
            }

            return nopayDTO;
        });
    }

    @Override
    public NoPayEntity getNoPay(String publicId) {
        Optional<NoPayEntity> byPublicId = noPayRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) {
            return byPublicId.get();
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteNoPay(String publicId) {
        Optional<NoPayEntity> byPublicId = noPayRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) {
            noPayRepo.delete(byPublicId.get());
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void saveLeave(LeaveEntity entity) {
        leaveRepo.save(entity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserId(String userId, int page, int size) {
        if (userId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findByUserId(userId, pageable);

        return leaveEntityPage.map(leaveEntity -> {
            LeaveDTO leaveDTO = new LeaveDTO();
            List<LeaveTra> leaveAdminsDtoList = new ArrayList<>();

            leaveEntity.getAdmins().forEach(movementAdminsEntity -> {
                Optional<EmployeeEntity> opE = employeeRepo.findBySltId(movementAdminsEntity.getSltId());
                if(opE.isEmpty()) return;
                EmployeeEntity employeeEntity = opE.get();
                LeaveTra leaveAdmins = new LeaveTra();
                leaveAdmins.setId(movementAdminsEntity.getId());
                leaveAdmins.setLeaveId(movementAdminsEntity.getLeaveId());
                leaveAdmins.setUserId(movementAdminsEntity.getUserId());
                leaveAdmins.setSltId(movementAdminsEntity.getSltId());
                leaveAdmins.setEmployeeId(movementAdminsEntity.getEmployeeId());
                leaveAdmins.setApprovedDate(movementAdminsEntity.getApprovedDate());
                leaveAdmins.setHighestRolePriority(movementAdminsEntity.getHighestRolePriority());
                leaveAdmins.setAccepted(movementAdminsEntity.getIsAccepted());

                leaveAdmins.setEmail(employeeEntity.getEmail());
                leaveAdmins.setFirstName(employeeEntity.getFirstName());
                leaveAdmins.setLastName(employeeEntity.getLastName());

                leaveAdminsDtoList.add(leaveAdmins);
            });

            leaveDTO.setAdminsTra(leaveAdminsDtoList);
            leaveDTO.setPublicId(leaveEntity.getPublicId());
            leaveDTO.setId(leaveEntity.getId());
            leaveDTO.setEmployeeID(leaveEntity.getEmployeeID());
            leaveDTO.setSubmitDate(leaveEntity.getSubmitDate());
            leaveDTO.setFromDate(leaveEntity.getFromDate());
            leaveDTO.setToDate(leaveEntity.getToDate());
            leaveDTO.setLeaveType(leaveEntity.getLeaveType());
            leaveDTO.setIsNoPay(leaveEntity.getIsNoPay());
            leaveDTO.setNumOfDays(leaveEntity.getNumOfDays());
            leaveDTO.setDescription(leaveEntity.getDescription());
            leaveDTO.setHalfDay(leaveEntity.getIsHalfDay());
            leaveDTO.setFullDay(leaveEntity.getIsFullDay());
            leaveDTO.setUnSuccessful(leaveEntity.getUnSuccessful());
            leaveDTO.setLate(leaveEntity.getIsLate());
            leaveDTO.setLateCover(leaveEntity.getIsLateCover());
            leaveDTO.setShort_Leave(leaveEntity.getIsShort_Leave());
            leaveDTO.setPending(leaveEntity.getIsPending());
            leaveDTO.setAccepted(leaveEntity.getIsAccepted());
            leaveDTO.setNotUsed(leaveEntity.getNotUsed());
            leaveDTO.setCanceled(leaveEntity.getIsCanceled());
            leaveDTO.setManualRequest(leaveEntity.getIsManualRequest());
            leaveDTO.setHappenDate(leaveEntity.getHappenDate());
            leaveDTO.setUserId(leaveDTO.getUserId());
            leaveDTO.setReject(leaveEntity.getIsReject());
            leaveDTO.setCanceled(leaveEntity.getIsCanceled());

            return leaveDTO;
        });
    }

    @Override
    public Page<LeaveDTO> getAllLeaves(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findAll(pageable);

        return leaveEntityPage.map(leaveEntity -> {
            LeaveDTO leaveDTO = new LeaveDTO();
            List<LeaveTra> leaveAdminsDtoList = new ArrayList<>();

            leaveEntity.getAdmins().forEach(movementAdminsEntity -> {
                Optional<EmployeeEntity> opE = employeeRepo.findBySltId(movementAdminsEntity.getSltId());
                if(opE.isEmpty()) return;
                EmployeeEntity employeeEntity = opE.get();
                LeaveTra leaveAdmins = new LeaveTra();
                leaveAdmins.setId(movementAdminsEntity.getId());
                leaveAdmins.setLeaveId(movementAdminsEntity.getLeaveId());
                leaveAdmins.setUserId(movementAdminsEntity.getUserId());
                leaveAdmins.setSltId(movementAdminsEntity.getSltId());
                leaveAdmins.setEmployeeId(movementAdminsEntity.getEmployeeId());
                leaveAdmins.setApprovedDate(movementAdminsEntity.getApprovedDate());
                leaveAdmins.setHighestRolePriority(movementAdminsEntity.getHighestRolePriority());
                leaveAdmins.setAccepted(movementAdminsEntity.getIsAccepted());

                leaveAdmins.setEmail(employeeEntity.getEmail());
                leaveAdmins.setFirstName(employeeEntity.getFirstName());
                leaveAdmins.setLastName(employeeEntity.getLastName());

                leaveAdminsDtoList.add(leaveAdmins);
            });

            leaveDTO.setAdminsTra(leaveAdminsDtoList);

            leaveDTO.setPublicId(leaveEntity.getPublicId());
            leaveDTO.setId(leaveEntity.getId());
            leaveDTO.setEmployeeID(leaveEntity.getEmployeeID());
            leaveDTO.setSubmitDate(leaveEntity.getSubmitDate());
            leaveDTO.setFromDate(leaveEntity.getFromDate());
            leaveDTO.setToDate(leaveEntity.getToDate());
            leaveDTO.setLeaveType(leaveEntity.getLeaveType());
            leaveDTO.setIsNoPay(leaveEntity.getIsNoPay());
            leaveDTO.setNumOfDays(leaveEntity.getNumOfDays());
            leaveDTO.setDescription(leaveEntity.getDescription());
            leaveDTO.setReject(leaveEntity.getIsReject());
            leaveDTO.setHalfDay(leaveEntity.getIsHalfDay());
            leaveDTO.setFullDay(leaveEntity.getIsFullDay());
            leaveDTO.setUnSuccessful(leaveEntity.getUnSuccessful());
            leaveDTO.setLate(leaveEntity.getIsLate());
            leaveDTO.setLateCover(leaveEntity.getIsLateCover());
            leaveDTO.setShort_Leave(leaveEntity.getIsShort_Leave());
            leaveDTO.setPending(leaveEntity.getIsPending());
            leaveDTO.setAccepted(leaveEntity.getIsAccepted());
            leaveDTO.setNotUsed(leaveEntity.getNotUsed());
            leaveDTO.setCanceled(leaveEntity.getIsCanceled());
            leaveDTO.setManualRequest(leaveEntity.getIsManualRequest());
            leaveDTO.setHappenDate(leaveEntity.getHappenDate());
            leaveDTO.setUserId(leaveDTO.getUserId());
            leaveDTO.setCanceled(leaveEntity.getIsCanceled());
            return leaveDTO;
        });
    }

    @Override
    public LeaveEntity getOneLeave(String publicId) {
        Optional<LeaveEntity> byPublicId = leaveRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) {
            return byPublicId.get();
        } else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void deleteLeave(String publicId) {
        Optional<LeaveEntity> byPublicId = leaveRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) {
            LeaveEntity leaveEntity = byPublicId.get();
            leaveEntity.setIsCanceled(true);
            leaveRepo.save(leaveEntity);
        } else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void saveAttendanceType(String shortName, String Description) {
        if (getAttendanceType(shortName) != null)
            throw new LMSServiceException_AllReadyExits(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());
        AttendanceTypeEntity entity = new AttendanceTypeEntity();
        entity.setShortName(shortName);
        entity.setPublicId(utils.generateId(10));
        entity.setDescription(Description);
        attendanceTypeRepo.save(entity);
    }

    @Override
    public AttendanceTypeEntity getAttendanceType(String shortName) {
        Optional<AttendanceTypeEntity> byShortName = attendanceTypeRepo.findByShortName(shortName);
        if (byShortName.isPresent()) {
            return byShortName.get();
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void updateAttendanceType(String old_shortName, String shortName, String Description) {
        Optional<AttendanceTypeEntity> byShortName = attendanceTypeRepo.findByShortName(old_shortName);
        if (byShortName.isPresent()) {
            AttendanceTypeEntity attendanceTypeEntity = byShortName.get();
            attendanceTypeEntity.setShortName(shortName);
            attendanceTypeEntity.setDescription(Description);
            attendanceTypeRepo.save(attendanceTypeEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteAttendanceType(String shortName) {
        Optional<AttendanceTypeEntity> byShortName = attendanceTypeRepo.findByShortName(shortName);
        if (byShortName.isPresent()) {
            AttendanceTypeEntity attendanceTypeEntity = byShortName.get();
            attendanceTypeRepo.delete(attendanceTypeEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void saveLeaveCategory(String name) {
        if (getLeaveCategory(name) != null)
            throw new LMSServiceException_AllReadyExits(ErrorMessages.RECORD_ALREADY_EXISTS.getErrorMessage());

        LeaveCategoryEntity leaveCategoryEntity = new LeaveCategoryEntity();
        leaveCategoryEntity.setName(name);
        leaveCategoryEntity.setPublicId(utils.generateId(10));

        leaveCategoryRepo.save(leaveCategoryEntity);

    }

    public LeaveCategoryEntity getLeaveCategory(String name) {
        if (name != null) {
            Optional<LeaveCategoryEntity> result = leaveCategoryRepo.findByName(name);
            if (result.isPresent()) {
                return result.get();
            }
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
        return null; // Return null if no category is found
    }

    @Override
    public void updateLeaveCategory(String old_name, String name) {
        Optional<LeaveCategoryEntity> result = leaveCategoryRepo.findByName(old_name);
        if (result.isPresent()) {
            LeaveCategoryEntity leaveCategoryEntity = result.get();
            leaveCategoryEntity.setName(name);
            leaveCategoryRepo.save(leaveCategoryEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteLeaveCategory(String name) {
        Optional<LeaveCategoryEntity> result = leaveCategoryRepo.findByName(name);
        if (result.isPresent()) {
            LeaveCategoryEntity leaveCategoryEntity = result.get();
            leaveCategoryRepo.delete(leaveCategoryEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
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
        Optional<LeaveTypeEntity> result = leaveTypeRepo.findByName(old_name);
        if (result.isPresent()) {
            LeaveTypeEntity leaveTypeEntity = result.get();
            leaveTypeEntity.setName(name);
            leaveTypeRepo.save(leaveTypeEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void updateLeaveType(String old_name, String userId, int days) {
        if (old_name == null)
            throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        Optional<LeaveTypeEntity> result = leaveTypeRepo.findByName(old_name);

        if (result.isPresent()) {
            LeaveTypeEntity leaveTypeEntity = result.get();

            if (userId == null)
                throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

            UserLeaveTypeRemainingEntity type = leaveTypeRemaiRepo.findByEmployeeIDAndLeaveType(userId, leaveTypeEntity);
            type.setRemainingLeaves(days);
            leaveTypeRemaiRepo.save(type);

        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public void deleteLeaveType(String name) {
        Optional<LeaveTypeEntity> result = leaveTypeRepo.findByName(name);
        if (result.isPresent()) {
            LeaveTypeEntity leaveTypeEntity = result.get();
            leaveTypeRepo.delete(leaveTypeEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }

    @Override
    public LeaveTypeTotDto getTotalLeaves(String employeeId, String leaveTypeName) {
        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployeeID(employeeId);

        for (UserLeaveTypeTotalEntity entity : totalEntities) {
            if (entity.getLeaveType().getName().equals(leaveTypeName)) {
                LeaveTypeTotDto dto = new LeaveTypeTotDto();
                dto.setName(leaveTypeName);
                dto.setRemainLeave(entity.getTotalLeaves());
                return dto;
            }
        }

        return new LeaveTypeTotDto();
    }

    @Override
    public LeaveTypeRetDto getRemainingLeaves(String employeeId, String leaveTypeName) {
        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo.findByEmployeeID(employeeId);

        for (UserLeaveTypeRemainingEntity entity : remainingEntities) {
            if (entity.getLeaveType().getName().equals(leaveTypeName)) {
                LeaveTypeRetDto dto = new LeaveTypeRetDto();
                dto.setName(leaveTypeName);
                dto.setRemainLeave(entity.getRemainingLeaves());
                return dto;
            }
        }

        return new LeaveTypeRetDto();
    }

    @Override
    public UserLeaveDetailsDto getAllLeaveDetails(String userId) {
        Optional<EmployeeEntity> opt = employeeRepo.findByPublicId(userId);
        if (opt.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        EmployeeEntity employee = opt.get();
        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployeeID(employee.getEmployeeId());
        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo
                .findByEmployeeID(employee.getEmployeeId());

        // Create a map for quick access to remaining leaves by leave type id
        Map<Long, Integer> remainingLeavesMap = new HashMap<>();
        for (UserLeaveTypeRemainingEntity entity : remainingEntities) {
            remainingLeavesMap.put(entity.getLeaveType().getId(), entity.getRemainingLeaves());
        }

        List<LeaveDetailDto> leaveDetails = new ArrayList<>();

        for (UserLeaveTypeTotalEntity totalEntity : totalEntities) {
            LeaveTypeEntity leaveType = totalEntity.getLeaveType();
            LeaveDetailDto detailDto = new LeaveDetailDto();
            detailDto.setLeaveTypeName(leaveType.getName());
            detailDto.setTotalLeaves(totalEntity.getTotalLeaves());

            // Get remaining leaves from map
            Integer remainingLeaves = remainingLeavesMap.getOrDefault(leaveType.getId(), 0);
            detailDto.setRemainingLeaves(remainingLeaves);

            leaveDetails.add(detailDto);
        }

        UserLeaveDetailsDto userLeaveDetailsDto = new UserLeaveDetailsDto();
        userLeaveDetailsDto.setEmployeeId(userId);
        userLeaveDetailsDto.setLeaveDetails(leaveDetails);

        return userLeaveDetailsDto;
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

    @Override
    public void updateLeave(LeaveReq req, String leaveId) {
        Optional<LeaveEntity> byPublicId = leaveRepo.findByPublicId(leaveId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        LeaveTypeEntity type = leaveTypeRepository.findByName(req.getLeaveType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type: " + req.getLeaveType()));

        LeaveEntity leaveEntity = byPublicId.get();
        if (req.getFromDate() != null)
            leaveEntity.setFromDate(stripTimeFromDate(req.getFromDate()));

        if (req.getToDate() != null)
            leaveEntity.setFromDate(stripTimeFromDate(req.getToDate()));

        if (req.getDescription() != null)
            leaveEntity.setDescription(req.getDescription());

        leaveEntity.setLeaveType(type);
        leaveRepo.save(leaveEntity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveAdminsRepo.findByUserId(userId, pageable).map(leaveAdminsEntity -> {
            Optional<LeaveEntity> publicId = leaveRepo.findByPublicId(leaveAdminsEntity.getLeaveId());

            if (publicId.isPresent()) {
                LeaveEntity leaveEntity = publicId.get();
                LeaveDTO leaveDTO = new LeaveDTO();

                List<LeaveTra> leaveAdminsDtoList = new ArrayList<>();
                leaveEntity.getAdmins().forEach(adminsEntity -> {
                    Optional<EmployeeEntity> opE = employeeRepo.findByEmployeeId(adminsEntity.getSltId());
                    if(opE.isEmpty()) return;
                    EmployeeEntity employeeEntity = opE.get();
                    LeaveTra leaveAdmins = new LeaveTra();
                    leaveAdmins.setId(adminsEntity.getId());
                    leaveAdmins.setLeaveId(adminsEntity.getLeaveId());
                    leaveAdmins.setUserId(adminsEntity.getUserId());
                    leaveAdmins.setSltId(adminsEntity.getSltId());
                    leaveAdmins.setEmployeeId(adminsEntity.getEmployeeId());
                    leaveAdmins.setApprovedDate(adminsEntity.getApprovedDate());
                    leaveAdmins.setHighestRolePriority(adminsEntity.getHighestRolePriority());
                    leaveAdmins.setAccepted(adminsEntity.getIsAccepted());

                    leaveAdmins.setEmail(employeeEntity.getEmail());
                    leaveAdmins.setFirstName(employeeEntity.getFirstName());
                    leaveAdmins.setLastName(employeeEntity.getLastName());

                    leaveAdminsDtoList.add(leaveAdmins);
                });

                leaveDTO.setAdminsTra(leaveAdminsDtoList);

                leaveDTO.setPublicId(leaveEntity.getPublicId());
                leaveDTO.setId(leaveEntity.getId());
                leaveDTO.setEmployeeID(leaveEntity.getEmployeeID());
                leaveDTO.setSubmitDate(leaveEntity.getSubmitDate());
                leaveDTO.setFromDate(leaveEntity.getFromDate());
                leaveDTO.setToDate(leaveEntity.getToDate());
                leaveDTO.setLeaveType(leaveEntity.getLeaveType());
                leaveDTO.setIsNoPay(leaveEntity.getIsNoPay());
                leaveDTO.setNumOfDays(leaveEntity.getNumOfDays());
                leaveDTO.setDescription(leaveEntity.getDescription());
                leaveDTO.setHalfDay(leaveEntity.getIsHalfDay());
                leaveDTO.setFullDay(leaveEntity.getIsFullDay());
                leaveDTO.setUnSuccessful(leaveEntity.getUnSuccessful());
                leaveDTO.setLate(leaveEntity.getIsLate());
                leaveDTO.setLateCover(leaveEntity.getIsLateCover());
                leaveDTO.setShort_Leave(leaveEntity.getIsShort_Leave());
                leaveDTO.setPending(leaveEntity.getIsPending());
                leaveDTO.setAccepted(leaveEntity.getIsAccepted());
                leaveDTO.setNotUsed(leaveEntity.getNotUsed());
                leaveDTO.setCanceled(leaveEntity.getIsCanceled());
                leaveDTO.setManualRequest(leaveEntity.getIsManualRequest());
                leaveDTO.setHappenDate(leaveEntity.getHappenDate());
                leaveDTO.setUserId(leaveDTO.getUserId());
                leaveDTO.setReject(leaveEntity.getIsReject());

                return leaveDTO;
            } else
                return null;
        });
    }

    @Override
    public AttendanceDTO createAttendance(AttendanceReq req) {
        AttendanceEntity attendanceEntity = new AttendanceEntity();
        attendanceEntity = convertToAttendanceEntity(req);
        AttendanceEntity save = attendanceRepo.save(attendanceEntity);

        AttendanceDTO attendanceDTO = new AttendanceDTO();
        attendanceDTO.setId(attendanceEntity.getId());
        attendanceDTO.setPublicId(attendanceEntity.getPublicId());
        attendanceDTO.setDate(attendanceEntity.getDate());
        attendanceDTO.setEmployeeID(attendanceEntity.getEmployeeID());
        attendanceDTO.setFullDay(attendanceEntity.getIsFullDay());
        attendanceDTO.setArrivalDate(attendanceEntity.getArrivalDate());
        attendanceDTO.setArrivalTime(attendanceEntity.getArrivalTime());
        attendanceDTO.setLeftTime(attendanceEntity.getLeftTime());
        attendanceDTO.setLate(attendanceEntity.getIsLate());
        attendanceDTO.setLateCover(attendanceEntity.getLateCover());
        attendanceDTO.setHalfDay(attendanceEntity.getIsHalfDay());
        attendanceDTO.setFullLeave(attendanceEntity.getIsFullLeave());
        attendanceDTO.setShortLeave(attendanceEntity.getIsShortLeave());
        attendanceDTO.setAbsent(attendanceEntity.getIsAbsent());
        attendanceDTO.setUnSuccessful(attendanceEntity.getIsUnSuccessful());
        attendanceDTO.setNoPay(attendanceEntity.getIsNoPay());
        attendanceDTO.setIssues(attendanceEntity.getIssues());
        attendanceDTO.setUnAuthorized(attendanceEntity.getIsUnAuthorized());
        attendanceDTO.setResolve(attendanceEntity.getResolve());
        attendanceDTO.setLeaveSuccess(attendanceEntity.getLeaveSuccess());
        attendanceDTO.setLeaveReq(attendanceEntity.getLeaveReq());
        attendanceDTO.setIssueDescription(attendanceEntity.getIssueDescription());
        attendanceDTO.setDueDateForUA(attendanceEntity.getDueDateForUA());
        attendanceDTO.setActive(attendanceEntity.getActive());
        attendanceDTO.setNopay(attendanceEntity.getNopay());
        attendanceDTO.setManual(attendanceEntity.getIsManual());
        attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

        return attendanceDTO;
    }

    public AttendanceDTO updateAttendance(AttendanceReq req, String publicId) {
        Optional<AttendanceEntity> opt = attendanceRepo.findByPublicId(publicId);
        if (opt.isPresent()) {

            AttendanceEntity attendanceEntity = opt.get();

            if (req.getEmployeeID() != null) {
                attendanceEntity.setEmployeeID(req.getEmployeeID());
            }

            if (req.getArrivalDate() != null) {
                attendanceEntity.setArrivalDate(req.getArrivalDate());
            }

            if (req.getArrivalTime() != null) {
                attendanceEntity.setArrivalTime(req.getArrivalTime());
            }

            if (req.getLeftTime() != null) {
                attendanceEntity.setLeftTime(req.getLeftTime());
            }

            if (req.getIssueDescription() != null) {
                attendanceEntity.setIssueDescription(req.getIssueDescription());
            }

            if (req.getDueDateForUA() != null) {
                attendanceEntity.setDueDateForUA(req.getDueDateForUA());
            }

            // Handle boolean fields with null checks
            if (req.getFullDay() != null) {
                attendanceEntity.setIsFullDay(req.getFullDay());
            }

            if (req.getLate() != null) {
                attendanceEntity.setIsLate(req.getLate());
            }

            if (req.getLateCover() != null) {
                attendanceEntity.setLateCover(req.getLateCover());
            }

            if (req.getHalfDay() != null) {
                attendanceEntity.setIsHalfDay(req.getHalfDay());
            }

            if (req.getFullLeave() != null) {
                attendanceEntity.setIsFullLeave(req.getFullLeave());
            }

            if (req.getShortLeave() != null) {
                attendanceEntity.setIsShortLeave(req.getShortLeave());
            }

            if (req.getAbsent() != null) {
                attendanceEntity.setIsAbsent(req.getAbsent());
            }

            if (req.getUnSuccessful() != null) {
                attendanceEntity.setIsUnSuccessful(req.getUnSuccessful());
            }

            if (req.getNoPay() != null) {
                attendanceEntity.setIsNoPay(req.getNoPay());
            }

            if (req.getIssues() != null) {
                attendanceEntity.setIssues(req.getIssues());
            }

            if (req.getUnAuthorized() != null) {
                attendanceEntity.setIsUnAuthorized(req.getUnAuthorized());
            }

            if (req.getResolve() != null) {
                attendanceEntity.setResolve(req.getResolve());
            }

            if (req.getLeaveSuccess() != null) {
                attendanceEntity.setLeaveSuccess(req.getLeaveSuccess());
            }

            if (req.getLeaveReq() != null) {
                attendanceEntity.setLeaveReq(req.getLeaveReq());
            }

            if (req.getActive() != null) {
                attendanceEntity.setActive(req.getActive());
            }

            if (req.getNopay() != null) {
                attendanceEntity.setNopay(req.getNopay());
            }

            if (req.getViaMovement() != null) {
                attendanceEntity.setViaMovement(req.getViaMovement());
            }

            if (req.getViaLeave() != null) {
                attendanceEntity.setViaLeave(req.getViaLeave());
            }

            /// ===================================================
            /// ===================================================
            /// ===================================================

            AttendanceEntity save = attendanceRepo.save(attendanceEntity);

            AttendanceDTO attendanceDTO = new AttendanceDTO();
            attendanceDTO.setId(save.getId());
            attendanceDTO.setPublicId(save.getPublicId());
            attendanceDTO.setDate(save.getDate());
            attendanceDTO.setEmployeeID(save.getEmployeeID());
            attendanceDTO.setFullDay(save.getIsFullDay());
            attendanceDTO.setArrivalDate(save.getArrivalDate());
            attendanceDTO.setArrivalTime(save.getArrivalTime());
            attendanceDTO.setLeftTime(save.getLeftTime());
            attendanceDTO.setLate(save.getIsLate());
            attendanceDTO.setLateCover(save.getLateCover());
            attendanceDTO.setHalfDay(save.getIsHalfDay());
            attendanceDTO.setFullLeave(save.getIsFullLeave());
            attendanceDTO.setShortLeave(save.getIsShortLeave());
            attendanceDTO.setAbsent(save.getIsAbsent());
            attendanceDTO.setUnSuccessful(save.getIsUnSuccessful());
            attendanceDTO.setNoPay(save.getIsNoPay());
            attendanceDTO.setIssues(save.getIssues());
            attendanceDTO.setUnAuthorized(save.getIsUnAuthorized());
            attendanceDTO.setResolve(save.getResolve());
            attendanceDTO.setLeaveSuccess(save.getLeaveSuccess());
            attendanceDTO.setLeaveReq(save.getLeaveReq());
            attendanceDTO.setIssueDescription(save.getIssueDescription());
            attendanceDTO.setDueDateForUA(save.getDueDateForUA());
            attendanceDTO.setActive(save.getActive());
            attendanceDTO.setNopay(save.getNopay());
            attendanceDTO.setManual(save.getIsManual());
            attendanceDTO.setTerminalID(attendanceEntity.getTerminalID());

            return attendanceDTO;
        } else {
            return null;
        }
    }


    public AttendanceEntity convertToAttendanceEntity(AttendanceReq req) {
        Optional<EmployeeEntity> employee = employeeRepo.findBySltId(req.getEmployeeID());
        if (employee.isEmpty()) return null;

        EmployeeEntity employeeEntity = employee.get();

        if (req == null) {
            return null;
        }

        AttendanceEntity entity = new AttendanceEntity();
        entity.setIsManual(true);
        entity.setPublicId(utils.generateId(10));
        entity.setUserId(employeeEntity.getPublicId());

        if (req.getDate() != null) {
            entity.setDate(req.getDate());
        } else {
            throw new IllegalArgumentException("Date is required for AttendanceEntity");
        }

        // Set other fields with null checks
        if (req.getEmployeeID() != null) {
            entity.setEmployeeID(req.getEmployeeID());
        }

        if (req.getArrivalDate() != null) {
            entity.setArrivalDate(req.getArrivalDate());
        }

        if (req.getArrivalTime() != null) {
            entity.setArrivalTime(req.getArrivalTime());
        }

        if (req.getLeftTime() != null) {
            entity.setLeftTime(req.getLeftTime());
        }

        if (req.getIssueDescription() != null) {
            entity.setIssueDescription(req.getIssueDescription());
        }

        if (req.getDueDateForUA() != null) {
            entity.setDueDateForUA(req.getDueDateForUA());
        }

        // Handle boolean fields with null checks
        if (req.getFullDay() != null) {
            entity.setIsFullDay(req.getFullDay());
        }

        if (req.getLate() != null) {
            entity.setIsLate(req.getLate());
        }

        if (req.getLateCover() != null) {
            entity.setLateCover(req.getLateCover());
        }

        if (req.getHalfDay() != null) {
            entity.setIsHalfDay(req.getHalfDay());
        }

        if (req.getFullLeave() != null) {
            entity.setIsFullLeave(req.getFullLeave());
        }

        if (req.getShortLeave() != null) {
            entity.setIsShortLeave(req.getShortLeave());
        }

        if (req.getAbsent() != null) {
            entity.setIsAbsent(req.getAbsent());
        }

        if (req.getUnSuccessful() != null) {
            entity.setIsUnSuccessful(req.getUnSuccessful());
        }

        if (req.getNoPay() != null) {
            entity.setIsNoPay(req.getNoPay());
        }

        if (req.getIssues() != null) {
            entity.setIssues(req.getIssues());
        }

        if (req.getUnAuthorized() != null) {
            entity.setIsUnAuthorized(req.getUnAuthorized());
        }

        if (req.getResolve() != null) {
            entity.setResolve(req.getResolve());
        }

        if (req.getLeaveSuccess() != null) {
            entity.setLeaveSuccess(req.getLeaveSuccess());
        }

        if (req.getLeaveReq() != null) {
            entity.setLeaveReq(req.getLeaveReq());
        }

        if (req.getActive() != null) {
            entity.setActive(req.getActive());
        }

        if (req.getNopay() != null) {
            entity.setNopay(req.getNopay());
        }

        if (req.getViaMovement() != null) {
            entity.setViaMovement(req.getViaMovement());
        }

        if (req.getViaLeave() != null) {
            entity.setViaLeave(req.getViaLeave());
        }
        if(req.getTerminalID()!=null){
            entity.setTerminalID(req.getTerminalID());
        }
        return entity;
    }
}
