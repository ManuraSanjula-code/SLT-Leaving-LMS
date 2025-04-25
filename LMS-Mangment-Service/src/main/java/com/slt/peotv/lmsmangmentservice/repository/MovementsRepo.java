package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Movement.MovementsEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovementsRepo extends CrudRepository<MovementsEntity, Long> {
    Page<MovementsEntity> findAll(Pageable pageable);
    Page<MovementsEntity> findAllByEmployeeId(String employeeID, Pageable pageable);
    Page<MovementsEntity> findAllByUserId(String userId, Pageable pageable);
    Optional<MovementsEntity> findByPublicId(String publicId);
    /*List<MovementsEntity> findByDueDateBefore(Date currentDate);
    List<MovementsEntity> findByDueDate(Date dueDate);*/
    List<MovementsEntity> findByHappenDate(Date happenDate);
    List<MovementsEntity> findByIsPendingAndEmployeeId(Boolean isPending, String employeeID);
}
