package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.card.InOutEntity;
import org.apache.zookeeper.cli.LsCommand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.util.Date;
import java.util.List;

@Repository
public interface InOutRepo extends CrudRepository<InOutEntity, Long> {

    List<InOutEntity> findByEmployeeIDAndPunchInMoa(String employeeID, Date punchInMoa);
    List<InOutEntity> findByEmployeeIDAndPunchInEv(String employeeID, Date punchInEv);

    Page<InOutEntity> findByEmployeeID(String employeeID, Pageable pageable);

    List<InOutEntity> findByPunchInMoaAndTimeMoaBefore(Date punchInMoa, Time timeMoa);
    List<InOutEntity> findByPunchInMoaAndTimeMoaAfter(Date punchInMoa, Time timeMoa);

    List<InOutEntity> findByPunchInMoaAndTimeMoaBetween(Date date, Time startTime, Time endTime);
    List<InOutEntity> findByPunchInEvAndTimeEveBetween(Date punchInEve, Time timeEveAfter, Time timeEveBefore);

    List<InOutEntity> findByPunchInEv(Date date);
    List<InOutEntity> findByPunchInMoa(Date date);


    List<InOutEntity> findByPunchInEvAndTimeEveAfter(Date date, Time timeMoa);
    List<InOutEntity> findByDate(Date date);

    List<InOutEntity> findByEmployeeIDAndDate(String employeeID, Date date);

    List<InOutEntity> findByEmployeeIDAndPunchInMoaBetween(String employeeID,Date startDate, Date endDate);
    List<InOutEntity> findByEmployeeIDAndDateBetween(String employeeID, Date dateAfter, Date dateBefore);
    List<InOutEntity> findByEmployeeIDAndPunchInEvBetween(String employeeID,Date startDate, Date endDate);

    List<InOutEntity> findByEmployeeIDAndDateAndPunchInMoaAndPunchInEvAndTimeMoaAndTimeEve(String employeeID, Date date, Date punchInMoa, Date punchInEv, Time timeMoa, Time timeEve);
}

