package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Holiday;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.*;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.feign_client.model.AccessLogRest;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.dto.NopayDTO;
import com.slt.peotv.lmsmangmentservice.model.dto.NoPayReasonDTO;
import com.slt.peotv.lmsmangmentservice.model.req.AccessLogReq;
import com.slt.peotv.lmsmangmentservice.model.req.AttendanceReq;
import com.slt.peotv.lmsmangmentservice.model.req.HolidayReq;
import com.slt.peotv.lmsmangmentservice.repository.NoPayReasonRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class LMSMapper {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private Utils utils;

    @Autowired
    private Helper helper;

    @Autowired
    private InOutRepo inOutRepo;

    @Autowired
    private NoPayReasonRepo noPayReasonRepo;

    public AccessLogRest toRest(AccessLogEntity accessLog) {
        if (accessLog == null) return null;

        return AccessLogRest.builder()
                .employeeId(accessLog.getEmployeeId())
                .logDate(accessLog.getLogDate())
                .logTime(accessLog.getLogTime())
                .terminalId(accessLog.getTerminalId())
                .inOut(accessLog.getInOut())
                .readStatus(accessLog.getReadStatus())
                .processed(accessLog.getProcessed())
                .etlRunTime(accessLog.getEtlRunTime())
                .isManual(accessLog.getIsManual())
                .createdDate(accessLog.getCreatedDate())
                .updatedDate(accessLog.getUpdatedDate())
                .isActive(accessLog.getIsActive())
                .build();
    }

    public AccessLogDTO accessLogToDTO(AccessLogEntity accessLogEntity) {
        AccessLogDTO accessLogDTO = new AccessLogDTO();
        accessLogDTO.setId(accessLogEntity.getId());
        accessLogDTO.setEmployeeID(accessLogEntity.getEmployeeId());
        accessLogDTO.setLogDate(accessLogEntity.getLogDate());
        accessLogDTO.setLogTime(accessLogEntity.getLogTime());
        accessLogDTO.setTerminalID(accessLogEntity.getTerminalId());
        accessLogDTO.setInOut(accessLogEntity.getInOut());
        accessLogDTO.setReadStatus(accessLogEntity.getReadStatus());
        accessLogDTO.setProcessed(accessLogEntity.getProcessed());
        accessLogDTO.setEtlRunTime(accessLogEntity.getEtlRunTime());
        accessLogDTO.setIsManual(accessLogEntity.getIsManual());
        accessLogDTO.setCreatedDate(accessLogEntity.getCreatedDate());
        accessLogDTO.setUpdatedDate(accessLogEntity.getUpdatedDate());
        accessLogDTO.setIsActive(accessLogEntity.getIsActive());
        return accessLogDTO;
    }

    public InOutDTO inOutDTO(InOutEntity inOutEntity) {
        InOutDTO inOutDTO = new InOutDTO();
        inOutDTO.setId(inOutEntity.getId());
        inOutDTO.setEmployeeID(inOutEntity.getEmployeeId());
        inOutDTO.setDate(inOutEntity.getDate());
        inOutDTO.setPunchTime(inOutEntity.getPunchTime());
        inOutDTO.setPunchTypeTime(inOutEntity.getPunchTypeTime());
        inOutDTO.setInOutType(inOutEntity.getInOutType());
        inOutDTO.setTerminalID(inOutEntity.getTerminalId());
        inOutDTO.setInOutValue(inOutEntity.getInOutValue());
        inOutDTO.setIsManual(inOutEntity.getIsManual());
        inOutDTO.setEtlRunTime(inOutEntity.getEtlRunTime());
        inOutDTO.setCreatedDate(inOutEntity.getCreatedDate());
        inOutDTO.setUpdatedDate(inOutEntity.getUpdatedDate());
        inOutDTO.setIsActive(inOutEntity.getIsActive());

        if (inOutEntity.getAccessLog() != null) {
            inOutDTO.setAccessLog(accessLogToDTO(inOutEntity.getAccessLog()));
        }

        return inOutDTO;
    }

    public AttendanceDTO toAttendanceDTO(AttendanceEntity entity) {
        if (entity == null) {
            return null;
        }

        AttendanceDTO dto = new AttendanceDTO();

        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null);
        dto.setUserId(entity.getEmployee() != null ? entity.getEmployee().getSltId() : null);
        dto.setDate(entity.getDate());
        dto.setArrivalDate(entity.getArrivalDate());

        dto.setArrivalTime(entity.getArrivalTime());
        dto.setLeftTime(entity.getLeftTime());

        dto.setArrivalTimeRaw(entity.getArrivalTimeRaw());
        dto.setLeftTimeRaw(entity.getLeftTimeRaw());

        dto.setTerminalId(entity.getTerminalId());

        dto.setAttendanceType(entity.getAttendanceType());
        dto.setLeaveStatus(entity.getLeaveStatus());
        dto.setPayStatus(entity.getPayStatus());
        dto.setResolve(entity.getResolve());
        dto.setRosterType(entity.getRosterType());

        dto.setIsLate(entity.getIsLate());
        dto.setIsLateCovered(entity.getIsLateCovered());
        dto.setIsUnauthorized(entity.getIsUnauthorized());
        dto.setIsUnSuccessful(entity.getIsUnSuccessful());
        dto.setIsHoliday(entity.getIsHoliday());
        dto.setIsResolved(entity.getIsResolved());
        dto.setHasIssues(entity.getHasIssues());
        dto.setIsManual(entity.getIsManual());
        dto.setIsActive(entity.getIsActive());

        dto.setIssueDescription(entity.getIssueDescription());
        dto.setDueDateForUA(entity.getDueDateForUA());
        dto.setEtlRunTime(entity.getEtlRunTime());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());

        dto.setViaMovement(entity.getViaMovement());
        dto.setViaLeave(entity.getViaLeave());


        List<InOutDTO> inOutDTOS = inOutRepo.findAllByAttendance(entity)
                .parallelStream()
                .map(this::inOutDTO)
                .collect(Collectors.toList());

        dto.setInOutDTOs(inOutDTOS);

        return dto;
    }

    public MovementDTO toMovementDTO(MovementsEntity entity) {
        if (entity == null) return null;

        MovementDTO dto = new MovementDTO();
        List<MovementTra> movementAdminsList = new ArrayList<>();

        if (entity.getAdmins() != null) {
            entity.getAdmins().forEach(adminEntity -> {
                Optional<EmployeeEntity> empOpt = employeeRepo.findBySltId(adminEntity.getEmployee().getSltId());
                if (!empOpt.isPresent()) {
                    empOpt = employeeRepo.findByEmployeeId(adminEntity.getEmployee().getSltId());
                }

                if (empOpt.isPresent()) {
                    EmployeeEntity employee = empOpt.get();
                    MovementTra adminTra = new MovementTra();

                    adminTra.setId(adminEntity.getId());
                    adminTra.setMovementId(adminEntity.getComponetID());
                    adminTra.setUserId(adminEntity.getEmployee().getPublicId());
                    adminTra.setSltId(adminEntity.getEmployee().getSltId());
                    adminTra.setEmployeeId(adminEntity.getEmployee().getEmployeeId());
                    adminTra.setApprovedDate(adminEntity.getApprovedDate());
                    adminTra.setHighestRolePriority(adminEntity.getHighestRolePriority());
                    adminTra.setAccepted(adminEntity.getIsAccepted());
                    adminTra.setEmail(employee.getEmail());
                    adminTra.setFirstName(employee.getFirstName());
                    adminTra.setLastName(employee.getLastName());
                    adminTra.setProfilePic(employee.getProfilePic());
                    movementAdminsList.add(adminTra);
                }
            });
        }

        dto.setAdminsTra(movementAdminsList);

        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setUserId(entity.getEmployee() != null ? entity.getEmployee().getPublicId() : null);
        dto.setInTime(entity.getInTimeRaw());
        dto.setOutTime(entity.getOutTimeRaw());
        dto.setHappenDateRaw(entity.getHappenDateRaw());
        dto.setComment(entity.getComment());
        dto.setLogTime(entity.getLogTime());
        dto.setCategory(entity.getCategory());
        dto.setDestination(entity.getDestination());
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null);
        dto.setReqDate(entity.getReqDate());
        dto.setMovementType(entity.getMovementType());
        dto.setAttSync(entity.getAttSync());
        dto.setHappenDate(entity.getHappenDate());
        dto.setRequestStatus(entity.getRequestStatus());
        dto.setCreateDate(entity.getCreateDate());
        dto.setUpdateDate(entity.getUpdateDate());
        dto.setIsEdited(entity.getIsEdited());

        if (entity.getAttendance() != null) {
            dto.setAttendance(entity.getAttendance().getPublicId());
        }

        return dto;
    }

    public NopayDTO toNopayDTO(NoPayEntity entity) {
        if (entity == null) return null;

        NopayDTO dto = new NopayDTO();

        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getSltId() : null);
        dto.setAttendanceId(entity.getAttendance() != null ? entity.getAttendance().getId() : null);
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setDate(entity.getDate());
        dto.setComment(entity.getComment());
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setUpdatedDate(entity.getUpdatedDate());
        dto.setIsActive(entity.getIsActive());

        Optional<NoPayReasonEntity> entities = noPayReasonRepo.findNoPayReasonEntitiesByNoPay(entity);
        if(entities.isPresent()) {
            NoPayReasonEntity noPayReasonEntity = entities.get();
            NoPayReasonDTO noPayReasonDTO = toNoPayReasonDTO(noPayReasonEntity);
            dto.setReasons(noPayReasonDTO);
        }
        return dto;
    }

    public NoPayReasonDTO toNoPayReasonDTO(NoPayReasonEntity entity) {
        if (entity == null) return null;

        NoPayReasonDTO dto = new NoPayReasonDTO();

        dto.setId(entity.getId());
        dto.setReasonCode(entity.getReason() != null ? entity.getReason().name() : null);
        dto.setCreatedDate(entity.getCreatedDate());
        dto.setIsActive(entity.getIsActive());

        return dto;
    }

    public LeaveDTO toLeaveDTO(LeaveEntity entity) {
        if (entity == null) return null;

        LeaveDTO dto = new LeaveDTO();
        List<LeaveTra> leaveAdminsList = new ArrayList<>();

        if (entity.getAdmins() != null) {
            entity.getAdmins().forEach(adminEntity -> {
                Optional<EmployeeEntity> empOpt = employeeRepo.findBySltId(adminEntity.getEmployee().getSltId());
                if (!empOpt.isPresent()) {
                    empOpt = employeeRepo.findByEmployeeId(adminEntity.getEmployee().getSltId());
                }

                if (empOpt.isPresent()) {
                    EmployeeEntity employee = empOpt.get();
                    LeaveTra adminTra = new LeaveTra();

                    adminTra.setId(adminEntity.getId());
                    adminTra.setLeaveId(adminEntity.getComponetID());
                    adminTra.setUserId(adminEntity.getEmployee().getPublicId());
                    adminTra.setSltId(adminEntity.getEmployee().getSltId());
                    adminTra.setEmployeeId(adminEntity.getEmployee().getEmployeeId());
                    adminTra.setApprovedDate(adminEntity.getApprovedDate());
                    adminTra.setHighestRolePriority(adminEntity.getHighestRolePriority());
                    adminTra.setAccepted(adminEntity.getIsAccepted());
                    adminTra.setEmail(employee.getEmail());
                    adminTra.setFirstName(employee.getFirstName());
                    adminTra.setLastName(employee.getLastName());
                    adminTra.setProfilePic(employee.getProfilePic());
                    leaveAdminsList.add(adminTra);
                }
            });
        }

        dto.setAdminsTra(leaveAdminsList);

        dto.setPublicId(entity.getPublicId());
        dto.setId(entity.getId());
        dto.setEmployeeID(entity.getEmployee() != null ? entity.getEmployee().getEmployeeId() : null);
        dto.setSubmitDate(entity.getSubmitDate());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        dto.setLeaveType(entity.getLeaveType());
        dto.setNumOfDays(entity.getNumOfDays());
        dto.setDescription(entity.getDescription());
        dto.setComponentBehavior(entity.getComponentBehavior());
        dto.setRequestStatus(entity.getRequestStatus());
        dto.setNotUsed(entity.getNotUsed());
        dto.setIsManualRequest(entity.getIsManualRequest());
        dto.setIsEdited(entity.getIsEdited());
        dto.setHappenDate(entity.getHappenDate());
        dto.setCreateDate(entity.getCreateDate());
        dto.setUpdateDate(entity.getUpdateDate());
        dto.setUserId(entity.getEmployee() != null ? entity.getEmployee().getPublicId() : null);

        return dto;
    }
    public AttendanceEntity toAttendanceEntity(AttendanceReq req) {
        if (req == null) throw new IllegalArgumentException(ErrorMessages.MISSING_REQUIRED_FIELD.getErrorMessage());

        Optional<EmployeeEntity> employee = helper.getEmployeeByIdV2(req.getEmployeeID());
        if (!employee.isPresent()) throw new IllegalArgumentException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        EmployeeEntity employeeEntity = employee.get();

        if (req.getDate() == null) {
            throw new IllegalArgumentException("Date is required for AttendanceEntity");
        }

        AttendanceEntity entity = new AttendanceEntity();
        entity.setIsManual(true);
        entity.setEmployee(employeeEntity);
        entity.setPublicId(utils.generateId(10));
        entity.setEtlRunTime(new Date());
        entity.setDate(helper.removeTimeFromDate(req.getDate()));

        entity.setArrivalDate(req.getArrivalDate() != null ?
                helper.removeTimeFromDate(req.getArrivalDate()) : null);

        entity.setArrivalTime(req.getArrivalTime());
        entity.setLeftTime(req.getLeftTime());
        entity.setIssueDescription(req.getIssueDescription());
        entity.setDueDateForUA(req.getDueDateForUA());

        if (req.getTerminalID() != null) {
            entity.setTerminalId(req.getTerminalID());
        }

        entity.setUpdatedDate(new Date());

        if (Boolean.TRUE.equals(req.getIsFullDay())) {
            entity.setAttendanceType(AttendanceType.FULL_DAY);
        } else if (Boolean.TRUE.equals(req.getIsHalfDay())) {
            entity.setAttendanceType(AttendanceType.HALF_DAY);
        } else if (Boolean.TRUE.equals(req.getIsAbsent())) {
            entity.setAttendanceType(AttendanceType.ABSENT);
        }

        if(req.getLeaveStatus() != null) entity.setLeaveStatus(req.getLeaveStatus());
        if(req.getPayStatus() != null) entity.setPayStatus(req.getPayStatus());
        if(req.getResolve() != null) entity.setResolve(req.getResolve());
        if(req.getAttendanceType() != null) entity.setAttendanceType(req.getAttendanceType());

        if (Boolean.TRUE.equals(req.getViaMovement())) {
            entity.setResolve(ResolveType.VIA_MOVEMENT);
        } else if (Boolean.TRUE.equals(req.getViaLeave())) {
            entity.setResolve(ResolveType.VIA_LEAVE);
        }

        entity.setIsLate(Boolean.TRUE.equals(req.getIsLate()));
        entity.setIsLateCovered(Boolean.TRUE.equals(req.getLateCover()));
        entity.setIsUnauthorized(Boolean.TRUE.equals(req.getIsUnAuthorized()));
        entity.setIsUnSuccessful(Boolean.TRUE.equals(req.getIsUnSuccessful()));
        entity.setHasIssues(Boolean.TRUE.equals(req.getIssues()));
        entity.setIsActive(Boolean.TRUE.equals(req.getActive()));

        return entity;
    }

    public void updateAttendanceEntityFromReq(AttendanceEntity entity, AttendanceReq req) {
        if (entity == null || req == null) return;

        if (req.getEmployeeID() == null) return;
        EmployeeEntity employee = Stream.of(
                        employeeRepo.findByEmployeeId(req.getEmployeeID()),
                        employeeRepo.findBySltId(req.getEmployeeID()),
                        employeeRepo.findByPublicId(req.getEmployeeID())
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        entity.setEmployee(employee);

        /* if (req.getArrivalDate() != null) entity.setArrivalDate(req.getArrivalDate()); */
        if (req.getArrivalTime() != null) entity.setArrivalTime(req.getArrivalTime());
        if (req.getLeftTime() != null) entity.setLeftTime(req.getLeftTime());

        if (req.getArrivalTimeRaw() != null) entity.setArrivalTimeRaw(req.getArrivalTimeRaw());
        if (req.getLeftTimeRaw() != null) entity.setLeftTimeRaw(req.getLeftTimeRaw());

        if (req.getIssueDescription() != null) entity.setIssueDescription(req.getIssueDescription());
        if (req.getDueDateForUA() != null) entity.setDueDateForUA(req.getDueDateForUA());

        if (Boolean.TRUE.equals(req.getIsFullDay())) {
            entity.setAttendanceType(AttendanceType.FULL_DAY);
        } else if (Boolean.TRUE.equals(req.getIsHalfDay())) {
            entity.setAttendanceType(AttendanceType.HALF_DAY);
        } else if (Boolean.TRUE.equals(req.getIsAbsent())) {
            entity.setAttendanceType(AttendanceType.ABSENT);
        }

        if(req.getLeaveStatus() != null) entity.setLeaveStatus(req.getLeaveStatus());
        else entity.setLeaveStatus(null);

        if(req.getPayStatus() != null) entity.setPayStatus(req.getPayStatus());
        
        if(req.getResolve() != null) entity.setResolve(req.getResolve());
        else entity.setResolve(null);

        if(req.getAttendanceType() != null) entity.setAttendanceType(req.getAttendanceType());


        if (req.getViaMovement() != null && Boolean.TRUE.equals(req.getViaMovement())) {
            entity.setResolve(ResolveType.VIA_MOVEMENT);
        } else if (req.getViaLeave() != null && Boolean.TRUE.equals(req.getViaLeave())) {
            entity.setResolve(ResolveType.VIA_LEAVE);
        }
        entity.setIsLate(req.getIsLate());
        entity.setIsLateCovered(req.getLateCover());
        entity.setIsUnauthorized(req.getIsUnAuthorized());
        entity.setIsUnSuccessful(req.getIsUnSuccessful());
        entity.setHasIssues(req.getIssues());
        entity.setIsActive(req.getActive());
        entity.setIsHoliday(req.getIsHoliday());
        entity.setIsResolved(req.getIsResolved());
        entity.setIsManual(req.getIsManual());

        entity.setUpdatedDate(new Date());
    }

    public Date stripTimeFromDate(Date dateWithTime) {
        if (dateWithTime == null) return null;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(dateWithTime);
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0);
        calendar.set(java.util.Calendar.MINUTE, 0);
        calendar.set(java.util.Calendar.SECOND, 0);
        calendar.set(java.util.Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public AccessLogEntity toAccessLogEntity(AccessLogReq req) {
        if (req == null) throw new IllegalArgumentException("InOutReq cannot be null");

        AccessLogEntity entity = new AccessLogEntity();
        req.setEmployeeID(req.getEmployeeID());
        req.setLogDate(req.getLogDate());
        req.setLogTime(req.getLogTime());
        req.setTerminalID(req.getTerminalID());
        req.setInOut(req.getInOut());
        req.setReadStatus(req.getReadStatus());
        req.setProcessed(req.getProcessed());
        return entity;
    }

    public Holiday mapReqoHoliday(HolidayReq holidayReq) {
        if (holidayReq == null) throw new IllegalArgumentException("HolidayReq cannot be null");
        Holiday holiday = new Holiday();
        holiday.setHolidayDate(holidayReq.getHolidayDate());
        holiday.setDescription(holidayReq.getDescription());
        holiday.setRecurring(holidayReq.isRecurring());
        holiday.setCreatedAt(LocalDateTime.now());
        return holiday;
    }

    public HolidayDTO maoHolidayToDTO(Holiday holiday) {
        if (holiday == null) throw new IllegalArgumentException("Holiday cannot be null");
        HolidayDTO holidayDTO = new HolidayDTO();
        holidayDTO.setId(holiday.getId());
        holidayDTO.setHolidayDate(holiday.getHolidayDate());
        holidayDTO.setDescription(holiday.getDescription());
        holidayDTO.setRecurring(holiday.isRecurring());
        holidayDTO.setCreatedAt(LocalDateTime.now());
        return holidayDTO;
    }
}