package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface InOutRepo extends CrudRepository<InOutEntity, Long> {

    List<InOutEntity> findByEmployeeID(String employeeID);

    List<InOutEntity> findByIsMoaningTrueAndIsEveningFalseAndPunchInMoa(Date punchInMoa);

    List<InOutEntity> findByIsMoaningFalseAndIsEveningTrueAndPunchInEv(Date punchInEv);

    List<InOutEntity> findByDateAndTimeMoa(Date date, Time timeMoa);

    List<InOutEntity> findByDateAndTimeMoaBetween(Date date, Time startTime, Time endTime);

    List<InOutEntity> findByDateAndTimeEveBetween(Date date, Time timeEveAfter, Time timeEveBefore);

    List<InOutEntity> findByDateAndTimeMoaAfter(Date date, Time timeMoa);

    /*@Query("SELECT i FROM InOutEntity i WHERE i.date = :yesterday AND i.timeMoa > '08:30:00' " +
            "AND TIMESTAMPDIFF(MINUTE, i.timeEve, '17:30:00') >= TIMESTAMPDIFF(MINUTE, '08:30:00', i.timeMoa)")
    List<InOutEntity> findByDateAndTimeMoaAfter830AndCoveredLateArrival(@Param("yesterday") Date yesterday);

    List<InOutEntity> findByDateAndTimeMoaAfterAndTimeEveBetween(Date date, Time startTime, Time endTime, Time eveAfter);

    @Query("SELECT i FROM InOutEntity i WHERE i.date = :yesterday AND i.timeMoa > '08:30:00' " +
            "AND TIMESTAMPDIFF(MINUTE, i.timeEve, '17:30:00') < TIMESTAMPDIFF(MINUTE, '08:30:00', i.timeMoa)")
    List<InOutEntity> findByDateAndTimeMoaAfter830AndNotCoveredLateArrival(@Param("yesterday") Date yesterday);
    List<InOutEntity> findByDateAndIsMoaningFalseAndTimeEveBetween(Date date, Time startTime, Time endTime);

    */

    /*@Query("SELECT u FROM UserEntity u WHERE u.employeeID NOT IN " +
            "(SELECT io.employeeID FROM InOutEntity io WHERE io.date = :date)")
    List<UserEntity> findAbsentEmployeesByDate(@Param("date") Date date);*/

    List<InOutEntity> findByDate(Date date);

    List<InOutEntity> findByEmployeeIDAndDate(String employeeID, Date date);
}

