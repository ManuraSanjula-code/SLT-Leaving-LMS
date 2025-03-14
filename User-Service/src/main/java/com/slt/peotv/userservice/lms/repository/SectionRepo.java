package com.slt.peotv.userservice.lms.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.slt.peotv.userservice.lms.entity.company.SectionEntity;

@Repository
public interface SectionRepo extends CrudRepository<SectionEntity, Long> {
    SectionEntity findBySection(String section);
}
