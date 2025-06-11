package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Attendance.AttendanceEntity;
import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface InOutRepo extends CrudRepository<InOutEntity, Long> {

    List<InOutEntity> findAllByAttendance(AttendanceEntity attendance);
    List<InOutEntity> findByEmployeeIdAndPunchTime(String employeeId, Date punchTime);
    Page<InOutEntity> findByEmployeeId(String employeeId, Pageable pageable);

    List<InOutEntity> findByDateAndPunchTypeTimeBefore(Date date, Time punchTypeTimeBefore);
    List<InOutEntity> findByDateAndPunchTypeTimeBetween(Date date, Time punchTypeTimeAfter, Time punchTypeTimeBefore);

    List<InOutEntity> findByPunchTime(Date punchTime);;
    List<InOutEntity> findByDateAndPunchTypeTimeAfter(Date date, Time punchTypeTimeAfter);
    List<InOutEntity> findByDate(Date date);
    List<InOutEntity> findByEmployeeIdAndDate(String employeeId, Date date);

    List<InOutEntity> findByEmployeeIdAndPunchTimeBetween(String employeeId, Date punchTimeAfter, Date punchTimeBefore);
    List<InOutEntity> findByEmployeeIdAndDateBetween(String employeeId, Date dateAfter, Date dateBefore);

    List<InOutEntity> findByEmployeeIdAndDateAndPunchTime(String employeeId, Date date, Date punchTime);
}

