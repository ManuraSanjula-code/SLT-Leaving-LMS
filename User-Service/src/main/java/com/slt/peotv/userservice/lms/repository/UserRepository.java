package com.slt.peotv.userservice.lms.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.slt.peotv.userservice.lms.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);
	UserEntity findByUserId(String userId);
	UserEntity findUserByEmailVerificationToken(String token);
	UserEntity findByEmployeeId(String employeeId);
    @Override
	Page<UserEntity> findAll(Pageable pageable);

	@Query(value="select * from users u where u.email_verification_status = true",
			countQuery="select count(*) from users u where u.email_verification_status = true",
			nativeQuery = true)
	Page<UserEntity> findAllUsersWithConfirmedEmailAddress( Pageable pageableRequest );

	@Query(value="select * from users u where u.first_name = ?1",nativeQuery=true)
	List<UserEntity> findUserByFirstName(String firstName);

	@Query(value="select * from users u where u.last_name = :lastName",nativeQuery=true)
	List<UserEntity> findUserByLastName(@Param("lastName") String lastName);

	@Query(value="select * from users u where first_name LIKE %:keyword% or last_name LIKE %:keyword%",nativeQuery=true)
	List<UserEntity> findUsersByKeyword(@Param("keyword") String keyword);

	@Query(value="select u.first_name, u.last_name from users u where u.first_name LIKE %:keyword% or u.last_name LIKE %:keyword%",nativeQuery=true)
	List<Object[]> findUserFirstNameAndLastNameByKeyword(@Param("keyword") String keyword);

	@Transactional
	@Modifying
	@Query(value="update users u set u.email_verification_status=:emailVerificationStatus where u.user_id=:userId", nativeQuery=true)
	void updateUserEmailVerificationStatus(@Param("emailVerificationStatus") boolean emailVerificationStatus,
			@Param("userId") String userId);

	@Query("select user from UserEntity user where user.userId =:userId")
	UserEntity findUserEntityByUserId(@Param("userId") String userId);

	@Query("select user.firstName, user.lastName from UserEntity user where user.userId =:userId")
	List<Object[]> getUserEntityFullNameById(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u set u.emailVerificationStatus =:emailVerificationStatus where u.userId = :userId")
    void updateUserEntityEmailVerificationStatus(
    		@Param("emailVerificationStatus") boolean emailVerificationStatus,
            @Param("userId") String userId);
    
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name = :roleName")
    List<UserEntity> findByRoleName(@Param("roleName") String roleName);

    // Custom query to find users by multiple roles
    @Query("SELECT u FROM UserEntity u JOIN u.roles r WHERE r.name IN :roleNames")
    List<UserEntity> findByRoleNames(@Param("roleNames") List<String> roleNames);

	@Query(
			value = "SELECT id FROM users WHERE email = :email " +
					"AND encrypted_password = SHA2(CONCAT(:password, salt), 256)",
			nativeQuery = true
	)
	Optional<Long> findUserIdByEmailAndPassword(
			@Param("email") String email,
			@Param("password") String rawPassword
	);

	@Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.priority >= :priority")
	Page<UserEntity> findByRolePriorityGreaterThanEqual(
			@Param("priority") int priority,
			Pageable pageable);

	@Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.priority = :priority")
	Page<UserEntity> findByRolePriority(
			@Param("priority") int priority,
			Pageable pageable);

	@Query("SELECT DISTINCT u FROM UserEntity u JOIN u.roles r WHERE r.priority BETWEEN :minPriority AND :maxPriority")
	Page<UserEntity> findByRolePriorityBetween(
			@Param("minPriority") int minPriority,
			@Param("maxPriority") int maxPriority,
			Pageable pageable);
}
