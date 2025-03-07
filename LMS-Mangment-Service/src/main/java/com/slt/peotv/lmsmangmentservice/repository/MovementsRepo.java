package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovementsRepo extends CrudRepository<MovementsEntity, Long> {
    List<MovementsEntity> findAllByEmployeeID(String employeeID);
    Optional<MovementsEntity> findByPublicId(String publicId);
    /*List<MovementsEntity> findByDueDateBefore(Date currentDate);
    List<MovementsEntity> findByDueDate(Date dueDate);*/
    List<MovementsEntity> findByHappenDate(Date happenDate);
    List<MovementsEntity> findByIsPendingAndEmployeeID(Boolean isPending, String employeeID);
}
