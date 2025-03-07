package com.slt.peotv.lmsmangmentservice.service.impl;

import com.slt.peotv.lmsmangmentservice.entity.Absentee.AbsenteeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Attendance.types.AttendanceTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.LeaveEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveCategoryEntity;
import com.slt.peotv.lmsmangmentservice.entity.Leave.types.LeaveTypeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import com.slt.peotv.lmsmangmentservice.exceptions.LMSServiceException_AllReadyExits;
import com.slt.peotv.lmsmangmentservice.repository.*;
import com.slt.peotv.lmsmangmentservice.service.LMS_Service;
import com.slt.peotv.lmsmangmentservice.exceptions.ErrorMessages;
import com.slt.peotv.lmsmangmentservice.utils.Utils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    private Utils utils;

    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    public List<AbsenteeEntity> getAllAbsentee() {
        return absenteeRepo.findAll();
    }

    @Override
    public AbsenteeEntity getOneAbsentee(String publicId,String employeeId) {
        Optional<AbsenteeEntity> byPublicId = absenteeRepo.findByPublicId(publicId);
        if(byPublicId.isPresent()) {
            return byPublicId.get();
        }else
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void saveAbsentee(String employeeId, Boolean isHalfDay, Boolean swipeErr) {
        AbsenteeEntity absentee = new AbsenteeEntity();
        absentee.setEmployeeID(employeeId);
        absentee.setIsHalfDay(isHalfDay);

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
    public List<AttendanceEntity> getAllAttendance() {
        return (List<AttendanceEntity>) attendanceRepo.findAll();
    }

    @Override
    public List<AttendanceEntity> getAttendanceByUserId(String employeeId) {
        if (employeeId == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return attendanceRepo.findByEmployeeID(employeeId);
    }

    @Override
    public List<AttendanceEntity> getAttendanceByEmployeeId(String employeeId) {
        if (employeeId == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return attendanceRepo.findByEmployeeID(employeeId);
    }

    @Override
    public void createMovements(MovementsEntity entity) {
        movementsRepo.save(entity);
    }

    @Override
    public List<MovementsEntity> getAllMovementByUser(String employeeId) {
        List<MovementsEntity> allByUser = movementsRepo.findAllByEmployeeID(employeeId);
        if (allByUser.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return allByUser;
    }

    @Override
    public List<MovementsEntity> getAllMovements() {
        return (List<MovementsEntity>) movementsRepo.findAll();
    }

    @Override
    public MovementsEntity getMovement(String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isPresent()) return byPublicId.get();
        else throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
    }

    @Override
    public void updateMovement(MovementsEntity entity, String publicId) {
        Optional<MovementsEntity> byPublicId = movementsRepo.findByPublicId(publicId);
        if (byPublicId.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());

        MovementsEntity movementsEntity = byPublicId.get();
        // Update fields if not null
        if (entity.getInTime() != null) movementsEntity.setInTime(entity.getInTime());
        if (entity.getOutTime() != null) movementsEntity.setOutTime(entity.getOutTime());
        if (entity.getComment() != null) movementsEntity.setComment(entity.getComment());
        if (entity.getLogTime() != null) movementsEntity.setLogTime(entity.getLogTime());
        if (entity.getSupAppTime() != null) movementsEntity.setSupAppTime(entity.getSupAppTime());
        if (entity.getManAppTime() != null) movementsEntity.setManAppTime(entity.getManAppTime());
        if (entity.getHodAppTime() != null) movementsEntity.setHodAppTime(entity.getHodAppTime());
        if (entity.getCategory() != null) movementsEntity.setCategory(entity.getCategory());
        if (entity.getDestination() != null) movementsEntity.setDestination(entity.getDestination());
        if (entity.getEmployeeId() != null) movementsEntity.setEmployeeId(entity.getEmployeeId());
        if (entity.getReqDate() != null) movementsEntity.setReqDate(entity.getReqDate());
        if (entity.getHod() != null) movementsEntity.setHod(entity.getHod());
        if (entity.getSupervisor() != null) movementsEntity.setSupervisor(entity.getSupervisor());
        if (entity.getAttSync() != null) movementsEntity.setAttSync(entity.getAttSync());
        if (entity.getHappenDate() != null) movementsEntity.setHappenDate(entity.getHappenDate());
        if (entity.getMovementType() != null) movementsEntity.setMovementType(entity.getMovementType());

        // Save updated entity
        movementsRepo.save(movementsEntity);
    }

    @Override
    public void deleteMovements(String publicId) {
        MovementsEntity movement = getMovement(publicId);
        movementsRepo.delete(movement);
    }

    @Override
    public void createNoPay(NoPayEntity entity) {
        noPayRepo.save(entity);
    }

    @Override
    public List<NoPayEntity> getAllNoPayByUser(String employeeId) {
        List<NoPayEntity> byUser = noPayRepo.findByEmployeeID(employeeId);
        if (byUser.isEmpty()) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return byUser;
    }

    @Override
    public List<NoPayEntity> getAllNoPays() {
        return (List<NoPayEntity>) noPayRepo.findAll();
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
    public List<LeaveEntity> getAllLeaveByUserByPubicId(String employeeId) {
        if (employeeId == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return leaveRepo.findByEmployeeID(employeeId);
    }

    @Override
    public List<LeaveEntity> getAllLeaveByUserByEmployeeId(String employeeId) {
        if (employeeId == null) throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        return leaveRepo.findByEmployeeID(employeeId);
    }

    @Override
    public List<LeaveEntity> getAllLeaves() {
        return leaveRepo.findAll();
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
            leaveRepo.delete(byPublicId.get());
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
    public void deleteLeaveType(String name) {
        Optional<LeaveTypeEntity> result = leaveTypeRepo.findByName(name);
        if (result.isPresent()) {
            LeaveTypeEntity leaveTypeEntity = result.get();
            leaveTypeRepo.delete(leaveTypeEntity);
        } else {
            throw new NoSuchElementException(ErrorMessages.NO_RECORD_FOUND.getErrorMessage());
        }
    }
}


