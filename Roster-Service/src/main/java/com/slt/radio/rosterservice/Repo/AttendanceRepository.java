package com.slt.radio.rosterservice.Repo;

import com.slt.radio.rosterservice.Model.One.LMS.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface AttendanceRepository extends MongoRepository<Attendance, String> {
    List<Attendance> findByEmployeeIdAndDate(String employeeID, Date date);
    List<Attendance> findByEmployeeIdAndArrivalDate(String employeeId, Date arrivalDate);
    List<Attendance> findByTeamIdAndDate(String teamId, Date date);
    Page<Attendance> findByDate(Date date, Pageable pageable);
    @Query(value = "{'employeeID': {'$in': ?0}, 'date': ?1}", fields = "{'employeeID': 1}")
    List<Attendance> findExistingAttendances(List<String> employeeIds, Date date);
    boolean existsByEmployeeIdAndArrivalDateAndArrivalTime(String employee, Date arrivalDate, LocalTime arrivalTime);
    boolean existsByEmployeeIdAndArrivalDate(String employee, Date arrivalDate);
    boolean existsByEmployeeIdAndDate(String employee, Date date);

}