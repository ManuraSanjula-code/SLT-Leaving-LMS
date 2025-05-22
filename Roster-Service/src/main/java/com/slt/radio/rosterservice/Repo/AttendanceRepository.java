package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    List<Attendance> findByEmployeeIDAndDate(String employeeID, Date date);
    List<Attendance> findByTeamIdAndDate(String teamId, Date date);
    Page<Attendance> findByDate(Date date, Pageable pageable);
}