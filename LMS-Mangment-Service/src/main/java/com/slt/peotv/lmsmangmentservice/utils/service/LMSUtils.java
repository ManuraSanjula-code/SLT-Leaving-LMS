package com.slt.peotv.lmsmangmentservice.utils.service;

import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Holiday;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayReasonEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.AuditLog;
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
import com.slt.peotv.lmsmangmentservice.model.req.InOutReq;
import com.slt.peotv.lmsmangmentservice.model.req.HolidayReq;
import com.slt.peotv.lmsmangmentservice.repository.NoPayReasonRepo;
import com.slt.peotv.lmsmangmentservice.repository.AuditLogRepo;
import com.slt.peotv.lmsmangmentservice.repository.AuditLogoRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.InOutRepo;
import com.slt.peotv.lmsmangmentservice.service.Check_Service;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.lang.reflect.Method;
import java.util.*;

@Service
public class LMSUtils {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private Utils utils;

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Autowired
    private Helper helper;

    @Autowired
    private Check_Service check_Service;

    @Autowired
    private AuditLogoRepo auditLogoRepo;

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
        dto.setTerminalId(entity.getTerminalId());

        dto.setAttendanceType(entity.getAttendanceType());
        dto.setLeaveStatus(entity.getLeaveStatus());
        dto.setPayStatus(entity.getPayStatus());
        dto.setResolve(entity.getResolve());

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
                .toList();

        dto.setInOutDTOs(inOutDTOS);

