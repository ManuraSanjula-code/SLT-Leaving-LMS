package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepo extends CrudRepository<AttendanceEntity, Long> {
    Page<AttendanceEntity> findAll(Pageable pageable);
    Page<AttendanceEntity> findByIsAbsent(Boolean isAbsent, Pageable pageable);
    Page<AttendanceEntity> findByEmployeeAndIsAbsent(EmployeeEntity employee, Boolean isAbsent,Pageable pageable);
    Page<AttendanceEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    List<AttendanceEntity> findByEmployee(EmployeeEntity employee);
    List<AttendanceEntity> findByDateAndEmployee(Date date, EmployeeEntity employee);
    List<AttendanceEntity> findByEmployeeAndDateBetween(EmployeeEntity employee, Date startDate, Date endDate);
    Optional<AttendanceEntity> findByPublicId(String publicId);
    boolean existsByEmployeeAndDate(EmployeeEntity Employee, Date date);
/*
    Optional<AttendanceEntity> findByEmployeeIDAndDate(String EmployeeID, Date date);
*/
    Optional<AttendanceEntity> findByEmployeeAndDate(EmployeeEntity employee, Date date);

    Optional<AttendanceEntity> findByEmployeeAndArrivalDate(EmployeeEntity employee, Date arrivalDate);
    Page<AttendanceEntity> findByIsAbsentTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsHalfDayTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnSuccessfulTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnAuthorizedTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnSuccessfulTrueAndEmployee(EmployeeEntity employeeId, Pageable pageable);
    Page<AttendanceEntity> findByIsUnAuthorizedTrueAndEmployee(EmployeeEntity employee, Pageable pageable);
    Page<AttendanceEntity> findByIsFullDayTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsLateTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsLateTrueAndLateCoverFalse(Pageable pageable);
    List<AttendanceEntity> findByDate(Date date);
    List<AttendanceEntity> findByDateBetween(Date startDate, Date endDate);
    List<AttendanceEntity> findByDateAndArrivalTimeBetween(Date date, Time startTime, Time endTime);


    @Query("SELECT e FROM AttendanceEntity e WHERE e.dueDateForUA < :currentDate")
    List<AttendanceEntity> findByDueDateForUA(@Param("currentDate") Date currentDate);

    List<AttendanceEntity> findByEmployeeAndArrivalDateBetween(
            EmployeeEntity employee,
            Date startDate,
            Date endDate
    );

    List<AttendanceEntity> findByEmployeeAndDateAndTerminalID(EmployeeEntity employee, Date date, String terminalID);
    long countByEmployeeAndDate(EmployeeEntity employee, Date date);
}
