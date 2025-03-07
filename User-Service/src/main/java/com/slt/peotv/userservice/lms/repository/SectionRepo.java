package com.slt.peotv.userservice.lms.repository;

import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepo extends CrudRepository<SectionEntity, Long> {
    SectionEntity findBySection(String section);
}
