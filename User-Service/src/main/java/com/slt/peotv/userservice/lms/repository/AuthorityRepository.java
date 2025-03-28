package com.slt.peotv.userservice.lms.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.slt.peotv.userservice.lms.entity.AuthorityEntity;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends CrudRepository<AuthorityEntity, Long> {
	AuthorityEntity findByName(String name);
	Optional<AuthorityEntity> findByPublicId(String publicId);

}
