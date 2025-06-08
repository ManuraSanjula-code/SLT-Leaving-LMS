package com.slt.peotv.lmsmangmentservice.service.impl;

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
import org.springframework.data.domain.PageImpl;
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

    @Override
    public List<InOutDTO> getAllInOuts(String id, boolean swap) {
        return List.of();
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
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();
        List<AttendanceEntity> attedance = attendanceRepo.findByEmployee(employeeEntity);

        EmployeeEntity emp = employee.get();

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

        List<AttendanceEntity> attendanceThisYear = attendanceRepo.findByEmployeeAndDateBetween(employee.get(), yearStartDate, todayEndDate);

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
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByEmployee(employeeEntity, pageable);
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
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnSuccessfulTrueAndEmployee(employeeEntity, pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAttendanceThatUnAByUserId(String userId, int page, int size) {
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<AttendanceEntity> attendanceEntityPage = attendanceRepo.findByIsUnAuthorizedTrueAndEmployee(employeeEntity, pageable);
        return attendanceEntityPage.map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public List<AttendanceEntity> getAttendanceByUserId(String employeeId) {
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(employeeId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();


        return attendanceRepo.findByEmployee(employeeEntity);
    }

    @Override
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId) {
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(employeeId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        return attendanceRepo.findByEmployee(employeeEntity);
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
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(employeeId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAllByEmployee(employeeEntity, pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        if (isAdmin)
            return allByUser.map(lmsMapper::toMovementDTOAdmin);
        else
            return allByUser.map(lmsMapper::toMovementDTO);
    }

    @Override
    public Page<MovementDTO> getAllMovementByAdmin(String userId, int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId)
                .or(() -> employeeRepo.findBySltId(userId))
                .or(() -> employeeRepo.findByEmployeeId(userId));

        return employee.map(employeeEntity -> componetAdminsRepo.findByEmployee(employeeEntity, pageable).map(coAdminsEntity -> {
            Optional<MovementsEntity> publicId = movementsRepo.findByPublicId(coAdminsEntity.getComponetID());
            if (isAdmin)
                return publicId.map(lmsMapper::toMovementDTOAdmin).orElse(null);
            else
                return publicId.map(lmsMapper::toMovementDTO).orElse(null);
        })).orElseGet(() -> new PageImpl<>(Collections.emptyList()));
    }

    @Override
    public Page<MovementDTO> getAllMovements(int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MovementsEntity> allByUser = movementsRepo.findAll(pageable);

        if (allByUser.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        if (isAdmin)
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
    public void updateMovement(MovementReq req, String publicId) {

        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        MovementsEntity movementsEntity = byPublicId.get();

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
        if (req.getIsAbsent() != null)
            movementsEntity.setIsAbsent(req.getIsAbsent());
        if (req.getIsUnSuccessfulAttdate() != null)
            movementsEntity.setIsUnSuccessfulAttdate(req.getIsUnSuccessfulAttdate());
        if (req.getIsHalfDay() != null)
            movementsEntity.setIsHalfDay(req.getIsHalfDay());
        if (req.getUnAuthorized() != null)
            movementsEntity.setUnAuthorized(req.getUnAuthorized());
        if (req.getIsLate() != null)
            movementsEntity.setIsLate(req.getIsLate());
        if (req.getIsLateCover() != null)
            movementsEntity.setIsLateCover(req.getIsLateCover());
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
        if (employeeId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(employeeId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<NoPayEntity> byUser = noPayRepo.findByEmployee(employeeEntity, pageable);

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
    public Page<LeaveDTO> getAllLeaveByUserByUserId(String userId, int page, int size, Boolean isAdmin) {
        if (userId == null)
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        Optional<EmployeeEntity> employee = employeeRepo.findByPublicId(userId);
        if (employee.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        EmployeeEntity employeeEntity = employee.get();

        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findByEmployee(employeeEntity, pageable);
        if(isAdmin)
            return leaveEntityPage.map(lmsMapper::toLeaveDTOAdmin);
        else
            return leaveEntityPage.map(lmsMapper::toLeaveDTO);
    }

    @Override
    public Page<LeaveDTO> getAllLeaves(int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Page<LeaveEntity> leaveEntityPage = leaveRepo.findAll(pageable);
        if(isAdmin)
            return leaveEntityPage.map(lmsMapper::toLeaveDTOAdmin);
        else
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
        // Find the existing leave entity
        Optional<LeaveEntity> byPublicId = leaveRepo.findByPublicId(leaveId);
        if (byPublicId.isEmpty())
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        LeaveEntity leaveEntity = byPublicId.get();

        // Validate and update leave type if provided
        if (req.getLeaveType() != null && !req.getLeaveType().trim().isEmpty()) {
            LeaveTypeEntity type = leaveTypeRepository.findByName(req.getLeaveType())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid leave type: " + req.getLeaveType()));
            leaveEntity.setLeaveType(type);
        }

        // Update dates with null checks
        if (req.getFromDate() != null) {
            leaveEntity.setFromDate(lmsMapper.stripTimeFromDate(req.getFromDate()));
        }

        if (req.getToDate() != null) {
            leaveEntity.setToDate(lmsMapper.stripTimeFromDate(req.getToDate()));
        }

        if (req.getHappenDate() != null) {
            leaveEntity.setHappenDate(lmsMapper.stripTimeFromDate(req.getHappenDate()));
        }

        // Update description with null and empty check
        if (req.getDescription() != null && !req.getDescription().trim().isEmpty()) {
            leaveEntity.setDescription(req.getDescription());
        }

        // Update numeric fields with null checks
        if (req.getNumOfDays() != null) {
            leaveEntity.setNumOfDays(req.getNumOfDays());
        }

        if (req.getIsNoPay() != null) {
            leaveEntity.setIsNoPay(req.getIsNoPay());
        }

        // Update Boolean flags with null checks
        if (req.getHalfDay() != null) {
            leaveEntity.setIsHalfDay(req.getHalfDay());
        }

        if (req.getFullDay() != null) {
            leaveEntity.setIsFullDay(req.getFullDay());
        }

        if (req.getUnauthorized() != null) {
            leaveEntity.setIsUnauthorized(req.getUnauthorized());
        }

        if (req.getManualRequest() != null) {
            leaveEntity.setIsManualRequest(req.getManualRequest());
        }

        if (req.getAbsent() != null) {
            leaveEntity.setIsAbsent(req.getAbsent());
        }

        if (req.getLateCover() != null) {
            leaveEntity.setIsLateCover(req.getLateCover());
        }

        if (req.getLate() != null) {
            leaveEntity.setIsLate(req.getLate());
        }

        if (req.getUnSuccessful() != null) {
            leaveEntity.setUnSuccessful(req.getUnSuccessful());
        }

        if (req.getEdited() != null) {
            leaveEntity.setIsEdited(req.getEdited());
        }

        if (req.getReject() != null) {
            leaveEntity.setIsReject(req.getReject());
        }

        if (req.getPending() != null) {
            leaveEntity.setIsPending(req.getPending());
        }

        if (req.getCanceled() != null) {
            leaveEntity.setIsCanceled(req.getCanceled());
        }

        // BUG FIX: This was using req.getReject() instead of req.getAccepted()
        if (req.getAccepted() != null) {
            leaveEntity.setIsAccepted(req.getAccepted());
        }

        if (req.getNotUsed() != null) {
            leaveEntity.setNotUsed(req.getNotUsed());
        }

        if (req.getShort_Leave() != null) {
            leaveEntity.setIsShort_Leave(req.getShort_Leave());
        }

        // Handle admin comments with null checks
        if (req.getAdminComment() != null && !req.getAdminComment().trim().isEmpty()) {
            lmsMapper.addAdminCommentToEntity(leaveEntity, req.getAdminComment(), req.getAdminId());
        }

        // Set update date
        leaveEntity.setUpdateDate(new Date());


        leaveRepo.save(leaveEntity);
    }

    @Override
    public Page<LeaveDTO> getAllLeaveByUserByUserIdAdmin(String userId, int page, int size, Boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size);
        Optional<EmployeeEntity> em = employeeRepo.findByPublicId(userId);
        return em.map(employeeEntity -> componetAdminsRepo.findByEmployee(employeeEntity, pageable).map(componetAdmins -> {
            Optional<LeaveEntity> publicId = leaveRepo.findByPublicId(componetAdmins.getComponetID());
            if(isAdmin)
                return publicId.map(lmsMapper::toLeaveDTOAdmin).orElse(null);
            else
                return publicId.map(lmsMapper::toLeaveDTO).orElse(null);
        })).orElseGet(Page::empty);
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

    @Override
    public Page<AttendanceDTO> getAllAbsent(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return attendanceRepo.findByIsAbsent(true, pageable).map(lmsMapper::toAttendanceDTO);
    }

    @Override
    public Page<AttendanceDTO> getAllAbsentByUser(int page, int size, String user) {
        EmployeeEntity employee = employeeRepo.findByEmployeeId(user)
                .or(() -> employeeRepo.findBySltId(user)
                        .or(() -> employeeRepo.findByPublicId(user)))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));
        Pageable pageable = PageRequest.of(page, size);
        return attendanceRepo.findByEmployeeAndIsAbsent(employee, true, pageable).map(lmsMapper::toAttendanceDTO);
    }
}