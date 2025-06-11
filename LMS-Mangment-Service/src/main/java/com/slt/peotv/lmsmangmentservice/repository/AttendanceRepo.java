package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.Enum.AttendanceType;
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

    Page<AttendanceEntity> findByAttendanceType(AttendanceType attendanceType,
                                                Pageable pageable);
    Page<AttendanceEntity> findByEmployeeAndAttendanceType(EmployeeEntity employee, AttendanceType attendanceType,
                                                           Pageable pageable);
    Page<AttendanceEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    List<AttendanceEntity> findByEmployee(EmployeeEntity employee);
    List<AttendanceEntity> findByDateAndEmployee(Date date, EmployeeEntity employee);
    List<AttendanceEntity> findByEmployeeAndDateBetween(EmployeeEntity employee, Date startDate, Date endDate);
    Optional<AttendanceEntity> findByPublicId(String publicId);
    boolean existsByEmployeeAndDate(EmployeeEntity Employee, Date date);
    boolean existsByEmployeeAndArrivalDateAndArrivalTime(EmployeeEntity employee, Date arrivalDate, Time arrivalTime);
    Optional<AttendanceEntity> findByEmployeeAndDate(EmployeeEntity employee, Date date);

    @Query("SELECT a FROM AttendanceEntity a WHERE a.employee = :employee AND a.date = :date AND a.isActive = true")
    Optional<AttendanceEntity> findByEmployeeAndDateAndIsActiveTrue(EmployeeEntity employee, Date date);
    Optional<AttendanceEntity> findByEmployeeAndArrivalDate(EmployeeEntity employee, Date arrivalDate);
    Page<AttendanceEntity> findByIsUnSuccessfulTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnauthorizedTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnSuccessfulTrueAndEmployee(EmployeeEntity employeeId, Pageable pageable);

    Page<AttendanceEntity> findByIsUnauthorizedTrueAndEmployee(EmployeeEntity employee, Pageable pageable);
    Page<AttendanceEntity> findByIsLateTrue(Pageable pageable);

    Page<AttendanceEntity> findByIsLateCovered(Boolean isLateCovered,
                                               Pageable pageable);
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

    List<AttendanceEntity> findByEmployeeAndDateAndTerminalId(EmployeeEntity employee, Date date, String terminalId);
    long countByEmployeeAndDate(EmployeeEntity employee, Date date);
}
