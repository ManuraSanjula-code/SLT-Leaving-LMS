package com.slt.peotv.lmsmangmentservice.repository;

import com.slt.peotv.lmsmangmentservice.entity.Employee.EmployeeEntity;
import com.slt.peotv.lmsmangmentservice.entity.NoPay.NoPayEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoPayRepo extends CrudRepository<NoPayEntity, Long> {
    Page<NoPayEntity> findByEmployee(EmployeeEntity employee, Pageable pageable);
    Page<NoPayEntity> findAll(Pageable pageable);
    Optional<NoPayEntity> findByPublicId(String publicId);
}
