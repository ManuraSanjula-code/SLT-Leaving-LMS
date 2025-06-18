package com.slt.radio.rosterservice.Repo;


import com.slt.radio.rosterservice.Model.One.LMS.InOut;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InOutRepository extends MongoRepository<InOut, String> {
    List<InOut> findByEmployeeIdAndDate(String employeeId, Date date);
    Optional<InOut> findTopByEmployeeIdAndDateOrderByPunchTimeAsc(String employeeID, Date date);
    List<InOut> findByEmployeeIdAndDateAndPunchTime(String employeeId, Date date, Date punchTime);
    List<InOut> findByEmployeeIdAndDateAndPunchTimeAndPunchTypeTime(String employeeId, Date date, Date punchTime, LocalTime punchTypeTime);
}

