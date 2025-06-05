package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.category.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeRemainingEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.UserLeaveTypeTotalEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.exceptions.LMSServiceException_AllReadyExits;
import com.slt.peotv.lmsmangmentservice.mapper.LMSMapper;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.req.*;
import com.slt.peotv.lmsmangmentservice.model.res.DashBoardRes;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
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
    private LMSMapper lmsMapper;
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
    private AccessLogRepo accessLogRepo;
    @Autowired
    private InOutRepo inOutRepo;

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
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
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
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        List<UserLeaveTypeRemainingEntity> remain = leaveTypeRemaiRepo.findByEmployeeID(emp.getEmployeeId());

        if (remain.isEmpty() || attedance.isEmpty()) {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }

        int totalRemainingLeaves = remain.stream()
                .filter(leave -> leave.getRemainingLeaves() != null)
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
                .filter(attendance -> Boolean.TRUE.equals(attendance.getIsFullDay()))
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
        dashBoardRes.setTotalAttendance(total);
        dashBoardRes.setRemainLeaveDistribution(remainLeaveDistribution);
        dashBoardRes.setMonthlyAttendanceDistribution(monthlyAttendanceDistribution);
        dashBoardRes.setName(name);
        dashBoardRes.setTotalLeave(totL);
        dashBoardRes.setLeaveBalance(totalRemainingLeaves);
        return dashBoardRes;
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceByUserId(String userId, int page, int size, Boolean admin) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByUserId(userId, pageable);
        if (admin)
            return attendanceEntityPage.map(lmsMapper::toAttendanceDTOAdmin);
        else
            return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUn(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrue(pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnA(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnAuthorizedTrue(pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrueAndUserId(userId, pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnAByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnAuthorizedTrueAndUserId(userId, pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
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
    public Page<AbsenteeDTO> getAllAbsentee(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AbsenteeEntity> absenteeEntityPage = absenteeRepo.findAll(pageable);
        return absenteeEntityPage.map(lmsMapper::toAbsenteeDto);
    }

    @Override
    public Page<AbsenteeDTO> getAllAbsenteeByUserId(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AbsenteeEntity> absenteeEntityPage = absenteeRepo.findByUserId(userId, pageable);
        return absenteeEntityPage.map(lmsMapper::toAbsenteeDto);
    }

    @Override
    public void createMovements(MovementsEntity entity) {
        entity.setUpdateDate(new Date());
        MovementsEntity movementsEntity = movementsRepo.save(entity);
    }

    @Override
    public void deleteAttendance(String publicId) {
        Optional<AttendanceEntity> entity = attendanceRepo.findByPublicId(publicId);
        entity.ifPresent(attendanceEntity -> {
            if (attendanceEntity.getIsManual())
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
    public Page<MovementDTO> getAllMovementByUser(String employeeId, int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAllByUserId(employeeId, pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        if(isAdmin)
            return allByUser.map(lmsMapper::toMovementDTOAdmin);
        else
            return allByUser.map(lmsMapper::toMovementDTO);
    }

    @Override
    public Page<MovementDTO> getAllMovementByAdmin(String userId, int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        return movementAdminsRepo.findByUserId(userId, pageable).map(movementAdminsEntity -> {
            Optional<MovementsEntity> publicId = movementsRepo.findByPublicId(movementAdminsEntity.getMovementId());
            if(isAdmin)
                return publicId.map(lmsMapper::toMovementDTOAdmin).orElse(null);
            else
                return publicId.map(lmsMapper::toMovementDTO).orElse(null);
        });
    }

    @Override
    public Page<MovementDTO> getAllMovements(int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAll(pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        if(isAdmin)
            return allByUser.map(lmsMapper::toMovementDTOAdmin);
        else
            return allByUser.map(lmsMapper::toMovementDTO);
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

        movementsRepo.save(movementsEntity);
    }

    @Override
    public void updateMovement(MovementReq req, String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        MovementsEntity movementsEntity = byPublicId.get();

        // Update fields if not null
        if (req.getEmployeeId() != null)
            movementsEntity.setEmployeeId(req.getEmployeeId());
        if (req.getUserId() != null)
            movementsEntity.setUserId(req.getUserId());
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
        if (req.getAbsent() != null)
            movementsEntity.setIsAbsent(req.getAbsent());
        if (req.getUnSuccessfulAttdate() != null)
            movementsEntity.setIsUnSuccessfulAttdate(req.getUnSuccessfulAttdate());
        if (req.getHalfDay() != null)
            movementsEntity.setIsHalfDay(req.getHalfDay());
        if (req.getUnAuthorized() != null)
            movementsEntity.setUnAuthorized(req.getUnAuthorized());
        if (req.getLate() != null)
            movementsEntity.setIsLate(req.getLate());
        if (req.getLateCover() != null)
            movementsEntity.setIsLateCover(req.getLateCover());
        if (req.getLogTime() != null)
            movementsEntity.setLogTime(req.getLogTime());
        if (req.getIntime() != null)
            movementsEntity.setInTime(req.getIntime());
        if (req.getOuttime() != null)
            movementsEntity.setOutTime(req.getOuttime());

        // Handle admin comments using mapper
        lmsMapper.addAdminCommentToEntity(movementsEntity, req.getAdminComment(), req.getAdminId());

        movementsEntity.setUpdateDate(new Date());
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

        return byUser.map(lmsMapper::toNopayDTO);
    }

    @Override
    public Page<NopayDTO> getAllNoPays(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<NoPayEntity> byUser = noPayRepo.findAll(pageable);

        if (byUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        return byUser.map(lmsMapper::toNopayDTO);
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
        entity.setUpdateDate(new Date());
        leaveRepo.save(entity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserId(String userId, int page, int size) {
        if (userId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findByUserId(userId, pageable);
        return leaveEntityPage.map(lmsMapper::toLeaveDTO);
    }

    @Override
    public Page<LeaveDTO> getAllLeaves(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findAll(pageable);
        return leaveEntityPage.map(lmsMapper::toLeaveDTO);
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
        return null;
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
    public LeaveTypeTotDTO getTotalLeaves(String employeeId, String leaveTypeName) {
        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployeeID(employeeId);

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
        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo.findByEmployeeID(employeeId);

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
        Optional<EmployeeEntity> opt = employeeRepo.findByPublicId(userId);
        if (opt.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        EmployeeEntity employee = opt.get();
        List<UserLeaveTypeTotalEntity> totalEntities = leaveTypeTotRepo.findByEmployeeID(employee.getEmployeeId());
        List<UserLeaveTypeRemainingEntity> remainingEntities = leaveTypeRemaiRepo.findByEmployeeID(employee.getEmployeeId());

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

        LeaveTypeEntity type = leaveTypeRepository.findByName(req.getLeaveType())
                .orElseThrow(() -> new IllegalArgumentException("Invalid leave type: " + req.getLeaveType()));

        LeaveEntity leaveEntity = byPublicId.get();

        if (req.getFromDate() != null) leaveEntity.setFromDate(lmsMapper.stripTimeFromDate(req.getFromDate()));
        if (req.getToDate() != null) leaveEntity.setToDate(lmsMapper.stripTimeFromDate(req.getToDate()));
        if (req.getDescription() != null) leaveEntity.setDescription(req.getDescription());
        if (req.getLeaveType() != null) leaveEntity.setLeaveType(type);
        if (req.getDescription() != null) leaveEntity.setDescription(req.getDescription());
        if (req.getNumOfDays() != null) leaveEntity.setNumOfDays(req.getNumOfDays());
        if (req.getHalfDay() != null) leaveEntity.setIsHalfDay(req.getHalfDay());
        if (req.getIsFullDay() != null) leaveEntity.setIsFullDay(req.getIsFullDay());
        if (req.getUnauthorized() != null) leaveEntity.setIsUnauthorized(req.getUnauthorized());
        if (req.getManualRequest() != null) leaveEntity.setIsManualRequest(req.getManualRequest());
        if (req.getAbsent() != null) leaveEntity.setIsAbsent(req.getAbsent());
        if (req.getIsLateCover() != null) leaveEntity.setIsLateCover(req.getIsLateCover());
        if (req.getIsLate() != null) leaveEntity.setIsLate(req.getIsLate());
        if (req.getUnSuccessful() != null) leaveEntity.setUnSuccessful(req.getUnSuccessful());
        if (req.getHappenDate() != null) leaveEntity.setHappenDate(lmsMapper.stripTimeFromDate(req.getHappenDate()));

        // Handle admin comments using mapper
        lmsMapper.addAdminCommentToEntity(leaveEntity, req.getAdminComment(), req.getAdminId());

        leaveRepo.save(leaveEntity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return leaveAdminsRepo.findByUserId(userId, pageable).map(leaveAdminsEntity -> {
            Optional<LeaveEntity> publicId = leaveRepo.findByPublicId(leaveAdminsEntity.getLeaveId());
            return publicId.map(lmsMapper::toLeaveDTO).orElse(null);
        });
    }

    @Override
    public AttendanceDTO createAttendance(AttendanceReq req) {
        AttendanceEntity attendanceEntity = lmsMapper.toAttendanceEntity(req);
        AttendanceEntity saved = attendanceRepo.save(attendanceEntity);
        return lmsMapper.toAttendanceDTO(saved);
    }

    @Override
    public AttendanceDTO updateAttendance(AttendanceReq req, String publicId) {
        Optional<AttendanceEntity> opt = attendanceRepo.findByPublicId(publicId);
        if (opt.isPresent()) {
            AttendanceEntity attendanceEntity = opt.get();

            // Use mapper to update entity from request
            lmsMapper.updateAttendanceEntityFromReq(attendanceEntity, req);

            // Handle admin comments using mapper
            lmsMapper.addAdminCommentToEntity(attendanceEntity, req.getAdminComment(), req.getAdminId());

            AttendanceEntity saved = attendanceRepo.save(attendanceEntity);
            return lmsMapper.toAttendanceDTO(saved);
        } else {
            return null;
        }
    }

    @Override
    public void createAccessLog(AccessLogReq req) {
        AccessLogEntity accessLogEntity = lmsMapper.toAccessLogEntity(req);
        accessLogEntity.setIsManual(true);
        accessLogEntity.setUpdateDate(new Date());
        lmsMapper.addAdminCommentToEntity(accessLogEntity, req.getAdminComment(), req.getAdminId());
        AccessLogEntity saved = accessLogRepo.save(accessLogEntity);
    }

    @Override
    public void createInout(InOutReq req) {
        InOutEntity inOutEntity = lmsMapper.toInOutEntity(req);
        inOutEntity.setIsManual(true);
        inOutEntity.setUpdateDate(new Date());
        lmsMapper.addAdminCommentToEntity(inOutEntity, req.getAdminComment(), req.getAdminId());
        InOutEntity save = inOutRepo.save(inOutEntity);
    }

    @Override
    public List<AccessLogEntity> getAccessLog(String employeeId, String date) {
        return List.of();
    }

    @Override
    public List<AttendanceDTO> getAttendance(String employeeId, String date) {
        return List.of();
    }
}