        return dto;
    }

    public AttendanceDTO toAttendanceDTOAdmin(AttendanceEntity entity) {
        if (entity == null) return null;
        AttendanceDTO dto = toAttendanceDTO(entity);
        return dto;
    }

    private String getFieldValue(Object obj, String methodName) {
        try {
            if (obj == null) return null;
            Method method = obj.getClass().getMethod(methodName);
            Object result = method.invoke(obj);
            return result != null ? result.toString() : null;
        } catch (Exception e) {
            // Log the error but don't fail the entire operation
            System.err.println("Failed to get field value using method: " + methodName + " on " + obj.getClass().getSimpleName() + " - " + e.getMessage());
            return null;
        }
    }

    private Object getEmployeeObject(Object editedByObj, String methodName) {
        try {
            if (editedByObj == null) return null;
            Method method = editedByObj.getClass().getMethod(methodName);
            Object result = method.invoke(editedByObj);

            // Check if result is a String (employee ID) instead of EmployeeEntity
            if (result instanceof String) {
                System.err.println("Warning: getEmployee() returned String instead of EmployeeEntity. This suggests a mapping issue.");
                return null;
            }

            return result;
        } catch (Exception e) {
            System.err.println("Failed to get employee object using method: " + methodName + " - " + e.getMessage());
            return null;
        }
    }

    public MovementDTO toMovementDTO(MovementsEntity entity) {
        if (entity == null) return null;

        MovementDTO dto = new MovementDTO();
        List<MovementTra> movementAdminsList = new ArrayList<>();

        if (entity.getAdmins() != null) {
            entity.getAdmins().forEach(adminEntity -> {
                Optional<EmployeeEntity> empOpt = employeeRepo.findBySltId(adminEntity.getEmployee().getSltId());
                if (empOpt.isEmpty()) {
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
        dto.setInTime(entity.getInTime());
        dto.setOutTime(entity.getOutTime());
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
        dto.setEmployeeId(entity.getEmployee() != null ? entity.getEmployee().getId().toString() : null);
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
                if (empOpt.isEmpty()) {
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

    public AuditLog createAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment, AuditAction action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEmployee(employee);
        auditLog.setAction(action);
        auditLog.setEntityId(entityId);
        auditLog.setEntityIdentifier(entityIdentifier);
        auditLog.setComment(comment);
        return auditLog;
    }

    public AuditLog createAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.CREATE);
    }

    public AuditLog updateAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.UPDATE);
    }

    public AuditLog deleteAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.DELETE);
    }

    public AuditLog approveAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.APPROVE);
    }

    public AuditLog rejectAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.REJECT);
    }

    public AuditLog resolveAuditLog(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, AuditAction.RESOLVE);
    }

    public AuditLog logAction(EmployeeEntity employee, Long entityId, String entityIdentifier, String comment, AuditAction action) {
        return createAuditLog(employee, entityId, entityIdentifier, comment, action);
    }

    public AttendanceEntity toAttendanceEntity(AttendanceReq req) {
        if (req == null) return null;

        Optional<EmployeeEntity> employee = employeeRepo.findBySltId(req.getEmployeeID());
        if (employee.isEmpty()) return null;

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
        entity.setArrivalDate(helper.removeTimeFromDate(req.getArrivalDate()));
        entity.setArrivalTime(req.getArrivalTime());
        entity.setLeftTime(req.getLeftTime());
        entity.setIssueDescription(req.getIssueDescription());
        entity.setDueDateForUA(req.getDueDateForUA());
        entity.setTerminalId(req.getTerminalID());
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

        entity.setIsLate(req.getIsLate());
        entity.setIsLateCovered(req.getLateCover());
        entity.setIsUnauthorized(req.getIsUnAuthorized());
        entity.setIsUnSuccessful(req.getIsUnSuccessful());
        entity.setHasIssues(req.getIssues());
        entity.setIsActive(req.getActive());

        return entity;
    }

    public void updateAttendanceEntityFromReq(AttendanceEntity entity, AttendanceReq req) {
        if (entity == null || req == null) return;

        if (req.getEmployeeID() == null) return;
        EmployeeEntity employee = employeeRepo.findByEmployeeId(req.getEmployeeID())
                .or(() -> employeeRepo.findBySltId(req.getEmployeeID()))
                .or(() -> employeeRepo.findByPublicId(req.getEmployeeID()))
                .orElseThrow(() -> new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage()));

        entity.setEmployee(employee);

        if (req.getArrivalDate() != null) entity.setArrivalDate(req.getArrivalDate());
        if (req.getArrivalTime() != null) entity.setArrivalTime(req.getArrivalTime());
        if (req.getLeftTime() != null) entity.setLeftTime(req.getLeftTime());
        if (req.getIssueDescription() != null) entity.setIssueDescription(req.getIssueDescription());
        if (req.getDueDateForUA() != null) entity.setDueDateForUA(req.getDueDateForUA());

        if (req.getIsFullDay() != null || req.getIsHalfDay() != null || req.getIsAbsent() != null) {
            if (Boolean.TRUE.equals(req.getIsFullDay())) {
                entity.setAttendanceType(AttendanceType.FULL_DAY);
            } else if (Boolean.TRUE.equals(req.getIsHalfDay())) {
                entity.setAttendanceType(AttendanceType.HALF_DAY);
            } else if (Boolean.TRUE.equals(req.getIsAbsent())) {
                entity.setAttendanceType(AttendanceType.ABSENT);
            }
        }

        if(req.getLeaveStatus() != null) entity.setLeaveStatus(req.getLeaveStatus());
        if(req.getPayStatus() != null) entity.setPayStatus(req.getPayStatus());
        if(req.getResolve() != null) entity.setResolve(req.getResolve());
        if(req.getAttendanceType() != null) entity.setAttendanceType(req.getAttendanceType());


        if (req.getViaMovement() != null && Boolean.TRUE.equals(req.getViaMovement())) {
            entity.setResolve(ResolveType.VIA_MOVEMENT);
        } else if (req.getViaLeave() != null && Boolean.TRUE.equals(req.getViaLeave())) {
            entity.setResolve(ResolveType.VIA_LEAVE);
        }

        if (req.getIsLate() != null) entity.setIsLate(req.getIsLate());
        if (req.getLateCover() != null) entity.setIsLateCovered(req.getLateCover());
        if (req.getIsUnAuthorized() != null) entity.setIsUnauthorized(req.getIsUnAuthorized());
        if (req.getIsUnSuccessful() != null) entity.setIsUnSuccessful(req.getIsUnSuccessful());
        if (req.getIssues() != null) entity.setHasIssues(req.getIssues());
        if (req.getActive() != null) entity.setIsActive(req.getActive());

        entity.setUpdatedDate(new Date());
    }

    public AttendanceReq toAttendanceReq(AttendanceEntity entity) {
        if (entity == null) return null;

        AttendanceReq req = new AttendanceReq();

        req.setEmployeeID(entity.getEmployee().getSltId());
        req.setDate(entity.getDate());
        req.setArrivalDate(entity.getArrivalDate());
        req.setArrivalTime(entity.getArrivalTime());
        req.setLeftTime(entity.getLeftTime());
        req.setTerminalID(entity.getTerminalId());
        req.setIssueDescription(entity.getIssueDescription());
        req.setDueDateForUA(entity.getDueDateForUA());

        if(req.getLeaveStatus() != null) entity.setLeaveStatus(req.getLeaveStatus());
        if(req.getPayStatus() != null) entity.setPayStatus(req.getPayStatus());
        if(req.getResolve() != null) entity.setResolve(req.getResolve());
        if(req.getAttendanceType() != null) entity.setAttendanceType(req.getAttendanceType());

        req.setIsLate(entity.getIsLate());
        req.setLateCover(entity.getIsLateCovered());
        req.setIsUnAuthorized(entity.getIsUnauthorized());
        req.setIsUnSuccessful(entity.getIsUnSuccessful());
        req.setIssues(entity.getHasIssues());
        req.setActive(entity.getIsActive());

        return req;
    }

    public void addAdminCommentToEntity(Object entity, String adminComment, String adminId) {
        if (adminComment == null || adminComment.isEmpty() || adminId == null) return;
        EmployeeEntity employee = employeeRepo.findBySltId(adminId)
                .or(() -> employeeRepo.findByEmployeeId(adminId))
                .or(() -> employeeRepo.findByPublicId(adminId))
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + adminId));

        AuditLog auditLog = new AuditLog();
        auditLog.setComment(adminComment);
        auditLog.setEmployee(employee);
        AuditLog savedAuditLog = auditLogRepo.save(auditLog);


        /*if (entity instanceof AttendanceEntity) {

        } else if (entity instanceof MovementsEntity) {

        } else if (entity instanceof LeaveEntity) {

        } else if (entity instanceof AccessLogEntity) {

        } else if (entity instanceof InOutEntity) {

        }*/
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

    public void validateInOutReq(InOutReq req) {
        if (req == null) {
            throw new IllegalArgumentException("InOutReq cannot be null");
        }

        // Validate required fields
        if (req.getEmployeeID() == null || req.getEmployeeID().trim().isEmpty()) {
            throw new IllegalArgumentException("EmployeeID is required and cannot be null or empty");
        }

        if (req.getDate() == null) {
            throw new IllegalArgumentException("Date is required and cannot be null");
        }

        if (req.getTerminalID() == null || req.getTerminalID().trim().isEmpty()) {
            throw new IllegalArgumentException("TerminalID is required and cannot be null or empty");
        }

        // Validate business logic constraints
        if (req.getPunchInMoa() == null && req.getPunchInEv() == null) {
            throw new IllegalArgumentException("At least one punch time (morning or evening) must be provided");
        }

        if (req.getPunchInMoa() != null && req.getTimeMoa() == null) {
            throw new IllegalArgumentException("TimeMoa is required when PunchInMoa is provided");
        }

        if (req.getPunchInEv() != null && req.getTimeEve() == null) {
            throw new IllegalArgumentException("TimeEve is required when PunchInEv is provided");
        }

        // Validate logical constraints
        if (req.getMoaning() != null && req.getMoaning() && req.getPunchInMoa() == null) {
            throw new IllegalArgumentException("PunchInMoa is required when isMoaning is true");
        }

        if (req.getEvening() != null && req.getEvening() && req.getPunchInEv() == null) {
            throw new IllegalArgumentException("PunchInEv is required when isEvening is true");
        }

        // Validate date consistency
        if (req.getPunchInMoa() != null && req.getPunchInEv() != null) {
            if (req.getPunchInMoa().after(req.getPunchInEv())) {
                throw new IllegalArgumentException("Morning punch time cannot be after evening punch time");
            }
        }

        // Validate InOut range (assuming it should be non-negative)
        if (req.getInOut() != null && req.getInOut() < 0) {
            throw new IllegalArgumentException("InOut value cannot be negative");
        }
    }

    public void validateAccessLogEntity(AccessLogReq entity) {
        if (entity == null) {
            throw new IllegalArgumentException("AccessLogEntity cannot be null");
        }
        if (entity.getEmployeeID() == null || entity.getEmployeeID().trim().isEmpty()) {
            throw new IllegalArgumentException("EmployeeID is required and cannot be null or empty");
        }
        if (entity.getLogDate() == null || entity.getLogDate().trim().isEmpty()) {
            throw new IllegalArgumentException("LogDate is required and cannot be null or empty");
        }
        if (entity.getLogTime() == null || entity.getLogTime().trim().isEmpty()) {
            throw new IllegalArgumentException("LogTime is required and cannot be null or empty");
        }
        if (entity.getTerminalID() == null || entity.getTerminalID().trim().isEmpty()) {
            throw new IllegalArgumentException("TerminalID is required and cannot be null or empty");
        }
        if (entity.getInOut() == null || entity.getInOut().trim().isEmpty()) {
            throw new IllegalArgumentException("InOut is required and cannot be null or empty");
        }
        if (entity.getReadStatus() == null || entity.getReadStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("ReadStatus is required and cannot be null or empty");
        }
    }

    public AccessLogEntity toAccessLogEntity(AccessLogReq req) {
        if (req == null) throw new IllegalArgumentException("InOutReq cannot be null");

        validateAccessLogEntity(req);

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