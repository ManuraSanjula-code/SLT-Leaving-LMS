package com.slt.peotv.userservice.lms.repository;

import com.slt.peotv.userservice.lms.entity.company.SectionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.slt.peotv.userservice.lms.entity.company.ProfilesEntity;

import java.util.Optional;

@Repository
public interface ProfilesRepo  extends CrudRepository<ProfilesEntity, Long> {
    ProfilesEntity findByName(String name);
    ProfilesEntity findByPublicId(String publicId);
}
