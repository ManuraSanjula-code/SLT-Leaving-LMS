package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
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
    Page<AttendanceEntity> findByUserId(String userId, Pageable pageable);
    List<AttendanceEntity> findByEmployeeID(String employeeID);

    Optional<AttendanceEntity> findByPublicId(String publicId);
    boolean existsByEmployeeIDAndDate(String EmployeeID, Date date);
    Optional<AttendanceEntity> findByEmployeeIDAndDate(String EmployeeID, Date date);
    Optional<AttendanceEntity> findByEmployeeIDAndArrivalDate(String employeeID, Date arrivalDate);

    Page<AttendanceEntity> findByIsAbsentTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsHalfDayTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnSuccessfulTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnAuthorizedTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsUnSuccessfulTrueAndUserId(String employeeId, Pageable pageable);
    Page<AttendanceEntity> findByIsUnAuthorizedTrueAndUserId(String employeeId, Pageable pageable);
    Page<AttendanceEntity> findByIsFullDayTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsLateTrue(Pageable pageable);
    Page<AttendanceEntity> findByIsLateTrueAndLateCoverFalse(Pageable pageable);
    List<AttendanceEntity> findByDate(Date date);
    List<AttendanceEntity> findByDateBetween(Date startDate, Date endDate);
    List<AttendanceEntity> findByDateAndArrivalTimeBetween(Date date, Time startTime, Time endTime);


    @Query("SELECT e FROM AttendanceEntity e WHERE e.dueDateForUA < :currentDate")
    List<AttendanceEntity> findByDueDateForUA(@Param("currentDate") Date currentDate);

    List<AttendanceEntity> findByEmployeeIDAndArrivalDateBetween(
            String employeeID,
            Date startDate,
            Date endDate
    );
}
