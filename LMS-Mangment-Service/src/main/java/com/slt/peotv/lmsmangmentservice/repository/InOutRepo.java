package com.slt.peotv.lmsmangmentservice.repository;

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

    Page<InOutEntity> findByEmployeeID(String employeeID, Pageable pageable);

    List<InOutEntity> findByIsMoaningTrueAndIsEveningFalseAndPunchInMoa(Date punchInMoa);

    List<InOutEntity> findByIsMoaningFalseAndIsEveningTrueAndPunchInEv(Date punchInEv);

    List<InOutEntity> findByPunchInMoaAndTimeMoaBefore(Date punchInMoa, Time timeMoa);
    List<InOutEntity> findByPunchInMoaAndTimeMoaAfter(Date punchInMoa, Time timeMoa);

    List<InOutEntity> findByDateAndTimeMoaBetween(Date date, Time startTime, Time endTime);
    List<InOutEntity> findByPunchInMoaAndTimeMoaBetween(Date date, Time startTime, Time endTime);

    List<InOutEntity> findByDateAndTimeEveBetween(Date date, Time timeEveAfter, Time timeEveBefore);

    List<InOutEntity> findByPunchInEvAndTimeEveBetween(Date punchInEve, Time timeEveAfter, Time timeEveBefore);
    List<InOutEntity> findByPunchInEv(Date date);
    List<InOutEntity> findByPunchInMoa(Date date);

    List<InOutEntity> findByDateAndTimeMoaAfter(Date date, Time timeMoa);
    List<InOutEntity> findByDateAndTimeEveAfter(Date date, Time timeMoa);
    List<InOutEntity> findByPunchInEvAndTimeEveAfter(Date date, Time timeMoa);

    List<InOutEntity> findByDate(Date date);

    List<InOutEntity> findByEmployeeIDAndDate(String employeeID, Date date);
    List<InOutEntity> findByEmployeeIDAndDateAndIsEvening(String employeeID, Date date, boolean isEvening);

}

