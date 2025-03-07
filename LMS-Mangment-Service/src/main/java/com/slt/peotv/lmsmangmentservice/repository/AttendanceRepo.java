package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
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
    List<AttendanceEntity> findByEmployeeID(String employeeID);
    Optional<AttendanceEntity> findByPublicId(String publicId);
    boolean existsByEmployeeIDAndDate(String EmployeeID, Date date);
    Optional<AttendanceEntity> findByEmployeeIDAndDate(String EmployeeID, Date date);

    List<AttendanceEntity> findByIsAbsentTrue();
    List<AttendanceEntity> findByIsHalfDayTrue();
    List<AttendanceEntity> findByIsUnSuccessfulTrue();
    List<AttendanceEntity> findByIsFullDayTrue();
    List<AttendanceEntity> findByIsLateTrue();
    List<AttendanceEntity> findByIsLateTrueAndLateCoverFalse();
    List<AttendanceEntity> findByDate(Date date);
    List<AttendanceEntity> findByDateBetween(Date startDate, Date endDate);
    List<AttendanceEntity> findByDateAndArrivalTimeBetween(Date date, Time startTime, Time endTime);


    @Query("SELECT e FROM AttendanceEntity e WHERE e.dueDateForUA < :currentDate")
    List<AttendanceEntity> findByDueDateForUA(@Param("currentDate") Date currentDate);

}
