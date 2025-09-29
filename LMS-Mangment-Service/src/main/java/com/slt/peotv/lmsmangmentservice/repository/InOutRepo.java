package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Comparator;
import java.util.Optional;
import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface InOutRepo extends JpaRepository<InOutEntity, Long> {

    List<InOutEntity> findAllByAttendance(AttendanceEntity attendance);
    List<InOutEntity> findByEmployeeIdAndPunchTime(String employeeId, Date punchTime);
    Page<InOutEntity> findByEmployeeId(String employeeId, Pageable pageable);
    List<InOutEntity> findByEmployeeId(String employeeId);

    List<InOutEntity> findByDateAndPunchTypeTimeBefore(Date date, Time punchTypeTimeBefore);
    List<InOutEntity> findByDateAndPunchTypeTimeBetween(Date date, Time punchTypeTimeAfter, Time punchTypeTimeBefore);
    List<InOutEntity> findByEmployeeIdAndDateAndPunchTypeTime(String employeeId, Date date, Time punchTypeTime);
    List<InOutEntity> findByEmployeeIdAndPunchTimeAndPunchTypeTime(String employeeId, Date punchTime, Time punchTypeTime);

    List<InOutEntity> findByPunchTime(Date punchTime);
    List<InOutEntity> findByDateAndPunchTypeTimeAfter(Date date, Time punchTypeTimeAfter);
    List<InOutEntity> findByDate(Date date);
    List<InOutEntity> findByEmployeeIdAndDate(String employeeId, Date date);

    List<InOutEntity> findByEmployeeIdAndPunchTimeBetween(String employeeId, Date punchTimeAfter, Date punchTimeBefore);
    List<InOutEntity> findByEmployeeIdAndDateBetween(String employeeId, Date dateAfter, Date dateBefore);

    List<InOutEntity> findByEmployeeIdAndDateAndPunchTime(String employeeId, Date date, Date punchTime);
    List<InOutEntity> findByEmployeeIdAndPunchTimeAndPunchTypeTimeAndTerminalId(String employeeId, Date punchTime, Time punchTypeTime, String terminalId);
    Optional<InOutEntity> findByEmployeeIdAndDateAndPunchTypeTimeAndTerminalId(String employeeId, Date date, Time punchTypeTime, String terminalId);

    @Query("SELECT i FROM InOutEntity i " +
            "WHERE i.employeeId = :employeeId " +
            "AND i.date = :date " +
            "AND i.inOutType = 'EVENING_OUT' " +
            "ORDER BY i.punchTime DESC")
    List<InOutEntity> findEveningOutPunchesOrderedByTime(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    @Query("SELECT i FROM InOutEntity i " +
            "WHERE i.employeeId = :employeeId " +
            "AND i.date = :date " +
            "ORDER BY i.punchTypeTime ASC")
    List<InOutEntity> findByEmployeeIdAndDateOrderByPunchTypeTimeAsc(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    @Query("SELECT i FROM InOutEntity i " +
            "WHERE i.employeeId = :employeeId " +
            "AND i.date = :date " +
            "ORDER BY i.punchTypeTime DESC")
    List<InOutEntity> findByEmployeeIdAndDateOrderByPunchTypeTimeDesc(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    @Query(value = "SELECT * FROM in_out i " +
            "WHERE i.employee_id = :employeeId " +
            "AND i.date = :date " +
            "AND i.in_out_type = 'EVENING_OUT' " +
            "ORDER BY i.punch_time DESC LIMIT 1", nativeQuery = true)
    Optional<InOutEntity> findLatestEveningOutPunchNative(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    @Query(value = "SELECT * FROM in_out i " +
            "WHERE i.employee_id = :employeeId " +
            "AND i.date = :date " +
            "ORDER BY i.punch_type_time ASC LIMIT 1", nativeQuery = true)
    Optional<InOutEntity> findEarliestByEmployeeIdAndDateNative(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    @Query(value = "SELECT * FROM in_out i " +
            "WHERE i.employee_id = :employeeId " +
            "AND i.date = :date " +
            "ORDER BY i.punch_type_time DESC LIMIT 1", nativeQuery = true)
    Optional<InOutEntity> findLatestByEmployeeIdAndDateNative(
            @Param("employeeId") String employeeId,
            @Param("date") Date date);

    default Optional<InOutEntity> findLatestEveningOutPunch(String employeeId, Date date) {
        List<InOutEntity> results = findEveningOutPunchesOrderedByTime(employeeId, date);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    default Optional<InOutEntity> findEarliestByEmployeeIdAndDate(String employeeId, Date date) {
        List<InOutEntity> results = findByEmployeeIdAndDateOrderByPunchTypeTimeAsc(employeeId, date);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    default Optional<InOutEntity> findLatestByEmployeeIdAndDate(String employeeId, Date date) {
        List<InOutEntity> results = findByEmployeeIdAndDateOrderByPunchTypeTimeDesc(employeeId, date);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    default Optional<InOutEntity> findMorningInPunch(String employeeId, Date date) {
        return findByEmployeeIdAndDate(employeeId, date).stream()
                .filter(i -> i.getInOutValue() != null && i.getInOutValue() == 1)
                .filter(dto -> dto.getPunchTypeTime() != null) // IN
                .min(Comparator.comparing(InOutEntity::getPunchTime));
    }

    default Optional<InOutEntity> findEveningOutPunch(String employeeId, Date date) {
        return findByEmployeeIdAndDate(employeeId, date).stream()
                .filter(i -> i.getInOutValue() != null && i.getInOutValue() == 0) // OUT
                .filter(dto -> dto.getPunchTypeTime() != null)
                .max(Comparator.comparing(InOutEntity::getPunchTime));
    }
}