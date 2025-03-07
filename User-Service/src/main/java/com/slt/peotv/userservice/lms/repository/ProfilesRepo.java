package com.slt.peotv.userservice.lms.repository;

import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfilesRepo  extends CrudRepository<ProfilesEntity, Long> {
    ProfilesEntity findByName(String name);
}
