package com.slt.peotv.lmsmangmentservice.mapper;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.AccessLog.AccessLogEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.EditedBy;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.model.dto.LeaveTra;
import com.slt.peotv.lmsmangmentservice.model.dto.MovementTra;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import com.slt.peotv.lmsmangmentservice.model.dto.*;
import com.slt.peotv.lmsmangmentservice.model.req.AccessLogReq;
import com.slt.peotv.lmsmangmentservice.model.req.AttendanceReq;
import com.slt.peotv.lmsmangmentservice.model.req.InOutReq;
import com.slt.peotv.lmsmangmentservice.repository.EditedByRepo;
import com.slt.peotv.lmsmangmentservice.repository.EmployeeRepo;
import com.slt.peotv.lmsmangmentservice.repository.LeaveTypeRepo;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LMSMapper {

    @Autowired
    private EmployeeRepo employeeRepo;

    @Autowired
    private Utils utils;

    @Autowired
    private EditedByRepo editedByRepo;

    @Autowired
    private LeaveTypeRepo leaveTypeRepository;

    public AccessLogDTO toDTO(AccessLogEntity entity) {
        if (entity == null) {
            return null;
        }

        AccessLogDTO dto = new AccessLogDTO();
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setLogDate(entity.getLogDate());
        dto.setLogTime(entity.getLogTime());
        dto.setTerminalID(entity.getTerminalID());
        dto.setInOut(entity.getInOut());
        dto.setReadStatus(entity.getReadStatus());
        dto.setProcessed(entity.getProcessed());
        dto.setEtlRunTime(entity.getEtlRunTime());

        return dto;
    }

    public List<AccessLogDTO> toAccessLogDTOList(List<AccessLogEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public InOutDTO toDTO(InOutEntity entity) {
        if (entity == null) {
            return null;
        }

        InOutDTO dto = new InOutDTO();
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setDate(entity.getDate());
        dto.setPunchInMoa(entity.getPunchInMoa());
        dto.setPunchInEv(entity.getPunchInEv());
        dto.setTimeMoa(entity.getTimeMoa());
        dto.setTimeEve(entity.getTimeEve());
        dto.setInOut(entity.getInOut());
        dto.setMoaning(entity.getIsMoaning());
        dto.setEvening(entity.getIsEvening());
        dto.setPast(entity.getIsPast());
        dto.setTerminalID(entity.getTerminalID());
        dto.setAccessLog(toDTO(entity.getAccessLog()));
        return dto;
    }

    public InOutEntity toEntity(InOutDTO dto) {
        if (dto == null) {
            return null;
        }

        return InOutEntity.builder()
                .employeeID(dto.getEmployeeID())
                .date(dto.getDate())
                .punchInMoa(dto.getPunchInMoa())
                .punchInEv(dto.getPunchInEv())
                .timeMoa(dto.getTimeMoa())
                .timeEve(dto.getTimeEve())
                .InOut(dto.getInOut())
                .isMoaning(dto.getMoaning())
                .isEvening(dto.getEvening())
                .isPast(dto.getPast())
                .terminalID(dto.getTerminalID())
                .build();
    }


    public List<InOutDTO> toDTOList(List<InOutEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


    public List<InOutEntity> toEntityList(List<InOutDTO> dtos) {
        if (dtos == null) {
            return null;
        }

        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    public AbsenteeDTO toAbsenteeDto(AbsenteeEntity entity) {
        if (entity == null) return null;

        AbsenteeDTO dto = new AbsenteeDTO();
        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setDate(entity.getDate());
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setUserId(entity.getUserId());
        dto.setAudited(entity.getAudited());
        dto.setIsNoPay(entity.getIsNoPay());
        return dto;
    }


    public AttendanceDTO toAttendanceDTO(AttendanceEntity entity) {
        if (entity == null) return null;

        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(entity.getId());
        dto.setPublicId(entity.getPublicId());
        dto.setDate(entity.getDate());
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setFullDay(entity.getIsFullDay());
        dto.setArrivalDate(entity.getArrivalDate());
        dto.setArrivalTime(entity.getArrivalTime());
        dto.setLeftTime(entity.getLeftTime());
        dto.setLate(entity.getIsLate());
        dto.setLateCover(entity.getLateCover());
        dto.setHalfDay(entity.getIsHalfDay());
        dto.setFullLeave(entity.getIsFullLeave());
        dto.setShortLeave(entity.getIsShortLeave());
        dto.setAbsent(entity.getIsAbsent());
        dto.setUnSuccessful(entity.getIsUnSuccessful());
        dto.setNoPay(entity.getIsNoPay());
        dto.setIssues(entity.getIssues());
        dto.setUnAuthorized(entity.getIsUnAuthorized());
        dto.setResolve(entity.getResolve());
        dto.setLeaveSuccess(entity.getLeaveSuccess());
        dto.setLeaveReq(entity.getLeaveReq());
        dto.setIssueDescription(entity.getIssueDescription());
        dto.setDueDateForUA(entity.getDueDateForUA());
        dto.setActive(entity.getActive());
        dto.setNopay(entity.getNopay());
        dto.setManual(entity.getIsManual());
        dto.setUserId(entity.getUserId());
        dto.setTerminalID(entity.getTerminalID());
        dto.setInOutDTOs(toDTOList(entity.getInOuts()));
        return dto;
    }

    public AttendanceDTO toAttendanceDTOAdmin(AttendanceEntity entity) {
        if (entity == null) return null;
        AttendanceDTO dto = toAttendanceDTO(entity);
        dto.setEditedByDTOs(toEditedByDTO(entity));
        return dto;
    }
    /*    public List<EditedByDTO> toEditedByDTO(AttendanceEntity entity){
        return entity.getEditedBys().stream().map(en->{
            if(en == null) return null;
            EditedByDTO dto = new EditedByDTO();
            dto.setComment(en.getComment());
            dto.setName(en.getEmployee().getFirstName() + " " + en.getEmployee().getLastName());
            dto.setProfilePicture(en.getEmployee().getProfilePic());
            dto.setSltId(en.getEmployee().getSltId());
            dto.setEmployeeId(en.getEmployee().getEmployeeId());
            return dto;
        }).toList();
    }*/
    /*public List<EditedByDTO> toEditedByDTO(Object entity) {
        try {
            Method getEditedBysMethod = entity.getClass().getMethod("getEditedBys");
            @SuppressWarnings("unchecked")
            List<Object> editedBys = (List<Object>) getEditedBysMethod.invoke(entity);

            return editedBys.stream().map(en -> {
                if(en == null) return null;

                EditedByDTO dto = new EditedByDTO();
                // Use reflection for each field access too
                dto.setComment(getFieldValue(en, "getComment"));
                dto.setName(getFieldValue(en, "getFirstName") + " " + getFieldValue(en, "getLastName"));
                dto.setProfilePicture(getFieldValue(en, "getProfilePic"));
                dto.setSltId(getFieldValue(en, "getSltId"));
                dto.setEmployeeId(getFieldValue(en, "getEmployeeId"));
                return dto;
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process entity", e);
        }
    }*/

    public List<EditedByDTO> toEditedByDTO(Object entity) {
        try {
            Method getEditedBysMethod = entity.getClass().getMethod("getEditedBys");
            @SuppressWarnings("unchecked")
            List<Object> editedBys = (List<Object>) getEditedBysMethod.invoke(entity);

            return editedBys.stream().map(editedByObj -> {
                if(editedByObj == null) return null;

                EditedByDTO dto = new EditedByDTO();

                // Get the comment directly from EditedBy entity
                dto.setComment(getFieldValue(editedByObj, "getComment"));

                // Get the employee object first, then access its properties
                Object employee = getEmployeeObject(editedByObj, "getEmployee");
                if (employee != null) {
                    String firstName = getFieldValue(employee, "getFirstName");
                    String lastName = getFieldValue(employee, "getLastName");
                    dto.setName((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : ""));
                    dto.setProfilePicture(getFieldValue(employee, "getProfilePic"));
                    dto.setSltId(getFieldValue(employee, "getSltId"));
                    dto.setEmployeeId(getFieldValue(employee, "getEmployeeId"));
                }

                return dto;
            }).toList();

        } catch (Exception e) {
            throw new RuntimeException("Failed to process entity", e);
        }
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
    /*private String getFieldValue(Object obj, String methodName) {
        try {
            Method method = obj.getClass().getMethod(methodName);
            return (String) method.invoke(obj);
        } catch (Exception e) {
            return null;
        }
    }*/

    public MovementDTO toMovementDTOAdmin(MovementsEntity entity){
        if (entity == null) return null;
        MovementDTO movementDTO = toMovementDTO(entity);
        movementDTO.setEditedByDTOs(toEditedByDTO(entity));
        return movementDTO;
    }

    public MovementDTO toMovementDTO(MovementsEntity entity) {
        if (entity == null) return null;

        MovementDTO dto = new MovementDTO();
        List<MovementTra> movementAdminsList = new ArrayList<>();

        // Convert admin entities to MovementTra DTOs
        if (entity.getAdmins() != null) {
            entity.getAdmins().forEach(adminEntity -> {
                Optional<EmployeeEntity> empOpt = employeeRepo.findBySltId(adminEntity.getEmployee().getSltId());
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
        dto.setUserId(entity.getEmployeeId());
        dto.setInTime(entity.getInTime());
        dto.setOutTime(entity.getOutTime());
        dto.setComment(entity.getComment());
        dto.setLogTime(entity.getLogTime());
        dto.setCategory(entity.getCategory());
        dto.setDestination(entity.getDestination());
        dto.setEmployeeId(entity.getEmployeeId());
        dto.setReqDate(entity.getReqDate());
        dto.setMovementType(entity.getMovementType());
        dto.setAttSync(entity.getAttSync());
        dto.setHappenDate(entity.getHappenDate());
        dto.setPending(entity.getIsPending());
        dto.setAccepted(entity.getIsAccepted());
        dto.setHalfDay(entity.getIsHalfDay());
        dto.setAbsent(entity.getIsAbsent());
        dto.setUnSuccessfulAttdate(entity.getIsUnSuccessfulAttdate());
        dto.setUnAuthorized(entity.getUnAuthorized());
        dto.setReject(entity.getIsReject());

        if (entity.getAttendance() != null) {
            dto.setAttendance(entity.getAttendance().getPublicId());
        }

        return dto;
    }

    public NopayDTO toNopayDTO(NoPayEntity entity) {
        if (entity == null) return null;

        NopayDTO dto = new NopayDTO();
        dto.setId(entity.getId());
        dto.setPublicId(entity.publicId);
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setSubmissionDate(entity.getSubmissionDate());
        dto.setAcctualDate(entity.getAcctualDate());
        dto.setHalfDay(entity.getIsHalfDay());
        dto.setUnSuccessful(entity.getUnSuccessful());
        dto.setLate(entity.getIsLate());
        dto.setLateCover(entity.getIsLateCover());
        dto.setAbsent(entity.getIsAbsent());
        dto.setComment(entity.getComment());
        dto.setHappenDate(entity.getHappenDate());

        if (entity.getAttendance() != null) {
            dto.setAttendance(entity.getAttendance().getPublicId());
        }

        return dto;
    }

    public LeaveDTO toLeaveDTO(LeaveEntity entity) {
        if (entity == null) return null;

        LeaveDTO dto = new LeaveDTO();
        List<LeaveTra> leaveAdminsList = new ArrayList<>();

        // Convert admin entities to LeaveTra DTOs
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
        dto.setEmployeeID(entity.getEmployeeID());
        dto.setSubmitDate(entity.getSubmitDate());
        dto.setFromDate(entity.getFromDate());
        dto.setToDate(entity.getToDate());
        dto.setLeaveType(entity.getLeaveType());
        dto.setIsNoPay(entity.getIsNoPay());
        dto.setNumOfDays(entity.getNumOfDays());
        dto.setDescription(entity.getDescription());
        dto.setHalfDay(entity.getIsHalfDay());
        dto.setFullDay(entity.getIsFullDay());
        dto.setUnSuccessful(entity.getUnSuccessful());
        dto.setLate(entity.getIsLate());
        dto.setLateCover(entity.getIsLateCover());
        dto.setShort_Leave(entity.getIsShort_Leave());
        dto.setPending(entity.getIsPending());
        dto.setAccepted(entity.getIsAccepted());
        dto.setNotUsed(entity.getNotUsed());
        dto.setCanceled(entity.getIsCanceled());
        dto.setManualRequest(entity.getIsManualRequest());
        dto.setHappenDate(entity.getHappenDate());
        dto.setUserId(entity.getUserId()); // Fixed: was getting from dto instead of entity
        dto.setReject(entity.getIsReject());

        return dto;
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
        entity.setPublicId(utils.generateId(10));
        entity.setUserId(employeeEntity.getPublicId());
        entity.setEtl_run_time(new Date());
        entity.setDate(req.getDate());
        entity.setEmployeeID(req.getEmployeeID());
        entity.setArrivalDate(req.getArrivalDate());
        entity.setArrivalTime(req.getArrivalTime());
        entity.setLeftTime(req.getLeftTime());
        entity.setIssueDescription(req.getIssueDescription());
        entity.setDueDateForUA(req.getDueDateForUA());
        entity.setIsFullDay(req.getFullDay());
        entity.setIsLate(req.getLate());
        entity.setLateCover(req.getLateCover());
        entity.setIsHalfDay(req.getHalfDay());
        entity.setIsFullLeave(req.getFullLeave());
        entity.setIsShortLeave(req.getShortLeave());
        entity.setIsAbsent(req.getAbsent());
        entity.setIsUnSuccessful(req.getUnSuccessful());
        entity.setIsNoPay(req.getNoPay());
        entity.setIssues(req.getIssues());
        entity.setIsUnAuthorized(req.getUnAuthorized());
        entity.setResolve(req.getResolve());
        entity.setLeaveSuccess(req.getLeaveSuccess());
        entity.setLeaveReq(req.getLeaveReq());
        entity.setActive(req.getActive());
        entity.setNopay(req.getNopay());
        entity.setViaMovement(req.getViaMovement());
        entity.setViaLeave(req.getViaLeave());
        entity.setTerminalID(req.getTerminalID());
        entity.setUpdateDate(new Date());

        return entity;
    }


    public void updateAttendanceEntityFromReq(AttendanceEntity entity, AttendanceReq req) {
        if (entity == null || req == null) return;

        if (req.getEmployeeID() != null) entity.setEmployeeID(req.getEmployeeID());
        if (req.getArrivalDate() != null) entity.setArrivalDate(req.getArrivalDate());
        if (req.getArrivalTime() != null) entity.setArrivalTime(req.getArrivalTime());
        if (req.getLeftTime() != null) entity.setLeftTime(req.getLeftTime());
        if (req.getIssueDescription() != null) entity.setIssueDescription(req.getIssueDescription());
        if (req.getDueDateForUA() != null) entity.setDueDateForUA(req.getDueDateForUA());
        if (req.getFullDay() != null) entity.setIsFullDay(req.getFullDay());
        if (req.getLate() != null) entity.setIsLate(req.getLate());
        if (req.getLateCover() != null) entity.setLateCover(req.getLateCover());
        if (req.getHalfDay() != null) entity.setIsHalfDay(req.getHalfDay());
        if (req.getFullLeave() != null) entity.setIsFullLeave(req.getFullLeave());
        if (req.getShortLeave() != null) entity.setIsShortLeave(req.getShortLeave());
        if (req.getAbsent() != null) entity.setIsAbsent(req.getAbsent());
        if (req.getUnSuccessful() != null) entity.setIsUnSuccessful(req.getUnSuccessful());
        if (req.getNoPay() != null) entity.setIsNoPay(req.getNoPay());
        if (req.getIssues() != null) entity.setIssues(req.getIssues());
        if (req.getUnAuthorized() != null) entity.setIsUnAuthorized(req.getUnAuthorized());
        if (req.getResolve() != null) entity.setResolve(req.getResolve());
        if (req.getLeaveSuccess() != null) entity.setLeaveSuccess(req.getLeaveSuccess());
        if (req.getLeaveReq() != null) entity.setLeaveReq(req.getLeaveReq());
        if (req.getActive() != null) entity.setActive(req.getActive());
        if (req.getNopay() != null) entity.setNopay(req.getNopay());
        if (req.getViaMovement() != null) entity.setViaMovement(req.getViaMovement());
        if (req.getViaLeave() != null) entity.setViaLeave(req.getViaLeave());

        entity.setUpdateDate(new Date());
    }


    public void addAdminCommentToEntity(Object entity, String adminComment, String adminId) {
        if (adminComment == null || adminComment.isEmpty() || adminId == null) return;
        EmployeeEntity employee = employeeRepo.findBySltId(adminId)
                .or(() -> employeeRepo.findByEmployeeId(adminId))
                .or(() -> employeeRepo.findByPublicId(adminId))
                .orElseThrow(() -> new RuntimeException("Employee not found with ID: " + adminId));

        EditedBy editedBy = new EditedBy();
        editedBy.setComment(adminComment);
        /*editedBy.setFirstName(employee.getFirstName());
        editedBy.setLastName(employee.getLastName());
        editedBy.setProfilePic(employee.getProfilePic());
        editedBy.setSltId(employee.getSltId());
        editedBy.setEmployeeId(employee.getEmployeeId());*/
        editedBy.setEmployee(employee);
        EditedBy savedEditedBy = editedByRepo.save(editedBy);


        // Handle different entity types
        if (entity instanceof AttendanceEntity) {
            AttendanceEntity attendanceEntity = (AttendanceEntity) entity;
            List<EditedBy> editedBys = attendanceEntity.getEditedBys();
            if (editedBys == null) editedBys = new ArrayList<>();
            editedBys.add(savedEditedBy);
            attendanceEntity.setIsEdited(true);
            attendanceEntity.setEditedBys(editedBys);
        } else if (entity instanceof MovementsEntity) {
            MovementsEntity movementEntity = (MovementsEntity) entity;
            List<EditedBy> editedBys = movementEntity.getEditedBys();
            if (editedBys == null) editedBys = new ArrayList<>();
            editedBys.add(savedEditedBy);
            movementEntity.setIsEdited(true);
            movementEntity.setEditedBys(editedBys);
        } else if (entity instanceof LeaveEntity) {
            LeaveEntity leaveEntity = (LeaveEntity) entity;
            List<EditedBy> editedBys = leaveEntity.getEditedBys();
            if (editedBys == null) editedBys = new ArrayList<>();
            editedBys.add(savedEditedBy);
            leaveEntity.setIsEdited(true);
            leaveEntity.setEditedBys(editedBys);
        }
        else if (entity instanceof AccessLogEntity) {
            AccessLogEntity accessLogEntity = (AccessLogEntity) entity;
            List<EditedBy> editedBys = accessLogEntity.getEditedBys();
            if (editedBys == null) editedBys = new ArrayList<>();
            editedBys.add(savedEditedBy);
            accessLogEntity.setIsEdited(true);
            accessLogEntity.setEditedBys(editedBys);
        }
        else if (entity instanceof InOutEntity) {
            InOutEntity inOutEntity = (InOutEntity) entity;
            List<EditedBy> editedBys = inOutEntity.getEditedBys();
            if (editedBys == null) editedBys = new ArrayList<>();
            editedBys.add(savedEditedBy);
            inOutEntity.setIsEdited(true);
            inOutEntity.setEditedBys(editedBys);
        }
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

    public InOutEntity toInOutEntity(InOutReq req) {
        if (req == null) {
            throw new IllegalArgumentException("InOutReq cannot be null");
        }

        // Validate required fields in request
        validateInOutReq(req);

        InOutEntity.InOutEntityBuilder builder = InOutEntity.builder()
                .employeeID(req.getEmployeeID().trim())
                .date(req.getDate())
                .punchInMoa(req.getPunchInMoa())
                .punchInEv(req.getPunchInEv())
                .timeMoa(req.getTimeMoa())
                .timeEve(req.getTimeEve())
                .terminalID(req.getTerminalID().trim())
                .etlRunTime(new Date())
                .isManual(true)
                .createDate(new Date())
                .isEdited(false)
                .editedBys(new ArrayList<>());

        // Handle optional fields with default values
        if (req.getInOut() != null) {
            builder.InOut(req.getInOut());
        } else {
            builder.InOut(0); // Default value
        }

        if (req.getMoaning() != null) {
            builder.isMoaning(req.getMoaning());
        } else {
            builder.isMoaning(false); // Default value
        }

        if (req.getEvening() != null) {
            builder.isEvening(req.getEvening());
        } else {
            builder.isEvening(false); // Default value
        }

        if (req.getPast() != null) {
            builder.isPast(req.getPast());
        } else {
            builder.isPast(false); // Default value
        }

        InOutEntity entity = builder.build();

        // Handle relationships if provided
        if (req.getAttendanceId() != null && !req.getAttendanceId().trim().isEmpty()) {
            // Note: You'll need to fetch the AttendanceEntity from repository in service layer
            // entity.setAttendance(attendanceRepo.findByPublicId(req.getAttendanceId()).orElse(null));
        }

        if (req.getAccessLog() != null && !req.getAccessLog().trim().isEmpty()) {
            // Note: You'll need to fetch the AccessLogEntity from repository in service layer
            // entity.setAccessLog(accessLogRepo.findByPublicId(req.getAccessLog()).orElse(null));
        }

        return entity;
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
    public AccessLogEntity toAccessLogEntity(AccessLogReq req){
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
}