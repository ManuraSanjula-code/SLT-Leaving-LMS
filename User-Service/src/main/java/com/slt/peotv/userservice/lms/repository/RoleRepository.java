package com.slt.peotv.userservice.lms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.slt.peotv.userservice.lms.entity.RoleEntity;
import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Long> {
	RoleEntity findByName(String name);
	List<RoleEntity> findAllByOrderByPriorityAsc();
	Optional<RoleEntity> findByPublicId(String publicId);

	@Query("SELECT r FROM RoleEntity r WHERE r.priority BETWEEN :minPriority AND :maxPriority")
	Page<RoleEntity> findRolesByPriorityBetween(
			@Param("minPriority") int minPriority,
			@Param("maxPriority") int maxPriority,
			Pageable pageable);
}
