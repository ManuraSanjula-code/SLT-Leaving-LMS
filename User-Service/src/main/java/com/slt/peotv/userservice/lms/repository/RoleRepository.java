package com.slt.peotv.userservice.lms.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
	RoleEntity findByName(String name);
	List<RoleEntity> findAllByOrderByPriorityAsc();
	Optional<RoleEntity> findByPublicId(String publicId);
}
