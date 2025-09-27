package com.slt.radio.rosterservice.repo;

import com.slt.radio.rosterservice.model.one.lms.InOut;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query; 
import org.springframework.stereotype.Repository;
import com.slt.radio.rosterservice.model.enums.InOutType;

import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface InOutRepository extends MongoRepository<InOut, String> {
    List<InOut> findByEmployeeIdAndDate(String employeeId, Date date);

    List<InOut> findByEmployeeIdAndPunchTime(String employeeId, Date punchTime);
    List<InOut> findByEmployeeIdAndPunchTimeAndInOutType(String employeeId, Date punchTime, InOutType inOutType);


    Optional<InOut> findTopByEmployeeIdAndDateOrderByPunchTimeAsc(String employeeID, Date date);
    Optional<InOut> findTopByEmployeeIdAndDateOrderByPunchTimeDesc(String employeeId, Date date);

    List<InOut> findByEmployeeIdAndDateAndPunchTime(String employeeId, Date date, Date punchTime);
    List<InOut> findByEmployeeIdAndDateAndPunchTimeAndPunchTypeTime(String employeeId, Date date, Date punchTime, LocalTime punchTypeTime);
    
    @Query("{'employeeId': ?0, 'date': ?1, 'punchTypeTime': {$gte: ?2}}")
    List<InOut> findByEmployeeIdAndDateAndPunchTypeTimeAfter(String employeeId, Date date, LocalTime time);
    
    @Query("{'employeeId': ?0, 'date': ?1, 'punchTypeTime': {$lte: ?2}}")
    List<InOut> findByEmployeeIdAndDateAndPunchTypeTimeBefore(String employeeId, Date date, LocalTime time);
    
    @Query(value = "{'employeeId': ?0, 'date': ?1, 'punchTypeTime': {$gte: ?2}}", 
           sort = "{'punchTime': 1}")
    Optional<InOut> findEarliestPunchAfterTime(String employeeId, Date date, LocalTime time);
    
    @Query(value = "{'employeeId': ?0, 'date': ?1, 'punchTypeTime': {$lte: ?2}}", 
           sort = "{'punchTime': -1}")
    Optional<InOut> findLatestPunchBeforeTime(String employeeId, Date date, LocalTime time);

    boolean existsByEmployeeIdAndDateAndPunchTypeTimeAndInOutValue(String employeeId, Date date, LocalTime punchTypeTime, Integer inOutValue);
    boolean existsByEmployeeIdAndPunchTimeAndPunchTypeTimeAndInOutValue(String employeeId, Date punchTime, LocalTime punchTypeTime, Integer inOutValue);


}