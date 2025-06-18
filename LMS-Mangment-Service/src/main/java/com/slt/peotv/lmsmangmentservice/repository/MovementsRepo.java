package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
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
    Page<MovementsEntity> findAllByEmployee(EmployeeEntity employee, Pageable pageable);
    List<MovementsEntity> findAllByEmployee(EmployeeEntity employee);
    Optional<MovementsEntity> findAllByEmployeeAndReqDate(EmployeeEntity employeeI, Date reqDate);
    Optional<MovementsEntity> findAllByEmployeeAndHappenDate(EmployeeEntity employeeI, Date happenDate);
    Optional<MovementsEntity> findByPublicId(String publicId);
    List<MovementsEntity> findByEmployeeAndReqDateBetween(
            EmployeeEntity employee,
            Date startDate,
            Date endDate
    );
}
